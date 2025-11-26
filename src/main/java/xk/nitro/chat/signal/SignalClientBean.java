package xk.nitro.chat.signal;

import org.signal.libsignal.protocol.*;
import org.signal.libsignal.protocol.message.*;
import org.signal.libsignal.protocol.state.SessionRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.context.event.EventListener;
import org.springframework.lang.NonNull;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;

import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.List;

@Component
@Scope(scopeName = "websocket", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class SignalClientBean {

    private static final Logger log = LoggerFactory.getLogger(SignalClientBean.class);
    private final SignalStoreGenerator signalStoreGenerator;
    private final ClientKeyStore clientKeyStore;
    private String currentUserId;
    private String currentFriendId;
    private SessionCipher sessionCipher;
    private SignalProtocolAddress signalProtocolAddress;
    private byte[] identityKey;
    private byte[] signedPreKey;
    private byte[] kyberPreKey;
    private List<byte[]> oneTimePreKeyList;
    private byte[] sessionRecord;

    public SignalClientBean(
            @NonNull ClientKeyStore clientKeyStore,
            @NonNull SignalStoreGenerator signalStoreGenerator) {
        this.clientKeyStore = clientKeyStore;
        this.signalStoreGenerator = signalStoreGenerator;
    }

    @EventListener
    public void handleWebSocketConnectListener(
            @NonNull SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal currentUser = accessor.getUser();

        if (currentUser != null) {
            currentUserId = currentUser.getName();
            currentFriendId = accessor.getFirstNativeHeader("friendId");
        }
    }


    private void extendedTripleDiffieHellmann() {
        signalProtocolAddress = new SignalProtocolAddress(currentFriendId, 1);
        try {
            sessionCipher = establishSession(signalProtocolAddress);
        } catch (InvalidKeyException | UntrustedIdentityException e) {
            e.printStackTrace();
        }
    }


    public byte[] encryptMessage(@NonNull String remoteUserId, @NonNull String message) {

        byte[] bytes = message.getBytes();
        log.info("Bytes exist {}, {}, {}, {}", identityKey == null, kyberPreKey == null, oneTimePreKeyList == null, signedPreKey == null);

        CiphertextMessage encryptedMessage;
        try {
            encryptedMessage = sessionCipher.encrypt(bytes);
        } catch (NoSessionException e) {
            throw new NoSignalSessionException(remoteUserId, e);
        } catch (UntrustedIdentityException e) {
            throw new RuntimeException("Encryption failed untrusted identity issue!", e);
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed!", e);
        }

        sessionRecord = clientKeyStore.getStore().loadSession(signalProtocolAddress).serialize();
        return encryptedMessage.serialize();
    }

    public String decryptMessage(@NonNull byte[] encryptedBytes) {
        try {
            PreKeySignalMessage preKeyMessage = new PreKeySignalMessage(encryptedBytes);
            byte[] plaintext = sessionCipher.decrypt(preKeyMessage, UsePqRatchet.NO);
            sessionRecord = clientKeyStore.getStore().loadSession(signalProtocolAddress).serialize();
            return new String(plaintext, StandardCharsets.UTF_8);
        } catch (InvalidMessageException e) {
            log.error("Invalid message from {}: {}", currentFriendId, e.getMessage(), e);
        } catch (InvalidKeyException | InvalidKeyIdException | DuplicateMessageException | LegacyMessageException e) {
            log.error("Decryption failed for message from {}: {}", currentFriendId, e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error decrypting message from {}: {}", currentFriendId, e.getMessage(), e);
        }

        try {
            SignalMessage signalMessage = new SignalMessage(encryptedBytes);
            byte[] plaintext = sessionCipher.decrypt(signalMessage);
            sessionRecord = clientKeyStore.getStore().loadSession(signalProtocolAddress).serialize();
            return new String(plaintext, StandardCharsets.UTF_8);
        } catch (InvalidMessageException e) {
            log.error("Invalid message from {}: {}", currentFriendId, e.getMessage(), e);
        } catch (InvalidKeyException | DuplicateMessageException | LegacyMessageException
                 | NoSessionException e) {
            log.error("Decryption failed for message from {}: {}", currentFriendId, e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error decrypting message from {}: {}", currentFriendId, e.getMessage(), e);
        }

        return null;

    }

    public void generateUserSignalStore() {
        clientKeyStore
                .setSignalProtocolStore(signalStoreGenerator.generateStore(identityKey, signedPreKey, kyberPreKey, oneTimePreKeyList));
        this.extendedTripleDiffieHellmann();
    }

    @NonNull
    public SessionCipher establishSession(
            @NonNull SignalProtocolAddress remoteAddress
    ) throws InvalidKeyException, UntrustedIdentityException {
        if (sessionRecord == null) {
            SessionBuilder sessionBuilder = new SessionBuilder(clientKeyStore.getStore(), remoteAddress);
            sessionBuilder.process(clientKeyStore.generatePreKeyBundle(Integer.valueOf(currentFriendId)), UsePqRatchet.NO);
        } else {
            try {
                clientKeyStore.getStore().storeSession(remoteAddress, new SessionRecord(sessionRecord));
            } catch (InvalidMessageException e) {
                throw new RuntimeException(e);
            }
        }
        return new SessionCipher(clientKeyStore.getStore(), remoteAddress);
    }

    public boolean sessionCipherExists() {
        return sessionCipher == null;
    }

    public String getCurrentUserId() {
        return currentUserId;
    }

    public void setIdentityKey(@NonNull byte[] identityKey) {
        this.identityKey = identityKey;
    }

    public void setSignedPreKey(@NonNull byte[] signedPreKey) {
        this.signedPreKey = signedPreKey;
    }

    public void setKyberPreKey(@NonNull byte[] kyberPreKey) {
        this.kyberPreKey = kyberPreKey;
    }

    public void setOneTimePreKeyList(@NonNull List<byte[]> oneTimePreKeyList) {
        this.oneTimePreKeyList = oneTimePreKeyList;
    }

    public void setSessionRecord(@NonNull byte[] sessionRecord) {
        this.sessionRecord = sessionRecord;
    }

    public byte[] getSessionRecord() {
        return sessionRecord;
    }


}

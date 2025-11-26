package xk.nitro.chat.signal;

import org.signal.libsignal.protocol.*;
import org.signal.libsignal.protocol.ecc.ECPublicKey;
import org.signal.libsignal.protocol.state.*;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import xk.nitro.chat.model.IdentityKeyPublic;
import xk.nitro.chat.model.KyberPreKeyObject;
import xk.nitro.chat.model.OneTimePreKeyPublic;
import xk.nitro.chat.model.SignedPreKeyPublic;
import xk.nitro.chat.model.User;
import xk.nitro.chat.service.KeyManagmentService;
import xk.nitro.chat.service.UserService;

import java.util.*;

/**
 * KeyStore, den jeder Client besitzt und die Schlüssel zwischenspeichert, die
 * benötigt werden,
 * um das Signal-Protokoll auszuführen
 *
 * @author Timo Taubmann (timo.taubmann@juniorjob.de)
 */
@Component
@Scope(scopeName = "websocket", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ClientKeyStore {

    private final UserService userService;
    private final KeyManagmentService keyManagmentService;
    private SignalProtocolStore store;

    public ClientKeyStore(@NonNull UserService userService,
                          @NonNull KeyManagmentService keyManagmentService) {
        this.keyManagmentService = keyManagmentService;
        this.userService = userService;
    }

    public void setSignalProtocolStore(@NonNull SignalProtocolStore store) {
        this.store = store;
    }

    /**
     * Erstellt ein PreKeyBundle, das beim Login in der Oberfläche initial ans
     * Backend übertragen wird.
     *
     * @return Das PreKey-Bundle, das für den erweiterten Diffie-Hellmann benötigt
     * wird
     *
     */
    @NonNull
    public PreKeyBundle generatePreKeyBundle(@NonNull Integer targetUserId)
            throws InvalidKeyException {

        User targetUser = userService.userInfo(targetUserId);

        IdentityKeyPublic identityKeyPublicFromDB = keyManagmentService.findIdentityKey(targetUser);
        IdentityKey identityKey = new IdentityKey(new ECPublicKey(identityKeyPublicFromDB.getIdentityKeyPublic()));

        SignedPreKeyPublic signedPreKeyPublicFromDB = keyManagmentService.findSignedPreKey(targetUser);
        ECPublicKey signedPreKeyPublic = new ECPublicKey(signedPreKeyPublicFromDB.getSpkPublicKey());

        List<OneTimePreKeyPublic> oneTimePreKeyPublicFromDB = keyManagmentService.findOneTimePreKeysUnused(targetUser);
        ECPublicKey oneTimePreKeyPublic = new ECPublicKey(oneTimePreKeyPublicFromDB.getFirst().getOtpkPublicKey());

        keyManagmentService.setStatusToUsedOTPK(oneTimePreKeyPublicFromDB.getFirst().getOtpkKeyId(), targetUser);

        KyberPreKeyObject kyberPreKeyObjectFromDB = keyManagmentService.findKyberPreKey(targetUser);

        KyberPreKeyRecord kyberPreKeyRecord;
        try {
            kyberPreKeyRecord = new KyberPreKeyRecord(kyberPreKeyObjectFromDB.getKyberKeyByteArray());
        } catch (InvalidMessageException e) {
            e.printStackTrace();
            throw new IllegalStateException("Failed to create KyberPreKeyRecord", e);
        }

        return new PreKeyBundle(
                identityKeyPublicFromDB.getRegistrationId(),
                1,
                oneTimePreKeyPublicFromDB.getFirst().getOtpkKeyId(),
                oneTimePreKeyPublic,
                1,
                signedPreKeyPublic,
                signedPreKeyPublicFromDB.getSpkSignature(),
                identityKey,
                1,
                kyberPreKeyRecord.getKeyPair().getPublicKey(),
                kyberPreKeyRecord.getSignature()
        );
    }

    public SignalProtocolStore getStore() {
        return store;
    }

}

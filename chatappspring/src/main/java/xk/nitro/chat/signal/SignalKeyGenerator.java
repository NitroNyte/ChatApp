package xk.nitro.chat.signal;

import org.signal.libsignal.protocol.IdentityKeyPair;
import org.signal.libsignal.protocol.ecc.ECKeyPair;
import org.signal.libsignal.protocol.kem.KEMKeyPair;
import org.signal.libsignal.protocol.kem.KEMKeyType;
import org.signal.libsignal.protocol.state.KyberPreKeyRecord;
import org.signal.libsignal.protocol.state.PreKeyRecord;
import org.signal.libsignal.protocol.state.SignedPreKeyRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;
import xk.nitro.chat.dto.signal.UserKeys;
import xk.nitro.chat.model.User;
import xk.nitro.chat.service.KeyManagmentService;
import xk.nitro.chat.service.UserService;

import java.util.ArrayList;
import java.util.List;

@Component
@SessionScope
public class SignalKeyGenerator {

    private static final Logger log = LoggerFactory.getLogger(SignalKeyGenerator.class);
    private final UserService userService;
    private final KeyManagmentService keyManagmentService;

    private static final int PREKEY_START_ID = 1;
    private static final int PREKEY_COUNT = 100;
    private static final int SIGNED_PREKEY_ID = 1;
    private static final int KYBER_PREKEY_ID = 1;

    public SignalKeyGenerator(@NonNull KeyManagmentService keyManagmentService, @NonNull UserService userService) {
        this.keyManagmentService = keyManagmentService;
        this.userService = userService;
    }

    public UserKeys generateOrCheckKeyExistence(@NonNull Integer targetUserId) {
        User targetUser = userService.userInfo(targetUserId);
        //TODO:This should be changed such that it will get it from the user and then create the other keys if needed
        IdentityKeyPair identityKeyPair = null;
        byte[] identityKeyPairByte;
        byte[] kyberPreKeyByte;
        byte[] signedPreKeyByte;
        List<byte[]> currentPreKeysByte;

        if (!keyManagmentService.iDKeyExistsByUser(targetUser)) {
            identityKeyPair = IdentityKeyPair.generate();
            identityKeyPairByte = identityKeyPair.serialize();
            keyManagmentService.saveIdentityKey(identityKeyPair, targetUser);
        } else {
            identityKeyPairByte = null;
        }

        if (!keyManagmentService.kyberPreKeyExistsByUser(targetUser)) {
            KEMKeyPair kemKeyPair = KEMKeyPair.generate(KEMKeyType.KYBER_1024);
            byte[] kyberPreKeySignature = identityKeyPair.getPrivateKey()
                    .calculateSignature(kemKeyPair.getPublicKey().serialize());
            KyberPreKeyRecord kyberPreKey = new KyberPreKeyRecord(KYBER_PREKEY_ID, System.currentTimeMillis(), kemKeyPair,
                    kyberPreKeySignature);
            kyberPreKeyByte = kyberPreKey.serialize();
            keyManagmentService.saveKyberKey(kyberPreKey, targetUser);
        } else {
            kyberPreKeyByte = null;
        }


        if (!keyManagmentService.spkExistsByUser(targetUser)) {
            ECKeyPair ecKeyPair = ECKeyPair.generate();
            byte[] signedPreKeySignature = identityKeyPair.getPrivateKey()
                    .calculateSignature(ecKeyPair.getPublicKey().serialize());
            SignedPreKeyRecord signedPreKey = new SignedPreKeyRecord(SIGNED_PREKEY_ID, System.currentTimeMillis(), ecKeyPair,
                    signedPreKeySignature);
            signedPreKeyByte = signedPreKey.serialize();
            keyManagmentService.saveSignedPreKey(signedPreKey, targetUser);
        } else {
            signedPreKeyByte = null;
        }

        if (!keyManagmentService.otpkExistsByUser(targetUser)) {
            List<PreKeyRecord> currentPreKeysArray = new ArrayList<>();
            currentPreKeysByte = new ArrayList<>();
            for (int PreKeyIndex = PREKEY_START_ID; PreKeyIndex <= PREKEY_COUNT; PreKeyIndex++) {
                ECKeyPair preKeyPair = ECKeyPair.generate();
                PreKeyRecord preKey = new PreKeyRecord(PreKeyIndex, preKeyPair);
                currentPreKeysArray.add(preKey);
                currentPreKeysByte.add(preKey.serialize());
            }
            keyManagmentService.saveOneTimePreKeys(currentPreKeysArray, targetUser);
        } else {
            currentPreKeysByte = null;
        }

        return new UserKeys(identityKeyPairByte, signedPreKeyByte, currentPreKeysByte, kyberPreKeyByte);

    }

}

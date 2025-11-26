package xk.nitro.chat.service;

import java.util.List;

import org.signal.libsignal.protocol.IdentityKeyPair;
import org.signal.libsignal.protocol.InvalidKeyException;
import org.signal.libsignal.protocol.state.KyberPreKeyRecord;
import org.signal.libsignal.protocol.state.PreKeyRecord;
import org.signal.libsignal.protocol.state.SignedPreKeyRecord;
import org.signal.libsignal.protocol.util.KeyHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import xk.nitro.chat.dao.*;
import xk.nitro.chat.model.IdentityKeyPublic;
import xk.nitro.chat.model.KyberPreKeyObject;
import xk.nitro.chat.model.OneTimePreKeyPublic;
import xk.nitro.chat.model.SignedPreKeyPublic;
import xk.nitro.chat.model.User;

@Service
public class KeyManagmentService {

    private static final Logger log = LoggerFactory.getLogger(KeyManagmentService.class);

    private final IdentityKeyBundleDao identityKeyBundleDao;
    private final OneTimePreKeyDao oneTimePreKeyDao;
    private final SignedPreKeyDao signedPreKeyDao;
    private final KyberPreKeyDao kyberPreKeyDao;
    private final UserService userService;

    public KeyManagmentService(@NonNull OneTimePreKeyDao oneTimePreKeyDao, @NonNull SignedPreKeyDao signedPreKeyDao,
                               @NonNull IdentityKeyBundleDao identityKeyBundleDao,
                               @NonNull UserService userService,
                               @NonNull KyberPreKeyDao kyberPreKeyDao) {
        this.oneTimePreKeyDao = oneTimePreKeyDao;
        this.signedPreKeyDao = signedPreKeyDao;
        this.identityKeyBundleDao = identityKeyBundleDao;
        this.kyberPreKeyDao = kyberPreKeyDao;
        this.userService = userService;

    }

    public void saveSignedPreKey(@NonNull SignedPreKeyRecord signedPreKey, @NonNull User currentLoggedInUser) {
        SignedPreKeyPublic signedPreKeyPublic = new SignedPreKeyPublic();
        signedPreKeyPublic.setSpkKeyId(signedPreKey.getId());
        try {
            signedPreKeyPublic.setSpkPublicKey(signedPreKey.getKeyPair().getPublicKey().serialize());
        } catch (InvalidKeyException e) {
            log.error("Something went wrong with getting the public key for SPK!", e);
        }

        signedPreKeyPublic.setSpkCreatedTime(String.valueOf(signedPreKey.getTimestamp()));
        signedPreKeyPublic.setSpkSignature(signedPreKey.getSignature());
        signedPreKeyPublic.setUser(currentLoggedInUser);

        signedPreKeyDao.save(signedPreKeyPublic);
    }

    public void saveOneTimePreKeys(@NonNull List<PreKeyRecord> preKeyRecords, @NonNull User currentLoggedInUser) {

        for (PreKeyRecord preKey : preKeyRecords) {
            OneTimePreKeyPublic oneTimePreKeyPublic = new OneTimePreKeyPublic();
            oneTimePreKeyPublic.setOtpkKeyId(preKey.getId());
            oneTimePreKeyPublic.setOtpkKeyStatus(false);

            try {
                oneTimePreKeyPublic.setOtpkPublicKey(preKey.getKeyPair().getPublicKey().serialize());
            } catch (InvalidKeyException e) {
                log.error("Something went wrong with setting the public key!", e);
            }

            oneTimePreKeyPublic.setUser(currentLoggedInUser);
            oneTimePreKeyDao.save(oneTimePreKeyPublic);
        }
    }

    public void setStatusToUsedOTPK(@NonNull Integer otpkKeyId, @NonNull User currentLoggedInUser) {
        OneTimePreKeyPublic otpkObject = oneTimePreKeyDao.findByOtpkKeyIdAndUser(otpkKeyId, currentLoggedInUser)
                .orElseThrow(() -> new RuntimeException(
                        "The one time pre key was not found for user:" + currentLoggedInUser.getId()));
        otpkObject.setOtpkKeyStatus(true);

        oneTimePreKeyDao.save(otpkObject);
    }

    public List<OneTimePreKeyPublic> findOneTimePreKeysUnused(@NonNull User currentLoggedInUser) {
        return oneTimePreKeyDao.findAllByUserAsc(currentLoggedInUser);
    }

    public OneTimePreKeyPublic findOtpkByUserAndKeyId(@NonNull Integer currentLoggedInUserId, @NonNull Integer keyId) {
        User currentLoggedInUser = userService.userInfo(currentLoggedInUserId);
        return oneTimePreKeyDao.findByUserAndOtpkKeyId(currentLoggedInUser, keyId).orElse(null);

    }

    public void saveIdentityKey(@NonNull IdentityKeyPair identityKeyPair,
                                User currentLoggedInUser) {
        int registrationId = KeyHelper.generateRegistrationId(false);
        IdentityKeyPublic identityKeyPublic = new IdentityKeyPublic();
        identityKeyPublic.setRegistrationId(registrationId);
        identityKeyPublic.setIdentityKeyPublic(identityKeyPair.getPublicKey().serialize());
        identityKeyPublic.setIdentityKeyPrivate(identityKeyPair.getPrivateKey().serialize());
        identityKeyPublic.setDeviceId(1);
        identityKeyPublic.setUser(currentLoggedInUser);

        identityKeyBundleDao.save(identityKeyPublic);
    }

    public void saveKyberKey(@NonNull KyberPreKeyRecord kyberKey, @NonNull User currentLoggedInUser) {

        KyberPreKeyObject kyberPreKeyObject = new KyberPreKeyObject();

        kyberPreKeyObject.setKyberKeyByteArray(kyberKey.serialize());
        kyberPreKeyObject.setUser(currentLoggedInUser);

        kyberPreKeyDao.save(kyberPreKeyObject);
    }

    public SignedPreKeyPublic findSignedPreKey(@NonNull User currentLoggedInUser) {
        return signedPreKeyDao.findByUser(currentLoggedInUser).orElse(null);
    }

    public List<OneTimePreKeyPublic> findOneTimePreKeys(@NonNull User currentLoggedInUser) {
        return oneTimePreKeyDao.findAllByUser(currentLoggedInUser);
    }

    public IdentityKeyPublic findIdentityKey(@NonNull User currentLoggedInUser) {
        return identityKeyBundleDao.findByUser(currentLoggedInUser).orElse(null);
    }

    public KyberPreKeyObject findKyberPreKey(@NonNull User currentLoggedInUser) {
        return kyberPreKeyDao.findByUser(currentLoggedInUser).orElse(null);
    }


    public boolean iDKeyExistsByUser(@NonNull User currentLoggedInUser) {
        return identityKeyBundleDao.existsByUser(currentLoggedInUser);
    }

    public boolean otpkExistsByUser(@NonNull User currentLoggedInUser) {
        return oneTimePreKeyDao.existsByUser(currentLoggedInUser);
    }

    public boolean kyberPreKeyExistsByUser(@NonNull User currentLoggedInUser) {
        return kyberPreKeyDao.existsByUser(currentLoggedInUser);
    }

    // Delete keys that are used, don't forget to implement
    public boolean spkExistsByUser(@NonNull User currentLoggedInUser) {
        return signedPreKeyDao.existsByUser(currentLoggedInUser);
    }
}

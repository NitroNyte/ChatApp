package xk.nitro.chat.signal;

import java.util.ArrayList;
import java.util.List;

import org.signal.libsignal.protocol.IdentityKeyPair;
import org.signal.libsignal.protocol.InvalidMessageException;
import org.signal.libsignal.protocol.state.KyberPreKeyRecord;
import org.signal.libsignal.protocol.state.PreKeyRecord;
import org.signal.libsignal.protocol.state.SignalProtocolStore;
import org.signal.libsignal.protocol.state.SignedPreKeyRecord;
import org.signal.libsignal.protocol.util.KeyHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import org.signal.libsignal.protocol.state.impl.InMemorySignalProtocolStore;


@Service
@Scope(scopeName = "websocket", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class SignalStoreGenerator {

    private static final Logger log = LoggerFactory.getLogger(SignalStoreGenerator.class);

    public SignalStoreGenerator() {}

    public SignalProtocolStore generateStore(@NonNull byte[] identityKeyByte, byte[] signedPreKeyByte, byte[] kyberPreKeyByte, List<byte[]> otpkByte) {

        IdentityKeyPair identityKeyPair = new IdentityKeyPair(identityKeyByte);
        SignedPreKeyRecord signedPreKeyRecord;
        try {
            signedPreKeyRecord = new SignedPreKeyRecord(signedPreKeyByte);
        } catch (InvalidMessageException e) {
            throw new RuntimeException(e);
        }

        List<PreKeyRecord> preKeyRecords = new ArrayList<>();
        otpkByte.forEach(otpk -> {
            try {
                PreKeyRecord preKeyRecord = new PreKeyRecord(otpk);
                preKeyRecords.add(preKeyRecord);
            } catch (InvalidMessageException e) {
                throw new RuntimeException(e);
            }
        });

        KyberPreKeyRecord kyberPreKeyRecord;
        try {
            kyberPreKeyRecord = new KyberPreKeyRecord(kyberPreKeyByte);
        } catch (InvalidMessageException e) {
            throw new RuntimeException(e);
        }


        int registrationId = KeyHelper.generateRegistrationId(false);

        SignalProtocolStore store = new InMemorySignalProtocolStore(identityKeyPair, registrationId);

        for (PreKeyRecord preKeyRecord : preKeyRecords) {
            store.storePreKey(preKeyRecord.getId(), preKeyRecord);
        }

        store.storeSignedPreKey(signedPreKeyRecord.getId(), signedPreKeyRecord);

        store.storeKyberPreKey(kyberPreKeyRecord.getId(), kyberPreKeyRecord);

        return store;

    }
}

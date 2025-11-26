package xk.nitro.chat.dto.signal;

import java.util.List;

public record UserKeys(
        byte[] identityKeyPair,
        byte[] signedPreKeyRecord,
        List<byte[]> preKeyRecordList,
        byte[] kyberPreKeyRecord
) {
}

package xk.nitro.chat.dto.signal;

public record UserKeyPacket(
        Integer userId,
        UserKeys userKeys) {}

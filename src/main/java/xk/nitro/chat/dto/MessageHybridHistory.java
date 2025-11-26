package xk.nitro.chat.dto;

import java.util.List;

public record MessageHybridHistory(
        List<MessageFormatDTO> inOrderDecryptedMessages,
        List<MessageFormatDTO> decryptedMessages
) {}

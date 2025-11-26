package xk.nitro.chat.dto;

import java.util.List;

public record HistoryMessageRequest(
        Integer friendId,
        List<MessageFormatDTO> messages
) {}

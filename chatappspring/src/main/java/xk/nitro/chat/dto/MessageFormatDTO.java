package xk.nitro.chat.dto;

import org.jetbrains.annotations.NotNull;
import xk.nitro.chat.enums.MessageUserStatus;


public record MessageFormatDTO(
        Integer senderId,
        Integer receiverId,
        String content,
        Long timeCreated,
        MessageUserStatus status) implements Comparable<MessageFormatDTO> {

    @Override
    public int compareTo(@NotNull MessageFormatDTO o) {
        return timeCreated().compareTo(o.timeCreated);
    }
}

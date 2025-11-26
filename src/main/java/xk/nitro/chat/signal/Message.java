package xk.nitro.chat.signal;

import xk.nitro.chat.enums.MessageUserStatus;

public record Message(
                Integer senderId,
                Integer receiverId,
                byte[] content,
                MessageUserStatus status) {
}

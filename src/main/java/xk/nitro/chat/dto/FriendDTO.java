package xk.nitro.chat.dto;

import xk.nitro.chat.enums.UserStatus;

public record FriendDTO(
        Integer id,
        String name,
        String surname,
        UserStatus status
) {}

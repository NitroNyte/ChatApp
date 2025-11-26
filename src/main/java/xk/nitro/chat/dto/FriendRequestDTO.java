package xk.nitro.chat.dto;

public record FriendRequestDTO(
        String name,
        String surname,
        Integer user,
        Integer friend) {}

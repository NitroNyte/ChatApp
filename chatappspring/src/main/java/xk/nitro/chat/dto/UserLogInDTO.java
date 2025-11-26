package xk.nitro.chat.dto;

import org.springframework.lang.NonNull;

import xk.nitro.chat.model.User;

public record UserLogInDTO(
        String email,
        String password) {

    public UserLogInDTO(@NonNull User user) {
        this(
                user.getEmail(),
                user.getPassword());
    }

}

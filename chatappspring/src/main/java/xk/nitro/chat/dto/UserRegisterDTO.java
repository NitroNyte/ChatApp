package xk.nitro.chat.dto;

import org.springframework.lang.NonNull;

import xk.nitro.chat.model.User;

public record UserRegisterDTO(
        String name,
        String surname,
        String email,
        String password) {

    public UserRegisterDTO(@NonNull User user) {
        this(

                user.getName(),
                user.getSurname(),
                user.getEmail(),
                user.getPassword());
    }

}

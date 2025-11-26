package xk.nitro.chat.dto;

import java.time.LocalDateTime;

import org.springframework.lang.NonNull;

import xk.nitro.chat.enums.UserStatus;
import xk.nitro.chat.model.User;

public record UserDTO(
                Integer id,
                String name,
                String surname,
                String email,
                String password,
                UserStatus status,
                LocalDateTime lastOnline) {

        public UserDTO(@NonNull User user) {
                        this(
                                user.getId(),
                                user.getName(),
                                user.getSurname(),
                                user.getEmail(),
                                user.getPassword(),
                                user.getStatus(),
                                user.getLastOnline()
                        );
        }

}

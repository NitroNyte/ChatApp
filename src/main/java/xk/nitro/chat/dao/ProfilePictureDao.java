package xk.nitro.chat.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import xk.nitro.chat.model.ProfilePicture;
import xk.nitro.chat.model.User;

import io.micrometer.common.lang.NonNull;

public interface ProfilePictureDao extends JpaRepository<ProfilePicture, Integer> {

    boolean existsByUser(@NonNull User currentLoggedInUser);

    @NonNull
    Optional<ProfilePicture> findByUser(@NonNull User currentLoggedInUser);

}

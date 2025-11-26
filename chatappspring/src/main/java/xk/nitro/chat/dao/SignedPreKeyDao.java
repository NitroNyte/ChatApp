package xk.nitro.chat.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import xk.nitro.chat.model.SignedPreKeyPublic;
import xk.nitro.chat.model.User;

import io.micrometer.common.lang.NonNull;

import java.util.Optional;

public interface SignedPreKeyDao extends JpaRepository<SignedPreKeyPublic, Integer> {

    @NonNull
    Optional<SignedPreKeyPublic> findByUser(User currentLoggedInUser);


    boolean existsByUser(@NonNull User currentLoggedInUser);

}

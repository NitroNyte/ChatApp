package xk.nitro.chat.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

import xk.nitro.chat.model.IdentityKeyPublic;
import xk.nitro.chat.model.User;

import java.util.Optional;

public interface IdentityKeyBundleDao extends JpaRepository<IdentityKeyPublic, Integer> {

    @NonNull
    Optional<IdentityKeyPublic> findByUser(@NonNull User currentLoggedInUser);

    boolean existsByUser(@NonNull User currentLoggedInUser);
}

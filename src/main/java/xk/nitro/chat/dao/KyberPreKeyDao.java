package xk.nitro.chat.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

import xk.nitro.chat.model.KyberPreKeyObject;
import xk.nitro.chat.model.User;

import java.util.Optional;


public interface KyberPreKeyDao extends JpaRepository<KyberPreKeyObject, Integer> {

    @NonNull
    Optional<KyberPreKeyObject> findByUser(@NonNull User currentLoggedInUser);

    boolean existsByUser(@NonNull User currentLoggedInUser);

}

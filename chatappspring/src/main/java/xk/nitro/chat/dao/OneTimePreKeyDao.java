package xk.nitro.chat.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;

import xk.nitro.chat.model.OneTimePreKeyPublic;
import xk.nitro.chat.model.User;

public interface OneTimePreKeyDao extends JpaRepository<OneTimePreKeyPublic, Integer> {

    // This might return null, since there might not be any keys in the database or
    // all the keys are used

    /*
     * SELECT otpk FROM OneTimePreKeyObject otpk
     * WHERE otpk.otpkKeyStatus = false AND otpk.user = :user
     */
    @Query("""
                SELECT otpk FROM OneTimePreKeyPublic otpk
                WHERE otpk.user = :user
            """)
    @NonNull
    List<OneTimePreKeyPublic> findAllByUser(@NonNull @Param("user") User currentLoggedInUser);


    @Query("""
                SELECT otpk FROM OneTimePreKeyPublic otpk
                WHERE otpk.user = :user AND otpk.otpkKeyStatus = false
            """)
    @NonNull
    List<OneTimePreKeyPublic> findAllByUserAsc(@NonNull @Param("user") User currentLoggedInUser);

    boolean existsByUser(@NonNull User currentLoggedInUser);

    @NonNull
    Optional<OneTimePreKeyPublic> findByOtpkKeyIdAndUser(@NonNull Integer otpkKeyId, @NonNull User user);

    @NonNull
    Optional<OneTimePreKeyPublic> findByUserAndOtpkKeyId(@NonNull User currentLoggedInUser, @NonNull Integer keyId);


}

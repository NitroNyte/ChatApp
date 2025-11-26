package xk.nitro.chat.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import xk.nitro.chat.dto.FriendDTO;
import xk.nitro.chat.model.User;

@Repository
public interface UserDao extends JpaRepository<User, Integer> {

        // JPA-Semantics:
        // getBy... -> @Nullable T
        // findBy... -> @NonNull Optional<T>
        // getAllBy... / findAllBy... -> @NonNull Collection<T> (List<T>, Set<T>, ...)

        @NonNull
        Optional<User> findByEmail(String userEmail);

        @Query(value = """
                        SELECT u.id, u.name, u.surname, u.status FROM Friendship f
                        JOIN User u ON ((f.user.id = :userId AND u.id = f.friend.id) OR (f.friend.id = :userId AND u.id = f.user.id))
                        WHERE f.status = 'ACCEPTED' GROUP BY u.name, u.surname
                        """)
        @NonNull
        List<FriendDTO> friendListForContacts(@Param("userId") @NonNull Integer userId);


        @Query(value = """
                                SELECT u.id, u.name, u.surname, u.status FROM Friendship f JOIN User u
                        ON ((f.user.id = :userId AND u.id = f.friend.id) OR (f.friend.id = :userId AND u.id = f.user.id))
                        WHERE f.status = 'ACCEPTED'
                        """)
        @NonNull
        List<FriendDTO> friendListForChatPage(@Param("userId") @NonNull Integer userId);

        @Nullable
        FriendDTO getUserById(@NonNull Integer userId);

        @NonNull
        User getReferenceById(@NonNull Integer currentUserId);

        boolean existsByEmailAndPassword(@NonNull String email, @NonNull String password);

        boolean existsByEmail(String email);
}

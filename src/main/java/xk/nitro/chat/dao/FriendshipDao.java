package xk.nitro.chat.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import xk.nitro.chat.dto.FriendRequestDTO;
import xk.nitro.chat.model.Friendship;

@Repository
public interface FriendshipDao extends JpaRepository<Friendship, Integer> {

        @Query(value = """
                            SELECT u.name, u.surname, f.user.id, f.friend.id
                            FROM User u INNER JOIN Friendship f ON u.id = f.user.id
                            WHERE f.friend.id = :userId AND f.status = 'SENT' GROUP BY u.name, u.surname, f.user.id, f.friend.id
                        """)
        @NonNull
        List<FriendRequestDTO> getAllFriendsToBeAdded(@NonNull @Param("userId") Integer userId);

        @Nullable
        Friendship getByUserIdAndFriendId(@NonNull Integer userId, @NonNull Integer friendId);

        @Query("""
                        SELECT COUNT(f) > 0
                        FROM Friendship f
                        WHERE (
                            (f.user.id = :currentUserId AND f.friend.id = :selectedFriendId)
                            OR
                            (f.user.id = :selectedFriendId AND f.friend.id = :currentUserId)
                        )
                        AND f.status = 'ACCEPTED'
                        """)
        boolean areFriends(
                        @NonNull @Param("currentUserId") Integer currentUserId,
                        @NonNull @Param("selectedFriendId") Integer selectedFriendId);

        @Query("""
                        SELECT COUNT(f) > 0
                        FROM Friendship f
                        WHERE (
                            (f.user.id = :currentUserId AND f.friend.id = :selectedFriendId)
                            OR
                            (f.user.id = :selectedFriendId AND f.friend.id = :currentUserId)
                        )
                        AND f.status = 'SENT'
                        """)
        boolean alreadySentFriendRequest(
                        @NonNull @Param("currentUserId") Integer currentUserId,
                        @NonNull @Param("selectedFriendId") Integer selectedFriendId);
}

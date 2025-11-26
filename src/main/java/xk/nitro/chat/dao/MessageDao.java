package xk.nitro.chat.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import xk.nitro.chat.model.Message;

@Repository
public interface MessageDao extends JpaRepository<Message, Integer> {

    @Query(value = """
            SELECT m FROM Message m
            WHERE (m.sender.id = :friendId AND m.receiver.id = :currentUserId)
            ORDER BY m.timeCreated ASC
            """)
    List<Message> chatHistoryOfTwoUsers(@NonNull Integer currentUserId,
                                        @NonNull Integer friendId);

    @Modifying
    @Transactional
    @Query(value = """
            UPDATE Message m
            SET m.status = 'SEEN'
            WHERE (m.sender.id = :currentFriendId AND m.receiver.id = :currentUserId)
            """)
    void changeMessageStatusToSeen(@NonNull Integer currentFriendId,
                                   @NonNull Integer currentUserId);

    @Modifying
    @Transactional
    @Query(value = """
            UPDATE Message m
            SET m.status = 'DELIVERED'
            WHERE m.receiver.id = :currentUserId AND m.status = 'SENT'
            """)
    void changeMessageStatusToDelivered(@NonNull Integer currentUserId);


    @Modifying
    @Transactional
    @Query("""
            DELETE FROM Message m
            WHERE (m.sender.id = :currentFriendId AND m.receiver.id = :currentUserId)
            """)
    void deleteMessageDecryptedOnce(@NonNull Integer currentUserId,
                                    @NonNull Integer currentFriendId);

}

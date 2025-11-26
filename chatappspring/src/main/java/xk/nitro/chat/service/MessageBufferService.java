package xk.nitro.chat.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import xk.nitro.chat.signal.Message;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import xk.nitro.chat.dao.MessageDao;
import xk.nitro.chat.dao.UserDao;
import xk.nitro.chat.model.User;

/**
 * A service class for utilising the UserDao and MessageDao class and its
 * methods
 * <p>
 * It is used for buffering messages that includes saving and getting the
 * messages from database
 */

@Service
public class MessageBufferService {
    private final UserDao userDao;
    private final MessageDao messageDao;
    private final ConcurrentHashMap<Integer, List<xk.nitro.chat.model.Message>> messageBufferOldMessages = new ConcurrentHashMap<>();

    public MessageBufferService(@NonNull UserDao userDao, @NonNull MessageDao messageDao) {
        this.userDao = userDao;
        this.messageDao = messageDao;
    }

    public List<xk.nitro.chat.model.Message> getMessageBufferOldMessages(@NonNull Integer userId) {
        return messageBufferOldMessages.getOrDefault(userId, new ArrayList<>());
    }

    public void clearBufferOldMessages(@NonNull Integer indexToBeRemoved) {
        messageBufferOldMessages.remove(indexToBeRemoved);
    }

    @NonNull
    public xk.nitro.chat.model.Message saveToDatabase(@NonNull Message currentMessageToBeSaved) {

        User currentSenderUser = userDao.findById(currentMessageToBeSaved.senderId())
                .orElseThrow(() -> new IllegalArgumentException("Cannot find User who is sending in database"));
        User currentReceiverUser = userDao.findById(currentMessageToBeSaved.receiverId())
                .orElseThrow(() -> new IllegalArgumentException("Cannot find User who is receiving in database"));

        xk.nitro.chat.model.Message messageEntityFormat = new xk.nitro.chat.model.Message(
                currentSenderUser,
                currentReceiverUser,
                currentMessageToBeSaved.content(),
                currentMessageToBeSaved.status()
        );

        //To be deprecated if there is no use for in next MR
        return messageDao.save(messageEntityFormat);

    }

    public void setOldMessagesBuffer(@NonNull Integer currentUserId, @NonNull Integer friendId) {
        messageBufferOldMessages.put(currentUserId, new ArrayList<>(
                messageDao.chatHistoryOfTwoUsers(currentUserId, friendId)));
    }

    public void deleteDecryptedMessages(@NonNull Integer currentUserId, @NonNull Integer friendId) {
        messageDao.deleteMessageDecryptedOnce(currentUserId, friendId);
    }

    public void changeMessageStatusToSeen(@NonNull Integer currentFriendId, @NonNull Integer currentUserId) {
        messageDao.changeMessageStatusToSeen(currentFriendId, currentUserId);
    }

    public void changeMessageStatusToDelivered(@NonNull Integer currentUserId) {
        messageDao.changeMessageStatusToDelivered(currentUserId);
    }


}

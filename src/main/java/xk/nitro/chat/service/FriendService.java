package xk.nitro.chat.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import xk.nitro.chat.enums.RequestStatus;
import xk.nitro.chat.dao.FriendshipDao;
import xk.nitro.chat.dao.UserDao;
import xk.nitro.chat.dto.FriendDTO;
import xk.nitro.chat.dto.FriendRequestDTO;
import xk.nitro.chat.model.Friendship;
import xk.nitro.chat.model.User;

/**
 * A service class for utilising the UserDao and FriendshipDao class and its
 * methods
 * 
 * Is responsible for checking if you are friends with that person you are going
 * to send a friend request or not.
 * 
 * Also checks if you are a friend with that person.
 */

@Service
public class FriendService {
    private final FriendshipDao friendshipDao;
    private final UserDao userDao;

    public FriendService(@NonNull FriendshipDao friendshipDao, @NonNull UserDao userDao) {
        this.friendshipDao = friendshipDao;
        this.userDao = userDao;
    }

    public void addFriend(@NonNull Integer senderId, @NonNull String receiverEmail) {
        User sender = userDao.findById(senderId).orElse(null);
        User receiver = userDao.findByEmail(receiverEmail)
                .orElse(null);

        if (sender != null && receiver != null) {
            Friendship friendship = new Friendship();
            friendship.setUser(sender);
            friendship.setFriend(receiver);
            friendship.setStatus(RequestStatus.SENT);
            friendship.setLastChatted(LocalDateTime.now());

            friendshipDao.save(friendship);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Sender or receiver not found");
        }
    }

    public boolean isAlreadyFriend(@NonNull Integer senderId, @NonNull String receiverEmail) {
        return userDao.findByEmail(receiverEmail)
                .map(receiver -> friendshipDao.areFriends(senderId, receiver.getId()))
                .orElse(false);
    }

    public boolean alreadySendFriendRequest(@NonNull Integer senderId, @NonNull String receiverEmail) {
        return userDao.findByEmail(receiverEmail)
                .map(receiver -> friendshipDao.alreadySentFriendRequest(senderId, receiver.getId()))
                .orElse(false);
    }

    public void acceptFriendRequest(@NonNull Integer userId, @NonNull Integer friendId) {
        Friendship friendship = friendshipDao.getByUserIdAndFriendId(friendId, userId);

        if (friendship != null && friendship.getStatus() == RequestStatus.SENT) {
            friendship.setStatus(RequestStatus.ACCEPTED);
            friendship.setLastChatted(LocalDateTime.now());
            friendshipDao.save(friendship);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Friend request not found or already accepted");
        }
    }

    public void removeFriend(@NonNull Integer userId, @NonNull Integer friendId) {
        Friendship friendship;

        if (friendshipDao.getByUserIdAndFriendId(userId, friendId) != null) {
            friendship = friendshipDao.getByUserIdAndFriendId(userId, friendId);
        } else if (friendshipDao.getByUserIdAndFriendId(friendId, userId) != null) {
            friendship = friendshipDao.getByUserIdAndFriendId(friendId, userId);
        } else {
            friendship = null;
        }

        if (friendship != null && (friendship.getStatus() == RequestStatus.SENT
                || friendship.getStatus() == RequestStatus.ACCEPTED)) {
            friendshipDao.delete(friendship);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Friend request not found or already accepted");
        }

    }

    public List<FriendDTO> friendsListForUser(Integer userId) {
        return userDao.friendListForChatPage(userId);
    }

    public List<FriendRequestDTO> findFriendsToBeAdded(Integer userId) {
        return friendshipDao.getAllFriendsToBeAdded(userId);
    }

    public boolean areFriends(Integer currentUserId, Integer selectedFriendId) {
        return friendshipDao.areFriends(currentUserId, selectedFriendId);
    }
}

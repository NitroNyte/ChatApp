package xk.nitro.chat.service;

import java.util.List;
import java.util.Optional;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import xk.nitro.chat.enums.UserStatus;
import xk.nitro.chat.dao.UserDao;
import xk.nitro.chat.dto.FriendDTO;
import xk.nitro.chat.dto.UserDTO;
import xk.nitro.chat.dto.UserRegisterDTO;
import xk.nitro.chat.model.User;

@Service
public class UserService {

    private final UserDao userDao;

    public UserService(@NonNull UserDao userDao) {
        this.userDao = userDao;
    }

    @NonNull
    public User userInfo(@NonNull Integer currentUserId) {
        return userDao.findById(currentUserId).orElseThrow(() -> new IllegalArgumentException("User does not exist"));
    }

    @NonNull
    public User userInfoByEmail(@NonNull String email) {

        return userDao.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User does not exist"));
    }

    @NonNull
    public List<FriendDTO> getAllFriendListChatPage(@NonNull Integer currentUserId) {
        return userDao.friendListForChatPage(currentUserId);
    }

    @Nullable
    public FriendDTO findFriendByHisId(@Nullable Integer userId) {
        return userDao.getUserById(userId);
    }

    @NonNull
    public List<FriendDTO> friendListForContacts(@Nullable Integer currentUserId) {
        return Optional.ofNullable(currentUserId)
                .map(userDao::friendListForContacts)
                .orElse(List.of());
    }

    public boolean existsByEmail(@Nullable String email) {
        return userDao.existsByEmail(email);
    }

    public void updateUserById(@NonNull UserDTO user) {
        if (userDao.existsById(user.id())) {
            User currentUser = userDao.getReferenceById(user.id());
            currentUser.setName(user.name());
            currentUser.setSurname(user.surname());
            currentUser.setEmail(user.email());
            currentUser.setPassword(user.password());
            userDao.save(currentUser);
        }
    }

    /**
     * Verifies if the user really exists in database
     * 
     * @param userToBeRegistered current user object that is trying to register
     * @return false if email is already in use, otherwise if not return true and
     *         user that is trying to register
     */
    public boolean registerUser(@NonNull UserRegisterDTO userToBeRegistered) {
        User currentUser = new User();
        currentUser.setName(userToBeRegistered.name());
        currentUser.setSurname(userToBeRegistered.surname());
        currentUser.setEmail(userToBeRegistered.email());
        currentUser.setPassword(userToBeRegistered.password());
        if (userDao.existsByEmail(currentUser.getEmail())) {
            return false;
        } else {
            userDao.save(currentUser);
            return true;
        }
    }

    /**
     * Sets user online for other's to see
     * 
     * @param id current user id
     */
    public void updateOnlineStatusToOnline(@NonNull Integer id) {
        User user = userDao.getReferenceById(id);
        user.setStatus(UserStatus.ONLINE);
        userDao.save(user);
    }

    /**
     * Sets user offline for other's to see
     * 
     * @param id current user id
     */
    public void updateOnlineStatusToOffline(@NonNull Integer id) {
        User user = userDao.getReferenceById(id);
        user.setStatus(UserStatus.OFFLINE);
        userDao.save(user);
    }


  

}


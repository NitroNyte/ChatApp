package xk.nitro.chat.service;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import xk.nitro.chat.dao.UserDao;

/**
 * A service class for utilising the UserDao class and its methods
 */
@Service
public class AuthService {
    private final UserDao userDao;

    public AuthService(@NonNull UserDao userDao) {
        this.userDao = userDao;
    }

    public boolean userExists(@Nullable String email, @Nullable String password) {
        return userDao.existsByEmailAndPassword(email, password);
    }
}

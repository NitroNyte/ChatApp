package xk.nitro.chat.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import xk.nitro.chat.dto.UserRegisterDTO;
import xk.nitro.chat.service.UserService;

/**
 * A controller responsible for registering a user
 * 
 * This controller uses UserService class to determine if the user already
 * exsits and email is in use
 */
@Controller
@RequestMapping("/register")
public class RegisterController {
    private final UserService userService;
    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(RegisterController.class);

    public RegisterController(@NonNull UserService userService) {
        this.userService = userService;
    }

    /**
     * Method for validation and registering a user to database
     * 
     * @param userToBeRegistered information of the user we got from the form
     * @param generalModel       used for the text to check if user email is already
     *                           in use somewhere
     * @return a redirect to login page if successful or an error
     */
    @PostMapping("/validate")
    public String registerNewUser(
            @ModelAttribute("userRegisterTemplate") @NonNull UserRegisterDTO userToBeRegistered,
            @NonNull Model generalModel) {
        if (userService.registerUser(userToBeRegistered)) {
            return "redirect:/";
        } else {
            generalModel.addAttribute("formRegisterText", "Email is already taken or is invalid");
            // Logic such that it shows error's, like email already exists or something
            // related
            return "redirect:/register?error";
        }
    }

}

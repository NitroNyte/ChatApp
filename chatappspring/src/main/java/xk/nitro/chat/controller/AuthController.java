package xk.nitro.chat.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import org.springframework.web.context.annotation.SessionScope;
import xk.nitro.chat.dto.*;
import xk.nitro.chat.model.ProfilePicture;
import xk.nitro.chat.model.User;
import xk.nitro.chat.service.*;
import jakarta.servlet.http.HttpSession;
import xk.nitro.chat.signal.SignalKeyGenerator;

/**
 * The controller responsible for authentication of the user and for directing
 * to the chat page
 * <p>
 * The Controller handles interaction between web UI and uses AuthService and
 * UserService class.
 * Always checks if user exsits, asign a principle based on ID, then the app
 * redirects to the home page
 */

@Controller
@SessionScope
public class AuthController {
    private final SignalKeyGenerator signalKeyGenerator;
    private final AuthService authService;
    private final UserService userService;
    private final ProfilePictureService profilePictureService;
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    public AuthController(
            @NonNull SignalKeyGenerator signalKeyGenerator,
            @NonNull AuthService authService,
            @NonNull UserService userService,
            @NonNull ProfilePictureService profilePictureService) {
        this.signalKeyGenerator = signalKeyGenerator;
        this.userService = userService;
        this.authService = authService;
        this.profilePictureService = profilePictureService;
    }

    @GetMapping
    public String index(@NonNull Model userLoginModel) {
        userLoginModel.addAttribute("userLogin", new UserLogInDTO(null, null));
        return "index";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "funky";
    }

    @PostMapping("/login")
    public String login(
            @ModelAttribute("userLogin") UserLogInDTO user,
            HttpSession session,
            Model model) {

        if (authService.userExists(user.email(), user.password())) {
            User currentUser = userService.userInfoByEmail(user.email());
            UserDTO userDTO = new UserDTO(currentUser);

            profilePictureService.setDefaultProfilePicture(currentUser);
            ProfilePicture currentUserPfp = profilePictureService.getByUser(currentUser);
            ProfilePictureDTO profilePictureToDTO = new ProfilePictureDTO(
                    currentUserPfp.getId(),
                    currentUser.getId(),
                    currentUserPfp.getImageName(),
                    currentUserPfp.getImageData());

            session.setAttribute("currentLoggedInUser", userDTO);
            session.setAttribute("currentUserPfp", profilePictureToDTO);

            userService.updateOnlineStatusToOnline(userDTO.id());

            return "redirect:/times2/chat";
        } else {
            model.addAttribute("errorMessage", "Invalid email or password.");
            return "index";
        }
    }

    @PostMapping("/api/user/exists")
    @ResponseBody
    public boolean loginCheck(@ModelAttribute UserLogInDTO user) {
        return authService.userExists(user.email(), user.password());
    }



    @GetMapping("/register")
    public String registerLink(@NonNull Model userRegisterModel) {
        userRegisterModel.addAttribute("userRegisterTemplate",
                new UserRegisterDTO(null, null, null, null));
        return "register";
    }

}

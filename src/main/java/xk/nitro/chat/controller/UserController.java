package xk.nitro.chat.controller;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import xk.nitro.chat.dto.ProfilePictureDTO;
import xk.nitro.chat.dto.UserDTO;
import xk.nitro.chat.dto.UserProfileDTO;
import xk.nitro.chat.model.ProfilePicture;
import xk.nitro.chat.service.ProfilePictureService;
import xk.nitro.chat.service.UserService;

import jakarta.servlet.http.HttpSession;

/**
 * A controller used for linking to editing page and editing the user that is
 * currently logged in.
 * This class provides two methods in which one if for redirecting to a edit
 * account page, and other is responsible for creating a user setting the
 * details and saving them to database.
 */
@Controller
@RequestMapping("times2/account")
public class UserController {

    private final static Logger log = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;
    private final ProfilePictureService profilePictureService;

    public UserController(@NonNull UserService userService, @NonNull ProfilePictureService profilePictureService) {
        this.userService = userService;
        this.profilePictureService = profilePictureService;
    }

    @GetMapping("/editAccount")
    public String userPage(@NonNull Model userModel,
                           @NonNull HttpSession session) {
        UserDTO currentUser = (UserDTO) session.getAttribute("currentLoggedInUser");

        UserProfileDTO currentUserProfile = new UserProfileDTO(currentUser, null);
        userModel.addAttribute("userProfileInfo", currentUserProfile);

        return "editAccount";
    }

    @GetMapping("/user/profileImage")
    public ResponseEntity<ByteArrayResource> getProfileImage(HttpSession session) {
        ProfilePictureDTO currentProfilePictureDTO = (ProfilePictureDTO) session
                .getAttribute("currentUserPfp");

        log.info("User data is: {}", currentProfilePictureDTO.imageName());

        if (currentProfilePictureDTO == null || currentProfilePictureDTO.imageData() == null
                || currentProfilePictureDTO.imageData().length == 0) {

            return ResponseEntity.notFound().build();
        }

        byte[] imageBytes = currentProfilePictureDTO.imageData();

        ByteArrayResource resource = new ByteArrayResource(imageBytes);

        return ResponseEntity.ok()
                .contentLength(imageBytes.length)
                .contentType(MediaType.IMAGE_PNG)
                .body(resource);
    }

    /**
     * Method for sending the updated inforamtion to the database and update the
     * current user
     *
     * @param userModelAfter user model after editing
     * @param session        current session
     * @return redirection to edit page where we can see the changes we made
     *
     */
    @PostMapping("/editAccount/edit")
    public String goBackByLink(
            @ModelAttribute("userEditProfileDTO") @NonNull UserProfileDTO userModelAfter,
            @NonNull HttpSession session) {

        UserDTO userBeforeChanges = (UserDTO) session.getAttribute("currentLoggedInUser");

        ProfilePictureDTO currentProfilePictureDTO = (ProfilePictureDTO) session
                .getAttribute("currentUserPfp");

        UserDTO userEditedInfo = userModelAfter.userEditInfo();
        UserDTO userAfterChanges = new UserDTO(
                userBeforeChanges.id(),
                userEditedInfo.name(),
                userEditedInfo.surname(),
                userEditedInfo.email(),
                userEditedInfo.password(),
                userBeforeChanges.status(),
                userBeforeChanges.lastOnline());

        session.setAttribute("currentLoggedInUser", userAfterChanges);

        userService.updateUserById(userAfterChanges);

        try {
            if (userModelAfter.newImageFile().getBytes().length != 0) {
                profilePictureService.setPersonalProfilePicture(userAfterChanges,
                        userModelAfter.newImageFile(),
                        currentProfilePictureDTO.profilePictureId());
            }
        } catch (IOException e) {
            log.warn("The picture is empty!!!");
        }

        ProfilePicture currentProfilePictureEntity = profilePictureService
                .getByUser(userService.userInfo(userAfterChanges
                        .id()));

        ProfilePictureDTO currentNewProfilePictureDTO = new ProfilePictureDTO(
                currentProfilePictureEntity.getId(), userAfterChanges.id(),
                currentProfilePictureEntity.getImageName(),
                currentProfilePictureEntity.getImageData());

        session.setAttribute("currentUserPfp", currentNewProfilePictureDTO);

        return "redirect:/times2/account/editAccount";
    }

}

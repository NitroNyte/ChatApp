package xk.nitro.chat.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import xk.nitro.chat.dto.*;
import xk.nitro.chat.dto.signal.UserKeyPacket;
import xk.nitro.chat.dto.signal.UserKeys;
import xk.nitro.chat.model.User;
import xk.nitro.chat.service.FriendService;
import xk.nitro.chat.service.UserService;

import jakarta.servlet.http.HttpSession;
import xk.nitro.chat.signal.SignalKeyGenerator;

/**
 * A class responsible for mostly mapping of links and displaying current users
 * friend list
 */
@Controller
@RequestMapping("/times2")
public class HomeController {

    private static final Logger log = LoggerFactory.getLogger(HomeController.class);

    private final FriendService friendService;
    private final UserService userService;
    private final SignalKeyGenerator signalKeyGenerator;

    public HomeController(@NonNull FriendService friendService,
                          @NonNull UserService userService,
                          @NonNull SignalKeyGenerator signalKeyGenerator) {
        this.friendService = friendService;
        this.userService = userService;
        this.signalKeyGenerator = signalKeyGenerator;
    }

    @GetMapping("/chat")
    public String chatPage(@NonNull Model generalModel, @NonNull HttpSession session) {

        if (session.getAttribute("currentLoggedInUser") == null) {
            return "redirect:/";
        }
        UserDTO currentUser = (UserDTO) session.getAttribute("currentLoggedInUser");
        List<FriendDTO> friendOfCurrentUser = userService.getAllFriendListChatPage(currentUser.id());
        generalModel.addAttribute("friends", friendOfCurrentUser);

        return "funky";
    }

    /**
     * A method used to set friend of the current user and display them on contact page
     *
     * @param generalModel model for setting the friend list on the page that up to come, contact page
     * @param session      current session
     * @return contact page in html format
     */
    @GetMapping("/contact")
    public String contactPage(@NonNull Model generalModel, @NonNull HttpSession session) {

        if (session.getAttribute("currentLoggedInUser") == null) {
            return "redirect:/";
        }

        UserDTO currentUser = (UserDTO) session.getAttribute("currentLoggedInUser");
        generalModel.addAttribute("userFriend", new UserDTO(null, null, null, null, null, null, null));
        generalModel.addAttribute("friendRequests", friendService.findFriendsToBeAdded(currentUser.id()));
        generalModel.addAttribute("friendsList", friendService.friendsListForUser(currentUser.id()));
        return "contact";
    }

    @GetMapping("/account")
    public String accountPage(@NonNull Model generalModel, @NonNull HttpSession session) {

        if (session.getAttribute("currentLoggedInUser") == null) {
            return "redirect:/";
        }

        UserDTO currentUser = (UserDTO) session.getAttribute("currentLoggedInUser");
        generalModel.addAttribute("user", currentUser);
        return "account";
    }

    /**
     * Maps to a current chat with that friend with a unqiue id
     *
     * @param id           current friend's id
     * @param generalModel we use for setting the friends information
     * @param session      current session
     * @return a link to chatpage with that friend or if not friend's to home page again
     */
    @GetMapping("/chat/{id}")
    public String chatWithFriend(
            @PathVariable @NonNull Integer id,
            @NonNull Model generalModel,
            @NonNull HttpSession session) {

        UserDTO currentUser = (UserDTO) session.getAttribute("currentLoggedInUser");
        User currentSelectedFriend = userService.userInfo(id);
        UserDTO currentSelectedFriendToDTO = new UserDTO(currentSelectedFriend);
        session.setAttribute("currentTalkingFriendId", currentSelectedFriendToDTO);

        if (!friendService.areFriends(currentUser.id(), currentSelectedFriendToDTO.id())) {
            // Test drive, throws error here if you are not friends with the person
            return "redirect:/times2/chat";
        }
        FriendDTO friend = userService.findFriendByHisId(id);
        generalModel.addAttribute("currentFriend", friend);
        generalModel.addAttribute("currentUser", currentUser);
        List<FriendDTO> friendOfCurrentUser = userService
                .getAllFriendListChatPage(currentUser.id());
        generalModel.addAttribute("friendsList", friendOfCurrentUser);

        return "chatting";
    }

    @GetMapping("/logout")
    public String logout(@NonNull HttpSession session) {
        Integer currentUserId = ((UserDTO) session.getAttribute("currentLoggedInUser")).id();
        if (currentUserId == null) {
            return "redirect:/";
        }
        userService.updateOnlineStatusToOffline(currentUserId);
        log.info("User loged out with id: {}", currentUserId);
        session.invalidate();
        return "redirect:/";
    }


    /**
     * Method which sends keys or null(has null checker) of the keys we need for the user
     *
     * @param session
     * @return a dto that has the user keys/null if user keys exist or aren't used
     */
    @GetMapping("/api/user/keys")
    @ResponseBody
    public ResponseEntity<UserKeyPacket> keyCheck(@NonNull HttpSession session) {
        Integer currentUserId = ((UserDTO) session.getAttribute("currentLoggedInUser")).id();
        UserKeys keyState = signalKeyGenerator.generateOrCheckKeyExistence(currentUserId);
        UserKeyPacket userKeyPacket = new UserKeyPacket(currentUserId, keyState);
        return new ResponseEntity<>(userKeyPacket, HttpStatus.OK);
    }
}

package xk.nitro.chat.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import xk.nitro.chat.dto.UserDTO;
import xk.nitro.chat.service.FriendService;
import xk.nitro.chat.service.UserService;
import jakarta.servlet.http.HttpSession;

/**
 * This controller is responsible for many things including adding friends and
 * also checking who is your friend
 */
@Controller
@RequestMapping("/times2/contact")
public class ContactController {

    private static final Logger logger = LoggerFactory.getLogger(ContactController.class);

    private final UserService userService;
    private final FriendService friendService;

    public ContactController(@NonNull FriendService friendService, @NonNull UserService userService) {
        this.friendService = friendService;
        this.userService = userService;
    }

    /**
     * A method for sending a friend request to a user based on email
     * 
     * @param friendToBeAdded    user object we use for getting info
     * @param session            current session
     * @return
     */

    @PostMapping("/addContact")
    public ResponseEntity<String> addContact(
            @ModelAttribute("userFriend") @NonNull UserDTO friendToBeAdded,
            @NonNull HttpSession session) {

        UserDTO currentUser = (UserDTO) session.getAttribute("currentLoggedInUser");

        if (friendToBeAdded.email() != null && userService.existsByEmail(friendToBeAdded.email())) {
            logger.info("Attempting to add contact with email: {}", friendToBeAdded.email());

            if (friendService.isAlreadyFriend(currentUser.id(), friendToBeAdded.email())
                    || friendService.alreadySendFriendRequest(currentUser.id(), friendToBeAdded.email())) {
                logger.warn("Attempted to add a contact that is already a friend");
                return new ResponseEntity<>("Friend is already in your contacts or already sent friend request",
                        HttpStatus.CONFLICT);
            }
            friendService.addFriend(currentUser.id(), friendToBeAdded.email());
            return new ResponseEntity<>("Sent friend request successfully", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Friend does not exist or email is wrong", HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/account")
    public String accountPage(@NonNull Model generalModel, @NonNull HttpSession session) {
        UserDTO currentUser = (UserDTO) session.getAttribute("currentLoggedInUser");
        generalModel.addAttribute("user", currentUser);

        return "account";
    }

    @GetMapping("/logout")
    public String logout(@NonNull HttpSession session) {
        Integer currentUserId = ((UserDTO) session.getAttribute("currentLoggedInUser")).id();
        userService.updateOnlineStatusToOffline(currentUserId);

        session.invalidate();

        return "redirect:/";
    }

    /**
     * Method for accepting the user on the user list pending to be accepted
     * 
     * @param currentAcceptedFriendId current friend id that user accepted
     * @param session                 current session
     * @return back to contact page
     */
    @GetMapping("/accept")
    public String acceptFriendRequest(@RequestParam("id") Integer currentAcceptedFriendId, HttpSession session) {
        Integer currentUserId = ((UserDTO) session.getAttribute("currentLoggedInUser")).id();
        friendService.acceptFriendRequest(currentUserId, currentAcceptedFriendId);

        return "redirect:/times2/contact";
    }

    /**
     * A method for the user to delete a friend from his friend list of pending list
     * 
     * @param currentDeletedFriendId current friend id that the user deleted from
     *                               friend list or pending list
     * @param session                current session
     * @return back to contact page
     */
    @GetMapping("/delete")
    public String deleteFriendRequestOrFriend(
            @RequestParam("id") @NonNull Integer currentDeletedFriendId,
            @NonNull HttpSession session) {
        Integer currentUserId = ((UserDTO) session.getAttribute("currentLoggedInUser")).id();
        friendService.removeFriend(currentUserId, currentDeletedFriendId);

        return "redirect:/times2/contact";
    }

}


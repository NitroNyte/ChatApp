package xk.nitro.chat.controller;

import java.security.Principal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;


import org.hibernate.boot.model.naming.IllegalIdentifierException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.lang.NonNull;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import xk.nitro.chat.dto.*;
import xk.nitro.chat.dto.signal.DataInput;
import xk.nitro.chat.dto.signal.InputTypeChecker;
import xk.nitro.chat.dto.signal.MultipleDataInput;
import xk.nitro.chat.dto.signal.SingularDataInput;
import xk.nitro.chat.signal.Message;
import xk.nitro.chat.signal.SignalClientBean;
import xk.nitro.chat.enums.MessageUserStatus;
import xk.nitro.chat.service.MessageBufferService;
import xk.nitro.chat.service.WSService;


/**
 * This controller is responsible for messaging to be displayed on the web
 * <br>
 * It uses the classes WSService and MessageBufferService, one handles the SIMP
 * message requests, the other message buffering
 */
@Controller
@RequestMapping("/chat/{id}")
@Scope(value = "websocket", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ChatController {


    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    private final WSService wsService;
    private final MessageBufferService messageBufferService;
    private final SignalClientBean client;

    private final Map<String, Boolean> keyState = new HashMap<>();

    public ChatController(
            @NonNull WSService wsService,
            @NonNull MessageBufferService messageBufferService,
            @NonNull SignalClientBean client) {
        this.wsService = wsService;
        this.messageBufferService = messageBufferService;
        this.client = client;
        keyState.put("IDENTITY_KEY", Boolean.FALSE);
        keyState.put("SIGNED_PRE_KEY", Boolean.FALSE);
        keyState.put("ONE_TIME_PRE_KEYS", Boolean.FALSE);
        keyState.put("KYBER_PRE_KEY", Boolean.FALSE);
        keyState.put("SESSION_RECORD", Boolean.FALSE);
    }

    /**
     * Map to chanel which we communicate then send to a unique user
     * based on Java principal and rely on it for SIMP messages sending
     * <br>
     * Convert a message from Java to JSON and then send with JS, also adds a sent
     * message to buffer to save in a database after the connection is closed
     *
     * @param message   an object of the message we are going to send
     * @param principal current user id
     */
    @MessageMapping("/message")
    @SendToUser("/queue/messages")
    public void sendMessage(@NonNull MessageFormatDTO message, @NonNull Principal principal) {
        String senderIdStringFormat = principal.getName();

        if (senderIdStringFormat == null || !senderIdStringFormat.matches("\\d+")) {
            throw new IllegalIdentifierException("Invalid or missing sender ID");
        }

        Integer senderId = Integer.valueOf(senderIdStringFormat);
        String receiverId = String.valueOf(message.receiverId());


        MessageFormatDTO messageDTO = new MessageFormatDTO(
                senderId,
                message.receiverId(),
                message.content(),
                System.currentTimeMillis(),
                MessageUserStatus.SENT);

        Message messageEncryptedFormat = new Message(
                senderId,
                message.receiverId(),
                client.encryptMessage(receiverId, message.content()),
                MessageUserStatus.SENT);

        messageBufferService.saveToDatabase(messageEncryptedFormat);


        wsService.sendSessionByteCode(senderIdStringFormat, new SingularDataInput(client.getSessionRecord()));
        wsService.notifyUser(senderIdStringFormat, messageDTO);
        wsService.notifyUser(receiverId, messageDTO);
    }

    /**
     * Gets the history of the chat that was done by the two friends
     * The messages get called from db using message buffer class
     *
     * @param currentUserPrincipalId Current user id in this case
     * @param request                DTO which contains the messages from front end and the friend id, it's for simplification
     */
    @MessageMapping("/message/history")
    @SendToUser("/queue/history")
    public void previousMessages(@NonNull Principal currentUserPrincipalId, @Payload HistoryMessageRequest request) {
        String userIdStringFormat = currentUserPrincipalId.getName();

        if (userIdStringFormat == null || !userIdStringFormat.matches("\\d+")) {
            throw new IllegalIdentifierException("Invalid or missing sender ID");
        }

        Integer userId = Integer.valueOf(userIdStringFormat);

        messageBufferService.setOldMessagesBuffer(userId, request.friendId());

        List<xk.nitro.chat.model.Message> messageEntity = messageBufferService
                .getMessageBufferOldMessages(userId);
        List<MessageFormatDTO> messageDTO = new ArrayList<>();


        messageEntity.forEach(msg -> {
            MessageFormatDTO messageConverted = new MessageFormatDTO(
                    msg.getSender().getId(),
                    msg.getReceiver().getId(),
                    client.decryptMessage(msg.getContent()),
                    ZonedDateTime.of(msg.getTimeCreated(), ZoneId.systemDefault()).toInstant().toEpochMilli(),
                    MessageUserStatus.SENT);

            messageDTO.add(messageConverted);
        });

        List<MessageFormatDTO> decryptedMessages = new ArrayList<>(messageDTO);

        messageBufferService.deleteDecryptedMessages(userId, request.friendId());

        if (!request.messages().isEmpty()) {
            messageDTO.addAll(request.messages());
        }

        Collections.sort(messageDTO);

        messageEntity.clear();
        MessageHybridHistory doubleMessages = new MessageHybridHistory(messageDTO, decryptedMessages);

        wsService.notifyUserHistory(userIdStringFormat, doubleMessages);
        wsService.notifyUserHistory(request.friendId().toString(), doubleMessages);

        messageDTO.clear();
        messageBufferService.clearBufferOldMessages(userId);
    }


    @MessageMapping("/user/userKeys")
    public void userKeyInput(@Payload DataInput keyInput) {

        String keyType = keyInput.key();
        InputTypeChecker inputData = keyInput.dataObject();
        switch (keyType) {
            case "SESSION_RECORD" -> {
                if(inputData instanceof SingularDataInput sessionRecord) {
                    client.setSessionRecord(sessionRecord.data());
                    keyState.put("SESSION_RECORD", Boolean.TRUE);
                }
            }
            case "IDENTITY_KEY" -> {
                if (inputData instanceof SingularDataInput identityKey) {
                    client.setIdentityKey(identityKey.data());
                    keyState.put("IDENTITY_KEY", Boolean.TRUE);
                }
            }
            case "SIGNED_PRE_KEY" -> {
                if (inputData instanceof SingularDataInput signedKey) {
                    client.setSignedPreKey(signedKey.data());
                    keyState.put("SIGNED_PRE_KEY", Boolean.TRUE);
                }
            }
            case "ONE_TIME_PRE_KEYS" -> {
                if (inputData instanceof MultipleDataInput oneTimeKeys) {
                    client.setOneTimePreKeyList(oneTimeKeys.data());
                    keyState.put("ONE_TIME_PRE_KEYS", Boolean.TRUE);
                }
            }
            case "KYBER_PRE_KEY" -> {
                if (inputData instanceof SingularDataInput kyberKey) {
                    client.setKyberPreKey(kyberKey.data());
                    keyState.put("KYBER_PRE_KEY", Boolean.TRUE);
                }
            }
            default -> throw new IllegalStateException("Unexpected value: " + keyType);
        }

        if (keyState.get("IDENTITY_KEY") && keyState.get("SIGNED_PRE_KEY")
                && keyState.get("KYBER_PRE_KEY") && keyState.get("ONE_TIME_PRE_KEYS")
                && keyState.get("SESSION_RECORD")) {
            createSessionCipher();
            keyState.clear();
            wsService.confirmContinue(client.getCurrentUserId(), "SESSION_CIPHER_READY");
        }

    }

    public void createSessionCipher() {
        if (client.sessionCipherExists()) {
            client.generateUserSignalStore();
        }
    }

    @MessageMapping("/message/seen")
    public void messageToSeen(@NonNull Principal currentUserPrincipalId, @NonNull String currentFriendId) {
        String userIdStringFormat = currentUserPrincipalId.getName();

        if (userIdStringFormat == null || !userIdStringFormat.matches("\\d+")) {
            log.warn("userID is empty, cannot get status for the user with principal: {}", userIdStringFormat);
            throw new IllegalIdentifierException("Invalid or missing user ID");
        }

        Integer userId = Integer.valueOf(userIdStringFormat);
        Integer friendId = Integer.valueOf(currentFriendId);

        messageBufferService.changeMessageStatusToSeen(friendId, userId);
    }
}

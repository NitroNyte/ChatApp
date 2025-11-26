package xk.nitro.chat.service;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import xk.nitro.chat.dto.MessageFormatDTO;

import org.springframework.lang.NonNull;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import xk.nitro.chat.dto.MessageHybridHistory;
import xk.nitro.chat.dto.signal.SingularDataInput;

import java.util.List;


/**
 * A service class responsible for creating message templates that we can send
 * to front end
 */
@Service
@Scope(scopeName = "websocket", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class WSService {

    private final SimpMessagingTemplate messagingTemplate;

    public WSService(@NonNull SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Using the simp constructor we utilise the method it provides to send to a
     * specific user
     * 
     * @param currentlySendingUser current user we are sending
     * @param messageToBeSended           object of the message we are going to send
     */
    public void notifyUser(@NonNull String currentlySendingUser, @NonNull MessageFormatDTO messageToBeSended) {
        messagingTemplate.convertAndSendToUser(currentlySendingUser, "/queue/messages", messageToBeSended);
    }

    /**
     * Using the simp constructor we utilise the method it provides to retrive
     * message history and send to a specific user currently speaking to
     * 
     * @param currentlySendingUser current user we are sending
     *
     */
    public void notifyUserHistory(@NonNull String currentlySendingUser, @NonNull MessageHybridHistory messageFromHistory) {
        messagingTemplate.convertAndSendToUser(currentlySendingUser, "/queue/history", messageFromHistory);
    }

    public void confirmContinue(@NonNull String currentlySendingUser, @NonNull String messageToBeSended) {
        messagingTemplate.convertAndSendToUser(currentlySendingUser, "/queue/cipherReady", messageToBeSended);
    }

    public void sendSessionByteCode(@NonNull String currentlySendingUser, @NonNull SingularDataInput sessionRecordByte) {
        messagingTemplate.convertAndSendToUser(currentlySendingUser, "/queue/sessionState", sessionRecordByte);
    }

}

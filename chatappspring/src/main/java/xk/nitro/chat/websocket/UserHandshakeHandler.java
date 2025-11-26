package xk.nitro.chat.websocket;

import java.security.Principal;
import java.util.Map;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.lang.NonNull;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import xk.nitro.chat.dto.UserDTO;

import jakarta.servlet.http.HttpSession;

/**
 * This class is used to create a principal for the user, so that we can use it in the websocket connection
 */
public class UserHandshakeHandler extends DefaultHandshakeHandler {


    /**
     * Create a handshake handler, since im not using spring security.
     * This is one way to set principal for private websocket connections
     *
     * @param request the request
     * @param wsHandler the websocket handler
     * @param attributes a list of attribute inside this context
     * @return the Principal
     */
    @Override
    protected Principal determineUser(
            @NonNull ServerHttpRequest request,
            @NonNull WebSocketHandler wsHandler,
            @NonNull Map<String, Object> attributes
    ) {
        if(request instanceof ServletServerHttpRequest servletRequest) {
            HttpSession session = servletRequest.getServletRequest().getSession(false);
            if(session != null) {
                UserDTO currentLoggedInUser = (UserDTO) session.getAttribute("currentLoggedInUser");
                if(currentLoggedInUser != null) {
                    attributes.put("user", currentLoggedInUser);
                    
                    return new UserPrincipal(String.valueOf(currentLoggedInUser.id()));
                }
            }
        }

        //Default return could change in the future
        return new UserPrincipal(null);
    }
}

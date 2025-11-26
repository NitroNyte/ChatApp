package xk.nitro.chat.websocket;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.ArrayList;
import java.util.Map;

/**
 * This class is used to intercept WebSocket handshakes and extract the
 * "friendId" parameter
 * from the request, storing it in the WebSocket session attributes.
 */
public class FriendInfoHandshakeInterceptor implements HandshakeInterceptor {

    /**
     * Intercepts the WebSocket handshake request to extract the "friendId"
     * parameter
     * and store it in the WebSocket session attributes.
     *
     * @return true to proceed with the handshake
     */
    @Override
    public boolean beforeHandshake(
            @NonNull ServerHttpRequest request,
            @NonNull ServerHttpResponse response,
            @NonNull WebSocketHandler wsHandler,
            @NonNull Map<String, Object> attributes) {

        if (request instanceof ServletServerHttpRequest servletRequest) {
            String friendId = servletRequest.getServletRequest().getParameter("friendId");
            if (friendId != null) {
                attributes.put("friendId", friendId);
            }
        }
        return true;
    }

    // Needs to be implemented, but we don't need it
    @Override
    public void afterHandshake(
            @NonNull ServerHttpRequest request,
            @NonNull ServerHttpResponse response,
            @NonNull WebSocketHandler wsHandler,
            @Nullable Exception exception) {
    }
}

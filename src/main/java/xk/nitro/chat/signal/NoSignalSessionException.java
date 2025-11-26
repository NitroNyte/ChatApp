package xk.nitro.chat.signal;

import org.signal.libsignal.protocol.NoSessionException;
import org.springframework.lang.NonNull;

public class NoSignalSessionException extends RuntimeException {

    public NoSignalSessionException(@NonNull String userId, @NonNull NoSessionException parent) {
        super("No session found for user: %s".formatted(userId), parent);
    }
}

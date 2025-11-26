package xk.nitro.chat.enums;

import org.springframework.lang.NonNull;

public enum MessageUserStatus {
    SENT,
    DELIVERED,
    SEEN;

    @NonNull
    public static MessageUserStatus fromString(@NonNull String status) {
        if (status.equals("SEEN")) {
            return SEEN;
        } else if (status.equals("SENT")) {
            return SENT;
        } else {
            return DELIVERED;
        }
    }
}

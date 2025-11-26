package xk.nitro.chat.model;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;

import xk.nitro.chat.enums.MessageUserStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import org.springframework.lang.NonNull;

@Entity
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Column(name = "content", columnDefinition = "LONGBLOB", nullable = false)
    private byte[] content;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MessageUserStatus status = MessageUserStatus.SENT;

    @Column(name = "checked_at", nullable = false)
    private LocalDateTime timeCreated = LocalDateTime.now();

    public Message(
            @NonNull User sender,
            @NonNull User receiver,
            @NonNull byte[] content,
            @NonNull MessageUserStatus status
    ) {
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.status = status;
    }

    protected Message() {
        // EMPTY FOR JPA
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof Message message)) return false;

        return Objects.equals(id, message.id) &&
                Arrays.equals(content, message.content) &&
                Objects.equals(timeCreated, message.timeCreated);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(id);
        result = 31 * result + Arrays.hashCode(content);
        result = 31 * result + Objects.hashCode(timeCreated);
        return result;
    }

    @NonNull
    public User getSender() {
        return sender;
    }

    @NonNull
    public User getReceiver() {
        return receiver;
    }

    @NonNull
    public byte[] getContent() {
        return content;
    }

    @NonNull
    public LocalDateTime getTimeCreated() {
        return timeCreated;
    }

    @NonNull
    public MessageUserStatus getStatus() {
        return status;
    }
}

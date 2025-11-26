package xk.nitro.chat.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class IdentityKeyPublic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_key_id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "device_id", nullable = false)
    private Integer deviceId;

    @Lob
    @Column(name = "id_key_public_key", columnDefinition = "LONGBLOB", nullable = false)
    private byte[] identityKeyPublic;

    @Lob
    @Column(name = "id_key_private_key", columnDefinition = "LONGBLOB", nullable = false)
    private byte[] identityKeyPrivate;

    @Column(name = "registration_id", nullable = false)
    private Integer registrationId;

    @Column(name = "first_created", nullable = false)
    private LocalDateTime firstCreated = LocalDateTime.now();

    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated = LocalDateTime.now();
}

package xk.nitro.chat.model;

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

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class OneTimePreKeyPublic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "otpk_id", nullable = false)
    private Integer otpkId;

    @Column(name = "otpk_key_id", nullable = false)
    private Integer otpkKeyId;

    @Lob
    @Column(name = "otpk_public_key", nullable = false)
    private byte[] otpkPublicKey;

    @Column(name="otpk_key_status", columnDefinition = "LONGBLOB", nullable = false)
    private boolean otpkKeyStatus = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

}

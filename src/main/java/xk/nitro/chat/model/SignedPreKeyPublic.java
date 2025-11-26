package xk.nitro.chat.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = { "spkPublicKey", "spkSignature" })
@Entity
public class SignedPreKeyPublic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "spk_id", nullable = false)
    private Integer spkId;

    @Column(name = "spk_key_id", nullable = false)
    private Integer spkKeyId;

    @Lob
    @Column(name = "spk_public_key", columnDefinition = "LONGBLOB", nullable = false)
    private byte[] spkPublicKey;


    @Lob
    @Column(name = "spk_signature", columnDefinition = "LONGBLOB", nullable = false)
    private byte[] spkSignature;

    @Column(name = "spk_created_time", nullable = false)
    private String spkCreatedTime;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}

package code.with.vanilson.market.identity.domain;

import code.with.vanilson.market.shared.domain.AggregateRoot;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends AggregateRoot {

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String tenantId;

    @Column(name = "totp_secret")
    private String totpSecret;

    @Column(name = "totp_enabled")
    private boolean totpEnabled;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_recovery_codes", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "code")
    @Builder.Default
    private Set<String> recoveryCodes = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    public void enableTotp(String secret) {
        this.totpSecret = secret;
        this.totpEnabled = true;
    }

    public void addRole(Role role) {
        this.roles.add(role);
    }
}

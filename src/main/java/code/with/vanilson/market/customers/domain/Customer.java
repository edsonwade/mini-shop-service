package code.with.vanilson.market.customers.domain;

import code.with.vanilson.market.events.domain.CustomerKycVerifiedEvent;
import code.with.vanilson.market.shared.domain.AggregateRoot;
import code.with.vanilson.market.shared.infrastructure.exception.KycAlreadyVerifiedException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer extends AggregateRoot {

    @Column(nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    private String phone;

    @Builder.Default
    private boolean kycVerified = false;

    public void verifyKyc() {
        if (this.kycVerified) {
            throw new KycAlreadyVerifiedException("KYC already verified");
        }
        this.kycVerified = true;
        registerEvent(new CustomerKycVerifiedEvent(this.getId()));
    }

}

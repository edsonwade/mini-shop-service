/**
 * Author: vanilson muhongo
 * Date:16/02/2026
 * Time:17:56
 * Version:1
 */

package code.with.vanilson.market.events.domain;

import code.with.vanilson.market.shared.domain.DomainEvent;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

public class CustomerKycVerifiedEvent implements DomainEvent {
    @Getter
    private final UUID customerId;
    private final Instant occurredOn;

    public CustomerKycVerifiedEvent(UUID customerId) {
        this.customerId = customerId;
        this.occurredOn = Instant.now();
    }

    @Override
    public Instant occurredOn() {
        return occurredOn;
    }

}

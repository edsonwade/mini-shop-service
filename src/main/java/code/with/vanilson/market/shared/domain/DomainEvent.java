package code.with.vanilson.market.shared.domain;

import java.time.Instant;

public interface DomainEvent {
    Instant occurredOn();
}

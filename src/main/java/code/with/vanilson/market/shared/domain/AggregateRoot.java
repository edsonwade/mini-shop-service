package code.with.vanilson.market.shared.domain;

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import lombok.Getter;
import org.springframework.data.domain.AfterDomainEventPublication;
import org.springframework.data.domain.DomainEvents;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@MappedSuperclass
public abstract class AggregateRoot extends BaseEntity {

    @Transient
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    protected void registerEvent(DomainEvent event) {
        this.domainEvents.add(event);
    }

    @DomainEvents
    protected Collection<DomainEvent> domainEvents() {
        return Collections.unmodifiableList(this.domainEvents);
    }

    @AfterDomainEventPublication
    protected void clearDomainEvents() {
        this.domainEvents.clear();
    }
}

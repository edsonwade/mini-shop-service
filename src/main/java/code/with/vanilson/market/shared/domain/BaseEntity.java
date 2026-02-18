package code.with.vanilson.market.shared.domain;

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.Getter;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.UUID;

@MappedSuperclass
@Getter
@EqualsAndHashCode(of = "id")
public abstract class BaseEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Common fields like version for optimistic locking could go here
    // private Long version;
}

package code.with.vanilson.market.shared.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRegisteredEvent implements Serializable {
    private UUID userId;
    private String email;
    private String tenantId;
    private String name; // Assuming name is captured or derived
}

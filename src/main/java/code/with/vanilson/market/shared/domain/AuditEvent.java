package code.with.vanilson.market.shared.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;
import java.util.Map;

@Document(collection = "audit_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditEvent {
    @Id
    private String id;
    private String userId;
    private String tenantId;
    private String action; // LOGIN_SUCCESS, LOGIN_FAILED, etc.
    private Instant timestamp;
    private Map<String, Object> metadata;
}

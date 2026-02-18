package code.with.vanilson.market.shared.application;

import code.with.vanilson.market.shared.domain.AuditEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final MongoTemplate mongoTemplate;

    @Async
    public void log(String userId, String tenantId, String action, Map<String, Object> metadata) {
        AuditEvent event = AuditEvent.builder()
                .userId(userId)
                .tenantId(tenantId)
                .action(action)
                .timestamp(Instant.now())
                .metadata(metadata)
                .build();
        mongoTemplate.save(event);
    }
}

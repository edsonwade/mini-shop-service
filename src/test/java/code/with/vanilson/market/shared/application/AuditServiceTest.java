package code.with.vanilson.market.shared.application;

import code.with.vanilson.market.shared.domain.AuditEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;


@DisplayName("AuditService Unit Tests")
class AuditServiceTest {


    private final MongoTemplate mongoTemplate = mock(MongoTemplate.class);


    private final AuditService auditService = new AuditService(mongoTemplate);

    private static final String USER_ID = UUID.randomUUID().toString();
    private static final String TENANT_ID = UUID.randomUUID().toString();
    private static final String ACTION = "LOGIN_SUCCESS";

    // ========================= Log Tests =========================

    @Test
    @DisplayName("should successfully log audit event with all parameters")
    void testLog_Success() {
        // Arrange
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("ipAddress", "192.168.1.1");
        metadata.put("browser", "Chrome");

        ArgumentCaptor<AuditEvent> eventCaptor = ArgumentCaptor.forClass(AuditEvent.class);

        // Act
        auditService.log(USER_ID, TENANT_ID, ACTION, metadata);

        // Assert
        verify(mongoTemplate, times(1)).save(eventCaptor.capture());
        AuditEvent capturedEvent = eventCaptor.getValue();

        assertThat(capturedEvent).isNotNull();
        assertThat(capturedEvent.getUserId()).isEqualTo(USER_ID);
        assertThat(capturedEvent.getTenantId()).isEqualTo(TENANT_ID);
        assertThat(capturedEvent.getAction()).isEqualTo(ACTION);
        assertThat(capturedEvent.getMetadata()).isEqualTo(metadata);
        assertThat(capturedEvent.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("should set timestamp to current time when logging audit event")
    void testLog_TimestampSetToCurrent() {
        // Arrange
        Map<String, Object> metadata = new HashMap<>();
        Instant beforeTime = Instant.now();

        ArgumentCaptor<AuditEvent> eventCaptor = ArgumentCaptor.forClass(AuditEvent.class);

        // Act
        auditService.log(USER_ID, TENANT_ID, ACTION, metadata);
        Instant afterTime = Instant.now();

        // Assert
        verify(mongoTemplate, times(1)).save(eventCaptor.capture());
        AuditEvent capturedEvent = eventCaptor.getValue();

        assertThat(capturedEvent.getTimestamp())
                .isAfterOrEqualTo(beforeTime)
                .isBeforeOrEqualTo(afterTime);
    }

    @Test
    @DisplayName("should successfully log audit event with empty metadata")
    void testLog_EmptyMetadata() {
        // Arrange
        Map<String, Object> emptyMetadata = new HashMap<>();

        ArgumentCaptor<AuditEvent> eventCaptor = ArgumentCaptor.forClass(AuditEvent.class);

        // Act
        auditService.log(USER_ID, TENANT_ID, ACTION, emptyMetadata);

        // Assert
        verify(mongoTemplate, times(1)).save(eventCaptor.capture());
        AuditEvent capturedEvent = eventCaptor.getValue();

        assertThat(capturedEvent.getMetadata()).isEmpty();
    }

    @Test
    @DisplayName("should successfully log audit event with null metadata")
    void testLog_NullMetadata() {
        // Arrange
        ArgumentCaptor<AuditEvent> eventCaptor = ArgumentCaptor.forClass(AuditEvent.class);

        // Act
        auditService.log(USER_ID, TENANT_ID, ACTION, null);

        // Assert
        verify(mongoTemplate, times(1)).save(eventCaptor.capture());
        AuditEvent capturedEvent = eventCaptor.getValue();

        assertThat(capturedEvent).isNotNull();
        assertThat(capturedEvent.getUserId()).isEqualTo(USER_ID);
    }

    @Test
    @DisplayName("should successfully log LOGIN_SUCCESS action")
    void testLog_LoginSuccessAction() {
        // Arrange
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("method", "email_password");

        ArgumentCaptor<AuditEvent> eventCaptor = ArgumentCaptor.forClass(AuditEvent.class);

        // Act
        auditService.log(USER_ID, TENANT_ID, "LOGIN_SUCCESS", metadata);

        // Assert
        verify(mongoTemplate, times(1)).save(eventCaptor.capture());
        AuditEvent capturedEvent = eventCaptor.getValue();

        assertThat(capturedEvent.getAction()).isEqualTo("LOGIN_SUCCESS");
    }

    @Test
    @DisplayName("should successfully log LOGIN_FAILED action")
    void testLog_LoginFailedAction() {
        // Arrange
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("reason", "invalid_credentials");

        ArgumentCaptor<AuditEvent> eventCaptor = ArgumentCaptor.forClass(AuditEvent.class);

        // Act
        auditService.log(USER_ID, TENANT_ID, "LOGIN_FAILED", metadata);

        // Assert
        verify(mongoTemplate, times(1)).save(eventCaptor.capture());
        AuditEvent capturedEvent = eventCaptor.getValue();

        assertThat(capturedEvent.getAction()).isEqualTo("LOGIN_FAILED");
    }

    @Test
    @DisplayName("should successfully log LOGOUT action")
    void testLog_LogoutAction() {
        // Arrange
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("logoutType", "manual");

        ArgumentCaptor<AuditEvent> eventCaptor = ArgumentCaptor.forClass(AuditEvent.class);

        // Act
        auditService.log(USER_ID, TENANT_ID, "LOGOUT", metadata);

        // Assert
        verify(mongoTemplate, times(1)).save(eventCaptor.capture());
        AuditEvent capturedEvent = eventCaptor.getValue();

        assertThat(capturedEvent.getAction()).isEqualTo("LOGOUT");
    }

    @Test
    @DisplayName("should successfully log TOKEN_REFRESH action")
    void testLog_TokenRefreshAction() {
        // Arrange
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("tokenType", "refresh_token");

        ArgumentCaptor<AuditEvent> eventCaptor = ArgumentCaptor.forClass(AuditEvent.class);

        // Act
        auditService.log(USER_ID, TENANT_ID, "TOKEN_REFRESH", metadata);

        // Assert
        verify(mongoTemplate, times(1)).save(eventCaptor.capture());
        AuditEvent capturedEvent = eventCaptor.getValue();

        assertThat(capturedEvent.getAction()).isEqualTo("TOKEN_REFRESH");
    }

    @Test
    @DisplayName("should successfully log 2FA_VERIFIED action")
    void testLog_2faVerifiedAction() {
        // Arrange
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("method", "totp");

        ArgumentCaptor<AuditEvent> eventCaptor = ArgumentCaptor.forClass(AuditEvent.class);

        // Act
        auditService.log(USER_ID, TENANT_ID, "2FA_VERIFIED", metadata);

        // Assert
        verify(mongoTemplate, times(1)).save(eventCaptor.capture());
        AuditEvent capturedEvent = eventCaptor.getValue();

        assertThat(capturedEvent.getAction()).isEqualTo("2FA_VERIFIED");
    }

    @Test
    @DisplayName("should successfully log PASSWORD_CHANGE action")
    void testLog_PasswordChangeAction() {
        // Arrange
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("timestamp", System.currentTimeMillis());

        ArgumentCaptor<AuditEvent> eventCaptor = ArgumentCaptor.forClass(AuditEvent.class);

        // Act
        auditService.log(USER_ID, TENANT_ID, "PASSWORD_CHANGE", metadata);

        // Assert
        verify(mongoTemplate, times(1)).save(eventCaptor.capture());
        AuditEvent capturedEvent = eventCaptor.getValue();

        assertThat(capturedEvent.getAction()).isEqualTo("PASSWORD_CHANGE");
    }

    @Test
    @DisplayName("should successfully log audit event with complex metadata")
    void testLog_ComplexMetadata() {
        // Arrange
        Map<String, Object> complexMetadata = new HashMap<>();
        complexMetadata.put("ipAddress", "192.168.1.100");
        complexMetadata.put("browser", "Mozilla Firefox");
        complexMetadata.put("os", "Windows 10");
        complexMetadata.put("userId", USER_ID);
        complexMetadata.put("timestamp", System.currentTimeMillis());
        complexMetadata.put("statusCode", 200);
        complexMetadata.put("isSuccess", true);

        ArgumentCaptor<AuditEvent> eventCaptor = ArgumentCaptor.forClass(AuditEvent.class);

        // Act
        auditService.log(USER_ID, TENANT_ID, "LOGIN_SUCCESS", complexMetadata);

        // Assert
        verify(mongoTemplate, times(1)).save(eventCaptor.capture());
        AuditEvent capturedEvent = eventCaptor.getValue();

        assertThat(capturedEvent.getMetadata()).hasSize(7);
        assertThat(capturedEvent.getMetadata()).containsEntry("ipAddress", "192.168.1.100");
        assertThat(capturedEvent.getMetadata()).containsEntry("browser", "Mozilla Firefox");
        assertThat(capturedEvent.getMetadata()).containsEntry("statusCode", 200);
    }

    @Test
    @DisplayName("should successfully call mongoTemplate.save with AuditEvent")
    void testLog_CallsMongoTemplateSave() {
        // Arrange
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("key", "value");

        // Act
        auditService.log(USER_ID, TENANT_ID, ACTION, metadata);

        // Assert
        verify(mongoTemplate, times(1)).save(any(AuditEvent.class));
        verifyNoMoreInteractions(mongoTemplate);
    }

    @Test
    @DisplayName("should log audit event with different user IDs")
    void testLog_DifferentUserIds() {
        // Arrange
        String userId1 = UUID.randomUUID().toString();
        String userId2 = UUID.randomUUID().toString();
        Map<String, Object> metadata = new HashMap<>();

        ArgumentCaptor<AuditEvent> eventCaptor = ArgumentCaptor.forClass(AuditEvent.class);

        // Act
        auditService.log(userId1, TENANT_ID, ACTION, metadata);
        auditService.log(userId2, TENANT_ID, ACTION, metadata);

        // Assert
        verify(mongoTemplate, times(2)).save(eventCaptor.capture());

        assertThat(eventCaptor.getAllValues()).hasSize(2);
        assertThat(eventCaptor.getAllValues().get(0).getUserId()).isEqualTo(userId1);
        assertThat(eventCaptor.getAllValues().get(1).getUserId()).isEqualTo(userId2);
    }

    @Test
    @DisplayName("should log audit event with different tenant IDs")
    void testLog_DifferentTenantIds() {
        // Arrange
        String tenantId1 = UUID.randomUUID().toString();
        String tenantId2 = UUID.randomUUID().toString();
        Map<String, Object> metadata = new HashMap<>();

        ArgumentCaptor<AuditEvent> eventCaptor = ArgumentCaptor.forClass(AuditEvent.class);

        // Act
        auditService.log(USER_ID, tenantId1, ACTION, metadata);
        auditService.log(USER_ID, tenantId2, ACTION, metadata);

        // Assert
        verify(mongoTemplate, times(2)).save(eventCaptor.capture());

        assertThat(eventCaptor.getAllValues()).hasSize(2);
        assertThat(eventCaptor.getAllValues().get(0).getTenantId()).isEqualTo(tenantId1);
        assertThat(eventCaptor.getAllValues().get(1).getTenantId()).isEqualTo(tenantId2);
    }

    @Test
    @DisplayName("should log multiple audit events sequentially")
    void testLog_MultipleSequentialEvents() {
        // Arrange
        Map<String, Object> metadata1 = new HashMap<>();
        metadata1.put("event", "first");

        Map<String, Object> metadata2 = new HashMap<>();
        metadata2.put("event", "second");

        Map<String, Object> metadata3 = new HashMap<>();
        metadata3.put("event", "third");

        ArgumentCaptor<AuditEvent> eventCaptor = ArgumentCaptor.forClass(AuditEvent.class);

        // Act
        auditService.log(USER_ID, TENANT_ID, "ACTION_1", metadata1);
        auditService.log(USER_ID, TENANT_ID, "ACTION_2", metadata2);
        auditService.log(USER_ID, TENANT_ID, "ACTION_3", metadata3);

        // Assert
        verify(mongoTemplate, times(3)).save(eventCaptor.capture());

        assertThat(eventCaptor.getAllValues()).hasSize(3);
        assertThat(eventCaptor.getAllValues())
                .extracting("action")
                .containsExactly("ACTION_1", "ACTION_2", "ACTION_3");
    }

    @Test
    @DisplayName("should handle log call with special characters in action")
    void testLog_SpecialCharactersInAction() {
        // Arrange
        String action = "ACTION_WITH_SPECIAL_CHARS_@#$%";
        Map<String, Object> metadata = new HashMap<>();

        ArgumentCaptor<AuditEvent> eventCaptor = ArgumentCaptor.forClass(AuditEvent.class);

        // Act
        auditService.log(USER_ID, TENANT_ID, action, metadata);

        // Assert
        verify(mongoTemplate, times(1)).save(eventCaptor.capture());
        AuditEvent capturedEvent = eventCaptor.getValue();

        assertThat(capturedEvent.getAction()).isEqualTo(action);
    }

    @Test
    @DisplayName("should handle log call with very long user ID")
    void testLog_VeryLongUserId() {
        // Arrange
        String longUserId = "a".repeat(500);
        Map<String, Object> metadata = new HashMap<>();

        ArgumentCaptor<AuditEvent> eventCaptor = ArgumentCaptor.forClass(AuditEvent.class);

        // Act
        auditService.log(longUserId, TENANT_ID, ACTION, metadata);

        // Assert
        verify(mongoTemplate, times(1)).save(eventCaptor.capture());
        AuditEvent capturedEvent = eventCaptor.getValue();

        assertThat(capturedEvent.getUserId()).isEqualTo(longUserId);
    }

    @Test
    @DisplayName("should handle log call with metadata containing null values")
    void testLog_MetadataWithNullValues() {
        // Arrange
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("key1", "value1");
        metadata.put("key2", null);
        metadata.put("key3", "value3");

        ArgumentCaptor<AuditEvent> eventCaptor = ArgumentCaptor.forClass(AuditEvent.class);

        // Act
        auditService.log(USER_ID, TENANT_ID, ACTION, metadata);

        // Assert
        verify(mongoTemplate, times(1)).save(eventCaptor.capture());
        AuditEvent capturedEvent = eventCaptor.getValue();

        assertThat(capturedEvent.getMetadata()).containsEntry("key2", null);
    }

    @Test
    @DisplayName("should verify AuditEvent object properties are properly set")
    void testLog_VerifyAllProperties() {
        // Arrange
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("ipAddress", "10.0.0.1");
        metadata.put("status", "success");

        ArgumentCaptor<AuditEvent> eventCaptor = ArgumentCaptor.forClass(AuditEvent.class);

        // Act
        auditService.log(USER_ID, TENANT_ID, ACTION, metadata);

        // Assert
        verify(mongoTemplate, times(1)).save(eventCaptor.capture());
        AuditEvent capturedEvent = eventCaptor.getValue();

        assertThat(capturedEvent)
                .hasFieldOrPropertyWithValue("userId", USER_ID)
                .hasFieldOrPropertyWithValue("tenantId", TENANT_ID)
                .hasFieldOrPropertyWithValue("action", ACTION)
                .hasFieldOrProperty("timestamp")
                .hasFieldOrProperty("metadata");
    }
}


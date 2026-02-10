package com.iyte_yazilim.proje_pazari.presentation.mappers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.presentation.security.UserPrincipal;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

class AutoRequestMapperTest {

    private AutoRequestMapper mapper;

    public record SimpleCommand(String name, int age) implements IRequest<Void> {}

    public record UserIdCommand(String userId, String name) implements IRequest<Void> {}

    public record OwnerIdCommand(String ownerId, String title) implements IRequest<Void> {}

    public record MultiFieldAuthCommand(String userId, String ownerId, String data)
            implements IRequest<Void> {}

    public record FileUploadCommand(String userId, MultipartFile file) implements IRequest<Void> {}

    public record EmptyCommand() implements IRequest<Void> {}

    public record RequesterCommand(String requesterId, String action) implements IRequest<Void> {}

    @BeforeEach
    void setUp() {
        mapper = new AutoRequestMapper();
    }

    private Authentication mockAuth(String userId) {
        Authentication auth = mock(Authentication.class);
        UserPrincipal principal = new UserPrincipal(userId, "test@example.com", "USER");
        when(auth.getPrincipal()).thenReturn(principal);
        return auth;
    }

    @Nested
    @DisplayName("Body-only mapping")
    class BodyOnlyMapping {

        @Test
        @DisplayName("should map body fields to record")
        void shouldMapBodyFieldsToRecord() {
            SimpleCommand body = new SimpleCommand("Alice", 25);

            SimpleCommand result = mapper.map(SimpleCommand.class, null, null, body, null);

            assertEquals("Alice", result.name());
            assertEquals(25, result.age());
        }

        @Test
        @DisplayName("should handle null body")
        void shouldHandleNullBody() {
            SimpleCommand result = mapper.map(SimpleCommand.class, null, null, null, null);

            assertNull(result.name());
            assertEquals(0, result.age());
        }
    }

    @Nested
    @DisplayName("Authentication injection")
    class AuthInjection {

        @Test
        @DisplayName("should inject userId from auth")
        void shouldInjectUserIdFromAuth() {
            UserIdCommand body = new UserIdCommand("ignored", "Alice");
            Authentication auth = mockAuth("auth-user-123");

            UserIdCommand result = mapper.map(UserIdCommand.class, null, null, body, auth);

            assertEquals("auth-user-123", result.userId());
            assertEquals("Alice", result.name());
        }

        @Test
        @DisplayName("should inject ownerId from auth")
        void shouldInjectOwnerIdFromAuth() {
            OwnerIdCommand body = new OwnerIdCommand("malicious-id", "Project X");
            Authentication auth = mockAuth("real-owner-456");

            OwnerIdCommand result = mapper.map(OwnerIdCommand.class, null, null, body, auth);

            assertEquals("real-owner-456", result.ownerId());
            assertEquals("Project X", result.title());
        }

        @Test
        @DisplayName("should inject requesterId from auth")
        void shouldInjectRequesterIdFromAuth() {
            RequesterCommand body = new RequesterCommand("ignored", "doSomething");
            Authentication auth = mockAuth("requester-789");

            RequesterCommand result =
                    mapper.map(RequesterCommand.class, null, null, body, auth);

            assertEquals("requester-789", result.requesterId());
            assertEquals("doSomething", result.action());
        }

        @Test
        @DisplayName("should inject all auth-eligible fields")
        void shouldInjectAllAuthEligibleFields() {
            MultiFieldAuthCommand body = new MultiFieldAuthCommand("bad1", "bad2", "keepme");
            Authentication auth = mockAuth("secure-user");

            MultiFieldAuthCommand result =
                    mapper.map(MultiFieldAuthCommand.class, null, null, body, auth);

            assertEquals("secure-user", result.userId());
            assertEquals("secure-user", result.ownerId());
            assertEquals("keepme", result.data());
        }
    }

    @Nested
    @DisplayName("Malicious override prevention")
    class MaliciousOverridePrevention {

        @Test
        @DisplayName("should override malicious userId from body with auth userId")
        void shouldOverrideMaliciousUserId() {
            UserIdCommand body = new UserIdCommand("attacker-id", "Evil");
            Authentication auth = mockAuth("real-user-id");

            UserIdCommand result = mapper.map(UserIdCommand.class, null, null, body, auth);

            assertEquals("real-user-id", result.userId());
        }
    }

    @Nested
    @DisplayName("Path variable mapping")
    class PathVariableMapping {

        @Test
        @DisplayName("should map path variables to record fields")
        void shouldMapPathVariables() {
            Map<String, String> pathVars = Map.of("name", "Bob");

            SimpleCommand result =
                    mapper.map(SimpleCommand.class, pathVars, null, null, null);

            assertEquals("Bob", result.name());
        }

        @Test
        @DisplayName("path variables should override body")
        void pathVariablesShouldOverrideBody() {
            SimpleCommand body = new SimpleCommand("BodyName", 20);
            Map<String, String> pathVars = Map.of("name", "PathName");

            SimpleCommand result =
                    mapper.map(SimpleCommand.class, pathVars, null, body, null);

            assertEquals("PathName", result.name());
            assertEquals(20, result.age());
        }

        @Test
        @DisplayName("query params should override body but not path vars")
        void queryParamsShouldOverrideBody() {
            SimpleCommand body = new SimpleCommand("BodyName", 20);
            Map<String, String> queryParams = Map.of("name", "QueryName");
            Map<String, String> pathVars = Map.of("name", "PathName");

            SimpleCommand result =
                    mapper.map(SimpleCommand.class, pathVars, queryParams, body, null);

            assertEquals("PathName", result.name());
        }
    }

    @Nested
    @DisplayName("MultipartFile handling")
    class MultipartFileHandling {

        @Test
        @DisplayName("should handle MultipartFile via constructor")
        void shouldHandleMultipartFile() {
            MockMultipartFile file =
                    new MockMultipartFile("file", "test.jpg", "image/jpeg", "data".getBytes());
            Authentication auth = mockAuth("user-123");

            FileUploadCommand result =
                    mapper.map(
                            FileUploadCommand.class,
                            null,
                            null,
                            null,
                            auth,
                            Map.of("file", file));

            assertEquals("user-123", result.userId());
            assertNotNull(result.file());
            assertEquals("test.jpg", result.file().getOriginalFilename());
        }
    }

    @Nested
    @DisplayName("Empty records")
    class EmptyRecords {

        @Test
        @DisplayName("should handle empty record with no fields")
        void shouldHandleEmptyRecord() {
            EmptyCommand result = mapper.map(EmptyCommand.class, null, null, null, null);

            assertNotNull(result);
        }
    }

    @Nested
    @DisplayName("Null auth safety")
    class NullAuthSafety {

        @Test
        @DisplayName("should not inject auth fields when auth is null")
        void shouldNotInjectWhenAuthIsNull() {
            UserIdCommand body = new UserIdCommand("original-id", "Alice");

            UserIdCommand result = mapper.map(UserIdCommand.class, null, null, body, null);

            assertEquals("original-id", result.userId());
        }

        @Test
        @DisplayName("should not inject when principal is not UserPrincipal")
        void shouldNotInjectWhenPrincipalIsNotUserPrincipal() {
            UserIdCommand body = new UserIdCommand("original-id", "Alice");
            Authentication auth = mock(Authentication.class);
            when(auth.getPrincipal()).thenReturn("some-string-principal");

            UserIdCommand result = mapper.map(UserIdCommand.class, null, null, body, auth);

            assertEquals("original-id", result.userId());
        }
    }

    @Nested
    @DisplayName("Merge priority")
    class MergePriority {

        @Test
        @DisplayName("should follow merge priority: body < queryParams < pathVars < auth < extraFields")
        void shouldFollowMergePriority() {
            UserIdCommand body = new UserIdCommand("body-user", "body-name");
            Map<String, String> queryParams = new HashMap<>();
            queryParams.put("name", "query-name");
            Map<String, String> pathVars = new HashMap<>();
            Authentication auth = mockAuth("auth-user");

            UserIdCommand result =
                    mapper.map(UserIdCommand.class, pathVars, queryParams, body, auth);

            assertEquals("auth-user", result.userId());
            assertEquals("query-name", result.name());
        }

        @Test
        @DisplayName("extraFields should have highest priority")
        void extraFieldsShouldHaveHighestPriority() {
            UserIdCommand body = new UserIdCommand("body-user", "body-name");
            Authentication auth = mockAuth("auth-user");
            Map<String, Object> extraFields = Map.of("userId", "extra-user");

            UserIdCommand result =
                    mapper.map(
                            UserIdCommand.class, null, null, body, auth, extraFields);

            assertEquals("extra-user", result.userId());
        }
    }
}

package com.iyte_yazilim.proje_pazari.presentation.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.iyte_yazilim.proje_pazari.application.common.IMediator;
import com.iyte_yazilim.proje_pazari.application.queries.downloadFile.DownloadFileQuery;
import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.presentation.mappers.IRequestMapper;
import com.iyte_yazilim.proje_pazari.presentation.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
        controllers = FileController.class,
        properties = "spring.data.elasticsearch.enabled=true")
class FileControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private IMediator mediator;

    @MockitoBean private IRequestMapper requestMapper;

    @MockitoBean private JwtUtil jwtUtil;

    @MockitoBean private UserDetailsService userDetailsService;

    @MockitoBean private AuthenticationProvider authenticationProvider;

    @MockitoBean private MessageService messageService;

    @MockitoBean private UserRepository userRepository;

    @MockitoBean
    private com.iyte_yazilim.proje_pazari.domain.interfaces.TokenBlacklistService
            tokenBlacklistService;

    @MockitoBean
    private com.iyte_yazilim.proje_pazari.infrastructure.security.config.RateLimitConfig
            rateLimitConfig;

    @MockitoBean
    private com.iyte_yazilim.proje_pazari.infrastructure.security.filter.IpBanFilter ipBanFilter;

    @MockitoBean
    private com.iyte_yazilim.proje_pazari.infrastructure.security.filter.MaintenanceModeFilter
            maintenanceModeFilter;

    @BeforeEach
    void setUp() throws Exception {
        when(messageService.getMessage(anyString())).thenReturn("Error");

        // Mock filters to allow requests through
        Mockito.doAnswer(
                        invocation -> {
                            jakarta.servlet.FilterChain chain = invocation.getArgument(2);
                            chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
                            return null;
                        })
                .when(ipBanFilter)
                .doFilter(any(), any(), any());

        Mockito.doAnswer(
                        invocation -> {
                            jakarta.servlet.FilterChain chain = invocation.getArgument(2);
                            chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
                            return null;
                        })
                .when(maintenanceModeFilter)
                .doFilter(any(), any(), any());
    }

    @Nested
    @DisplayName("GET /api/v1/files/{path} - Download File")
    class DownloadFileTests {

        @Test
        @DisplayName("Should redirect to presigned URL when file exists")
        void shouldRedirectToPresignedUrl_whenFileExists() throws Exception {
            String presignedUrl =
                    "https://minio.example.com/bucket/profiles/ulid123.jpg?signed=true";
            when(mediator.send(any(DownloadFileQuery.class)))
                    .thenReturn(
                            ApiResponse.success(presignedUrl, "File URL generated successfully"));

            mockMvc.perform(get("/api/v1/files/profiles/ulid123.jpg"))
                    .andExpect(status().isFound())
                    .andExpect(header().string(HttpHeaders.LOCATION, presignedUrl));
        }

        @Test
        @DisplayName("Should return 404 when file does not exist")
        void shouldReturn404_whenFileNotFound() throws Exception {
            when(mediator.send(any(DownloadFileQuery.class)))
                    .thenReturn(ApiResponse.notFound("File not found"));

            mockMvc.perform(get("/api/v1/files/profiles/nonexistent.jpg"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("File not found"));
        }

        @Test
        @DisplayName("Should return 400 when path contains directory traversal")
        void shouldReturn400_whenPathContainsDirectoryTraversal() throws Exception {
            when(mediator.send(any(DownloadFileQuery.class)))
                    .thenReturn(ApiResponse.badRequest("Invalid file path"));

            mockMvc.perform(get("/api/v1/files/../../etc/passwd"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should be accessible without authentication (public endpoint)")
        void shouldBeAccessibleWithoutAuthentication() throws Exception {
            String presignedUrl = "https://minio.example.com/bucket/profiles/photo.png?signed=true";
            when(mediator.send(any(DownloadFileQuery.class)))
                    .thenReturn(
                            ApiResponse.success(presignedUrl, "File URL generated successfully"));

            // No Authorization header — endpoint is permitAll
            mockMvc.perform(get("/api/v1/files/profiles/photo.png"))
                    .andExpect(status().isFound())
                    .andExpect(header().string(HttpHeaders.LOCATION, presignedUrl));
        }

        @Test
        @DisplayName("Should pass correct path to mediator")
        void shouldPassCorrectPathToMediator() throws Exception {
            when(mediator.send(any(DownloadFileQuery.class)))
                    .thenReturn(
                            ApiResponse.success(
                                    "https://example.com/url", "File URL generated successfully"));

            mockMvc.perform(get("/api/v1/files/projects/user123/document.pdf"))
                    .andExpect(status().isFound());

            ArgumentCaptor<DownloadFileQuery> captor =
                    ArgumentCaptor.forClass(DownloadFileQuery.class);
            verify(mediator).send(captor.capture());
            assert captor.getValue().path().contains("projects/user123/document.pdf");
        }

        @Test
        @DisplayName("Should handle URL-encoded file paths")
        void shouldHandleUrlEncodedFilePaths() throws Exception {
            when(mediator.send(any(DownloadFileQuery.class)))
                    .thenReturn(
                            ApiResponse.success(
                                    "https://example.com/url", "File URL generated successfully"));

            mockMvc.perform(get("/api/v1/files/profiles/file%20name.jpg"))
                    .andExpect(status().isFound());

            verify(mediator).send(any(DownloadFileQuery.class));
        }

        @Test
        @DisplayName("Should redirect for PDF file download")
        void shouldRedirectForPdfDownload() throws Exception {
            String presignedUrl = "https://minio.example.com/bucket/docs/report.pdf?signed=true";
            when(mediator.send(any(DownloadFileQuery.class)))
                    .thenReturn(
                            ApiResponse.success(presignedUrl, "File URL generated successfully"));

            mockMvc.perform(get("/api/v1/files/docs/report.pdf"))
                    .andExpect(status().isFound())
                    .andExpect(header().string(HttpHeaders.LOCATION, presignedUrl));
        }

        @Test
        @DisplayName("Should redirect for image file download")
        void shouldRedirectForImageDownload() throws Exception {
            String presignedUrl =
                    "https://minio.example.com/bucket/profiles/avatar.png?signed=true";
            when(mediator.send(any(DownloadFileQuery.class)))
                    .thenReturn(
                            ApiResponse.success(presignedUrl, "File URL generated successfully"));

            mockMvc.perform(get("/api/v1/files/profiles/avatar.png"))
                    .andExpect(status().isFound())
                    .andExpect(header().string(HttpHeaders.LOCATION, presignedUrl));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/files/{path} - File Path Organization")
    class FilePathOrganizationTests {

        @Test
        @DisplayName("Should support user-scoped file paths")
        void shouldSupportUserScopedPaths() throws Exception {
            when(mediator.send(any(DownloadFileQuery.class)))
                    .thenReturn(
                            ApiResponse.success(
                                    "https://example.com/url", "File URL generated successfully"));

            mockMvc.perform(get("/api/v1/files/profiles/user123-ulid.jpg"))
                    .andExpect(status().isFound());

            ArgumentCaptor<DownloadFileQuery> captor =
                    ArgumentCaptor.forClass(DownloadFileQuery.class);
            verify(mediator).send(captor.capture());
            assert captor.getValue().path().contains("profiles/");
        }

        @Test
        @DisplayName("Should support project-scoped file paths")
        void shouldSupportProjectScopedPaths() throws Exception {
            when(mediator.send(any(DownloadFileQuery.class)))
                    .thenReturn(
                            ApiResponse.success(
                                    "https://example.com/url", "File URL generated successfully"));

            mockMvc.perform(get("/api/v1/files/projects/proj-ulid/attachment.pdf"))
                    .andExpect(status().isFound());

            ArgumentCaptor<DownloadFileQuery> captor =
                    ArgumentCaptor.forClass(DownloadFileQuery.class);
            verify(mediator).send(captor.capture());
            assert captor.getValue().path().contains("projects/");
        }
    }
}

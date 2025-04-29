package com.secure.controller;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.secure.exception.CustomException;
import com.secure.model.BlockedUser;
import com.secure.repository.BlockedUserRepository;
import com.secure.utils.EmailProvider;
import com.secure.utils.JwtProvider;
import com.secure.utils.TemplateProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlockedUserControllerTest {

    @InjectMocks
    private BlockedUserController blockedUserController;

    @Mock
    private BlockedUserRepository blockedUserRepository;

    @Mock
    private EmailProvider emailProvider;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private TemplateProvider templateProvider;

    @Mock
    private DecodedJWT decodedJWT;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        blockedUserController = new BlockedUserController(
                blockedUserRepository, emailProvider, jwtProvider, templateProvider
        );
    }

    @Test
    void warning_success() {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer test-token");

        when(jwtProvider.extractAuthToken(any())).thenReturn("test-token");
        when(jwtProvider.extractClaims(any())).thenReturn(decodedJWT);
        Claim mockBankClaim = mock(Claim.class);
        Claim mockEmailClaim = mock(Claim.class);

        when(decodedJWT.getClaim("userBank")).thenReturn(mockBankClaim);
        when(decodedJWT.getClaim("email")).thenReturn(mockEmailClaim);

        when(mockBankClaim.asString()).thenReturn("TestBank");
        when(mockEmailClaim.asString()).thenReturn("test@example.com");
        when(templateProvider.buildMultipleAttemptWarningEmail(any())).thenReturn("Email body");

        // Act
        ResponseEntity<Map<String, Object>> response = blockedUserController.warning(request, "login");

        // Assert
        assertEquals("success", response.getBody().get("status"));
        verify(emailProvider, times(1)).sendEmail(eq("test@example.com"), any(), eq("Email body"));
    }

    @Test
    void block_success() {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer test-token");

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("reason", "Suspicious activity");

        when(jwtProvider.extractAuthToken(any())).thenReturn("test-token");
        when(jwtProvider.extractClaims(any())).thenReturn(decodedJWT);
        Claim mockBankClaim = mock(Claim.class);
        Claim mockEmailClaim = mock(Claim.class);

        when(decodedJWT.getClaim("userBank")).thenReturn(mockBankClaim);
        when(decodedJWT.getClaim("email")).thenReturn(mockEmailClaim);

        when(mockBankClaim.asString()).thenReturn("TestBank");
        when(mockEmailClaim.asString()).thenReturn("block@example.com");
        when(templateProvider.buildAccountSuspendedEmail(any(), any())).thenReturn("Blocked email content");

        // Act
        ResponseEntity<Map<String, Object>> response = blockedUserController.block(request, requestBody);

        // Assert
        assertEquals("success", response.getBody().get("status"));
        verify(blockedUserRepository, times(1)).save(any(BlockedUser.class));
        verify(emailProvider, times(1)).sendEmail(eq("block@example.com"), any(), eq("Blocked email content"));
    }

    @Test
    void warning_missingJwtToken() {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        // No JWT token in the Authorization header

        // Act
        ResponseEntity<Map<String, Object>> response = blockedUserController.warning(request, "login");

        // Assert
        assertEquals("error", response.getBody().get("status"));
        assertEquals("JWT token is missing.", response.getBody().get("message"));
        verify(emailProvider, times(0)).sendEmail(any(), any(), any());
    }

    @Test
    void warning_invalidJwtToken() {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer invalid-token");

        when(jwtProvider.extractAuthToken(any())).thenReturn("invalid-token");
        when(jwtProvider.extractClaims(any())).thenReturn(null); // Simulate invalid token

        // Act
        ResponseEntity<Map<String, Object>> response = blockedUserController.warning(request, "login");

        // Assert
        assertEquals("error", response.getBody().get("status"));
        assertEquals("Invalid JWT token.", response.getBody().get("message"));
        verify(emailProvider, times(0)).sendEmail(any(), any(), any());
    }

    @Test
    void warning_missingUserInfoInJwt() {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-token");

        when(jwtProvider.extractAuthToken(any())).thenReturn("valid-token");
        when(jwtProvider.extractClaims(any())).thenReturn(decodedJWT);
        when(decodedJWT.getClaim("userBank")).thenReturn(null); // Simulate missing userBank
        when(decodedJWT.getClaim("email")).thenReturn(null); // Simulate missing email

        // Act
        ResponseEntity<Map<String, Object>> response = blockedUserController.warning(request, "login");

        // Assert
        assertEquals("error", response.getBody().get("status"));
        assertEquals("userBank or userEmail not found in JWT.", response.getBody().get("message"));
        verify(emailProvider, times(0)).sendEmail(any(), any(), any());
    }


    @Test
    void block_serviceLayerFailure() {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-token");

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("reason", "Suspicious activity");

        when(jwtProvider.extractAuthToken(any())).thenReturn("valid-token");
        when(jwtProvider.extractClaims(any())).thenReturn(decodedJWT);
        when(decodedJWT.getClaim("userBank")).thenReturn(mock(Claim.class));
        when(decodedJWT.getClaim("email")).thenReturn(mock(Claim.class));

        when(decodedJWT.getClaim("userBank").asString()).thenReturn("TestBank");
        when(decodedJWT.getClaim("email").asString()).thenReturn("test@example.com");

        // Simulate database save failure
        doThrow(new RuntimeException("Database error")).when(blockedUserRepository).save(any(BlockedUser.class));

        // Act
        ResponseEntity<Map<String, Object>> response = blockedUserController.block(request, requestBody);

        // Assert
        assertEquals("error", response.getBody().get("status"));
        assertEquals("An unexpected error occurred: Database error", response.getBody().get("message"));
        verify(emailProvider, times(0)).sendEmail(any(), any(), any());
    }

    @Test
    void block_emailSendingFailure() {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-token");

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("reason", "Suspicious activity");

        when(jwtProvider.extractAuthToken(any())).thenReturn("valid-token");
        when(jwtProvider.extractClaims(any())).thenReturn(decodedJWT);
        when(decodedJWT.getClaim("userBank")).thenReturn(mock(Claim.class));
        when(decodedJWT.getClaim("email")).thenReturn(mock(Claim.class));

        when(decodedJWT.getClaim("userBank").asString()).thenReturn("TestBank");
        when(decodedJWT.getClaim("email").asString()).thenReturn("test@example.com");
        when(templateProvider.buildAccountSuspendedEmail(any(), any())).thenReturn("Blocked email content");

        // Simulate email sending failure
        doThrow(new RuntimeException("Email sending failed")).when(emailProvider).sendEmail(any(), any(), any());

        // Act
        ResponseEntity<Map<String, Object>> response = blockedUserController.block(request, requestBody);

        // Assert
        assertEquals("error", response.getBody().get("status"));
        assertEquals("An unexpected error occurred: Email sending failed", response.getBody().get("message"));
    }

    @Test
    void block_generalException() {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-token");

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("reason", "Suspicious activity");

        when(jwtProvider.extractAuthToken(any())).thenReturn("valid-token");
        when(jwtProvider.extractClaims(any())).thenReturn(decodedJWT);
        when(decodedJWT.getClaim("userBank")).thenReturn(mock(Claim.class));
        when(decodedJWT.getClaim("email")).thenReturn(mock(Claim.class));

        when(decodedJWT.getClaim("userBank").asString()).thenReturn("TestBank");
        when(decodedJWT.getClaim("email").asString()).thenReturn("test@example.com");

        // Simulate a general unexpected exception
        doThrow(new RuntimeException("Unexpected error")).when(blockedUserRepository).save(any(BlockedUser.class));

        // Act
        ResponseEntity<Map<String, Object>> response = blockedUserController.block(request, requestBody);

        // Assert
        assertEquals("error", response.getBody().get("status"));
        assertEquals("An unexpected error occurred: Unexpected error", response.getBody().get("message"));
    }




}

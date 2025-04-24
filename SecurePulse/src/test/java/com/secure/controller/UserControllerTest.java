package com.secure.controller;

import com.secure.exception.CustomException;
import com.secure.model.User;
import com.secure.repository.BlockedUserRepository;
import com.secure.services.UserService;
import com.secure.utils.DecryptionProvider;
import com.secure.utils.EmailProvider;
import com.secure.utils.JwtProvider;

import com.secure.utils.OtpProvider;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserControllerTest {

    @InjectMocks
    private UserController userController;

    @Mock
    private UserService userService;

    @Mock
    private BlockedUserRepository blockedUserRepository;

    @Mock
    private DecryptionProvider decryptionProvider;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private HttpServletResponse response;

    @Mock
    private OtpProvider otpProvider;

    @Mock
    private EmailProvider emailProvider;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // Utility to mock a User object
    private User mockUser() {
        User user = new User();
        user.setUserId(1);
        user.setEmail("test@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setPhoneNumber("1234567890");
        return user;
    }

    @Test
    void testLoginUser_SuccessfulLogin() throws Exception {
        Map<String, String> loginRequest = Map.of(
                "encryptedEmail", "encEmail",
                "encryptedPassword", "encPass"
        );
        String bank = "TestBank";

        when(decryptionProvider.decryptString("encEmail")).thenReturn("test@example.com");
        when(decryptionProvider.decryptString("encPass")).thenReturn("password");

        User user = mockUser();

        when(userService.getUserByEmailAndPassword("test@example.com", "password", bank))
                .thenReturn(Optional.of(user));

        when(blockedUserRepository.findByEmailAndBankName("test@example.com", bank))
                .thenReturn(Optional.empty());

        when(jwtProvider.generateToken(user, bank)).thenReturn("mocked-jwt-token");

        ResponseEntity<Map<String, Object>> result = userController.loginUser(loginRequest, bank, response);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue((Boolean) result.getBody().get("status"));
        assertEquals("Login successful. JWT token set in cookie.", result.getBody().get("message"));
        assertEquals("mocked-jwt-token", result.getBody().get("utoken"));
    }

    @Test
    void testUpdateMpinAmount_UserNotFound() {
        Map<String, Object> request = Map.of(
                "email", "test@example.com",
                "mpinAmount", "1000"
        );

        when(userService.getUserByEmail("test@example.com")).thenReturn(Optional.empty());

        ResponseEntity<?> response = userController.updateMpinAmount(request);

        assertNotNull(response);
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();

        assertFalse((Boolean) responseBody.get("success"));
        assertEquals("User not found.", responseBody.get("message"));
    }

    @Test
    void testVerifyMpinSuccess() {
        Map<String, String> request = Map.of(
                "eEmail", "encryptedEmail",
                "eMpin", "encryptedMpin"
        );

        when(decryptionProvider.decryptString("encryptedEmail")).thenReturn("test@example.com");
        when(decryptionProvider.decryptString("encryptedMpin")).thenReturn("1234");

        Map<String, Object> responseBody = Map.of(
                "success", true,
                "message", "Verification successful"
        );

        when(userService.verifyMpin("test@example.com", "1234"))
                .thenReturn(ResponseEntity.ok(responseBody));

        ResponseEntity<?> response = userController.verifyMpin(request);

        assertNotNull(response);
        assertTrue(response.getBody() instanceof Map);

        Map<String, Object> responseMap = (Map<String, Object>) response.getBody();

        assertTrue((Boolean) responseMap.get("success"));
        assertEquals("Verification successful", responseMap.get("message"));
    }

    @Test
    void testVerifyMpin_InvalidMpin() {
        Map<String, String> request = Map.of(
                "eEmail", "encryptedEmail",
                "eMpin", "encryptedMpin"
        );

        when(decryptionProvider.decryptString("encryptedEmail")).thenReturn("test@example.com");
        when(decryptionProvider.decryptString("encryptedMpin")).thenReturn("1234");
        when(userService.verifyMpin("test@example.com", "1234"))
                .thenThrow(new CustomException("Invalid MPIN"));

        Exception exception = assertThrows(CustomException.class, () -> userController.verifyMpin(request));

        assertEquals("Invalid MPIN", exception.getMessage());
    }

    @Test
    void testSetMpin_Success() {
        Map<String, String> request = Map.of(
                "email", "test@example.com",
                "mpin", "123456"
        );

        when(userService.setUserMpin("test@example.com", "123456"))
                .thenReturn(ResponseEntity.ok(Map.of("success", true, "message", "MPIN set successfully")));

        ResponseEntity<?> response = userController.setMpin(request);

        assertNotNull(response);
        assertTrue(((Map<?, ?>) response.getBody()).containsKey("success"));
        assertEquals(true, ((Map<?, ?>) response.getBody()).get("success"));
    }

    @Test
    void testSetMpin_InvalidMpin() {
        Map<String, String> request = Map.of(
                "email", "test@example.com",
                "mpin", "123"
        );

        when(userService.setUserMpin("test@example.com", "123"))
                .thenThrow(new CustomException("MPIN must be 6 digits"));

        Exception exception = assertThrows(CustomException.class, () -> userController.setMpin(request));

        assertEquals("MPIN must be 6 digits", exception.getMessage());
    }

    @Test
    void testSendOtp_Success() {
        String email = "test@example.com";
        Map<String, String> request = Map.of("email", email);

        when(otpProvider.generateOtp(email)).thenReturn("123456");

        Map<String, Object> response = userController.sendOtp(request);

        assertTrue((Boolean) response.get("status"));
        assertEquals("OTP has been sent successfully to your email.", response.get("message"));

        verify(otpProvider).generateOtp(email);
        verify(emailProvider).sendEmail(eq(email), anyString(), contains("123456"));
    }

    @Test
    void testSendOtp_MissingEmail() {
        Map<String, String> request = Map.of();

        CustomException exception = assertThrows(CustomException.class, () -> userController.sendOtp(request));
        assertEquals("Email is required to send OTP.", exception.getMessage());
    }

    @Test
    void testVerifyOtp_SuccessWithLoginPurpose() throws Exception {
        String encryptedEmail = "encEmail";
        String encryptedOtp = "encOtp";
        String email = "test@example.com";
        String otp = "123456";
        String purpose = "login";
        String otpToken = "otp.jwt.token";

        Map<String, String> request = Map.of(
                "encryptedEmail", encryptedEmail,
                "encryptedOtp", encryptedOtp,
                "purpose", purpose
        );

        when(decryptionProvider.decryptString(encryptedEmail)).thenReturn(email);
        when(decryptionProvider.decryptString(encryptedOtp)).thenReturn(otp);
        when(otpProvider.validateOtp(email, otp)).thenReturn(true);
        when(jwtProvider.generateOtpToken(email)).thenReturn(otpToken);

        ResponseEntity<Map<String, Object>> result = userController.verifyOtp(request, response);

        assertNotNull(result);
        assertEquals(true, result.getBody().get("otpVerified"));
        assertEquals("OTP verification was successful!", result.getBody().get("message"));
        assertEquals(otpToken, result.getBody().get("otp_token"));

        verify(emailProvider).sendEmail(eq(email), contains("Successful Login"), contains("Login Successful"));
    }

    @Test
    void testVerifyOtp_InvalidOtp() throws Exception {
        String encryptedEmail = "encEmail";
        String encryptedOtp = "encOtp";
        String email = "test@example.com";
        String otp = "wrongOtp";

        Map<String, String> request = Map.of(
                "encryptedEmail", encryptedEmail,
                "encryptedOtp", encryptedOtp,
                "purpose", "login"
        );

        when(decryptionProvider.decryptString(encryptedEmail)).thenReturn(email);
        when(decryptionProvider.decryptString(encryptedOtp)).thenReturn(otp);
        when(otpProvider.validateOtp(email, otp)).thenReturn(false);

        ResponseEntity<Map<String, Object>> result = userController.verifyOtp(request, response);

        assertEquals(false, result.getBody().get("otpVerified"));
        assertEquals("Invalid OTP. Please check and try again.", result.getBody().get("message"));
    }

    @Test
    void testVerifyOtp_MissingFields() {
        Map<String, String> request = Map.of(
                "encryptedEmail", "encEmail"
                // Missing OTP and purpose
        );

        ResponseEntity<?> responseEntity = userController.verifyOtp(request, response);

        assertEquals(200, responseEntity.getStatusCodeValue());
        Map<?, ?> responseBody = (Map<?, ?>) responseEntity.getBody();
        assertEquals(false, responseBody.get("otpVerified"));
        assertEquals("Email, OTP, and purpose are required for verification.", responseBody.get("message"));
    }


}

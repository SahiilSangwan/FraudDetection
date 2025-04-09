package com.secure.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import com.secure.filter.JwtAuthFilter;
import com.secure.services.JwtService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpMethod;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtService jwtService;

    public SecurityConfig(JwtService jwtService) {
        this.jwtService = jwtService;
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Disable CSRF for APIs
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // No session
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/users", "/api/users/login","/api/users/logout","/api/admin/register","/api/users/set-mpin","/api/admin/login").permitAll() // Public routes
                        .requestMatchers("/api/users/sendotp", "/api/users/verifyotp","/api/alert/warning/{purpose}","/api/alert/block").access((authentication, context) -> {
                            HttpServletRequest request = context.getRequest();
                            String authToken = jwtService.extractTokenFromCookies(request, "auth_token");
                            boolean isValidAuthToken = authToken != null && jwtService.validateToken(authToken);
                            System.out.println("🔑 Auth Token Valid: " + isValidAuthToken);
                            return new AuthorizationDecision(isValidAuthToken);
                        })
                        .requestMatchers("/api/admin/**").access((authentication, context) -> {
                            HttpServletRequest request = context.getRequest();
                            String authToken = jwtService.extractTokenFromCookies(request, "admin_token");
                            boolean isValidAuthToken = authToken != null && jwtService.validateToken(authToken);
                            System.out.println("🔑 Admin Token Valid: " + isValidAuthToken);
                            return new AuthorizationDecision(isValidAuthToken);
                        })
                        .anyRequest().access((authentication, context) -> {
                            HttpServletRequest request = context.getRequest();
                            String otpToken = jwtService.extractTokenFromCookies(request, "otp_token");
                            boolean isValidOtpToken = otpToken != null && jwtService.validateToken(otpToken);
                            System.out.println("🔑 OTP Token Valid: " + isValidOtpToken);
                            return new AuthorizationDecision(isValidOtpToken);
                        })
                )
                .addFilterBefore(new JwtAuthFilter(jwtService), UsernamePasswordAuthenticationFilter.class)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()));

        return http.build();
    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*")); // Allow all origins
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS")); // Allow all methods
        configuration.setAllowedHeaders(List.of("*")); // Allow all headers
        configuration.setAllowCredentials(true); // Allow cookies

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
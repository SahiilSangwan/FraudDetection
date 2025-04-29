

package com.secure.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;


import com.secure.filter.JwtAuthFilter;
import com.secure.filter.RateLimitFilter;
import com.secure.utils.JwtProvider;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtProvider jwtProvider;
    private final RateLimitFilter rateLimitFilter;

    public SecurityConfig(JwtProvider jwtProvider, RateLimitFilter rateLimitFilter) {
        this.jwtProvider = jwtProvider;
        this.rateLimitFilter = rateLimitFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Disable CSRF
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Stateless session management
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/users", "/api/users/login", "/api/users/logout", "/api/admin/**", "/api/users/set-mpin","/api/alert/warning/{purpose}", "/api/alert/block")
                        .permitAll() // Allow access to these paths
                        .requestMatchers("/api/users/sendotp", "/api/users/verifyotp")
                        .access((authentication, context) -> {
                            HttpServletRequest request = context.getRequest();
                            String authToken = jwtProvider.extractTokenFromCookies(request, "auth_token");
                            boolean isValidAuthToken = authToken != null && jwtProvider.validateToken(authToken);
                            System.out.println("🔑 Auth Token Valid: " + isValidAuthToken);
                            return new AuthorizationDecision(isValidAuthToken); // Check validity of auth token
                        })
                        .anyRequest()
                        .access((authentication, context) -> {
                            HttpServletRequest request = context.getRequest();
                            String otpToken = jwtProvider.extractTokenFromCookies(request, "otp_token");
                            boolean isValidOtpToken = otpToken != null && jwtProvider.validateToken(otpToken);
                            System.out.println("🔑 OTP Token Valid: " + isValidOtpToken);
                            return new AuthorizationDecision(isValidOtpToken); // Check validity of OTP token
                        })
                )
                .addFilterBefore(new JwtAuthFilter(jwtProvider), UsernamePasswordAuthenticationFilter.class) // Add JwtAuthFilter before UsernamePasswordAuthenticationFilter
                .addFilterAfter(rateLimitFilter, JwtAuthFilter.class) // Add RateLimitFilter after JwtAuthFilter
                .cors(cors -> cors.configurationSource(corsConfigurationSource())); // Configure CORS

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*")); // Allow all origins
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS")); // Allow these HTTP methods
        configuration.setAllowedHeaders(List.of("*")); // Allow all headers
        configuration.setAllowCredentials(true); // Allow credentials in CORS requests

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Apply CORS configuration to all endpoints

        return source;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


}

package com.secure.filter;

import com.auth0.jwt.interfaces.DecodedJWT;

import com.secure.services.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        System.out.println("🔍 Incoming Request: " + requestURI);

        // ✅ Allow `/api/users` & `/api/users/login` without any token
        if (requestURI.startsWith("/api/users") && (requestURI.equals("/api/users") || requestURI.equals("/api/users/login"))) {
            chain.doFilter(request, response);
            return;
        }

        // ✅ For `/api/users/sendotp` & `/api/users/verifyotp` → Require `auth_token`
        if ( requestURI.startsWith("/api/alert") || requestURI.startsWith("/api/users/sendotp") || requestURI.startsWith("/api/users/verifyotp") || requestURI.startsWith("/api/users/logout")) {
            if (!validateTokenFromCookie(request, "auth_token")) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "❌ Forbidden: Invalid auth_token");
                return;
            }
        }
        // ✅ For all other routes → Require `otp_token`
        else {
            if (!validateTokenFromCookie(request, "otp_token")) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "❌ Forbidden: Invalid otp_token");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    /**
     * Extract and validate a JWT token from a cookie
     */
    private boolean validateTokenFromCookie(HttpServletRequest request, String tokenName) {
        String token = jwtService.extractTokenFromCookies(request, tokenName);
        if (token != null) {
            System.out.println("🔑 Checking " + tokenName + ": " + token);
            boolean ans=jwtService.validateToken(token);
            System.out.println(ans);
            return jwtService.validateToken(token);
        }
        return false;
    }
}
package com.secure.filter;

import com.secure.utils.ApplicationCache;
import com.secure.utils.JwtProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    public JwtAuthFilter(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        // ✅ Allow `/api/users` & `/api/users/login` without any token
        if ((requestURI.equals("/api/users") ||
                requestURI.equals("/api/users/login") ||
                requestURI.equals("/api/admin/login") ||
                requestURI.equals("/api/users/set-mpin") ||
                requestURI.equals("/api/admin/register") ||
                requestURI.equals("/api/users/logout") ||
                requestURI.startsWith("/api/alert")
        )) {
            chain.doFilter(request, response);
            return;
        }

        // ✅ For `/api/users/sendotp` → Require `auth_token`
        if (requestURI.startsWith("/api/users/sendotp")) {
            if (!validateTokenFromCookie(request, "auth_token")) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "❌ Forbidden: Invalid auth_token");
                return;
            }
        }

        // ✅ For `/api/users/verifyotp` → Require `otp_token`
        else if (requestURI.startsWith("/api/users/verifyotp")) {
            if (!validateTokenFromCookie(request, "auth_token")) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "❌ Forbidden: Invalid otp_token");
                return;
            }
        }

        // ✅ For `/api/alert` and `/api/admin/.*` → Require `admin_token`
        else if (requestURI.matches("/api/admin/.*")) {
            if (!validateTokenFromCookie(request, "admin_token")) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "❌ Forbidden: Invalid admin_token");
                return;
            }
        }

        // Default: Otherwise, require `otp_token` (For other paths not explicitly mentioned above)
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
        String token = jwtProvider.extractTokenFromCookies(request, tokenName);
        if (token != null ) {
            System.out.println("🔑 Checking " + tokenName + ": " + token);
            if(ApplicationCache.containsKey(token)) return jwtProvider.validateToken(token);
        }
        return false;
    }
}
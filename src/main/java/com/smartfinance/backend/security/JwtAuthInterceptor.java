package com.smartfinance.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtAuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JwtAuthInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(@org.springframework.lang.NonNull HttpServletRequest request,
                             @org.springframework.lang.NonNull HttpServletResponse response,
                             @org.springframework.lang.NonNull Object handler) throws Exception {
        // Allow CORS preflight requests
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String path = request.getRequestURI();

        // Allow public endpoints
        if (path.startsWith("/api/health") ||
            path.equals("/api/auth/login") ||
            path.equals("/api/auth/register") ||
            path.startsWith("/actuator") ||
            path.startsWith("/error")) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7).trim();
            UserPrincipal principal = jwtUtil.parseToken(token);
            if (principal != null) {
                request.setAttribute("userPrincipal", principal);
                return true;
            }
        }

        sendUnauthorized(response, "Unauthorized: Please sign in with valid credentials.");
        return false;
    }

    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("status", 401);
        body.put("message", message);

        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}

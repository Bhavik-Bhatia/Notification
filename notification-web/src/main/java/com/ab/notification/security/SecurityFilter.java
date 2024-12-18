package com.ab.notification.security;


import com.ab.jwt.JwtUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

/**
 * We will use Security Filter for securing our endpoint for sending mails via JWT Tokens.
 */
public class SecurityFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        LOGGER.debug("Enter doFilter()");
        final String authHeader = getToken((HttpServletRequest) request);
        final String jwtToken;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            errorResponse((HttpServletResponse) response, "Invalid authorization header!");
            return;
        }

        jwtToken = authHeader.substring(7);
        if (jwtUtil.isTokenExpired(jwtToken)) {
            errorResponse((HttpServletResponse) response, "User token expired!");
            return;
        }

        String subject = String.valueOf(jwtUtil.extractUsername(jwtToken));
        LOGGER.debug("Exit doFilter(): Subject/Username from token: " + subject);
        filterChain.doFilter(request, response);
    }

    private String getToken(HttpServletRequest request) {
        return request.getHeader("Authorization");
    }

    private void errorResponse(HttpServletResponse response, String message) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        try {
            response.getWriter().write("{ \"error\": \"" + message + "\" }");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}

package com.ab.notification.security;


import com.ab.jwt.JwtUtil;
import com.ab.notification.annotation.Log;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * We will use Security Filter for securing our endpoint for sending mails via JWT Tokens. Also compare
 */
@Component
public class SecurityFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${services.list}")
    private String servicesList;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    @Log
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        final String authHeader = getToken((HttpServletRequest) request);
        final String jwtToken;
        boolean isFromInternalServiceCall = false;

//      For testing purpose only, remove this in production
        if (authHeader == null || !authHeader.startsWith("AdminTest83649498")) {
            LOGGER.debug("Exit doFilter(): Testing mode, bypassing security filter");
            filterChain.doFilter(request, response);
        }


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
        for (String service : servicesList.split(",")) {
            if (service.equals(subject)) {
                isFromInternalServiceCall = true;
                break;
            }
        }
        if (isFromInternalServiceCall) {
            LOGGER.debug("Subject/Username from token: " + subject);
            filterChain.doFilter(request, response);
        } else {
            errorResponse((HttpServletResponse) response, "Request from invalid source!");
        }
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
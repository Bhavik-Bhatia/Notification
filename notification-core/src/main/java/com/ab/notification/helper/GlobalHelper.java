package com.ab.notification.helper;

import com.ab.jwt.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class GlobalHelper {

    private final JwtUtil jwtUtil;

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalHelper.class);

    @Autowired
    public GlobalHelper(JwtUtil util) {
        jwtUtil = util;
    }

    /**
     * This method will be used to generate token from microservice name via Hs526 algo and key
     * hence securing internal microservice calls
     *
     * @param serviceName String
     * @return String
     */
    public Map<String, String> generateTokenViaSubjectForRestCall(String serviceName) {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaders.AUTHORIZATION, "Bearer " + jwtUtil.generateJWTToken(serviceName));
        return headers;
    }

}


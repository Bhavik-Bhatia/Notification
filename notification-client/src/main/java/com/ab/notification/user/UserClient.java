package com.ab.notification.user;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient(value = "user", url = "http://auth:8181/auth")
public interface UserClient {

    @GetMapping(value = "/user/get-users", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<String[]> getUserEmails(@RequestHeader Map<String, String> headers);

}

package com.ab.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public class EmailDTO {
    private Map<String, String> mailMap;
    private String mailTo;
    private boolean isProcessedByWriter;
}

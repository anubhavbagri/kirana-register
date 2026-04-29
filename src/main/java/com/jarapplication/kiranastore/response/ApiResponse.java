package com.jarapplication.kiranastore.response;

import lombok.Data;
import org.springframework.stereotype.Component;

/** Standard API Response */
@Component
@Data
public class ApiResponse {

    private boolean success = true;
    private Object data;
    private String status;
    private String error;
    private Object errorMessage;
    private String errorCode;
}

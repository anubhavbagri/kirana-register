package com.jarapplication.kiranastore.exception;

import com.jarapplication.kiranastore.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class ExceptionController {

    /**
     * userName doesn't exist Exception
     *
     * @param e
     * @return
     */
    @ExceptionHandler(value = UserNameExistsException.class)
    public Object UserNameExistsException(UserNameExistsException e) {
        log.error("User Name Doesn't exist {}", e.getMessage(), e);
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setSuccess(false);
        apiResponse.setStatus("error");
        apiResponse.setErrorMessage(e.getMessage());
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    /**
     * Illegal Argument Exception
     *
     * @param e
     * @return
     */
    @ExceptionHandler(value = IllegalArgumentException.class)
    public Object IllegalArgumentException(IllegalArgumentException e) {
        log.error("Illegal Argument Exception {}", e.getMessage(), e);
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setSuccess(false);
        apiResponse.setStatus("error");
        apiResponse.setErrorMessage(e.getMessage());
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    /**
     * Rate limit Exceeded Exception
     *
     * @param e
     * @return
     */
    @ExceptionHandler(value = RateLimitExceededException.class)
    public Object RateLimitExceededException(RateLimitExceededException e) {
        log.error("Rate Limit Exception {}", e.getMessage(), e);
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setSuccess(false);
        apiResponse.setStatus("error");
        apiResponse.setErrorMessage(e.getMessage());
        apiResponse.setStatus("429");
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    /**
     * @param e
     * @return
     */
    @ExceptionHandler(value = HttpMessageNotReadableException.class)
    public Object handleSpringRequestBodyException(HttpMessageNotReadableException e) {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setSuccess(false);
        apiResponse.setStatus("10002");
        apiResponse.setErrorMessage("Invalid Request Body");
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    /**
     * Missing param in url
     *
     * @param e
     * @return
     */
    @ExceptionHandler(value = MissingServletRequestParameterException.class)
    public Object handleSpringRequestParamException(MissingServletRequestParameterException e) {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setSuccess(false);
        apiResponse.setStatus("10002");
        apiResponse.setErrorMessage("Invalid Request Parameter");
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    /**
     * Exception for general
     *
     * @param e
     * @return
     */
    @ExceptionHandler(value = Exception.class)
    public Object handleException(Exception e) {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setSuccess(false);
        apiResponse.setStatus("error");
        apiResponse.setErrorMessage("Something went wrong. Please try again later!");
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }
}

package com.foodshare;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> validationError(
            MethodArgumentNotValidException exception
    ) {
        return ResponseEntity.badRequest().body(Map.of(
                "message",
                "Please check all fields. Use a valid email, " +
                        "10-digit mobile number and password of 8–64 characters."
        ));
    }
}
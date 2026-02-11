package com.lsb.listProjectBackend.config;

import com.lsb.listProjectBackend.domain.LsbException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalDefaultExceptionHandler {
    @ExceptionHandler(value = LsbException.class)
    public ResponseEntity<String> lsbErrorHandler(LsbException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}

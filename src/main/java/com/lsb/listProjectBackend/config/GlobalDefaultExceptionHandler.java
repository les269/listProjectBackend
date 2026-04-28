package com.lsb.listProjectBackend.config;

import com.lsb.listProjectBackend.domain.common.LsbException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class GlobalDefaultExceptionHandler {

    @ExceptionHandler(LsbException.class)
    public ResponseEntity<String> lsbErrorHandler(LsbException ex) {
        log.warn("LsbException [{}]: {}", ex.getStatus(), ex.getMessage(), ex);
        return ResponseEntity.status(ex.getStatus()).body(ex.getMessage());
    }
}

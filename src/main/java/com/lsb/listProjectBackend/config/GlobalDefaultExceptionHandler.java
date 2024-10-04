package com.lsb.listProjectBackend.config;

import com.lsb.listProjectBackend.domain.LsbException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalDefaultExceptionHandler {
    @ExceptionHandler(value = LsbException.class)
    public ResponseEntity<String> lsbErrorHandler(LsbException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}

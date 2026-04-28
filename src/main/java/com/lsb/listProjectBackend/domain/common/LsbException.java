package com.lsb.listProjectBackend.domain.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LsbException extends RuntimeException{
    private String message;
}

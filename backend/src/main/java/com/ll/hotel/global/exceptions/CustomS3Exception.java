package com.ll.hotel.global.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomS3Exception extends RuntimeException {
    private final HttpStatus resultCode;
    private final String msg;

    public CustomS3Exception(HttpStatus resultCode, String msg, Throwable cause) {
        super(resultCode + ":" + msg, cause);
        this.resultCode = resultCode;
        this.msg = msg;
    }
}
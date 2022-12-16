package com.leekimcho.guestservice.client.exception;

import com.leekimcho.problemservice.common.advice.exception.CustomException;
import org.springframework.http.HttpStatus;

public class FeignClientException extends CustomException {

    public FeignClientException(HttpStatus status, String message) {
        super(status, message);
    }
}

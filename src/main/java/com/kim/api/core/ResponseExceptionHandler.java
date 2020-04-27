package com.kim.api.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;

/**
 * 공통 에러 처리
 */
@Slf4j
@ControllerAdvice
public class ResponseExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(value = {Exception.class, Throwable.class})
    protected Object defaultHandler(Exception e, HttpServletRequest request) {
        return getResponseEntity("fail", e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, request.getRequestURL().toString(), e);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception e, Object body, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return getResponseEntity("fail", status.getReasonPhrase(), status, request.getContextPath(), e);
    }

    private ResponseEntity<Object> getResponseEntity(String result, String message, HttpStatus status, String from, Exception e) {
        log.error("Error occurred from {}", from, e);
        return new ResponseEntity<>(new CommonResponse(result, message), status);
    }
}

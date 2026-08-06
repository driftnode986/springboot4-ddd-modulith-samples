package com.example.shop.order.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * ドメインが投げた例外を HTTP の応答に対応づける。
 * この対応づけは HTTP 側の都合なので、ドメインには置かない。
 */
@RestControllerAdvice
public class OrderExceptionHandler {

    /** 入力そのものが成立していない。 */
    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail handleInvalidInput(IllegalArgumentException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    /** 形は整っているが、業務の規則で受け付けられない。 */
    @ExceptionHandler(IllegalStateException.class)
    ProblemDetail handleRuleViolation(IllegalStateException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage());
    }
}

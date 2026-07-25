package com.example.gyeonjutravel.global.apiPayload.handler;

import java.util.List;

import com.example.gyeonjutravel.global.apiPayload.exception.BaseErrorCode;
import com.example.gyeonjutravel.global.apiPayload.response.ErrorCode;
import com.example.gyeonjutravel.global.apiPayload.response.ErrorResponse;
import com.example.gyeonjutravel.global.apiPayload.response.FieldErrorResponse;
import com.example.gyeonjutravel.global.apiPayload.exception.GeneralException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(GeneralException.class)
    protected ResponseEntity<ErrorResponse> handleBusinessException(GeneralException generalException) {
        BaseErrorCode errorCode = generalException.getErrorCode();
        if (errorCode.getStatus().is5xxServerError()) {
            log.error("Business exception: code={}, message={}",
                    errorCode.getCode(), generalException.getMessage(), generalException);
        }
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.of(errorCode, generalException.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException exception
    ) {
        List<FieldErrorResponse> errors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::toFieldErrorResponse)
                .toList();

        return ResponseEntity
                .status(ErrorCode.INVALID_INPUT_VALUE.getStatus())
                .body(ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, errors));
    }

    @ExceptionHandler(BindException.class)
    protected ResponseEntity<ErrorResponse> handleBindException(BindException exception) {
        List<FieldErrorResponse> errors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::toFieldErrorResponse)
                .toList();

        return ResponseEntity
                .status(ErrorCode.INVALID_INPUT_VALUE.getStatus())
                .body(ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, errors));
    }

    @ExceptionHandler({
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class
    })
    protected ResponseEntity<ErrorResponse> handleInvalidRequestException(java.lang.Exception exception) {
        return ResponseEntity
                .status(ErrorCode.INVALID_INPUT_VALUE.getStatus())
                .body(ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, exception.getMessage()));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    protected ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException() {
        return ResponseEntity
                .status(ErrorCode.METHOD_NOT_ALLOWED.getStatus())
                .body(ErrorResponse.of(ErrorCode.METHOD_NOT_ALLOWED));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    protected ResponseEntity<ErrorResponse> handleHttpMediaTypeNotSupportedException() {
        return ResponseEntity
                .status(ErrorCode.UNSUPPORTED_MEDIA_TYPE.getStatus())
                .body(ErrorResponse.of(ErrorCode.UNSUPPORTED_MEDIA_TYPE));
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    protected ResponseEntity<ErrorResponse> handleNoHandlerFoundException() {
        return ResponseEntity
                .status(ErrorCode.NOT_FOUND.getStatus())
                .body(ErrorResponse.of(ErrorCode.NOT_FOUND));
    }

    @ExceptionHandler(java.lang.Exception.class)
    protected ResponseEntity<ErrorResponse> handleException(java.lang.Exception exception) {
        log.error("Unhandled exception", exception);
        return ResponseEntity
                .status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus())
                .body(ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR));
    }

    private FieldErrorResponse toFieldErrorResponse(FieldError fieldError) {
        String rejectedValue = fieldError.getRejectedValue() == null
                ? null
                : fieldError.getRejectedValue().toString();

        return new FieldErrorResponse(
                fieldError.getField(),
                rejectedValue,
                fieldError.getDefaultMessage()
        );
    }
}

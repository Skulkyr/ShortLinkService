package org.pogonin.shortlinkservice.api.handler;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.pogonin.shortlinkservice.core.exception.AliasAlreadyExistException;
import org.pogonin.shortlinkservice.core.exception.LinkAlreadyExistException;
import org.pogonin.shortlinkservice.core.exception.LinkGenerateException;
import org.pogonin.shortlinkservice.core.exception.LinkNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;


@RestControllerAdvice
@Log4j2
public class GlobalExceptionHandler {
    @Value("${spring.application.setting.base-host}")
    private String BASE_HOST;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(BindingResult result) {
        String message = result.getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(","));

        return ResponseEntity.badRequest().body(new ErrorResponse(400, message));
    }

    @ExceptionHandler(LinkNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleLinkNotFoundException(LinkNotFoundException e) {
        log.info(e.getMessage());
        return new ResponseEntity<>(
                new ErrorResponse(404, "Указанный адрес не существует"),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AliasAlreadyExistException.class)
    public ResponseEntity<ErrorResponse> handleAliasAlreadyExistException(AliasAlreadyExistException e) {
        log.info(e.getMessage());
        return new ResponseEntity<>(
                new ErrorResponse(400, "Выбранное имя уже занято, выберите другое"),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(LinkAlreadyExistException.class)
    public ResponseEntity<ErrorResponse> handleLinkAlreadyExistException(LinkAlreadyExistException e) {
        log.info(e.getMessage(), e.getShortLink());
        String fullLink = BASE_HOST + e.getShortLink();
        return new ResponseEntity<>(
                new ErrorResponse(400, "Короткая ссылка для данного ресурса уже существует: " + fullLink),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(LinkGenerateException.class)
    public ResponseEntity<ErrorResponse> handleLinkGenerateException(LinkGenerateException e) {
        log.info(e.getMessage());
        return new ResponseEntity<>(
                new ErrorResponse(400, "Невозможно сгенерировать короткую ссылку с указанными параметрами"),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleOtherException(Error e) {
        log.error("Unnamed error", e);
        return new ResponseEntity<>(
                new ErrorResponse(400, "Неизвестная ошибка"),
                HttpStatus.BAD_REQUEST);
    }

    @Getter
    public static class ErrorResponse {
        private final LocalDateTime timestamp;
        private final int statusCode;
        private final String error;

        private ErrorResponse(int statusCode, String error) {
            timestamp = LocalDateTime.now();
            this.statusCode = statusCode;
            this.error = error;
        }
    }
}

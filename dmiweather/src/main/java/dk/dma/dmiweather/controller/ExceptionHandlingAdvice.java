package dk.dma.dmiweather.controller;

import dk.dma.common.dto.JSonError;
import dk.dma.common.exception.ErrorMessage;
import dk.dma.common.exception.APIException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * @author Klaus Groenbaek
 *         Created 05/04/2017.
 */
@ControllerAdvice
@Slf4j
public class ExceptionHandlingAdvice {

    @ExceptionHandler(APIException.class)
    public ResponseEntity<JSonError> handleException(APIException e) {
        return new ResponseEntity<>(e.toJsonError(), HttpStatus.valueOf(e.getError().getHttpCode()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<JSonError> otherExceptions(Exception e) {
        log.error("Uncaught exception", e);
        ErrorMessage error = ErrorMessage.UNCAUGHT_EXCEPTION;
        return new ResponseEntity<>(error.toJsonError().setDetails(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

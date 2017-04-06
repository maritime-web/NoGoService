package dk.dma.dmiweather.controller;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.exc.PropertyBindingException;
import dk.dma.common.dto.JSonError;
import dk.dma.common.exception.ErrorMessage;
import dk.dma.common.exception.APIException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.stream.Collectors;

/**
 * Controller advice for a general way of handling common exceptions
 * @author Klaus Groenbaek
 *         Created 05/04/2017.
 */
@ControllerAdvice
@Slf4j
public class ExceptionHandlingAdvice extends ResponseEntityExceptionHandler {

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

    //Exceptions during request parsing
    @ExceptionHandler(HttpMessageConversionException.class)
    @ResponseBody
    public ResponseEntity<Object> handleBadInput(HttpMessageConversionException e) {
        log.info("Exception handled: ", e);
        ErrorMessage error = ErrorMessage.REQUEST_NOT_PARSED;
        JSonError jSonError = error.toJsonError().setDetails(extractDetails(e));
        return new ResponseEntity<>(jSonError, HttpStatus.valueOf(error.getHttpCode()));
    }

    @Override
    protected ResponseEntity<Object> handleBindException(BindException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        // bind exceptions are automatically logged by spring as a warning
        return getBindingError(ex);
    }

    private ResponseEntity<Object> getBindingError(BindingResult ex) {
        FieldError fieldError = ex.getFieldError();
        String details = fieldError.getField() + " " + fieldError.getDefaultMessage();
        ErrorMessage error = ErrorMessage.REQUEST_NOT_PARSED;
        JSonError jSonError = error.toJsonError().setDetails(details);
        return new ResponseEntity<>(jSonError, HttpStatus.valueOf(error.getHttpCode()));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException e, HttpHeaders headers, HttpStatus status, WebRequest request) {
        log.warn("bean validation failed " + e.getMessage());
        return getBindingError(e.getBindingResult());
    }

    private String extractDetails(Throwable t) {
        Throwable original = t;
        do  {
            if (t instanceof PropertyBindingException) {
                PropertyBindingException exception = (PropertyBindingException) t;
                return "Unknown property '" + exception.getPropertyName() + "', known properties are " +
                        exception.getKnownPropertyIds().stream()
                                .map(String::valueOf)
                                .collect(Collectors.joining(", "));
            }
            if (t instanceof JsonParseException) {
                JsonParseException exception = (JsonParseException) t;
                return exception.getMessage();
            }
            if (t instanceof HttpClientErrorException) {
                HttpClientErrorException exception = (HttpClientErrorException) original;
                String body = exception.getResponseBodyAsString();
                if (body.length() > 500) {
                    body = body.substring(0, 500) + "...";
                }
                return body;
            }
            t = t.getCause();
        } while (t != null);
        return original.getMessage();
    }


}

package com.sdase.mahlware.scanner.streaming.exception;

import com.sdase.mahlware.scanner.streaming.model.CheckResultEvent;
import com.sdase.mahlware.scanner.streaming.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(DocumentProcessingException.class)
    public ResponseEntity<CheckResultEvent> handleDocumentProcessingException(DocumentProcessingException ex) {
        LOGGER.error("Error processing document: {}", ex.getMessage(), ex);

        CheckResultEvent event = new CheckResultEvent()
                .setState(CheckResultEvent.StateEnum.ERROR)
                .setDetails(ex.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(event);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CheckResultEvent> handleException(Exception ex) {
        LOGGER.error("Unexpected error: {}", ex.getMessage(), ex);
        CheckResultEvent event = new CheckResultEvent()
                .setState(CheckResultEvent.StateEnum.ERROR)
                .setDetails("Unexpected error: " + ex.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(event);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CheckResultEvent> handleValidationExceptions(MethodArgumentNotValidException ex) {
        StringBuilder errorMessage = new StringBuilder();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                errorMessage.append(error.getField()).append(": ").append(error.getDefaultMessage()).append("; ")
        );
        LOGGER.error("Validation error: {}", ex.getMessage(), errorMessage);
        CheckResultEvent resultEvent = new CheckResultEvent(CheckResultEvent.StateEnum.IGNORED, Constants.IBAN_CHECKER_EVENT_NAME, errorMessage.toString());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resultEvent);
    }
}

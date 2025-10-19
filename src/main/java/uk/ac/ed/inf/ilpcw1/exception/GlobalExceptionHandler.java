package uk.ac.ed.inf.ilpcw1.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles our custom InvalidRequestException and returns a 400 Bad Request.
     * @param ex - the exception
     * @return - ResponseEntity with error details being invalid request
     */
    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<Map<String, String>> handleInvalidRequestException(InvalidRequestException ex) {
        logger.warn("Bad request: {}", ex.getMessage());
        Map<String, String> error = new HashMap<>();
        error.put("error", "Bad Request");
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handles malformed JSON requests.
     * @param ex - the exception
     * @return - ResponseEntity with error details being malformed JSON
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        logger.warn("Malformed JSON request: {}", ex.getMessage());
        Map<String, String> error = new HashMap<>();
        error.put("error", "Malformed JSON");
        error.put("message", "The JSON request body is malformed or invalid.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handles validation errors for request bodies annotated with @Valid.
     * @param ex - the exception
     * @return - ResponseEntity with error details being validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        logger.warn("Validation error: {}", ex.getMessage());

        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());

        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("error", "Validation Failed");
        errorBody.put("messages", errors);

        return new ResponseEntity<>(errorBody, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles our custom InvalidCoordinateException and returns a 400 Bad Request.
     * @param ex - the exception
     * @return - ResponseEntity with error details being invalid coordinate
     */
    @ExceptionHandler(InvalidCoordinateException.class)
    public ResponseEntity<Map<String, String>> handleInvalidCoordinateException(InvalidCoordinateException ex) {
        logger.warn("Invalid coordinate: {}", ex.getMessage());
        Map<String, String> error = new HashMap<>();
        error.put("error", "Bad Request");
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handles our custom InvalidAngleException and returns a 400 Bad Request.
     * @param ex - the exception
     * @return - ResponseEntity with error details being invalid angle
     */
    @ExceptionHandler(InvalidAngleException.class)
    public ResponseEntity<Map<String, String>> handleInvalidAngleException(InvalidAngleException ex) {
        logger.warn("Invalid angle: {}", ex.getMessage());
        Map<String, String> error = new HashMap<>();
        error.put("error", "Bad Request");
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }


    /**
     * Handles HttpRequestMethodNotSupportedException, which occurs when a valid
     * endpoint is called with an invalid HTTP method (e.g., GET on a POST endpoint).
     *
     * @param ex - the exception
     * @return - 400 Bad Request as per the coursework specification.
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, String>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        logger.warn("Invalid HTTP method: {}", ex.getMessage());

        Map<String, String> error = new HashMap<>();
        error.put("error", "Bad Request");

        // Create a helpful error message
        String message = String.format(
                "HTTP method '%s' is not supported for this endpoint. Supported method(s): %s",
                ex.getMethod(),
                String.join(", ", ex.getSupportedMethods())
        );
        error.put("message", message);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handles NoHandlerFoundException, which occurs when a request is made to an
     * unknown URL.
     *
     * @param ex - the exception
     * @return - 400 bad request request
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, String>> handleNoHandlerFound(NoResourceFoundException ex) {
        logger.warn("Invalid endpoint requested");
        Map<String, String> error = new HashMap<>();
        error.put("error", "Bad Request");
        error.put("message", "The requested endpoint does not exist.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }


    /**
     * Handles all other uncaught exceptions and returns a 500 Internal Server Error.
     * @param ex - the exception
     * @return - ResponseEntity with error details being internal server error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleAllExceptions(Exception ex) {
        logger.error("An unexpected error occurred: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An unexpected error occurred. Please try again later.");
    }
}

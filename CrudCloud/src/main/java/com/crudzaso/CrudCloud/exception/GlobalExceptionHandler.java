package com.crudzaso.CrudCloud.exception;

import com.crudzaso.CrudCloud.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for all REST endpoints.
 *
 * Handles exceptions and returns consistent ErrorResponse format.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles BusinessException and its subclasses.
     *
     * @param ex the business exception
     * @param request the HTTP request
     * @return error response with appropriate status code
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex,
            HttpServletRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(System.currentTimeMillis())
                .status(ex.getStatusCode())
                .error(HttpStatus.valueOf(ex.getStatusCode()).getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getServletPath())
                .build();

        log.warn("Business exception: {}", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.valueOf(ex.getStatusCode()));
    }

    /**
     * Handles AppException and returns descriptive error messages.
     *
     * @param ex the app exception
     * @param request the HTTP request
     * @return error response with appropriate status code
     */
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ErrorResponse> handleAppException(
            AppException ex,
            HttpServletRequest request) {

        HttpStatus status = determineStatusByErrorCode(ex.getCode());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(System.currentTimeMillis())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getServletPath())
                .build();

        log.warn("Application exception: {}", ex.getMessage());
        return new ResponseEntity<>(errorResponse, status);
    }

    /**
     * Handles validation errors from @Valid annotations.
     *
     * @param ex the validation exception
     * @param request the HTTP request
     * @return error response with field-level details
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            fieldErrors.put(error.getField(), error.getDefaultMessage())
        );

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(System.currentTimeMillis())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message("Validation failed")
                .path(request.getServletPath())
                .details(fieldErrors)
                .build();

        log.warn("Validation error: {}", fieldErrors);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Determines the appropriate HTTP status based on error code.
     *
     * @param errorCode the error code from AppException
     * @return the appropriate HttpStatus
     */
    private HttpStatus determineStatusByErrorCode(String errorCode) {
        return switch (errorCode) {
            case "EMAIL_ALREADY_EXISTS" -> HttpStatus.CONFLICT;
            case "USER_NOT_FOUND", "PLAN_NOT_FOUND" -> HttpStatus.NOT_FOUND;
            case "SUBSCRIPTION_CREATION_FAILED" -> HttpStatus.BAD_REQUEST;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

    /**
     * Handles DataIntegrityViolationException (database constraint violations).
     *
     * @param ex the data integrity violation exception
     * @param request the HTTP request
     * @return error response with user-friendly message
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex,
            HttpServletRequest request) {

        String message = "Error de integridad en la base de datos";
        String details = "Datos inválidos o duplicados";

        // Parse error message for more specific messages
        String exMessage = ex.getMessage();
        if (exMessage != null) {
            if (exMessage.contains("duplicate") || exMessage.contains("unique")) {
                message = "El email ya está registrado";
                details = "Este correo electrónico ya está en uso";
            } else if (exMessage.contains("not-null constraint")) {
                message = "Datos incompletos";
                details = "Todos los campos requeridos deben estar completos";
            } else if (exMessage.contains("foreign key")) {
                message = "Referencia a datos no válidos";
                details = "No se encontró el registro relacionado";
            }
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(System.currentTimeMillis())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(message)
                .details(Map.of("detalle", details))
                .path(request.getServletPath())
                .build();

        log.warn("Data integrity violation: {}", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles all other unexpected exceptions.
     *
     * @param ex the exception
     * @param request the HTTP request
     * @return generic error response
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(System.currentTimeMillis())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .message("Ocurrió un error inesperado")
                .details(Map.of(
                    "tipo", ex.getClass().getSimpleName(),
                    "descripcion", "Por favor contacte al administrador si el problema persiste"
                ))
                .path(request.getServletPath())
                .build();

        log.error("Unexpected exception: ", ex);
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}






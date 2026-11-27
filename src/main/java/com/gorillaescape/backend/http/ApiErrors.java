package com.gorillaescape.backend.http;

import jakarta.validation.ConstraintViolationException;
import java.net.URI;
import java.util.Map;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.transaction.TransactionException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApiErrors {
    private static final Logger LOG = LoggerFactory.getLogger(ApiErrors.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail invalidBody(MethodArgumentNotValidException error) {
        var result = problem(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "Revisa los campos de la solicitud.");
        Map<String, String> fields = new TreeMap<>();
        error.getBindingResult().getFieldErrors().forEach(e -> fields.put(e.getField(), "Valor inválido."));
        result.setProperty("violations", fields);
        return result;
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, HandlerMethodValidationException.class,
            ConstraintViolationException.class, MethodArgumentTypeMismatchException.class})
    ProblemDetail badRequest(Exception ignored) {
        return problem(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "JSON o parámetros inválidos.");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ProblemDetail conflict() {
        return problem(HttpStatus.CONFLICT, "DATA_CONFLICT", "La operación viola una restricción de datos.");
    }

    @ExceptionHandler({DataAccessException.class, TransactionException.class})
    ProblemDetail unavailable(Exception ignored) {
        return problem(HttpStatus.SERVICE_UNAVAILABLE, "STORAGE_UNAVAILABLE", "Almacenamiento no disponible; no reintentes POST automáticamente.");
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    ProblemDetail unsupportedMedia() {
        return problem(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "UNSUPPORTED_MEDIA_TYPE", "Usa Content-Type application/json.");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    org.springframework.http.ResponseEntity<ProblemDetail> unsupportedMethod(HttpRequestMethodNotSupportedException error) {
        var response = org.springframework.http.ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED);
        if (error.getSupportedHttpMethods() != null) response.allow(error.getSupportedHttpMethods().toArray(org.springframework.http.HttpMethod[]::new));
        return response.body(problem(HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED", "Método no permitido."));
    }

    @ExceptionHandler({org.springframework.web.servlet.resource.NoResourceFoundException.class,
            org.springframework.web.servlet.NoHandlerFoundException.class})
    ProblemDetail notFound() {
        return problem(HttpStatus.NOT_FOUND, "NOT_FOUND", "Recurso no encontrado.");
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail unexpected(Exception error) {
        LOG.error("event=request_failed requestId={} category={}", MDC.get("requestId"), error.getClass().getSimpleName());
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Error interno; conserva el identificador de solicitud.");
    }

    static ProblemDetail problem(HttpStatus status, String code, String detail) {
        var result = ProblemDetail.forStatusAndDetail(status, detail);
        result.setType(URI.create("urn:gorilla:problem:" + code.toLowerCase(java.util.Locale.ROOT).replace('_', '-')));
        result.setProperty("code", code);
        result.setProperty("requestId", MDC.get("requestId"));
        return result;
    }
}

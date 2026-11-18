package com.github.juanmarini.ms.casos.exceptions.handler;

import com.github.juanmarini.ms.casos.exceptions.CasoAtivoExistenteException;
import com.github.juanmarini.ms.casos.exceptions.CasoEncerradoException;
import com.github.juanmarini.ms.casos.exceptions.ResourceNotFoundException;
import com.github.juanmarini.ms.casos.exceptions.ServicoIndisponivelException;
import com.github.juanmarini.ms.casos.exceptions.SituacaoAlunoInvalidaException;
import com.github.juanmarini.ms.casos.exceptions.TransicaoStatusInvalidaException;
import com.github.juanmarini.ms.casos.exceptions.dto.CustomErrorDTO;
import com.github.juanmarini.ms.casos.exceptions.dto.ValidationErrorDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<CustomErrorDTO> handleResourceNotFound(ResourceNotFoundException e,
                                                                 HttpServletRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        CustomErrorDTO err = new CustomErrorDTO(Instant.now(), status.value(),
                e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CustomErrorDTO> methodArgumentNotValid(MethodArgumentNotValidException e,
                                                                 HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ValidationErrorDTO err = new ValidationErrorDTO(Instant.now(), status.value(),
                "Dados inválidos", request.getRequestURI());

        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            err.addError(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<CustomErrorDTO> handleHttpMessageNotReadable(HttpMessageNotReadableException e,
                                                                       HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        CustomErrorDTO err = new CustomErrorDTO(Instant.now(), status.value(),
                "Requisição inválida (JSON malformado ou corpo não interpretável).",
                request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<CustomErrorDTO> handleTypeMismatch(MethodArgumentTypeMismatchException e,
                                                             HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        CustomErrorDTO err = new CustomErrorDTO(Instant.now(), status.value(),
                "Requisição inválida (parâmetro com tipo/formato incorreto).",
                request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(CasoAtivoExistenteException.class)
    public ResponseEntity<CustomErrorDTO> handleCasoAtivoExistente(CasoAtivoExistenteException e,
            HttpServletRequest request) {
        HttpStatus status = HttpStatus.CONFLICT;
        CustomErrorDTO err = new CustomErrorDTO(Instant.now(), status.value(),
                e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(CasoEncerradoException.class)
    public ResponseEntity<CustomErrorDTO> handleCasoEncerrado(CasoEncerradoException e,
            HttpServletRequest request) {
        HttpStatus status = HttpStatus.CONFLICT;
        CustomErrorDTO err = new CustomErrorDTO(Instant.now(), status.value(),
                e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(TransicaoStatusInvalidaException.class)
    public ResponseEntity<CustomErrorDTO> handleTransicaoStatusInvalida(TransicaoStatusInvalidaException e,
            HttpServletRequest request) {
        HttpStatus status = HttpStatus.CONFLICT;
        CustomErrorDTO err = new CustomErrorDTO(Instant.now(), status.value(),
                e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(SituacaoAlunoInvalidaException.class)
    public ResponseEntity<CustomErrorDTO> handleSituacaoAlunoInvalida(SituacaoAlunoInvalidaException e,
            HttpServletRequest request) {
        HttpStatus status = HttpStatus.CONFLICT;
        CustomErrorDTO err = new CustomErrorDTO(Instant.now(), status.value(),
                e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(ServicoIndisponivelException.class)
    public ResponseEntity<CustomErrorDTO> handleServicoIndisponivel(ServicoIndisponivelException e,
            HttpServletRequest request) {
        HttpStatus status = HttpStatus.SERVICE_UNAVAILABLE;
        CustomErrorDTO err = new CustomErrorDTO(Instant.now(), status.value(),
                e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<CustomErrorDTO> handleNoResourceFound(NoResourceFoundException e,
                                                                HttpServletRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        CustomErrorDTO err = new CustomErrorDTO(Instant.now(), status.value(),
                "Recurso não encontrado.", request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<CustomErrorDTO> handleMethodNotSupported(HttpRequestMethodNotSupportedException e,
                                                                   HttpServletRequest request) {
        HttpStatus status = HttpStatus.METHOD_NOT_ALLOWED;
        CustomErrorDTO err = new CustomErrorDTO(Instant.now(), status.value(),
                e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CustomErrorDTO> handleGenericException(Exception e,
                                                                 HttpServletRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        CustomErrorDTO err = new CustomErrorDTO(Instant.now(), status.value(),
                e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }
}

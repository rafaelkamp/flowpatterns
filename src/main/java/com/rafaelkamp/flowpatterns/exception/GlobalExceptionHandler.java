package com.rafaelkamp.flowpatterns.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Tratamento centralizado de exceções. As respostas seguem o formato
 * Problem Details (RFC 9457), com detalhes de campo em erros de validação.
 */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ProblemDetail> recursoNaoEncontrado(RecursoNaoEncontradoException exception,
                                                               HttpServletRequest request) {
        return resposta(HttpStatus.NOT_FOUND, "Recurso não encontrado", exception.getMessage(), request);
    }

    @ExceptionHandler(ConflitoException.class)
    public ResponseEntity<ProblemDetail> conflito(ConflitoException exception, HttpServletRequest request) {
        return resposta(HttpStatus.CONFLICT, "Conflito de estado", exception.getMessage(), request);
    }

    @ExceptionHandler(AcessoNegadoException.class)
    public ResponseEntity<ProblemDetail> acessoNegado(AcessoNegadoException exception, HttpServletRequest request) {
        return resposta(HttpStatus.FORBIDDEN, "Acesso negado", exception.getMessage(), request);
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ProblemDetail> regraNegocio(RegraNegocioException exception, HttpServletRequest request) {
        return resposta(HttpStatus.UNPROCESSABLE_ENTITY, "Regra de negócio não atendida", exception.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> validacao(MethodArgumentNotValidException exception,
                                                    HttpServletRequest request) {
        List<Map<String, String>> erros = exception.getBindingResult().getFieldErrors().stream()
                .map(this::detalhar)
                .toList();
        ProblemDetail problem = resposta(HttpStatus.BAD_REQUEST, "Dados inválidos",
                "Um ou mais campos enviados são inválidos", request).getBody();
        problem.setProperty("erros", erros);
        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> violacaoParametro(ConstraintViolationException exception,
                                                           HttpServletRequest request) {
        List<Map<String, String>> erros = exception.getConstraintViolations().stream()
                .map(violation -> Map.of(
                        "campo", violation.getPropertyPath().toString(),
                        "mensagem", violation.getMessage()))
                .toList();
        ProblemDetail problem = resposta(HttpStatus.BAD_REQUEST, "Parâmetros inválidos",
                "Um ou mais parâmetros enviados são inválidos", request).getBody();
        problem.setProperty("erros", erros);
        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> corpoInvalido(HttpMessageNotReadableException exception,
                                                        HttpServletRequest request) {
        return resposta(HttpStatus.BAD_REQUEST, "Corpo da requisição inválido",
                "O corpo enviado não pôde ser processado. Verifique o formato JSON e os tipos dos campos.",
                request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetail> tipoInvalido(MethodArgumentTypeMismatchException exception,
                                                      HttpServletRequest request) {
        return resposta(HttpStatus.BAD_REQUEST, "Parâmetro inválido",
                "O valor '" + exception.getValue() + "' não é válido para o parâmetro '" + exception.getName() + "'",
                request);
    }

    @ExceptionHandler({OptimisticLockingFailureException.class, DataIntegrityViolationException.class})
    public ResponseEntity<ProblemDetail> concorrencia(RuntimeException exception, HttpServletRequest request) {
        return resposta(HttpStatus.CONFLICT, "Conflito de atualização",
                "O recurso foi alterado por outra requisição. Recarregue os dados e tente novamente.", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> erroInesperado(Exception exception, HttpServletRequest request) {
        LOG.error("Erro inesperado ao processar a requisição {}", request.getRequestURI(), exception);
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno",
                "Ocorreu um erro inesperado. Tente novamente mais tarde.", request);
    }

    private Map<String, String> detalhar(FieldError erro) {
        return Map.of(
                "campo", erro.getField(),
                "mensagem", erro.getDefaultMessage() == null ? "Valor inválido" : erro.getDefaultMessage());
    }

    private ResponseEntity<ProblemDetail> resposta(HttpStatus status, String titulo, String detalhe,
                                                   HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detalhe);
        problem.setTitle(titulo);
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("timestamp", Instant.now());
        return ResponseEntity.status(status).body(problem);
    }
}

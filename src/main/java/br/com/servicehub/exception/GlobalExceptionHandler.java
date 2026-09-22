package br.com.servicehub.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Tratamento centralizado de erros: qualquer exceção lançada nas camadas
 * Controller/Service chega aqui e vira uma resposta HTTP padronizada (ErrorResponse).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** 404 - recurso não encontrado (ex.: GET /api/servicos/99). */
    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErrorResponse> handleNaoEncontrado(RecursoNaoEncontradoException ex,
                                                            HttpServletRequest request) {
        return montar(HttpStatus.NOT_FOUND, ex.getMessage(), request, null);
    }

    /** 409 - violação de regra de negócio (ex.: nome duplicado). */
    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErrorResponse> handleRegraNegocio(RegraNegocioException ex,
                                                           HttpServletRequest request) {
        return montar(HttpStatus.CONFLICT, ex.getMessage(), request, null);
    }

    /** 409 - restrição do banco (ex.: UNIQUE violado por requisições simultâneas). */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleIntegridade(DataIntegrityViolationException ex,
                                                          HttpServletRequest request) {
        return montar(HttpStatus.CONFLICT,
                "Operação viola uma restrição do banco de dados (ex.: registro duplicado)", request, null);
    }

    /** 400 - falha no @Valid: devolve a lista de campos inválidos. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidacao(MethodArgumentNotValidException ex,
                                                        HttpServletRequest request) {
        List<ErrorResponse.CampoErro> campos = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new ErrorResponse.CampoErro(fe.getField(), fe.getDefaultMessage()))
                .toList();
        return montar(HttpStatus.BAD_REQUEST, "Dados inválidos. Verifique os campos informados.", request, campos);
    }

    /** 400 - JSON ausente ou malformado (ex.: preço "abc"). */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleJsonInvalido(HttpMessageNotReadableException ex,
                                                           HttpServletRequest request) {
        return montar(HttpStatus.BAD_REQUEST,
                "Corpo da requisição ausente ou com JSON malformado", request, null);
    }

    /** 400 - parâmetro de URL com tipo errado (ex.: GET /api/servicos/abc). */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTipoParametro(MethodArgumentTypeMismatchException ex,
                                                            HttpServletRequest request) {
        String mensagem = "Parâmetro '" + ex.getName() + "' com valor inválido: '" + ex.getValue() + "'";
        return montar(HttpStatus.BAD_REQUEST, mensagem, request, null);
    }

    /** 404 - rota inexistente. */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleRotaInexistente(NoResourceFoundException ex,
                                                              HttpServletRequest request) {
        return montar(HttpStatus.NOT_FOUND, "Rota não encontrada", request, null);
    }

    /** 405 - método HTTP não suportado na rota. */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMetodo(HttpRequestMethodNotSupportedException ex,
                                                     HttpServletRequest request) {
        return montar(HttpStatus.METHOD_NOT_ALLOWED,
                "Método " + ex.getMethod() + " não é suportado nesta rota", request, null);
    }

    /** 415 - Content-Type não suportado (ex.: enviar texto em vez de JSON). */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMediaType(HttpMediaTypeNotSupportedException ex,
                                                        HttpServletRequest request) {
        return montar(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "Content-Type não suportado. Use application/json", request, null);
    }

    /** 500 - qualquer erro inesperado (sem expor detalhes internos ao cliente). */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenerico(Exception ex, HttpServletRequest request) {
        log.error("Erro inesperado em {} {}", request.getMethod(), request.getRequestURI(), ex);
        return montar(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno inesperado", request, null);
    }

    private ResponseEntity<ErrorResponse> montar(HttpStatus status, String mensagem,
                                                 HttpServletRequest request,
                                                 List<ErrorResponse.CampoErro> campos) {
        ErrorResponse corpo = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                mensagem,
                request.getRequestURI(),
                campos
        );
        return ResponseEntity.status(status).body(corpo);
    }
}

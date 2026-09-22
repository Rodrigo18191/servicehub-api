package br.com.servicehub.exception;

/** Lançada quando uma regra de negócio é violada (ex.: nome duplicado). Vira HTTP 409. */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}

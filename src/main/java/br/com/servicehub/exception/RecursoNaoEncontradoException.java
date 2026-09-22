package br.com.servicehub.exception;

/** Lançada quando o recurso buscado não existe. Vira HTTP 404. */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}

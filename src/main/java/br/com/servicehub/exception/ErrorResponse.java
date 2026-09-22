package br.com.servicehub.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Formato padrão de todas as respostas de erro da API.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ErrorResponse", description = "Resposta padrão de erro")
public record ErrorResponse(

        @Schema(description = "Momento do erro", example = "2026-09-22T19:30:00")
        LocalDateTime timestamp,

        @Schema(description = "Código HTTP", example = "404")
        int status,

        @Schema(description = "Nome do status HTTP", example = "Not Found")
        String erro,

        @Schema(description = "Mensagem explicando o erro", example = "Serviço com id 99 não encontrado")
        String mensagem,

        @Schema(description = "Caminho da requisição", example = "/api/servicos/99")
        String path,

        @Schema(description = "Erros de validação por campo (apenas em respostas 400 de validação)")
        List<CampoErro> campos
) {

    @Schema(name = "CampoErro", description = "Erro de validação de um campo")
    public record CampoErro(
            @Schema(description = "Nome do campo", example = "preco")
            String campo,

            @Schema(description = "Mensagem de validação", example = "O preço deve ser maior que zero")
            String mensagem
    ) {
    }
}

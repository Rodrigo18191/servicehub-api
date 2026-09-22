package br.com.servicehub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * Corpo da requisição PATCH: altera apenas o status (ativo/inativo) do serviço.
 */
@Schema(name = "ServicoStatusRequest", description = "Novo status do serviço")
public record ServicoStatusRequest(

        @Schema(description = "true = ativo, false = inativo", example = "false",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "O campo 'ativo' é obrigatório")
        Boolean ativo
) {
}

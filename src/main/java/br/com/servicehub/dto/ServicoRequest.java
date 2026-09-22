package br.com.servicehub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Dados de entrada para cadastrar (POST) ou atualizar (PUT) um serviço.
 * Não contém id nem datas: esses campos são controlados pela API.
 */
@Schema(name = "ServicoRequest", description = "Dados para cadastrar ou atualizar um serviço")
public record ServicoRequest(

        @Schema(description = "Nome do serviço (único)", example = "Instalação de chuveiro elétrico",
                minLength = 3, maxLength = 100, requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "O nome é obrigatório")
        @Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres")
        String nome,

        @Schema(description = "Descrição detalhada do serviço",
                example = "Instalação de chuveiro com troca de resistência e verificação do disjuntor",
                maxLength = 500)
        @Size(max = 500, message = "A descrição deve ter no máximo 500 caracteres")
        String descricao,

        @Schema(description = "Preço em reais", example = "150.00", minimum = "0.01",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "O preço é obrigatório")
        @Positive(message = "O preço deve ser maior que zero")
        @Digits(integer = 8, fraction = 2, message = "O preço deve ter no máximo 8 dígitos inteiros e 2 decimais")
        BigDecimal preco,

        @Schema(description = "Categoria do serviço", example = "Elétrica", maxLength = 50,
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "A categoria é obrigatória")
        @Size(max = 50, message = "A categoria deve ter no máximo 50 caracteres")
        String categoria,

        @Schema(description = "Duração estimada em minutos", example = "60", minimum = "1")
        @Positive(message = "A duração deve ser maior que zero")
        Integer duracaoMinutos
) {
}

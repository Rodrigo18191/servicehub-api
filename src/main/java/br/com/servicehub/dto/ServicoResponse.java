package br.com.servicehub.dto;

import br.com.servicehub.model.Servico;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Dados de saída devolvidos pela API. Desacopla o contrato da API da entidade JPA.
 */
@Schema(name = "ServicoResponse", description = "Serviço retornado pela API")
public record ServicoResponse(

        @Schema(description = "Identificador do serviço", example = "1")
        Long id,

        @Schema(description = "Nome do serviço", example = "Instalação de chuveiro elétrico")
        String nome,

        @Schema(description = "Descrição detalhada",
                example = "Instalação de chuveiro com troca de resistência e verificação do disjuntor")
        String descricao,

        @Schema(description = "Preço em reais", example = "150.00")
        BigDecimal preco,

        @Schema(description = "Categoria", example = "Elétrica")
        String categoria,

        @Schema(description = "Duração estimada em minutos", example = "60")
        Integer duracaoMinutos,

        @Schema(description = "Indica se o serviço está disponível", example = "true")
        Boolean ativo,

        @Schema(description = "Data/hora de criação", example = "2026-09-22T19:30:00")
        LocalDateTime criadoEm,

        @Schema(description = "Data/hora da última atualização", example = "2026-09-22T19:30:00")
        LocalDateTime atualizadoEm
) {

    /** Converte a entidade em DTO de resposta. */
    public static ServicoResponse from(Servico servico) {
        return new ServicoResponse(
                servico.getId(),
                servico.getNome(),
                servico.getDescricao(),
                servico.getPreco(),
                servico.getCategoria(),
                servico.getDuracaoMinutos(),
                servico.getAtivo(),
                servico.getCriadoEm(),
                servico.getAtualizadoEm()
        );
    }
}

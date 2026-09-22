package br.com.servicehub.controller;

import br.com.servicehub.dto.ServicoRequest;
import br.com.servicehub.dto.ServicoResponse;
import br.com.servicehub.dto.ServicoStatusRequest;
import br.com.servicehub.exception.ErrorResponse;
import br.com.servicehub.service.ServicoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Camada de entrada HTTP do recurso Serviço.
 * Responsabilidades: receber a requisição, validar o formato (@Valid),
 * delegar ao Service e devolver o status HTTP adequado. Sem regra de negócio aqui.
 */
@RestController
@RequestMapping(value = "/api/servicos", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Serviços", description = "Cadastro, consulta, atualização e remoção de serviços do catálogo")
public class ServicoController {

    private static final String EXEMPLO_REQUEST = """
            {
              "nome": "Instalação de chuveiro elétrico",
              "descricao": "Instalação de chuveiro com troca de resistência e verificação do disjuntor",
              "preco": 150.00,
              "categoria": "Elétrica",
              "duracaoMinutos": 60
            }
            """;

    private static final String EXEMPLO_ERRO_400 = """
            {
              "timestamp": "2026-09-22T19:30:00",
              "status": 400,
              "erro": "Bad Request",
              "mensagem": "Dados inválidos. Verifique os campos informados.",
              "path": "/api/servicos",
              "campos": [
                { "campo": "preco", "mensagem": "O preço deve ser maior que zero" },
                { "campo": "nome", "mensagem": "O nome é obrigatório" }
              ]
            }
            """;

    private static final String EXEMPLO_ERRO_404 = """
            {
              "timestamp": "2026-09-22T19:30:00",
              "status": 404,
              "erro": "Not Found",
              "mensagem": "Serviço com id 99 não encontrado",
              "path": "/api/servicos/99"
            }
            """;

    private static final String EXEMPLO_ERRO_409 = """
            {
              "timestamp": "2026-09-22T19:30:00",
              "status": 409,
              "erro": "Conflict",
              "mensagem": "Já existe um serviço cadastrado com o nome 'Instalação de chuveiro elétrico'",
              "path": "/api/servicos"
            }
            """;

    private final ServicoService service;

    public ServicoController(ServicoService service) {
        this.service = service;
    }

    // ============================ POST ============================
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Cadastrar serviço",
            description = "Cria um novo serviço no catálogo. O nome deve ser único. "
                    + "Retorna 201 com o serviço criado e o header Location apontando para o novo recurso.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Serviço criado com sucesso",
                    content = @Content(schema = @Schema(implementation = ServicoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou JSON malformado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = EXEMPLO_ERRO_400))),
            @ApiResponse(responseCode = "409", description = "Já existe um serviço com esse nome",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = EXEMPLO_ERRO_409)))
    })
    public ResponseEntity<ServicoResponse> criar(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados do serviço a ser cadastrado", required = true,
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ServicoRequest.class),
                            examples = @ExampleObject(name = "Serviço de elétrica", value = EXEMPLO_REQUEST)))
            @Valid @RequestBody ServicoRequest request) {

        ServicoResponse criado = service.criar(request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(criado.id())
                .toUri();

        return ResponseEntity.created(location).body(criado);
    }

    // ============================ GET (lista) ============================
    @GetMapping
    @Operation(
            summary = "Listar serviços",
            description = "Retorna todos os serviços ordenados por id. "
                    + "Opcionalmente filtra pela categoria (sem diferenciar maiúsculas/minúsculas).")
    @ApiResponse(responseCode = "200", description = "Lista de serviços (pode ser vazia)",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ServicoResponse.class))))
    public ResponseEntity<List<ServicoResponse>> listar(
            @Parameter(description = "Filtra pela categoria", example = "Elétrica")
            @RequestParam(name = "categoria", required = false) String categoria) {

        return ResponseEntity.ok(service.listar(categoria));
    }

    // ============================ GET (por id) ============================
    @GetMapping("/{id}")
    @Operation(summary = "Buscar serviço por id", description = "Retorna os dados de um serviço específico.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Serviço encontrado",
                    content = @Content(schema = @Schema(implementation = ServicoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Id em formato inválido (ex.: texto)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Serviço não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = EXEMPLO_ERRO_404)))
    })
    public ResponseEntity<ServicoResponse> buscarPorId(
            @Parameter(description = "Id do serviço", example = "1") @PathVariable("id") Long id) {

        return ResponseEntity.ok(service.buscarPorId(id));
    }

    // ============================ PUT ============================
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Atualizar serviço",
            description = "Substitui todos os dados editáveis do serviço (nome, descrição, preço, categoria e duração). "
                    + "Para ativar/desativar use o PATCH /{id}/status.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Serviço atualizado",
                    content = @Content(schema = @Schema(implementation = ServicoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = EXEMPLO_ERRO_400))),
            @ApiResponse(responseCode = "404", description = "Serviço não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = EXEMPLO_ERRO_404))),
            @ApiResponse(responseCode = "409", description = "Outro serviço já usa esse nome",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ServicoResponse> atualizar(
            @Parameter(description = "Id do serviço", example = "1") @PathVariable("id") Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Novos dados do serviço", required = true,
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ServicoRequest.class),
                            examples = @ExampleObject(name = "Atualização de preço", value = """
                                    {
                                      "nome": "Instalação de chuveiro elétrico",
                                      "descricao": "Instalação completa, com troca de resistência",
                                      "preco": 180.00,
                                      "categoria": "Elétrica",
                                      "duracaoMinutos": 90
                                    }
                                    """)))
            @Valid @RequestBody ServicoRequest request) {

        return ResponseEntity.ok(service.atualizar(id, request));
    }

    // ============================ PATCH ============================
    @PatchMapping(value = "/{id}/status", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Ativar ou desativar serviço",
            description = "Atualização parcial: altera somente o campo 'ativo', sem mexer nos demais dados.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status alterado",
                    content = @Content(schema = @Schema(implementation = ServicoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Campo 'ativo' ausente",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Serviço não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = EXEMPLO_ERRO_404)))
    })
    public ResponseEntity<ServicoResponse> alterarStatus(
            @Parameter(description = "Id do serviço", example = "1") @PathVariable("id") Long id,
            @Valid @RequestBody ServicoStatusRequest request) {

        return ResponseEntity.ok(service.alterarStatus(id, request.ativo()));
    }

    // ============================ DELETE ============================
    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir serviço", description = "Remove o serviço definitivamente. Retorna 204 sem corpo.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Serviço excluído", content = @Content),
            @ApiResponse(responseCode = "404", description = "Serviço não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = EXEMPLO_ERRO_404)))
    })
    public ResponseEntity<Void> excluir(
            @Parameter(description = "Id do serviço", example = "1") @PathVariable("id") Long id) {

        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}

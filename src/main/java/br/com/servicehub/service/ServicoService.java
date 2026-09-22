package br.com.servicehub.service;

import br.com.servicehub.dto.ServicoRequest;
import br.com.servicehub.dto.ServicoResponse;
import br.com.servicehub.exception.RecursoNaoEncontradoException;
import br.com.servicehub.exception.RegraNegocioException;
import br.com.servicehub.model.Servico;
import br.com.servicehub.repository.ServicoRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Camada de regras de negócio do recurso Serviço.
 * O Controller só delega para cá; toda decisão (existe? nome duplicado? como converter?) fica aqui.
 */
@Service
public class ServicoService {

    private final ServicoRepository repository;

    // Injeção de dependência pelo construtor (recomendada pelo Spring)
    public ServicoService(ServicoRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ServicoResponse criar(ServicoRequest request) {
        validarNomeUnico(request.nome(), null);

        Servico servico = new Servico();
        aplicarDados(servico, request);

        return ServicoResponse.from(repository.save(servico));
    }

    @Transactional(readOnly = true)
    public List<ServicoResponse> listar(String categoria) {
        List<Servico> servicos = (categoria == null || categoria.isBlank())
                ? repository.findAll(Sort.by(Sort.Direction.ASC, "id"))
                : repository.findByCategoriaIgnoreCaseOrderByIdAsc(categoria.trim());

        return servicos.stream()
                .map(ServicoResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ServicoResponse buscarPorId(Long id) {
        return ServicoResponse.from(buscarEntidade(id));
    }

    @Transactional
    public ServicoResponse atualizar(Long id, ServicoRequest request) {
        Servico servico = buscarEntidade(id);
        validarNomeUnico(request.nome(), id);

        aplicarDados(servico, request);

        // saveAndFlush força o UPDATE agora, para a resposta já sair com "atualizadoEm" novo
        return ServicoResponse.from(repository.saveAndFlush(servico));
    }

    @Transactional
    public ServicoResponse alterarStatus(Long id, Boolean ativo) {
        Servico servico = buscarEntidade(id);
        servico.setAtivo(ativo);
        return ServicoResponse.from(repository.saveAndFlush(servico));
    }

    @Transactional
    public void excluir(Long id) {
        Servico servico = buscarEntidade(id);
        repository.delete(servico);
    }

    // ----------------- métodos auxiliares -----------------

    private Servico buscarEntidade(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Serviço com id " + id + " não encontrado"));
    }

    /**
     * Regra de negócio: não podem existir dois serviços com o mesmo nome.
     * No update, ignora o próprio registro (idAtual).
     */
    private void validarNomeUnico(String nome, Long idAtual) {
        String nomeNormalizado = nome.trim();
        boolean jaExiste = (idAtual == null)
                ? repository.existsByNomeIgnoreCase(nomeNormalizado)
                : repository.existsByNomeIgnoreCaseAndIdNot(nomeNormalizado, idAtual);

        if (jaExiste) {
            throw new RegraNegocioException("Já existe um serviço cadastrado com o nome '" + nomeNormalizado + "'");
        }
    }

    /** Copia os dados do DTO para a entidade, normalizando espaços. */
    private void aplicarDados(Servico servico, ServicoRequest request) {
        servico.setNome(request.nome().trim());
        servico.setDescricao(request.descricao() == null || request.descricao().isBlank()
                ? null
                : request.descricao().trim());
        servico.setPreco(request.preco());
        servico.setCategoria(request.categoria().trim());
        servico.setDuracaoMinutos(request.duracaoMinutos());
    }
}

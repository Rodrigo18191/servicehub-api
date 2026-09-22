package br.com.servicehub.service;

import br.com.servicehub.dto.ServicoRequest;
import br.com.servicehub.dto.ServicoResponse;
import br.com.servicehub.exception.RecursoNaoEncontradoException;
import br.com.servicehub.exception.RegraNegocioException;
import br.com.servicehub.model.Servico;
import br.com.servicehub.repository.ServicoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testes unitários da camada de regras de negócio.
 * O repositório é simulado (mock), então não precisa de banco para rodar.
 */
@ExtendWith(MockitoExtension.class)
class ServicoServiceTest {

    @Mock
    private ServicoRepository repository;

    @InjectMocks
    private ServicoService service;

    private ServicoRequest requestValido() {
        return new ServicoRequest("  Instalação de chuveiro  ", "Troca de resistência",
                new BigDecimal("150.00"), "Elétrica", 60);
    }

    private Servico servicoSalvo(Long id) {
        Servico s = new Servico();
        s.setId(id);
        s.setNome("Instalação de chuveiro");
        s.setDescricao("Troca de resistência");
        s.setPreco(new BigDecimal("150.00"));
        s.setCategoria("Elétrica");
        s.setDuracaoMinutos(60);
        s.setAtivo(true);
        return s;
    }

    @Test
    @DisplayName("criar: salva o serviço quando o nome ainda não existe")
    void criarDeveSalvarQuandoNomeNaoExiste() {
        when(repository.existsByNomeIgnoreCase("Instalação de chuveiro")).thenReturn(false);
        when(repository.save(any(Servico.class))).thenAnswer(inv -> {
            Servico s = inv.getArgument(0);
            s.setId(1L);
            return s;
        });

        ServicoResponse resposta = service.criar(requestValido());

        assertEquals(Long.valueOf(1L), resposta.id());
        assertEquals("Instalação de chuveiro", resposta.nome()); // espaços removidos
        verify(repository).save(any(Servico.class));
    }

    @Test
    @DisplayName("criar: lança RegraNegocioException (409) quando o nome já existe")
    void criarDeveFalharQuandoNomeDuplicado() {
        when(repository.existsByNomeIgnoreCase("Instalação de chuveiro")).thenReturn(true);

        assertThrows(RegraNegocioException.class, () -> service.criar(requestValido()));
        verify(repository, never()).save(any(Servico.class));
    }

    @Test
    @DisplayName("buscarPorId: lança RecursoNaoEncontradoException (404) quando o id não existe")
    void buscarPorIdDeveFalharQuandoNaoExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException ex = assertThrows(RecursoNaoEncontradoException.class,
                () -> service.buscarPorId(99L));
        assertEquals("Serviço com id 99 não encontrado", ex.getMessage());
    }

    @Test
    @DisplayName("alterarStatus: desativa o serviço")
    void alterarStatusDeveDesativar() {
        Servico existente = servicoSalvo(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.saveAndFlush(existente)).thenReturn(existente);

        ServicoResponse resposta = service.alterarStatus(1L, false);

        assertFalse(resposta.ativo());
    }

    @Test
    @DisplayName("excluir: remove o serviço existente")
    void excluirDeveRemover() {
        Servico existente = servicoSalvo(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(existente));

        service.excluir(1L);

        verify(repository).delete(existente);
    }

    @Test
    @DisplayName("excluir: lança 404 e não remove nada quando o id não existe")
    void excluirDeveFalharQuandoNaoExiste() {
        when(repository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> service.excluir(5L));
        verify(repository, never()).delete(any(Servico.class));
    }
}

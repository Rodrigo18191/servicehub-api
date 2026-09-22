package br.com.servicehub.repository;

import br.com.servicehub.model.Servico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Camada de acesso a dados. O Spring Data JPA gera a implementação em tempo de execução:
 * save, findById, findAll, delete... vêm prontos do JpaRepository.
 * Os métodos abaixo são "query methods": o SQL é derivado do nome do método.
 */
@Repository
public interface ServicoRepository extends JpaRepository<Servico, Long> {

    List<Servico> findByCategoriaIgnoreCaseOrderByIdAsc(String categoria);

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);
}

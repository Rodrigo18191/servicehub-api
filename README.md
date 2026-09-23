# ServiceHub API — Checkpoint

API REST para gerenciar o catálogo de serviços da plataforma **ServiceHub**, desenvolvida com Spring Boot.

**Aluno:** Rodrigo Henrique Toniolli Oliveira
**Disciplina:** Desenvolvimento Backend com Java (APIs e Web Services)

---

## Tecnologias

| Tecnologia | Uso |
|---|---|
| Java 17+ | Linguagem |
| Spring Boot 4.1.1 | Framework (gerado pelo Spring Initializr) |
| Spring Web | Endpoints REST |
| Spring Data JPA / Hibernate | Persistência (ORM) |
| PostgreSQL | Banco de dados |
| Bean Validation | Validação dos dados de entrada (respostas 400) |
| SpringDoc OpenAPI 3.1.1 | Documentação Swagger UI |
| JUnit 5 + Mockito | Testes unitários da camada Service |

## Arquitetura

Arquitetura em camadas, cada uma com uma responsabilidade única:

```
Cliente (Swagger / Postman)
      │  HTTP + JSON
      ▼
Controller   → recebe a requisição, valida o formato (@Valid), devolve o status HTTP
      │  DTO
      ▼
Service      → regras de negócio (existe? nome duplicado?), conversão DTO ↔ entidade
      │  Entidade
      ▼
Repository   → acesso ao banco (Spring Data JPA)
      ▼
PostgreSQL

GlobalExceptionHandler (@RestControllerAdvice) → converte exceções em respostas 400/404/409 padronizadas
```

```
src/main/java/br/com/servicehub
├── ServicehubApiApplication.java
├── config/       OpenApiConfig            (metadados do Swagger)
├── controller/   ServicoController        (endpoints REST)
├── service/      ServicoService           (regras de negócio)
├── repository/   ServicoRepository        (JpaRepository)
├── model/        Servico                  (entidade JPA → tabela "servicos")
├── dto/          ServicoRequest, ServicoResponse, ServicoStatusRequest
└── exception/    GlobalExceptionHandler, ErrorResponse,
                  RecursoNaoEncontradoException, RegraNegocioException
```

## Endpoints — recurso `Serviço`

| Método | Rota | Descrição | Sucesso | Erros |
|---|---|---|---|---|
| POST | `/api/servicos` | Cadastrar serviço | **201** + `Location` | 400, 409 |
| GET | `/api/servicos` | Listar (filtro opcional `?categoria=`) | **200** | — |
| GET | `/api/servicos/{id}` | Buscar por id | **200** | 400, 404 |
| PUT | `/api/servicos/{id}` | Atualizar dados | **200** | 400, 404, 409 |
| PATCH | `/api/servicos/{id}/status` | Ativar / desativar | **200** | 400, 404 |
| DELETE | `/api/servicos/{id}` | Excluir | **204** | 404 |

### Exemplo de payload (POST / PUT)

```json
{
  "nome": "Instalação de chuveiro elétrico",
  "descricao": "Instalação de chuveiro com troca de resistência e verificação do disjuntor",
  "preco": 150.00,
  "categoria": "Elétrica",
  "duracaoMinutos": 60
}
```

### Formato padrão de erro

```json
{
  "timestamp": "2026-09-22T19:30:00",
  "status": 400,
  "erro": "Bad Request",
  "mensagem": "Dados inválidos. Verifique os campos informados.",
  "path": "/api/servicos",
  "campos": [ { "campo": "preco", "mensagem": "O preço deve ser maior que zero" } ]
}
```

## Como rodar

### 1. Pré-requisitos
- JDK 17 ou superior
- Docker (para o PostgreSQL)
- **Não precisa instalar o Maven:** o projeto inclui o Maven Wrapper (`mvnw` / `mvnw.cmd`)

### 2. Subir o banco (PostgreSQL via Docker)
```bash
docker compose up -d
```
Isso cria o banco `servicehub` (usuário `postgres`, senha `postgres`) na porta 5432.
As tabelas são criadas automaticamente pelo Hibernate (`ddl-auto=update`).
Para parar: `docker compose down` (os dados ficam no volume `servicehub-data`).

> **Sem Docker?** Use um PostgreSQL local, rode `CREATE DATABASE servicehub;` e, se a sua
> senha não for `postgres`, defina a variável de ambiente `DB_PASSWORD`
> (PowerShell: `$env:DB_PASSWORD="minha_senha"` · Linux/Mac: `export DB_PASSWORD=minha_senha`).

### 3. Executar
Pela IDE: rode a classe `ServicehubApiApplication`.

Pelo terminal:
```bash
# Windows
mvnw.cmd spring-boot:run
# Linux / Mac
./mvnw spring-boot:run
```

### 4. Acessar
- **Swagger UI:** http://localhost:8080/swagger-ui.html
- OpenAPI (JSON): http://localhost:8080/v3/api-docs

O arquivo `api-requests.http` tem todas as requisições (sucesso e erro) prontas para executar no
IntelliJ ou no VS Code (extensão REST Client).

### Testes
```bash
mvnw.cmd test      # Windows
./mvnw test        # Linux / Mac
```
Os testes unitários do `ServicoService` usam Mockito e **não precisam de banco**.

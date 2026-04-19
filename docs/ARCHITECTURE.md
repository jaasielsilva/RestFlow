# ERP Corporativo SaaS — Manual de Arquitetura e Desenvolvimento

> **Documento vivo.** Atualize este arquivo sempre que um novo padrão for estabelecido.

---

## 1. Project Overview & Tech Stack

**ERP Corporativo** é um SaaS B2B multi-tenant para empresas de serviços. Cada cliente (tenant) assina um plano e acessa os módulos contratados via portal dedicado.

| Camada | Tecnologia |
|---|---|
| Backend | Java 21, Spring Boot 3.5 |
| Persistência | Spring Data JPA, Hibernate, MySQL 8 |
| Web | Thymeleaf, Bootstrap 5.3, Font Awesome 6 |
| Segurança | Spring Security |
| Build | Maven |
| Testes | JUnit 5, Mockito, jqwik (PBT) |

---

## 2. Layer Structure & Dependency Rules

### Grafo de dependências

```
controller/web  ──►  usecase  ──►  repository
controller/api  ──►  usecase  ──►  service (shared)
                              ──►  repository
service (shared) ──►  repository
model   (sem dependências externas)
dto     (sem dependências externas)
```

### Regras obrigatórias

1. **Controllers** não contêm lógica de negócio — apenas delegam ao `UseCase` e montam o modelo de view.
2. **UseCases** (`@Component` com sufixo `UseCase`) orquestram toda a lógica de negócio. São a única camada que pode combinar múltiplos repositórios e serviços.
3. **Services** (`@Service`) são reservados para utilitários de infraestrutura compartilhados: `AuditService`, `ModuleVisualMapper`, `PlatformSettingService`. Não contêm lógica de negócio de domínio.
4. **Nenhuma camada inferior** (model, repository, dto) referencia camada superior.
5. **Repositories** estendem `JpaRepository` e `JpaSpecificationExecutor` quando precisam de filtros dinâmicos.

### Estrutura de pacotes

```
com.jaasielsilva.erpcorporativo.app
├── config/           — configurações Spring (Security, DataInitializer, etc.)
├── controller/
│   ├── api/v1/admin/       — REST endpoints do SUPER_ADMIN
│   ├── api/v1/tenantadmin/ — REST endpoints do ADMIN do tenant
│   └── web/
│       ├── admin/          — páginas Thymeleaf do painel admin
│       ├── auth/           — login, logout, recuperação de senha
│       ├── home/           — dashboard do SUPER_ADMIN
│       ├── os/             — módulo de Ordens de Serviço (tenant)
│       └── tenantadmin/    — portal do tenant
├── dto/
│   ├── api/admin/          — records de request/response da API admin
│   └── web/
│       ├── admin/          — forms e viewmodels do painel admin
│       ├── home/           — viewmodels do dashboard
│       ├── os/             — viewmodels do módulo OS
│       └── tenantadmin/    — viewmodels do portal tenant
├── exception/        — exceções de domínio (AppException, ConflictException, etc.)
├── mapper/           — conversores entity ↔ DTO
├── model/            — entidades JPA e enums
├── repository/       — interfaces Spring Data JPA
├── security/         — AppUserDetails, SecurityPrincipalUtils
├── service/
│   ├── api/v1/admin/ — serviços da API admin
│   ├── shared/       — utilitários compartilhados
│   └── web/          — serviços web (delegam para UseCases)
├── tenant/           — TenantContext, TenantHibernateFilterAspect, TenantRequestFilter
└── usecase/
    ├── api/          — UseCases da API
    └── web/          — UseCases das páginas web
```

---

## 3. Naming Conventions

### Sufixos obrigatórios por tipo

| Tipo | Sufixo | Exemplo |
|---|---|---|
| Controller web | `WebController` | `AdminContractWebController` |
| Controller API | `ApiController` | `ContractAdminApiController` |
| UseCase | `UseCase` | `ContractUseCase` |
| Service (infra) | `WebService` ou `Service` | `AdminTenantWebService` |
| Repository | `Repository` | `ContractRepository` |
| Form (entrada web) | `Form` | `ContractForm` |
| ViewModel (saída web) | `ViewModel` | `ContractViewModel` |
| Response (saída API) | `Response` | `TenantResponse` |
| Request (entrada API) | `Request` | `TenantRequest` |
| Mapper | `Mapper` | `TenantAdminApiMapper` |
| Specifications | `Specifications` | `ContractSpecifications` |

### Entidades JPA

- Nome da classe: **português, singular, PascalCase** — `Tenant`, `Usuario`, `OrdemServico`, `Contract`
- Nome da tabela: **plural, snake_case** — `tenants`, `usuarios`, `ordens_servico`, `contracts`
- Foreign keys: `fk_{tabela}_{referencia}` — `fk_contracts_tenant`
- Unique constraints: `uk_{tabela}_{campo}` — `uk_tenants_slug`

### DTOs

- `ViewModel` → sempre um Java **record** imutável
- `Form` → sempre uma **classe mutável** com `@Data`, `@NoArgsConstructor` e anotações Bean Validation
- `Response` (API) → sempre um Java **record** imutável

### Constantes de módulo

Códigos de módulo são strings em **MAIÚSCULAS** definidas no `DataInitializer`:

```java
ensureModule("PEDIDOS", "Pedidos", "/app/pedidos", "Gestão de ordens de serviço");
ensureModule("FINANCEIRO", "Financeiro", "/app/modulos/financeiro", "Gestão financeira");
```

---

## 4. Multi-Tenancy & Security Patterns

### Isolamento de dados em duas camadas

**Camada 1 — Filtro Hibernate automático:**

Entidades de negócio do tenant recebem `@FilterDef` e `@Filter`:

```java
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantId", type = Long.class))
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
public class OrdemServico { ... }
```

O `TenantHibernateFilterAspect` ativa o filtro automaticamente em todas as chamadas de repositório quando `TenantContext.getTenantId()` não é nulo.

**Camada 2 — Verificação explícita de ownership no UseCase:**

```java
private OrdemServico findOwned(Long tenantId, Long id) {
    return osRepository.findById(id)
        .filter(os -> os.getTenant().getId().equals(tenantId))
        .orElseThrow(() -> new ResourceNotFoundException("OS não encontrada."));
}
```

**Entidades do SUPER_ADMIN** (ex.: `Contract`) **não recebem** `@FilterDef`/`@Filter` — são gerenciadas globalmente.

### Prefixos de rota e proteção

| Prefixo | Role | Configuração |
|---|---|---|
| `/admin/**` | `SUPER_ADMIN` | `SecurityConfig` |
| `/api/v1/admin/**` | `SUPER_ADMIN` | `SecurityConfig` |
| `/app/**` | `ADMIN`, `USER` | `SecurityConfig` |
| `/api/v1/tenantadmin/**` | `ADMIN`, `USER` | `SecurityConfig` |

### SUPER_ADMIN e TenantContext

O `SUPER_ADMIN` opera sem `tenantFilter` ativo. Seu tenant é o tenant da plataforma (`slug = "platform"`) e não deve ser confundido com tenants de clientes.

---

## 5. Thymeleaf Page Patterns

### Estrutura de diretórios de templates

```
src/main/resources/templates/
├── admin/          — painel do SUPER_ADMIN
│   ├── contratos/  — módulo de contratos
│   ├── tenants/    — gestão de tenants
│   └── ...
├── tenant/         — portal do cliente
│   ├── os/         — módulo de ordens de serviço
│   └── ...
├── auth/           — login, logout, recuperação de senha
├── error/          — páginas de erro (403, 404, 500)
├── fragments/      — sidebar, topbar, toast, table-actions
└── home/           — dashboard do SUPER_ADMIN
```

### Layout base obrigatório

Toda página deve incluir via `th:replace`:

```html
<aside th:replace="~{fragments/sidebar :: sidebar('activeKey')}"></aside>
<div th:replace="~{fragments/topbar :: topbar('Título', 'Subtítulo', false)}"></div>
```

### Atributos de modelo obrigatórios

Todo `WebController` deve popular:

```java
model.addAttribute("activeMenu", "contratos");   // chave do item ativo na sidebar
model.addAttribute("pageTitle", "Contratos");    // título da página
model.addAttribute("pageSubtitle", "...");       // subtítulo
```

### Feedback ao usuário (toast)

Sempre via `RedirectAttributes` após POST:

```java
redirectAttributes.addFlashAttribute("toastSuccess", "Operação realizada.");
redirectAttributes.addFlashAttribute("toastError", "Erro ao processar.");
```

Renderizado no template com:

```html
<div data-app-toast data-toast-type="success"
     th:attr="data-toast-message=${toastSuccess}"
     th:if="${toastSuccess != null}"></div>
```

### Paginação

Parâmetros padrão: `page` (base 0), `size` (padrão 20). O `ViewModel` de lista encapsula `currentPage`, `totalPages`, `totalElements` e métodos `hasNext()` / `hasPrev()`.

### Formulários compartilhados

Criação e edição compartilham o mesmo `form.html`, diferenciados por `isEdit` (boolean):

```html
<form th:action="${isEdit} ? @{/admin/contratos/{id}(id=${contractId})} : @{/admin/contratos}"
      th:object="${form}" method="post">
```

---

## 6. New Module Creation Checklist

Para criar um novo módulo de negócio no portal do tenant:

### Passo 1 — Registrar no DataInitializer

```java
ensureModule("MEUMODULO", "Meu Módulo", "/app/meumodulo", "Descrição do módulo");
```

### Passo 2 — Entidade JPA com isolamento de tenant

```java
@Entity
@Table(name = "meus_registros")
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantId", type = Long.class))
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class MeuRegistro {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", foreignKey = @ForeignKey(name = "fk_meus_registros_tenant"))
    Tenant tenant;
    // ... campos de negócio
    @CreationTimestamp LocalDateTime createdAt;
    @UpdateTimestamp LocalDateTime updatedAt;
}
```

### Passo 3 — Repository com Specifications

```java
public interface MeuRegistroRepository
        extends JpaRepository<MeuRegistro, Long>, JpaSpecificationExecutor<MeuRegistro> { }

public final class MeuRegistroSpecifications {
    public static Specification<MeuRegistro> byTenant(Long tenantId) { ... }
    public static Specification<MeuRegistro> byFiltro(String valor) { ... }
}
```

### Passo 4 — UseCase

```java
@Component @RequiredArgsConstructor
public class MeuModuloUseCase {
    // Toda lógica de negócio aqui
    // Verificar ownership: entity.getTenant().getId().equals(tenantId)
    // Gerar número sequencial: "REG-" + String.format("%04d", findMaxSequence() + 1)
    // Chamar auditService.log(...) em todas as mutações
}
```

### Passo 5 — Controller com proteção de módulo

```java
@GetMapping
public String index(Authentication authentication, ...) {
    tenantPortalWebService.requireEnabledModule(authentication, "MEUMODULO"); // obrigatório
    // ...
}
```

### Passo 6 — DTOs

- `MeuRegistroForm` — classe mutável com Bean Validation
- `MeuRegistroViewModel` — record imutável
- `MeuRegistroListViewModel` — record com `items`, `currentPage`, `totalPages`, KPIs

### Passo 7 — Templates

```
templates/tenant/meumodulo/
├── index.html   — listagem com KPIs e filtros
├── form.html    — criação/edição (isEdit flag)
└── detail.html  — detalhe com ações
```

### Passo 8 — ModuleVisualMapper

```java
case "meumodulo" -> "fa-solid fa-icon-aqui";   // iconClass
case "meumodulo" -> "module-tone-blue";         // toneClass
```

---

## 7. REST API Standards

### Prefixos

- `/api/v1/admin/**` — operações do SUPER_ADMIN
- `/api/v1/tenantadmin/**` — operações do ADMIN do tenant

### Padrão de resposta de sucesso

```json
{ "status": "success", "data": { ... } }
```

Usando `ApiResponse.success(data)`.

### Padrão de resposta de erro

```json
{ "status": 409, "error": "CONFLICT", "message": "Já existe um contrato ativo." }
```

### Listagem paginada

```json
{
  "content": [...],
  "page": 0,
  "totalPages": 5,
  "totalElements": 98
}
```

### Validação

- `@Valid` é **obrigatório** em todos os `@RequestBody` de endpoints de escrita.
- Erros de validação retornam HTTP 422 com detalhes por campo (tratado pelo `GlobalExceptionHandler`).

### Delegação

`ApiController` não contém lógica — delega integralmente ao `UseCase`:

```java
@PostMapping
@ResponseStatus(HttpStatus.CREATED)
public ApiResponse<ContractViewModel> create(Authentication auth, @Valid @RequestBody ContractForm form) {
    return ApiResponse.success(contractUseCase.create(form, auth.getName()));
}
```

---

## 8. Audit Logging Pattern

Toda ação relevante do SUPER_ADMIN deve registrar um `AuditLog`:

```java
auditService.log(
    AuditAction.CONTRACT_CRIADO,
    "Contrato criado para tenant 'Empresa X' — plano: Pro",
    "Contract",      // entidade afetada
    savedId,         // ID do registro
    executadoPor,    // email do executor
    tenant           // tenant de contexto (null para ações globais)
);
```

`AuditService` é `@Async` com `REQUIRES_NEW` — falhas de auditoria não fazem rollback da transação principal.

Adicione novas constantes ao enum `AuditAction` para cada novo tipo de operação auditável.

---

## 9. Testing Strategy

### Testes unitários (example-based)

- Foco em casos específicos e edge cases
- Mockar repositórios com Mockito
- Verificar regras de negócio, chamadas de auditoria, transformações de DTO

### Testes de propriedade (jqwik PBT)

Para lógica com invariantes universais (persistência, ordenação, agregações, flags calculadas):

```java
@Property(tries = 100)
void contractFlagsAreConsistent(@ForAll LocalDate dataTermino) {
    // Property N: <descrição da propriedade>
    // Feature: saas-contracts-and-architecture
    // Validates: Requirements X.Y
}
```

Adicionar ao `pom.xml`:
```xml
<dependency>
    <groupId>net.jqwik</groupId>
    <artifactId>jqwik</artifactId>
    <version>1.8.4</version>
    <scope>test</scope>
</dependency>
```

### Testes de integração

- Usar H2 in-memory para testes de repositório
- `@SpringBootTest` + MockMvc para testes de controller
- Verificar HTTP 403 para acessos não autorizados

### Convenção de nomenclatura de testes

```
{Classe}Test.java          — testes unitários
{Classe}PropertyTest.java  — testes de propriedade (jqwik)
{Classe}IntegrationTest.java — testes de integração
```

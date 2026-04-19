# Implementation Plan: SaaS Contracts & Architecture Manual

## Overview

Implementação incremental do módulo de Contratos & Assinaturas e do Manual de Arquitetura para o ERP Corporativo SaaS B2B multi-tenant (Java 21 / Spring Boot 3.5 / Thymeleaf / MySQL). A ordem garante que cada etapa compile e seja integrável à anterior: fundação (enums, entidades, converter, repositórios) → lógica de negócio (UseCase) → camada web (controller, templates) → integrações (sidebar, dashboard, auditoria) → documento de arquitetura → testes.

---

## Tasks

- [x] 1. Enums e Converter — fundação de tipos
  - [x] 1.1 Criar enum `ContractStatus` com valores `ATIVO`, `SUSPENSO`, `ENCERRADO`, `AGUARDANDO_ASSINATURA`
    - Criar arquivo `src/main/java/com/jaasielsilva/erpcorporativo/app/model/ContractStatus.java`
    - _Requirements: 6.1_

  - [x] 1.2 Criar enum `PaymentStatus` com valores `PAGO`, `PENDENTE`, `ATRASADO`, `CANCELADO`
    - Criar arquivo `src/main/java/com/jaasielsilva/erpcorporativo/app/model/PaymentStatus.java`
    - _Requirements: 8.1_

  - [x] 1.3 Criar `YearMonthConverter` — `AttributeConverter<YearMonth, String>` com `@Converter(autoApply = true)`
    - Serializa para `"YYYY-MM"` (VARCHAR 7); deserializa com `YearMonth.parse(s)`
    - Criar arquivo `src/main/java/com/jaasielsilva/erpcorporativo/app/model/YearMonthConverter.java`
    - _Requirements: 8.1_

  - [x] 1.4 Estender enum `AuditAction` com as novas constantes de contratos e pagamentos
    - Adicionar: `CONTRACT_CRIADO`, `CONTRACT_ATUALIZADO`, `CONTRACT_ENCERRADO`, `CONTRACT_SUSPENSO`, `CONTRACT_REMOVIDO`, `PAGAMENTO_REGISTRADO`, `PAGAMENTO_ATUALIZADO`
    - _Requirements: 6.4, 10.4, 11.4_

- [x] 2. Entidades JPA — `Contract` e `PaymentRecord`
  - [x] 2.1 Criar entidade `Contract`
    - Campos: `id`, `@ManyToOne Tenant tenant`, `@ManyToOne SubscriptionPlan subscriptionPlan`, `BigDecimal valorMensal`, `LocalDate dataInicio`, `LocalDate dataTermino` (nullable), `@Enumerated(EnumType.STRING) ContractStatus status`, `String observacoes` (TEXT), `LocalDateTime createdAt`, `LocalDateTime updatedAt`
    - Sem `@FilterDef`/`@Filter` — entidade gerenciada exclusivamente pelo SUPER_ADMIN
    - Criar arquivo `src/main/java/com/jaasielsilva/erpcorporativo/app/model/Contract.java`
    - _Requirements: 6.1_

  - [x] 2.2 Criar entidade `PaymentRecord`
    - Campos: `id`, `@ManyToOne Contract contract`, `YearMonth mesReferencia` (usa `YearMonthConverter`), `BigDecimal valorPago`, `LocalDate dataPagamento` (nullable), `@Enumerated(EnumType.STRING) PaymentStatus status`, `String observacoes`, `LocalDateTime createdAt`, `LocalDateTime updatedAt`
    - Criar arquivo `src/main/java/com/jaasielsilva/erpcorporativo/app/model/PaymentRecord.java`
    - _Requirements: 8.1_

- [x] 3. Repositórios
  - [x] 3.1 Criar `ContractRepository` estendendo `JpaRepository<Contract, Long>` e `JpaSpecificationExecutor<Contract>`
    - Métodos: `existsByTenantIdAndStatus`, `countByStatus`, `countByStatusAndDataTerminoBefore`, `sumValorMensalByStatus` (JPQL `@Query`), `findByStatusAndDataTerminoBetween`, `findFirstByTenantIdAndStatus`
    - Criar arquivo `src/main/java/com/jaasielsilva/erpcorporativo/app/repository/contract/ContractRepository.java`
    - _Requirements: 6.3, 9.1_

  - [x] 3.2 Criar `ContractSpecifications` com predicados estáticos para filtros dinâmicos
    - Métodos: `byTenantNome(String nome)` (LIKE case-insensitive), `byStatus(ContractStatus)`, `byPlanoCodigo(String)`
    - Criar arquivo `src/main/java/com/jaasielsilva/erpcorporativo/app/repository/contract/ContractSpecifications.java`
    - _Requirements: 7.2_

  - [x] 3.3 Criar `PaymentRecordRepository` estendendo `JpaRepository<PaymentRecord, Long>`
    - Métodos: `findByContractIdOrderByMesReferenciaDesc`, `findFirstByContractIdOrderByMesReferenciaDesc`, `countByStatus`
    - Criar arquivo `src/main/java/com/jaasielsilva/erpcorporativo/app/repository/contract/PaymentRecordRepository.java`
    - _Requirements: 8.1, 8.2_

- [x] 4. Checkpoint — compilação da fundação
  - Garantir que todos os arquivos criados até aqui compilem sem erros. Verificar imports, tipos e anotações JPA. Perguntar ao usuário se houver dúvidas.

- [x] 5. DTOs — Forms e ViewModels
  - [x] 5.1 Criar `ContractForm` (classe mutável com Bean Validation)
    - Campos: `@NotNull Long tenantId`, `@NotNull Long subscriptionPlanId`, `@NotNull @DecimalMin("0.00") BigDecimal valorMensal`, `@NotNull LocalDate dataInicio`, `LocalDate dataTermino` (nullable), `@NotNull ContractStatus status`, `@Size(max=2000) String observacoes`
    - Criar arquivo `src/main/java/com/jaasielsilva/erpcorporativo/app/dto/web/admin/contract/ContractForm.java`
    - _Requirements: 7.4, 7.5, 7.6_

  - [x] 5.2 Criar `PaymentRecordForm` (classe mutável com Bean Validation)
    - Campos: `@NotNull YearMonth mesReferencia`, `@NotNull @DecimalMin("0.00") BigDecimal valorPago`, `LocalDate dataPagamento` (nullable), `@NotNull PaymentStatus status`, `@Size(max=1000) String observacoes`
    - Criar arquivo `src/main/java/com/jaasielsilva/erpcorporativo/app/dto/web/admin/contract/PaymentRecordForm.java`
    - _Requirements: 8.3, 8.4_

  - [x] 5.3 Criar `PaymentRecordViewModel` (Java record imutável)
    - Campos: `id`, `mesReferencia`, `valorPago`, `dataPagamento`, `status`, `boolean isAtrasado`, `observacoes`, `createdAt`
    - _Requirements: 8.5_

  - [x] 5.4 Criar `ContractViewModel` (Java record imutável)
    - Campos: todos os campos do contrato + `tenantNome`, `subscriptionPlanNome`, `boolean isVencido`, `boolean isVencendoEm30Dias`, `PaymentStatus ultimoPagamentoStatus` (nullable), `List<PaymentRecordViewModel> pagamentos`, `createdAt`, `updatedAt`
    - Lógica de `isVencido`: `dataTermino != null && dataTermino.isBefore(today) && status == ATIVO`
    - Lógica de `isVencendoEm30Dias`: `dataTermino != null && dataTermino` entre hoje e hoje+30 dias && `status == ATIVO`
    - Criar arquivo `src/main/java/com/jaasielsilva/erpcorporativo/app/dto/web/admin/contract/ContractViewModel.java`
    - _Requirements: 6.5, 9.2, 8.6_

  - [x] 5.5 Criar `ContractListViewModel` (Java record imutável)
    - Campos: `List<ContractViewModel> items`, `currentPage`, `totalPages`, `totalElements`, `totalAtivos`, `totalVencidos`, `totalAtrasados`, `BigDecimal mrrTotal`
    - Métodos utilitários: `hasNext()`, `hasPrev()`
    - Criar arquivo `src/main/java/com/jaasielsilva/erpcorporativo/app/dto/web/admin/contract/ContractListViewModel.java`
    - _Requirements: 9.1_

  - [x] 5.6 Criar `ContractKpiViewModel` e `ContractAlertViewModel` (Java records)
    - `ContractKpiViewModel`: `totalAtivos`, `totalVencidos`, `totalAtrasados`, `BigDecimal mrrTotal`
    - `ContractAlertViewModel`: `contractId`, `tenantNome`, `dataTermino`, `boolean isVencendo`, `boolean isPagamentoAtrasado`
    - Criar em `src/main/java/com/jaasielsilva/erpcorporativo/app/dto/web/admin/contract/`
    - _Requirements: 9.1, 9.3_

  - [x] 5.7 Estender `AdminDashboardViewModel` com os novos campos de contratos
    - Adicionar campos: `ContractKpiViewModel contractKpis`, `List<ContractAlertViewModel> contractAlerts`
    - _Requirements: 9.1, 9.3_

- [x] 6. UseCase — `ContractUseCase`
  - [x] 6.1 Criar `ContractUseCase` com `@Component` e injeção de dependências via `@RequiredArgsConstructor`
    - Dependências: `ContractRepository`, `PaymentRecordRepository`, `TenantRepository`, `SubscriptionPlanRepository`, `AuditService`
    - Criar arquivo `src/main/java/com/jaasielsilva/erpcorporativo/app/usecase/web/admin/ContractUseCase.java`
    - _Requirements: 6.1_

  - [x] 6.2 Implementar método `list(tenantNome, status, planoCodigo, page, size)` em `ContractUseCase`
    - Compor `Specification` usando `ContractSpecifications`; buscar página via `contractRepository.findAll(spec, pageable)`
    - Calcular KPIs: `totalAtivos`, `totalVencidos`, `totalAtrasados`, `mrrTotal` usando métodos do repositório
    - Retornar `ContractListViewModel` populado
    - _Requirements: 7.1, 7.2, 9.1_

  - [x] 6.3 Implementar método `getById(Long id)` em `ContractUseCase`
    - Buscar contrato; lançar `ResourceNotFoundException` se não encontrado
    - Buscar pagamentos via `paymentRecordRepository.findByContractIdOrderByMesReferenciaDesc`
    - Montar e retornar `ContractViewModel` com lista de pagamentos
    - _Requirements: 7.3_

  - [x] 6.4 Implementar método `create(ContractForm form, String executadoPor)` em `ContractUseCase`
    - Regra 1: verificar `existsByTenantIdAndStatus(tenantId, ATIVO)` — lançar `AppException` se já existir contrato ativo
    - Regra 2: após salvar, atualizar `tenant.subscriptionPlan` com o plano selecionado
    - Regra 3: chamar `auditService.log(CONTRACT_CRIADO, ...)`
    - _Requirements: 6.2, 6.3, 6.4, 11.4_

  - [x] 6.5 Implementar método `update(Long id, ContractForm form, String executadoPor)` em `ContractUseCase`
    - Atualizar campos do contrato; chamar `auditService.log(CONTRACT_ATUALIZADO, ...)`
    - _Requirements: 7.5, 11.4_

  - [x] 6.6 Implementar método `updateStatus(Long id, ContractStatus novoStatus, String executadoPor)` em `ContractUseCase`
    - Se novo status for `ENCERRADO` ou `SUSPENSO`, chamar `auditService.log(CONTRACT_ENCERRADO / CONTRACT_SUSPENSO, ...)`
    - _Requirements: 6.4, 10.4_

  - [x] 6.7 Implementar método `delete(Long id, String executadoPor)` em `ContractUseCase`
    - Chamar `auditService.log(CONTRACT_REMOVIDO, ...)`; deletar contrato
    - _Requirements: 11.4_

  - [x] 6.8 Implementar métodos `addPayment` e `updatePayment` em `ContractUseCase`
    - `addPayment`: criar `PaymentRecord`, chamar `auditService.log(PAGAMENTO_REGISTRADO, ...)`
    - `updatePayment`: atualizar status e observações, chamar `auditService.log(PAGAMENTO_ATUALIZADO, ...)`
    - _Requirements: 8.3, 8.4_

- [x] 7. Estender `BuildAdminDashboardUseCase` com KPIs e alertas de contratos
  - Consultar `ContractRepository` e `PaymentRecordRepository` para popular `contractKpis` e `contractAlerts`
  - Alertas: contratos com `dataTermino` nos próximos 30 dias + contratos com pagamentos `ATRASADO`
  - _Requirements: 9.1, 9.3_

- [x] 8. Checkpoint — lógica de negócio
  - Garantir que `ContractUseCase` e `BuildAdminDashboardUseCase` compilem sem erros. Verificar regras de negócio e chamadas de auditoria. Perguntar ao usuário se houver dúvidas.

- [x] 9. Controller Web — `AdminContractWebController`
  - [x] 9.1 Criar `AdminContractWebController` com mapeamentos GET para listagem, formulário e detalhe
    - `GET /admin/contratos` → `index` (lista + KPIs); `GET /admin/contratos/new` → `form` (isEdit=false); `GET /admin/contratos/{id}` → `detail`; `GET /admin/contratos/{id}/edit` → `form` (isEdit=true)
    - Popular `pageTitle`, `pageSubtitle`, `activeMenu = "contratos"` em todos os métodos
    - Criar arquivo `src/main/java/com/jaasielsilva/erpcorporativo/app/controller/web/admin/AdminContractWebController.java`
    - _Requirements: 7.1, 7.3, 7.4, 7.5_

  - [x] 9.2 Implementar endpoints POST de criação e edição em `AdminContractWebController`
    - `POST /admin/contratos` → create; `POST /admin/contratos/{id}` → update
    - Usar `@Valid`, `BindingResult`; em caso de erro reexibir formulário com mensagens; em sucesso redirecionar com `toastSuccess`
    - Capturar `AppException` e adicionar como erro global via `bindingResult.reject(...)`
    - _Requirements: 7.4, 7.5, 7.6_

  - [x] 9.3 Implementar endpoints POST de status, delete e pagamentos em `AdminContractWebController`
    - `POST /admin/contratos/{id}/status` → updateStatus; `POST /admin/contratos/{id}/delete` → delete
    - `POST /admin/contratos/{id}/payments` → addPayment; `POST /admin/contratos/{id}/payments/{pid}` → updatePayment
    - Usar `RedirectAttributes` para `toastSuccess`/`toastError`
    - _Requirements: 7.7, 8.3, 8.4_

- [x] 10. Controller API — `ContractAdminApiController`
  - Criar `ContractAdminApiController` com `@RestController` em `/api/v1/admin/contratos`
  - Endpoints: `GET /` (list), `GET /{id}` (detail), `POST /` (create), `PUT /{id}` (update), `DELETE /{id}` (delete)
  - Usar `@Valid` em todos os `@RequestBody`; delegar integralmente ao `ContractUseCase`
  - Criar arquivo `src/main/java/com/jaasielsilva/erpcorporativo/app/controller/api/v1/admin/ContractAdminApiController.java`
  - _Requirements: 11.1, 11.2_

- [x] 11. Templates Thymeleaf — módulo de contratos
  - [x] 11.1 Criar template `templates/admin/contratos/index.html`
    - Cards de KPI (total ativos, vencidos, atrasados, MRR); filtros (tenant, status, plano); tabela com colunas: tenant, plano, valor mensal, data início, data término, status, último pagamento, ações
    - Badge de alerta para contratos vencendo em 30 dias (Requirement 9.2); badge "Vencido" para contratos vencidos (Requirement 6.5)
    - Paginação com parâmetros `page` e `size`
    - _Requirements: 7.1, 7.2, 9.2_

  - [x] 11.2 Criar template `templates/admin/contratos/form.html`
    - Formulário compartilhado para criação e edição, diferenciado pelo atributo `isEdit`
    - Campos: tenant (select), plano (select), valor mensal, data início, data término, status, observações
    - Exibir mensagens de erro de validação por campo
    - _Requirements: 7.4, 7.5, 7.6_

  - [x] 11.3 Criar template `templates/admin/contratos/detail.html`
    - Exibir todos os campos do contrato; seção de histórico de pagamentos ordenada por mês decrescente
    - Botão de encerrar contrato com modal de confirmação; formulário inline para adicionar pagamento
    - Badge vermelho para pagamentos com status `ATRASADO`
    - _Requirements: 7.3, 7.7, 8.2, 8.3, 8.5_

- [x] 12. Integração — sidebar e dashboard
  - [x] 12.1 Adicionar entrada "Contratos" na sidebar do admin (`fragments/sidebar.html`)
    - Link `th:href="@{/admin/contratos}"` com ícone `fa-solid fa-file-contract`
    - Aplicar classe `active` quando `active == 'contratos'`
    - _Requirements: 7.1_

  - [x] 12.2 Atualizar template do dashboard admin para exibir KPIs e alertas de contratos
    - Seção de KPIs: total ativos, vencidos, atrasados, MRR
    - Seção de alertas: lista de contratos vencendo em 30 dias e com pagamentos atrasados, com link para detalhe
    - _Requirements: 9.1, 9.3, 9.4_

  - [x] 12.3 Integrar status de contrato na listagem e detalhe de tenants
    - Na listagem de tenants: adicionar coluna/badge com status do contrato ativo (`ATIVO`, `SUSPENSO`, `ENCERRADO`, `SEM CONTRATO`)
    - Na página de detalhe do tenant: exibir contrato ativo (plano, valor, data término, status) e link para criar novo contrato pré-preenchido
    - _Requirements: 10.1, 10.2, 10.3_

- [x] 13. Checkpoint — integração completa
  - Garantir que todas as rotas respondam corretamente, sidebar exiba o link de contratos, dashboard exiba KPIs e alertas, e detalhe de tenant exiba status contratual. Perguntar ao usuário se houver dúvidas.

- [x] 14. Manual de Arquitetura — `docs/ARCHITECTURE.md`
  - Criar arquivo `docs/ARCHITECTURE.md` com as 9 seções definidas no design
  - Seção 1: Project Overview & Tech Stack
  - Seção 2: Layer Structure & Dependency Rules (grafo de dependências, regras de camadas, uso de `@Component UseCase` vs `@Service`) — _Requirements: 1.1, 1.2, 1.3, 1.4_
  - Seção 3: Naming Conventions (sufixos obrigatórios, pacotes, entidades em português, `ViewModel` como record, `Form` como classe mutável, constantes de módulo) — _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5_
  - Seção 4: Multi-Tenancy & Security Patterns (isolamento em duas camadas, SUPER_ADMIN sem filtro, prefixos de rota, auditoria) — _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5_
  - Seção 5: Thymeleaf Page Patterns (estrutura de diretórios, layout base, atributos obrigatórios, toast feedback, paginação, form compartilhado) — _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6_
  - Seção 6: New Module Creation Checklist (checklist completo, `requireEnabledModule`, número sequencial, `ModuleVisualMapper`, `Specifications`) — _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5_
  - Seção 7: REST API Standards (prefixos, padrão de erro, listagem paginada, `@Valid` + HTTP 422, delegação ao UseCase) — _Requirements: 12.1, 12.2, 12.3, 12.4, 12.5_
  - Seção 8: Audit Logging Pattern — _Requirements: 5.5_
  - Seção 9: Testing Strategy — _Requirements: 1.1_

- [x] 15. Testes unitários e de integração
  - [x] 15.1 Criar `YearMonthConverterTest` — verificar conversão `"2024-03"` ↔ `YearMonth.of(2024, 3)` e casos nulos
    - _Requirements: 8.1_

  - [ ]* 15.2 Criar `ContractSpecificationsTest` — verificar cada predicado com entradas concretas
    - _Requirements: 7.2_

  - [ ]* 15.3 Criar `ContractUseCaseTest` — happy paths de create/update/delete com repositórios mockados
    - Verificar regra de contrato ativo único, atualização do plano do tenant, chamadas de auditoria
    - _Requirements: 6.2, 6.3, 6.4_

  - [ ]* 15.4 Criar `ContractViewModelBuilderTest` — verificar `isVencido` e `isVencendoEm30Dias` para casos limite
    - Casos: `dataTermino` nulo, exatamente hoje, exatamente hoje+30 dias, ontem, amanhã
    - _Requirements: 6.5, 9.2_

  - [ ]* 15.5 Criar `AdminContractWebControllerTest` (MockMvc) — verificar roteamento, atributos de modelo e comportamento de redirect
    - _Requirements: 7.1, 7.4, 7.5_

- [x] 16. Testes baseados em propriedades (jqwik)
  - [x] 16.1 Adicionar dependência `jqwik 1.8.4` ao `pom.xml` com escopo `test`
    - _Requirements: (infraestrutura de testes)_

  - [x]* 16.2 Criar `ContractViewModelPropertyTest` — Properties 5, 6, 10
    - **Property 5: Contract date-based display flags are consistent**
    - **Validates: Requirements 6.5, 9.2**
    - **Property 6: Tenant without contracts resolves to SEM_CONTRATO label**
    - **Validates: Requirements 6.6, 10.2**
    - **Property 10: Payment record display properties are correct**
    - **Validates: Requirements 8.5, 8.6**

  - [ ]* 16.3 Criar `ContractBusinessRulesPropertyTest` — Properties 2, 3, 4
    - **Property 2: Contract creation updates tenant subscription plan**
    - **Validates: Requirements 6.2**
    - **Property 3: Single ATIVO contract invariant per tenant**
    - **Validates: Requirements 6.3**
    - **Property 4: Contract CRUD operations always produce AuditLog entries**
    - **Validates: Requirements 6.4, 10.4, 11.4**

  - [ ]* 16.4 Criar `ContractFilterPropertyTest` — Property 7
    - **Property 7: Contract filter returns only matching results**
    - **Validates: Requirements 7.2**

  - [ ]* 16.5 Criar `ContractKpiPropertyTest` — Property 11
    - **Property 11: Contract KPI aggregation is correct**
    - **Validates: Requirements 9.1**

  - [ ]* 16.6 Criar `ContractPersistencePropertyTest` — Properties 1, 8 (H2 in-memory)
    - **Property 1: Contract field persistence round-trip**
    - **Validates: Requirements 6.1**
    - **Property 8: Payment record field persistence round-trip**
    - **Validates: Requirements 8.1**

  - [ ]* 16.7 Criar `PaymentRecordOrderingPropertyTest` — Property 9
    - **Property 9: Payment records are ordered by mesReferencia descending**
    - **Validates: Requirements 8.2**

  - [ ]* 16.8 Criar `ContractSecurityPropertyTest` — Property 12 (Spring Security test)
    - **Property 12: Non-SUPER_ADMIN users cannot access contract routes**
    - **Validates: Requirements 11.1, 11.2, 11.3**

- [x] 17. Checkpoint final — garantir que todos os testes passem
  - Executar `./mvnw test` e verificar que não há falhas. Perguntar ao usuário se houver dúvidas.

---

## Notes

- Tarefas marcadas com `*` são opcionais e podem ser puladas para um MVP mais rápido
- Cada tarefa referencia os requisitos específicos para rastreabilidade
- Os checkpoints garantem validação incremental a cada etapa
- Os testes de propriedade validam invariantes universais do módulo de contratos
- Os testes unitários validam exemplos específicos e casos limite
- O Manual de Arquitetura (`docs/ARCHITECTURE.md`) é um documento vivo — deve ser atualizado sempre que um novo padrão for estabelecido

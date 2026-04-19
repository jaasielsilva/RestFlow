# Requirements Document

## Introduction

Este documento cobre dois artefatos complementares para o ERP Corporativo SaaS B2B multi-tenant construído em Java 21 / Spring Boot 3 / Thymeleaf:

1. **Manual de Arquitetura e Desenvolvimento** — documento oficial que codifica os padrões de código, arquitetura em camadas, convenções de nomenclatura, padrão de páginas Thymeleaf e guia de criação de novos módulos de negócio, garantindo consistência e escalabilidade à medida que o produto cresce.

2. **Módulo de Contratos e Assinaturas (Super Admin)** — funcionalidade no painel do SUPER_ADMIN para gerenciar contratos comerciais com cada tenant: plano contratado, valor negociado, vigência, status do contrato, histórico de pagamentos e alertas de vencimento.

O sistema já possui: painel do SUPER_ADMIN (gestão de tenants, módulos, planos, permissões, auditoria), portal do tenant com módulos habilitáveis por plano, e o módulo de Ordens de Serviço como primeiro módulo de negócio implementado.

---

## Glossary

- **Platform**: A instância central do ERP Corporativo SaaS, operada pelo dono da plataforma.
- **Super_Admin**: Usuário com `Role.SUPER_ADMIN`, único na plataforma, que opera o painel administrativo central.
- **Tenant**: Empresa cliente que contrata o SaaS. Representada pela entidade `Tenant`.
- **Tenant_Admin**: Usuário com `Role.ADMIN` dentro de um tenant, que administra o portal daquele tenant.
- **Tenant_User**: Usuário com `Role.USER` dentro de um tenant.
- **SubscriptionPlan**: Plano de assinatura da plataforma (ex.: Starter, Pro, Enterprise), que define quais módulos estão disponíveis.
- **Contract**: Contrato comercial entre a Platform e um Tenant, registrando o plano contratado, valor negociado, vigência e condições.
- **ContractStatus**: Ciclo de vida de um contrato — `ATIVO`, `SUSPENSO`, `ENCERRADO`, `AGUARDANDO_ASSINATURA`.
- **PaymentRecord**: Registro de um pagamento (ou inadimplência) associado a um contrato.
- **PaymentStatus**: Status de um pagamento — `PAGO`, `PENDENTE`, `ATRASADO`, `CANCELADO`.
- **Architecture_Manual**: Documento vivo (Markdown) que define padrões de desenvolvimento do projeto.
- **Module**: Funcionalidade de negócio habilitável por tenant, representada por `PlatformModule`.
- **UseCase**: Classe `@Component` na camada `usecase/` que orquestra a lógica de negócio de um módulo.
- **WebController**: Classe `@Controller` na camada `controller/web/` que serve páginas Thymeleaf.
- **ApiController**: Classe `@RestController` na camada `controller/api/v1/` que serve endpoints JSON.
- **Form**: DTO de entrada para operações de escrita via formulário web.
- **ViewModel**: DTO de saída (record Java) para renderização em templates Thymeleaf.
- **TenantFilter**: Filtro Hibernate (`@Filter`) que restringe queries ao `tenant_id` do usuário autenticado.

---

## Requirements

### Requirement 1: Manual de Arquitetura — Estrutura de Camadas

**User Story:** Como desenvolvedor do projeto, quero um manual que defina claramente a arquitetura em camadas, para que todo novo código siga o mesmo padrão e o projeto permaneça manutenível ao escalar.

#### Acceptance Criteria

1. THE Architecture_Manual SHALL documentar as camadas obrigatórias do projeto: `model`, `repository`, `usecase`, `service`, `controller/web`, `controller/api`, `dto`, `mapper`, `security`, `tenant`, `config` e `exception`.
2. THE Architecture_Manual SHALL definir a regra de dependência entre camadas: `controller` depende de `usecase`; `usecase` depende de `repository` e `service`; `service` depende de `repository`; nenhuma camada inferior referencia camada superior.
3. THE Architecture_Manual SHALL especificar que toda lógica de negócio reside exclusivamente na camada `usecase`, e que controllers não devem conter lógica além de delegação e montagem do modelo de view.
4. THE Architecture_Manual SHALL definir que `@Service` é reservado para serviços de infraestrutura e utilitários compartilhados (ex.: `ModuleVisualMapper`, `AuditService`), enquanto `@Component` com sufixo `UseCase` é usado para orquestração de negócio.
5. THE Architecture_Manual SHALL documentar o padrão de isolamento multi-tenant via `TenantFilter` Hibernate, explicando quando e como aplicar `@FilterDef` e `@Filter` em entidades de negócio.

---

### Requirement 2: Manual de Arquitetura — Convenções de Nomenclatura

**User Story:** Como desenvolvedor do projeto, quero convenções de nomenclatura claras e consistentes, para que qualquer membro da equipe possa localizar e criar arquivos sem ambiguidade.

#### Acceptance Criteria

1. THE Architecture_Manual SHALL definir o padrão de nomenclatura de pacotes: `controller/web/{contexto}`, `controller/api/v1/{contexto}`, `usecase/web/{modulo}`, `usecase/api/{modulo}`, `dto/web/{modulo}`, `dto/api/{contexto}`, `repository/{dominio}`.
2. THE Architecture_Manual SHALL especificar sufixos obrigatórios por tipo de classe: `WebController`, `ApiController`, `UseCase`, `WebService`, `Repository`, `Form`, `ViewModel`, `Response`, `Request`, `Mapper`.
3. THE Architecture_Manual SHALL definir que entidades JPA usam nomes em português no singular (ex.: `Tenant`, `Usuario`, `OrdemServico`) e tabelas no plural em snake_case (ex.: `tenants`, `usuarios`, `ordens_servico`).
4. THE Architecture_Manual SHALL especificar que `ViewModel` é sempre um Java `record` imutável, e `Form` é sempre uma classe mutável com anotações de validação Bean Validation.
5. THE Architecture_Manual SHALL definir que constantes de código de módulo (ex.: `"PEDIDOS"`, `"FINANCEIRO"`) são strings em maiúsculas, centralizadas em uma classe de constantes ou no `DataInitializer`.

---

### Requirement 3: Manual de Arquitetura — Padrão de Páginas Thymeleaf

**User Story:** Como desenvolvedor do projeto, quero um guia de padrão de páginas Thymeleaf, para que todas as telas do sistema tenham aparência e comportamento consistentes.

#### Acceptance Criteria

1. THE Architecture_Manual SHALL documentar a estrutura de diretórios de templates: `templates/admin/`, `templates/tenant/`, `templates/auth/`, `templates/error/`, e `templates/fragments/`.
2. THE Architecture_Manual SHALL definir que toda página usa um layout base via Thymeleaf Layout Dialect ou `th:replace` de fragmentos, incluindo obrigatoriamente: sidebar, topbar, área de conteúdo e área de toast/notificação.
3. THE Architecture_Manual SHALL especificar os atributos de modelo obrigatórios que todo `WebController` deve popular: `pageTitle`, `pageSubtitle`, `activeMenu`.
4. THE Architecture_Manual SHALL documentar o padrão de feedback ao usuário: `toastSuccess` e `toastError` via `RedirectAttributes.addFlashAttribute`, renderizados pelo fragmento de toast global.
5. THE Architecture_Manual SHALL definir o padrão de paginação: parâmetros `page` (base 0) e `size` (padrão 20) via `@RequestParam`, com `Page<T>` do Spring Data encapsulado em um `ViewModel` de lista.
6. THE Architecture_Manual SHALL especificar que formulários de criação e edição compartilham o mesmo template `form.html`, diferenciados pelo atributo de modelo `isEdit` (boolean).

---

### Requirement 4: Manual de Arquitetura — Guia de Criação de Novo Módulo

**User Story:** Como desenvolvedor do projeto, quero um guia passo a passo para criar um novo módulo de negócio, para que eu possa adicionar funcionalidades ao ERP de forma padronizada e sem retrabalho.

#### Acceptance Criteria

1. THE Architecture_Manual SHALL documentar o checklist completo de criação de módulo, cobrindo: registro no `DataInitializer`, criação da entidade JPA com `@FilterDef`/`@Filter` de tenant, repository, UseCase, WebController, ApiController (opcional), DTOs (Form + ViewModel), templates Thymeleaf e entrada no `ModuleVisualMapper`.
2. THE Architecture_Manual SHALL especificar que todo módulo de negócio deve ter seu acesso protegido por `tenantPortalWebService.requireEnabledModule(authentication, "CODIGO_MODULO")` no início de cada método de controller que exige o módulo habilitado.
3. THE Architecture_Manual SHALL definir o padrão de geração de número sequencial legível por tenant (ex.: `OS-0001`), usando query `findMaxSequenceByTenantId` no repository.
4. THE Architecture_Manual SHALL documentar como registrar o módulo no `ModuleVisualMapper` com ícone Font Awesome e classe de cor (`module-tone-*`).
5. THE Architecture_Manual SHALL especificar que módulos de negócio devem usar `Specification` do Spring Data JPA para filtros dinâmicos de listagem, com classe `{Entidade}Specifications` separada.

---

### Requirement 5: Manual de Arquitetura — Padrões de Segurança e Multi-Tenancy

**User Story:** Como dono da plataforma, quero que o manual defina padrões de segurança e isolamento de dados, para que nenhum tenant acesse dados de outro tenant acidentalmente.

#### Acceptance Criteria

1. THE Architecture_Manual SHALL documentar que o isolamento de dados por tenant é garantido em duas camadas: (a) filtro Hibernate `tenantFilter` ativado pelo `TenantHibernateFilterAspect` para entidades anotadas, e (b) verificação explícita de ownership em UseCases via `filter(entity -> entity.getTenant().getId().equals(tenantId))`.
2. THE Architecture_Manual SHALL especificar que o `SUPER_ADMIN` opera sem `tenantFilter` ativo, pois seu tenant é o tenant da plataforma e não deve ser confundido com tenants de clientes.
3. THE Architecture_Manual SHALL definir que rotas do painel admin seguem o prefixo `/admin/**` e são protegidas por `hasRole('SUPER_ADMIN')` na `SecurityConfig`.
4. THE Architecture_Manual SHALL definir que rotas do portal do tenant seguem o prefixo `/app/**` e são protegidas por `hasAnyRole('ADMIN', 'USER')` na `SecurityConfig`.
5. THE Architecture_Manual SHALL documentar o padrão de auditoria: toda ação relevante do SUPER_ADMIN deve registrar um `AuditLog` com `acao`, `descricao`, `entidade`, `entidadeId` e `executadoPor`.

---

### Requirement 6: Módulo de Contratos — Entidade e Ciclo de Vida

**User Story:** Como Super Admin, quero registrar e gerenciar contratos comerciais com cada tenant, para que eu tenha controle formal sobre o que foi acordado, por quanto tempo e por qual valor.

#### Acceptance Criteria

1. THE Platform SHALL persistir contratos com os campos: tenant associado, plano de assinatura contratado, valor mensal negociado (podendo diferir do valor padrão do plano), data de início de vigência, data de término de vigência (opcional para contratos sem prazo), status do contrato (`ContractStatus`) e observações livres.
2. WHEN o Super_Admin cria um contrato para um tenant, THE Platform SHALL associar automaticamente o `SubscriptionPlan` selecionado ao `Tenant`, atualizando o campo `subscriptionPlan` da entidade `Tenant`.
3. THE Platform SHALL garantir que cada tenant possua no máximo um contrato com status `ATIVO` simultaneamente.
4. WHEN o Super_Admin altera o status de um contrato para `ENCERRADO` ou `SUSPENSO`, THE Platform SHALL registrar um `AuditLog` com a ação, o tenant afetado e o usuário executor.
5. WHEN a data de término de vigência de um contrato `ATIVO` é atingida, THE Platform SHALL exibir o contrato com indicador visual de "Vencido" na listagem de contratos do admin.
6. IF um tenant não possui nenhum contrato cadastrado, THEN THE Platform SHALL exibir o tenant com indicador "Sem Contrato" na listagem de tenants do admin.

---

### Requirement 7: Módulo de Contratos — Interface do Super Admin (CRUD)

**User Story:** Como Super Admin, quero uma interface completa para criar, visualizar, editar e encerrar contratos, para que eu possa gerenciar o relacionamento comercial com cada cliente diretamente no painel.

#### Acceptance Criteria

1. THE Admin_Panel SHALL exibir uma página de listagem de contratos acessível em `/admin/contratos`, com colunas: tenant, plano, valor mensal, data de início, data de término, status e ações.
2. THE Admin_Panel SHALL permitir filtrar a listagem de contratos por: tenant (busca por nome), status do contrato e plano de assinatura.
3. THE Admin_Panel SHALL exibir uma página de detalhe do contrato em `/admin/contratos/{id}`, mostrando todos os campos do contrato e o histórico de pagamentos associados.
4. THE Admin_Panel SHALL exibir um formulário de criação de contrato em `/admin/contratos/new`, com seleção de tenant, plano, valor negociado, datas de vigência, status inicial e observações.
5. THE Admin_Panel SHALL exibir um formulário de edição de contrato em `/admin/contratos/{id}/edit`, permitindo alterar valor, datas, status e observações.
6. WHEN o Super_Admin submete o formulário de criação ou edição com dados inválidos, THE Admin_Panel SHALL reexibir o formulário com mensagens de erro de validação por campo.
7. THE Admin_Panel SHALL permitir encerrar um contrato diretamente da página de detalhe, com confirmação modal antes da ação.

---

### Requirement 8: Módulo de Contratos — Histórico de Pagamentos

**User Story:** Como Super Admin, quero registrar e visualizar o histórico de pagamentos de cada contrato, para que eu tenha rastreabilidade financeira do relacionamento com cada tenant.

#### Acceptance Criteria

1. THE Platform SHALL persistir registros de pagamento com os campos: contrato associado, mês/ano de referência, valor pago, data de pagamento, status do pagamento (`PaymentStatus`) e observações.
2. THE Admin_Panel SHALL exibir o histórico de pagamentos de um contrato na página de detalhe do contrato, ordenado por mês de referência decrescente.
3. THE Admin_Panel SHALL permitir ao Super_Admin adicionar um novo registro de pagamento a partir da página de detalhe do contrato.
4. THE Admin_Panel SHALL permitir ao Super_Admin editar o status e as observações de um registro de pagamento existente.
5. WHEN um registro de pagamento tem status `ATRASADO`, THE Admin_Panel SHALL exibir o registro com destaque visual (ex.: badge vermelho) na listagem de pagamentos.
6. THE Admin_Panel SHALL exibir na listagem de contratos um indicador do status do último pagamento de cada contrato.

---

### Requirement 9: Módulo de Contratos — Dashboard e Alertas

**User Story:** Como Super Admin, quero visualizar métricas e alertas sobre contratos no painel administrativo, para que eu possa agir proativamente sobre vencimentos e inadimplências.

#### Acceptance Criteria

1. THE Admin_Panel SHALL exibir no dashboard administrativo os seguintes indicadores de contratos: total de contratos ativos, total de contratos vencidos (data de término ultrapassada), total de contratos com pagamento atrasado e receita mensal recorrente (soma dos valores mensais de contratos ativos).
2. THE Admin_Panel SHALL exibir na listagem de contratos um badge de alerta para contratos com data de término nos próximos 30 dias.
3. WHEN o Super_Admin acessa o dashboard administrativo, THE Admin_Panel SHALL exibir uma seção de alertas listando contratos que vencem nos próximos 30 dias e contratos com pagamentos em atraso.
4. THE Admin_Panel SHALL permitir ao Super_Admin navegar diretamente do alerta para a página de detalhe do contrato correspondente.

---

### Requirement 10: Módulo de Contratos — Integração com Gestão de Tenants

**User Story:** Como Super Admin, quero que a gestão de contratos esteja integrada à gestão de tenants, para que eu possa ver o status contratual de um tenant diretamente na sua página de detalhes.

#### Acceptance Criteria

1. THE Admin_Panel SHALL exibir na página de detalhe de um tenant o contrato ativo associado, incluindo: plano, valor mensal, data de término e status.
2. THE Admin_Panel SHALL exibir na listagem de tenants uma coluna ou badge indicando o status do contrato ativo de cada tenant (`ATIVO`, `SUSPENSO`, `ENCERRADO`, `SEM CONTRATO`).
3. THE Admin_Panel SHALL disponibilizar na página de detalhe do tenant um link direto para criar um novo contrato para aquele tenant, pré-preenchendo o campo de tenant no formulário.
4. WHEN o Super_Admin encerra ou suspende um contrato, THE Platform SHALL registrar o evento no `AuditLog` com referência ao tenant e ao contrato afetado.

---

### Requirement 11: Módulo de Contratos — Segurança e Acesso

**User Story:** Como dono da plataforma, quero que o módulo de contratos seja acessível exclusivamente pelo Super Admin, para que dados comerciais sensíveis não sejam expostos a usuários de tenants.

#### Acceptance Criteria

1. THE Platform SHALL restringir todas as rotas `/admin/contratos/**` ao papel `SUPER_ADMIN` via configuração na `SecurityConfig`.
2. THE Platform SHALL restringir todas as rotas da API `/api/v1/admin/contratos/**` ao papel `SUPER_ADMIN`.
3. IF um usuário sem papel `SUPER_ADMIN` tenta acessar qualquer rota de contratos, THEN THE Platform SHALL retornar HTTP 403 para requisições de API e redirecionar para a página de erro para requisições web.
4. THE Platform SHALL registrar no `AuditLog` toda operação de criação, edição e encerramento de contrato, incluindo o identificador do executor (`executadoPor`).

---

### Requirement 12: Manual de Arquitetura — Padrões de API REST

**User Story:** Como desenvolvedor do projeto, quero que o manual defina padrões para os endpoints REST, para que a API seja consistente e fácil de consumir por integrações futuras.

#### Acceptance Criteria

1. THE Architecture_Manual SHALL definir que todos os endpoints REST seguem o prefixo `/api/v1/`, com subprefixo `/admin/` para operações do SUPER_ADMIN e `/tenantadmin/` para operações do ADMIN do tenant.
2. THE Architecture_Manual SHALL especificar o padrão de resposta de erro da API: objeto JSON com campos `status` (HTTP code), `error` (código de erro do enum `ApiErrorCode`) e `message` (descrição legível).
3. THE Architecture_Manual SHALL definir que operações de listagem da API retornam objeto com campos `content` (lista), `page`, `totalPages` e `totalElements`.
4. THE Architecture_Manual SHALL especificar que `@Valid` é obrigatório em todos os parâmetros `@RequestBody` de endpoints de escrita, e que erros de validação retornam HTTP 422 com detalhes por campo.
5. THE Architecture_Manual SHALL documentar que `ApiController` não contém lógica de negócio — delega integralmente ao `UseCase` correspondente, assim como o `WebController`.

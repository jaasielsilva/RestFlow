# Documento de Requisitos — Módulo CRM & Clientes

## Introdução

Este documento cobre o **Módulo de CRM & Clientes** do ERP Corporativo SaaS B2B multi-tenant (Java 21 / Spring Boot 3.5 / Thymeleaf / MySQL).

O módulo é um **módulo de negócio do tenant**, habilitável por plano de assinatura, acessível no portal do tenant em `/app/clientes`. Ele oferece gestão completa de clientes (pessoas físicas e jurídicas), histórico de interações, funil de oportunidades/leads e integração com outros módulos (ex.: Ordens de Serviço).

O módulo segue exatamente os padrões estabelecidos pelo módulo de referência `OrdemServico` e pelo Manual de Arquitetura (`docs/ARCHITECTURE.md`): isolamento multi-tenant via `TenantFilter` Hibernate, verificação de ownership no UseCase, proteção de módulo via `requireEnabledModule`, e padrão de páginas Thymeleaf com `pageTitle`, `pageSubtitle` e `activeMenu`.

---

## Glossário

- **Platform**: A instância central do ERP Corporativo SaaS, operada pelo dono da plataforma.
- **Tenant**: Empresa cliente que contrata o SaaS. Representada pela entidade `Tenant`.
- **Tenant_Admin**: Usuário com `Role.ADMIN` dentro de um tenant, que administra o portal daquele tenant.
- **Tenant_User**: Usuário com `Role.USER` dentro de um tenant.
- **CRM_Module**: O módulo de CRM & Clientes, identificado pelo código `"CLIENTES"` no `DataInitializer`.
- **Cliente**: Pessoa física ou jurídica cadastrada no tenant, representada pela entidade `Cliente`.
- **TipoCliente**: Enum que classifica o cliente como `PESSOA_FISICA` ou `PESSOA_JURIDICA`.
- **StatusCliente**: Enum que representa o ciclo de vida do cliente: `ATIVO`, `INATIVO`, `PROSPECTO`, `BLOQUEADO`.
- **Interacao**: Registro de um contato ou evento relacionado a um cliente (ligação, e-mail, reunião, visita), representado pela entidade `Interacao`.
- **TipoInteracao**: Enum que classifica a interação: `LIGACAO`, `EMAIL`, `REUNIAO`, `VISITA`, `OUTRO`.
- **Oportunidade**: Registro de uma oportunidade de negócio associada a um cliente, representado pela entidade `Oportunidade`.
- **StatusOportunidade**: Enum que representa o estágio do funil: `PROSPECCAO`, `QUALIFICACAO`, `PROPOSTA`, `NEGOCIACAO`, `FECHADO_GANHO`, `FECHADO_PERDIDO`.
- **TenantFilter**: Filtro Hibernate (`@Filter`) que restringe queries ao `tenant_id` do usuário autenticado.
- **UseCase**: Classe `@Component` na camada `usecase/` que orquestra a lógica de negócio do módulo.
- **WebController**: Classe `@Controller` na camada `controller/web/` que serve páginas Thymeleaf.
- **ApiController**: Classe `@RestController` na camada `controller/api/v1/` que serve endpoints JSON.
- **Form**: DTO de entrada para operações de escrita via formulário web.
- **ViewModel**: DTO de saída (record Java) para renderização em templates Thymeleaf.
- **SubscriptionPlan**: Plano de assinatura que define quais módulos estão disponíveis para o tenant.

---

## Requisitos

### Requisito 1: Cadastro de Clientes — Dados Básicos

**User Story:** Como Tenant_Admin ou Tenant_User, quero cadastrar clientes com dados básicos de identificação, para que eu tenha um registro centralizado de todos os meus clientes dentro do meu tenant.

#### Critérios de Aceitação

1. THE CRM_Module SHALL persistir clientes com os campos: tipo (`TipoCliente`), nome completo ou razão social, CPF ou CNPJ (opcional), e-mail (opcional), telefone principal (opcional), telefone secundário (opcional), endereço completo (logradouro, número, complemento, bairro, cidade, estado, CEP — todos opcionais), status (`StatusCliente`), observações livres (TEXT, opcional) e número sequencial legível por tenant (ex.: `CLI-0001`).
2. THE CRM_Module SHALL garantir que o número sequencial do cliente seja único dentro do tenant e gerado automaticamente no momento da criação.
3. WHEN um Tenant_User tenta criar um cliente, THE CRM_Module SHALL verificar se o módulo `"CLIENTES"` está habilitado para o tenant antes de processar a requisição.
4. IF o CPF ou CNPJ informado já estiver cadastrado para outro cliente do mesmo tenant, THEN THE CRM_Module SHALL rejeitar o cadastro com mensagem de erro descritiva.
5. THE CRM_Module SHALL garantir que todos os dados de clientes sejam isolados por tenant via `TenantFilter` Hibernate, de modo que nenhum tenant acesse clientes de outro tenant.
6. WHEN um cliente é criado com sucesso, THE CRM_Module SHALL exibir mensagem de confirmação via `toastSuccess`.

---

### Requisito 2: Cadastro de Clientes — Pessoa Física vs. Jurídica

**User Story:** Como Tenant_Admin ou Tenant_User, quero diferenciar clientes pessoa física de pessoa jurídica, para que eu possa armazenar os dados corretos para cada tipo.

#### Critérios de Aceitação

1. WHEN o tipo do cliente é `PESSOA_FISICA`, THE CRM_Module SHALL exibir os campos: nome completo, CPF, data de nascimento (opcional) e gênero (opcional, enum: `MASCULINO`, `FEMININO`, `OUTRO`, `NAO_INFORMADO`).
2. WHEN o tipo do cliente é `PESSOA_JURIDICA`, THE CRM_Module SHALL exibir os campos: razão social, nome fantasia (opcional), CNPJ, inscrição estadual (opcional) e nome do contato principal (opcional).
3. THE CRM_Module SHALL armazenar o tipo do cliente e garantir que os campos específicos de cada tipo sejam validados de acordo com o tipo selecionado.
4. WHEN o tipo do cliente é alterado durante a edição, THE CRM_Module SHALL limpar os campos específicos do tipo anterior que não se aplicam ao novo tipo.

---

### Requisito 3: Listagem e Busca de Clientes

**User Story:** Como Tenant_Admin ou Tenant_User, quero listar e buscar clientes com filtros, para que eu possa localizar rapidamente qualquer cliente cadastrado.

#### Critérios de Aceitação

1. THE CRM_Module SHALL exibir a listagem de clientes em `/app/clientes` com as colunas: número, nome/razão social, tipo, CPF/CNPJ (mascarado), e-mail, telefone, status e ações.
2. THE CRM_Module SHALL suportar filtros dinâmicos na listagem: busca por nome/razão social (substring, case-insensitive), filtro por `TipoCliente`, filtro por `StatusCliente` e busca por CPF/CNPJ (substring).
3. THE CRM_Module SHALL paginar a listagem com parâmetros `page` (base 0) e `size` (padrão 20), retornando `Page<Cliente>` do Spring Data.
4. THE CRM_Module SHALL exibir na listagem indicadores visuais de status: badge verde para `ATIVO`, badge cinza para `INATIVO`, badge amarelo para `PROSPECTO` e badge vermelho para `BLOQUEADO`.
5. THE CRM_Module SHALL exibir na listagem o total de clientes ativos, inativos e prospectos como cards de KPI no topo da página.
6. WHEN nenhum cliente corresponde aos filtros aplicados, THE CRM_Module SHALL exibir mensagem informativa "Nenhum cliente encontrado" na área da tabela.

---

### Requisito 4: Visualização e Edição de Clientes

**User Story:** Como Tenant_Admin ou Tenant_User, quero visualizar o perfil completo de um cliente e editá-lo, para que eu possa manter os dados sempre atualizados.

#### Critérios de Aceitação

1. THE CRM_Module SHALL exibir a página de detalhe do cliente em `/app/clientes/{id}` com todos os campos cadastrais, o histórico de interações e as oportunidades associadas.
2. THE CRM_Module SHALL exibir na página de detalhe do cliente as Ordens de Serviço vinculadas ao cliente (quando o módulo `"PEDIDOS"` estiver habilitado), com link para cada OS.
3. WHEN um Tenant_User com permissão de escrita acessa a edição de um cliente, THE CRM_Module SHALL exibir o formulário de edição em `/app/clientes/{id}/edit` pré-preenchido com os dados atuais.
4. WHEN o formulário de edição é submetido com dados inválidos, THE CRM_Module SHALL reexibir o formulário com mensagens de erro de validação por campo.
5. WHEN a edição é concluída com sucesso, THE CRM_Module SHALL redirecionar para a página de detalhe do cliente com `toastSuccess`.
6. IF um Tenant_User sem permissão de escrita tenta acessar a edição, THEN THE CRM_Module SHALL redirecionar para a página de detalhe do cliente.

---

### Requisito 5: Ciclo de Vida e Status do Cliente

**User Story:** Como Tenant_Admin, quero gerenciar o status dos clientes, para que eu possa controlar quais clientes estão ativos, inativos, em prospecção ou bloqueados.

#### Critérios de Aceitação

1. THE CRM_Module SHALL permitir alterar o status de um cliente para qualquer valor de `StatusCliente` a partir da página de detalhe.
2. WHEN o status de um cliente é alterado para `BLOQUEADO`, THE CRM_Module SHALL exibir campo obrigatório de motivo do bloqueio (texto livre, máximo 500 caracteres).
3. WHEN o status de um cliente é alterado com sucesso, THE CRM_Module SHALL exibir `toastSuccess` com o novo status.
4. THE CRM_Module SHALL permitir excluir um cliente somente se ele não possuir Ordens de Serviço vinculadas e não possuir Oportunidades com status diferente de `FECHADO_GANHO` ou `FECHADO_PERDIDO`.
5. IF um Tenant_User tenta excluir um cliente com vínculos ativos, THEN THE CRM_Module SHALL rejeitar a exclusão com mensagem de erro descritiva listando os vínculos impeditivos.

---

### Requisito 6: Histórico de Interações

**User Story:** Como Tenant_Admin ou Tenant_User, quero registrar e visualizar o histórico de interações com cada cliente, para que eu tenha rastreabilidade completa do relacionamento.

#### Critérios de Aceitação

1. THE CRM_Module SHALL persistir interações com os campos: cliente associado, tipo (`TipoInteracao`), data e hora da interação, assunto (máximo 200 caracteres), descrição (TEXT, opcional), usuário responsável pelo registro e número sequencial legível por tenant (ex.: `INT-0001`).
2. THE CRM_Module SHALL exibir o histórico de interações na página de detalhe do cliente, ordenado por data e hora decrescente.
3. THE CRM_Module SHALL permitir adicionar uma nova interação diretamente da página de detalhe do cliente, via formulário inline ou modal.
4. THE CRM_Module SHALL permitir editar e excluir interações existentes a partir da página de detalhe do cliente.
5. WHEN uma interação é registrada, THE CRM_Module SHALL atualizar o campo `ultimaInteracaoEm` do cliente com a data e hora da interação registrada.
6. THE CRM_Module SHALL exibir na listagem de clientes a data da última interação de cada cliente.
7. IF a data da interação informada for posterior à data e hora atual do servidor, THEN THE CRM_Module SHALL rejeitar o registro com mensagem de erro.

---

### Requisito 7: Funil de Oportunidades

**User Story:** Como Tenant_Admin ou Tenant_User, quero gerenciar oportunidades de negócio associadas a clientes, para que eu possa acompanhar o funil de vendas do meu tenant.

#### Critérios de Aceitação

1. THE CRM_Module SHALL persistir oportunidades com os campos: cliente associado, título (máximo 200 caracteres), status (`StatusOportunidade`), valor estimado (decimal, opcional), data prevista de fechamento (opcional), usuário responsável, descrição (TEXT, opcional) e número sequencial legível por tenant (ex.: `OPO-0001`).
2. THE CRM_Module SHALL exibir uma página de funil de oportunidades em `/app/clientes/oportunidades`, agrupando as oportunidades por `StatusOportunidade` em colunas (estilo kanban ou tabela agrupada).
3. THE CRM_Module SHALL exibir na página de funil os seguintes KPIs: total de oportunidades abertas, valor total estimado de oportunidades abertas, total de oportunidades fechadas ganhas no mês corrente e taxa de conversão (fechadas ganhas / total fechadas, em percentual).
4. THE CRM_Module SHALL permitir criar, editar e excluir oportunidades a partir da página de funil e da página de detalhe do cliente.
5. WHEN o status de uma oportunidade é alterado para `FECHADO_GANHO`, THE CRM_Module SHALL registrar automaticamente a data de fechamento real (`dataFechamentoReal`) com a data atual.
6. WHEN o status de uma oportunidade é alterado para `FECHADO_PERDIDO`, THE CRM_Module SHALL exibir campo obrigatório de motivo da perda (texto livre, máximo 500 caracteres).
7. THE CRM_Module SHALL exibir na página de detalhe do cliente a lista de oportunidades associadas, com status, valor estimado e data prevista de fechamento.

---

### Requisito 8: Integração com Ordens de Serviço

**User Story:** Como Tenant_Admin ou Tenant_User, quero vincular Ordens de Serviço a clientes cadastrados no CRM, para que eu tenha visibilidade completa do histórico de serviços prestados a cada cliente.

#### Critérios de Aceitação

1. WHERE o módulo `"PEDIDOS"` está habilitado para o tenant, THE CRM_Module SHALL exibir na página de detalhe do cliente a lista de Ordens de Serviço vinculadas, com número, título, status e data de criação.
2. WHERE o módulo `"PEDIDOS"` está habilitado para o tenant, THE CRM_Module SHALL permitir criar uma nova Ordem de Serviço pré-vinculada ao cliente a partir da página de detalhe do cliente.
3. THE CRM_Module SHALL permitir vincular um cliente do CRM a uma Ordem de Serviço existente pelo campo `clienteId` (FK opcional na entidade `OrdemServico`).
4. WHEN uma Ordem de Serviço é vinculada a um cliente, THE CRM_Module SHALL exibir o nome do cliente na página de detalhe da OS.
5. WHERE o módulo `"PEDIDOS"` não está habilitado para o tenant, THE CRM_Module SHALL ocultar a seção de Ordens de Serviço na página de detalhe do cliente, sem exibir erro.

---

### Requisito 9: Segurança e Controle de Acesso

**User Story:** Como dono da plataforma, quero que o módulo de CRM seja acessível apenas por usuários do tenant com o módulo habilitado, para que dados de clientes não sejam expostos a tenants sem o módulo contratado.

#### Critérios de Aceitação

1. THE CRM_Module SHALL restringir todas as rotas `/app/clientes/**` ao papel `ADMIN` ou `USER` via configuração na `SecurityConfig` (já coberto pela regra `/app/**`).
2. THE CRM_Module SHALL verificar se o módulo `"CLIENTES"` está habilitado para o tenant no início de cada método de controller, via `tenantPortalWebService.requireEnabledModule(authentication, "CLIENTES")`.
3. IF o módulo `"CLIENTES"` não está habilitado para o tenant, THEN THE CRM_Module SHALL redirecionar o usuário para o dashboard do tenant com mensagem de erro.
4. THE CRM_Module SHALL aplicar o `TenantFilter` Hibernate nas entidades `Cliente`, `Interacao` e `Oportunidade`, garantindo que queries retornem apenas registros do tenant autenticado.
5. THE CRM_Module SHALL verificar ownership explicitamente no UseCase para todas as operações de leitura e escrita: `entity.getTenant().getId().equals(tenantId)`.
6. WHEN um Tenant_User com permissão somente leitura (`canWrite() == false`) tenta acessar rotas de criação ou edição, THE CRM_Module SHALL redirecionar para a listagem ou detalhe correspondente.

---

### Requisito 10: Dashboard e KPIs do CRM

**User Story:** Como Tenant_Admin, quero visualizar métricas do CRM no portal do tenant, para que eu possa acompanhar a saúde do relacionamento com clientes e o desempenho do funil de vendas.

#### Critérios de Aceitação

1. THE CRM_Module SHALL exibir na página de listagem de clientes os seguintes KPIs: total de clientes ativos, total de prospectos, total de clientes inativos e total de clientes bloqueados.
2. THE CRM_Module SHALL exibir na página de funil de oportunidades os seguintes KPIs: total de oportunidades abertas, valor total estimado das oportunidades abertas, total de oportunidades fechadas ganhas no mês corrente e taxa de conversão do mês corrente.
3. THE CRM_Module SHALL exibir na listagem de clientes a coluna "Última Interação" com a data da interação mais recente de cada cliente, ou "Sem interação" quando não houver nenhuma.
4. THE CRM_Module SHALL exibir na listagem de clientes a coluna "Oportunidades Abertas" com o contador de oportunidades com status diferente de `FECHADO_GANHO` e `FECHADO_PERDIDO` para cada cliente.
5. WHEN o Tenant_Admin acessa o dashboard do tenant, THE CRM_Module SHALL exibir um card resumo do CRM com: total de clientes ativos, total de oportunidades abertas e valor total estimado do funil.

---

### Requisito 11: Importação e Exportação de Clientes

**User Story:** Como Tenant_Admin, quero importar e exportar a base de clientes em formato CSV, para que eu possa migrar dados de outros sistemas e fazer backups da base.

#### Critérios de Aceitação

1. THE CRM_Module SHALL permitir exportar a listagem de clientes (com filtros aplicados) em formato CSV, com os campos: número, tipo, nome/razão social, CPF/CNPJ, e-mail, telefone, cidade, estado, status e data de cadastro.
2. THE CRM_Module SHALL permitir importar clientes a partir de um arquivo CSV com o mesmo formato de exportação, validando cada linha individualmente.
3. WHEN a importação de CSV é processada, THE CRM_Module SHALL retornar um relatório com: total de linhas processadas, total de clientes importados com sucesso, total de linhas com erro e descrição dos erros por linha.
4. IF uma linha do CSV de importação contém CPF/CNPJ já cadastrado para o tenant, THEN THE CRM_Module SHALL ignorar a linha e registrá-la no relatório de erros sem interromper o processamento das demais linhas.
5. THE CRM_Module SHALL limitar o arquivo de importação a no máximo 1.000 linhas por operação, retornando erro descritivo se o limite for excedido.

---

### Requisito 12: Etiquetas e Segmentação de Clientes

**User Story:** Como Tenant_Admin ou Tenant_User, quero categorizar clientes com etiquetas personalizadas, para que eu possa segmentar a base de clientes de acordo com critérios do meu negócio.

#### Critérios de Aceitação

1. THE CRM_Module SHALL permitir criar, editar e excluir etiquetas (`Tag`) com nome (máximo 50 caracteres) e cor (código hexadecimal), isoladas por tenant.
2. THE CRM_Module SHALL permitir associar múltiplas etiquetas a um cliente via relacionamento `@ManyToMany`.
3. THE CRM_Module SHALL exibir as etiquetas associadas ao cliente na listagem e na página de detalhe.
4. THE CRM_Module SHALL permitir filtrar a listagem de clientes por etiqueta.
5. IF uma etiqueta é excluída, THEN THE CRM_Module SHALL remover automaticamente a associação com todos os clientes do tenant, sem excluir os clientes.

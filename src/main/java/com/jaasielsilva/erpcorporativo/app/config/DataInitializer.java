package com.jaasielsilva.erpcorporativo.app.config;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;

import com.jaasielsilva.erpcorporativo.app.config.properties.AppBootstrapProperties;
import com.jaasielsilva.erpcorporativo.app.model.ArticleVisibility;
import com.jaasielsilva.erpcorporativo.app.model.KnowledgeArticle;
import com.jaasielsilva.erpcorporativo.app.model.PlanAddon;
import com.jaasielsilva.erpcorporativo.app.model.PlanTier;
import com.jaasielsilva.erpcorporativo.app.model.PlatformModule;
import com.jaasielsilva.erpcorporativo.app.model.Role;
import com.jaasielsilva.erpcorporativo.app.model.SubscriptionPlan;
import com.jaasielsilva.erpcorporativo.app.model.Tenant;
import com.jaasielsilva.erpcorporativo.app.model.Usuario;
import com.jaasielsilva.erpcorporativo.app.repository.knowledge.KnowledgeArticleRepository;
import com.jaasielsilva.erpcorporativo.app.repository.module.PlatformModuleRepository;
import com.jaasielsilva.erpcorporativo.app.repository.plan.PlanAddonRepository;
import com.jaasielsilva.erpcorporativo.app.repository.plan.SubscriptionPlanRepository;
import com.jaasielsilva.erpcorporativo.app.repository.tenant.TenantRepository;
import com.jaasielsilva.erpcorporativo.app.repository.usuario.UsuarioRepository;

import com.jaasielsilva.erpcorporativo.app.service.shared.PlatformSettingService;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TenantRepository tenantRepository;
    private final AppBootstrapProperties bootstrapProperties;
    private final PlatformSettingService platformSettingService;
    private final PlatformModuleRepository platformModuleRepository;
    private final KnowledgeArticleRepository knowledgeArticleRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final PlanAddonRepository planAddonRepository;

    @Override
    public void run(String... args) {
        Tenant platformTenant = ensurePlatformTenant();
        ensureSuperAdmin(platformTenant);
        platformSettingService.ensureDefaults();
        ensureDefaultModules();
        ensureDefaultAddons();
        ensureDefaultSubscriptionPlans();
        ensureOnboardingArticles();
    }

    private Tenant ensurePlatformTenant() {
        String tenantSlug = bootstrapProperties.getTenant().getSlug();

        return tenantRepository.findBySlug(tenantSlug)
                .orElseGet(() -> tenantRepository.save(Tenant.builder()
                        .nome(bootstrapProperties.getTenant().getNome())
                        .slug(tenantSlug)
                        .ativo(true)
                        .build()));
    }

    private void ensureSuperAdmin(Tenant platformTenant) {
        validateBootstrapSecret();
        validateSingleSuperAdmin();

        String canonicalEmail = bootstrapProperties.getSuperAdmin().getEmail();
        String legacyEmail = "admin@admin.com";

        Optional<Usuario> existingAdmin = usuarioRepository.findFirstByEmailIgnoreCase(canonicalEmail);

        if (existingAdmin.isEmpty()) {
            existingAdmin = usuarioRepository.findFirstByEmailIgnoreCase(legacyEmail);
        }

        Usuario admin = existingAdmin.orElseGet(Usuario::new);
        admin.setNome(bootstrapProperties.getSuperAdmin().getNome());
        admin.setEmail(canonicalEmail);
        admin.setRole(Role.SUPER_ADMIN);
        admin.setAtivo(true);
        admin.setTenant(platformTenant);

        if (!isBcryptHash(admin.getPassword())) {
            admin.setPassword(passwordEncoder.encode(bootstrapProperties.getSuperAdmin().getPassword()));
        }

        boolean novoCadastro = admin.getId() == null;
        usuarioRepository.save(admin);

        if (novoCadastro) {
            System.out.println("SUPER ADMIN criado com senha criptografada e tenant da plataforma.");
            return;
        }

        System.out.println("SUPER ADMIN validado e estrutura multi-tenant inicializada.");
    }

    private boolean isBcryptHash(String password) {
        return password != null && password.startsWith("$2");
    }

    private void validateBootstrapSecret() {
        if (!StringUtils.hasText(bootstrapProperties.getSuperAdmin().getPassword())) {
            throw new IllegalStateException(
                    "Defina BOOTSTRAP_SUPER_ADMIN_PASSWORD ou application-local.yml antes de iniciar a aplicação.");
        }
    }

    private void validateSingleSuperAdmin() {
        long superAdminCount = usuarioRepository.countByRole(Role.SUPER_ADMIN);

        if (superAdminCount > 1) {
            throw new IllegalStateException("A plataforma permite apenas um único SUPER_ADMIN.");
        }
    }

    private void ensureDefaultModules() {
        ensureModule("DASHBOARD",        "Dashboard",         "/app",                        "Painel principal do tenant");
        ensureModule("USUARIOS",         "Usuários",          "/app/usuarios",               "Gestão de usuários do tenant");
        ensureModule("CONFIGURACOES",    "Configurações",     "/app/configuracoes",          "Preferências do tenant");
        ensureModule("CONHECIMENTO",     "Base de Conhecimento", "/app/conhecimento",        "Guias e artigos técnicos");
        ensureModule("FINANCEIRO",       "Financeiro",        "/app/modulos/financeiro",     "Gestão financeira");
        ensureModule("ESTOQUE",          "Estoque",           "/app/modulos/estoque",        "Controle de estoque");
        ensureModule("PEDIDOS",          "Pedidos",           "/app/pedidos",                "Gestão de ordens de serviço");
        ensureModule("RELATORIOS",       "Relatórios",        "/app/modulos/relatorios",     "Relatórios do tenant");
        ensureModule("CLIENTES",         "Clientes",          "/app/clientes",               "CRM e gestão de clientes");
        ensureModule("SUPORTE",          "Suporte",           "/app/suporte",                "Atendimento e gestão de chamados");
        ensureModule("COMERCIAL",        "Minha Conta",       "/app/minha-conta",            "Portal comercial e assinatura");
        ensureModule("INTEGRACOES",      "Integrações",       "/app/integracoes",            "Webhooks e integrações externas");
        ensureModule("BI_AVANCADO",      "BI Avançado",       "/app/bi-avancado",            "Analytics avançado do tenant");
        ensureModule("AUTOMACOES",       "Automações",        "/app/automacoes",             "Regras de automação de workflow");
        ensureModule("COMPLIANCE",       "Compliance",        "/app/compliance",             "LGPD e trilhas de consentimento");
    }

    private void ensureModule(String codigo, String nome, String rota, String descricao) {
        platformModuleRepository.findByCodigoIgnoreCase(codigo).orElseGet(() ->
                platformModuleRepository.save(PlatformModule.builder()
                        .codigo(codigo)
                        .nome(nome)
                        .rota(rota)
                        .descricao(descricao)
                        .ativo(true)
                        .build())
        );
    }

    private void ensureDefaultAddons() {
        ensureAddon("USERS_EXTRA", "Usuários extras", "Pacote adicional de assentos para usuários.");
        ensureAddon("STORAGE_EXTRA", "Armazenamento extra", "Ampliação de armazenamento de anexos.");
        ensureAddon("SUPPORT_PRIORITY", "Suporte prioritário", "Fila de suporte priorizada para o tenant.");
        ensureAddon("AUTOMATION_PACK", "Pacote de automações", "Regras e automações avançadas de processos.");
        ensureAddon("INTEGRATIONS_PACK", "Pacote de integrações", "Webhooks e conectores externos adicionais.");
        ensureAddon("BI_ADVANCED", "BI avançado", "Métricas e analytics avançados multi-módulo.");
        ensureAddon("LGPD_COMPLIANCE", "Compliance LGPD", "Fluxos avançados de privacidade e rastreabilidade.");
    }

    private void ensureAddon(String codigo, String nome, String descricao) {
        planAddonRepository.findByCodigoIgnoreCase(codigo)
                .orElseGet(() -> planAddonRepository.save(PlanAddon.builder()
                        .codigo(codigo)
                        .nome(nome)
                        .descricao(descricao)
                        .ativo(true)
                        .build()));
    }

    private void ensureDefaultSubscriptionPlans() {
        ensurePlan(
                "START",
                "Start",
                "Plano inicial para operacao comercial e atendimento.",
                PlanTier.START,
                10,
                20,
                false,
                "START",
                Set.of("DASHBOARD", "USUARIOS", "CLIENTES", "CONHECIMENTO", "SUPORTE"),
                Set.of("USERS_EXTRA", "STORAGE_EXTRA")
        );
        ensurePlan(
                "GROWTH",
                "Growth",
                "Plano principal com operacao, relatorios e configuracoes.",
                PlanTier.GROWTH,
                40,
                80,
                true,
                "GROWTH",
                Set.of("DASHBOARD", "USUARIOS", "CLIENTES", "CONHECIMENTO", "SUPORTE", "PEDIDOS", "RELATORIOS", "CONFIGURACOES", "COMERCIAL"),
                Set.of("USERS_EXTRA", "STORAGE_EXTRA", "SUPPORT_PRIORITY", "INTEGRATIONS_PACK")
        );
        ensurePlan(
                "SCALE",
                "Scale",
                "Plano premium com stack completa e add-ons avancados.",
                PlanTier.SCALE,
                120,
                300,
                true,
                "SCALE",
                Set.of("DASHBOARD", "USUARIOS", "CLIENTES", "CONHECIMENTO", "SUPORTE", "PEDIDOS", "RELATORIOS", "CONFIGURACOES", "FINANCEIRO", "ESTOQUE", "COMERCIAL", "INTEGRACOES", "BI_AVANCADO", "AUTOMACOES", "COMPLIANCE"),
                Set.of("USERS_EXTRA", "STORAGE_EXTRA", "SUPPORT_PRIORITY", "AUTOMATION_PACK", "INTEGRATIONS_PACK", "BI_ADVANCED", "LGPD_COMPLIANCE")
        );
    }

    private void ensurePlan(
            String codigo,
            String nome,
            String descricao,
            PlanTier tier,
            Integer maxUsers,
            Integer maxStorageGb,
            boolean annualDiscountEligible,
            String onboardingTemplate,
            Set<String> moduleCodes,
            Set<String> addonCodes
    ) {
        SubscriptionPlan plan = subscriptionPlanRepository.findByCodigoIgnoreCase(codigo)
                .orElseGet(() -> SubscriptionPlan.builder().codigo(codigo).build());

        plan.setNome(nome);
        plan.setDescricao(descricao);
        plan.setAtivo(true);
        plan.setTier(tier);
        plan.setMaxUsers(maxUsers);
        plan.setMaxStorageGb(maxStorageGb);
        plan.setAnnualDiscountEligible(annualDiscountEligible);
        plan.setOnboardingTemplate(onboardingTemplate);
        plan.setModules(platformModuleRepository.findAll().stream()
                .filter(m -> moduleCodes.contains(m.getCodigo()))
                .collect(Collectors.toSet()));
        plan.setAddons(planAddonRepository.findAll().stream()
                .filter(a -> addonCodes.contains(a.getCodigo()))
                .collect(Collectors.toSet()));

        subscriptionPlanRepository.save(plan);
    }

    private void ensureOnboardingArticles() {
        if (knowledgeArticleRepository.count() > 0) return;

        ensureArticle("Bem-vindo ao ERP Corporativo", "Primeiros Passos",
                "Olá! Seja bem-vindo ao ERP Corporativo.\n\n" +
                "Este é o seu portal de gestão. Aqui você encontra todos os módulos contratados no seu plano.\n\n" +
                "**Como navegar:**\n" +
                "- Use o menu lateral para acessar cada módulo\n" +
                "- O Dashboard mostra um resumo geral da sua operação\n" +
                "- Em Configurações você gerencia as preferências do seu tenant\n\n" +
                "Explore os artigos desta base de conhecimento para aprender a usar cada funcionalidade.",
                "Visão geral do portal e como começar a usar o sistema.");

        ensureArticle("Como gerenciar usuários", "Usuários",
                "O módulo de Usuários permite que você crie e gerencie os membros da sua equipe.\n\n" +
                "**Perfis disponíveis:**\n" +
                "- **ADMIN** — acesso total ao portal, pode criar e editar tudo\n" +
                "- **USER** — acesso conforme as permissões configuradas pelo administrador da plataforma\n\n" +
                "**Como criar um usuário:**\n" +
                "1. Acesse o menu Usuários\n" +
                "2. Clique em Novo Usuário\n" +
                "3. Preencha nome, email, senha e perfil\n" +
                "4. Salve\n\n" +
                "O novo usuário já pode fazer login imediatamente com as credenciais cadastradas.",
                "Aprenda a criar e gerenciar usuários da sua equipe.");

        ensureArticle("Como criar uma Ordem de Serviço", "Pedidos",
                "O módulo de Pedidos gerencia suas Ordens de Serviço (OS).\n\n" +
                "**Ciclo de vida de uma OS:**\n" +
                "ABERTA → EM_ANDAMENTO → AGUARDANDO_CLIENTE → CONCLUIDA\n\n" +
                "**Como criar uma OS:**\n" +
                "1. Acesse o menu Pedidos\n" +
                "2. Clique em Nova OS\n" +
                "3. Preencha o título, descrição e dados do cliente\n" +
                "4. Defina o status inicial, valor e data prevista\n" +
                "5. Atribua um responsável (opcional)\n" +
                "6. Salve\n\n" +
                "**Dicas:**\n" +
                "- O número da OS é gerado automaticamente (OS-0001, OS-0002...)\n" +
                "- Você pode atualizar o status diretamente na página de detalhe da OS\n" +
                "- Use os filtros na listagem para encontrar OS por título, cliente ou status.",
                "Aprenda a criar e acompanhar ordens de serviço.");

        ensureArticle("Entendendo os níveis de acesso", "Configurações",
                "O sistema possui um controle granular de permissões por módulo.\n\n" +
                "**Níveis de acesso:**\n" +
                "- **NONE** — o usuário não vê o módulo na sidebar\n" +
                "- **READ** — o usuário pode visualizar, mas não criar ou editar\n" +
                "- **WRITE** — o usuário pode visualizar, criar e editar\n" +
                "- **FULL** — acesso completo, incluindo exclusão\n\n" +
                "**Quem configura as permissões?**\n" +
                "O administrador da plataforma (SUPER_ADMIN) define os níveis de acesso para cada módulo e role " +
                "na matriz de permissões do seu tenant.\n\n" +
                "Se você precisar ajustar as permissões da sua equipe, entre em contato com o suporte.",
                "Entenda como funcionam os níveis de acesso no sistema.");

        ensureArticle("Precisa de ajuda?", "Suporte",
                "Se você tiver dúvidas ou encontrar algum problema, entre em contato com o suporte.\n\n" +
                "**Canais de atendimento:**\n" +
                "- Email: suporte@erpcorporativo.com\n" +
                "- Horário: Segunda a Sexta, 9h às 18h\n\n" +
                "**Antes de contatar o suporte:**\n" +
                "- Verifique se a dúvida está respondida nos artigos desta base de conhecimento\n" +
                "- Anote o que você estava fazendo quando o problema ocorreu\n" +
                "- Se possível, tire um print da tela com o erro\n\n" +
                "Estamos aqui para ajudar!",
                "Como entrar em contato com o suporte.");

        ensureArticle("Central de chamados do suporte", "Suporte",
                "O módulo de Suporte permite registrar chamados para atendimento da sua operação.\n\n" +
                "**Fluxo sugerido:**\n" +
                "1. Abra um novo chamado com assunto claro e detalhes do problema\n" +
                "2. Escolha a prioridade e categoria corretas\n" +
                "3. Vincule o cliente afetado quando aplicável\n" +
                "4. Acompanhe os comentários e atualizações na timeline\n\n" +
                "**Boas práticas:**\n" +
                "- Anexe evidências (prints, documentos ou logs)\n" +
                "- Use comentários internos para alinhamento da equipe\n" +
                "- Mantenha o cliente informado com mensagens públicas\n\n" +
                "Isso melhora o tempo de resposta e o cumprimento de SLA.",
                "Aprenda como operar a central de chamados do módulo de suporte.");
    }

    private void ensureArticle(String titulo, String categoria, String conteudo, String resumo) {
        boolean exists = knowledgeArticleRepository.findAllCategorias().stream()
                .anyMatch(c -> c.equalsIgnoreCase(categoria))
                && knowledgeArticleRepository.findAllAdmin(categoria, titulo, org.springframework.data.domain.PageRequest.of(0, 1))
                .getTotalElements() > 0;
        if (exists) return;

        knowledgeArticleRepository.save(KnowledgeArticle.builder()
                .titulo(titulo)
                .categoria(categoria)
                .conteudo(conteudo)
                .resumo(resumo)
                .visibilidade(ArticleVisibility.PUBLICO)
                .publicado(true)
                .build());
    }
}

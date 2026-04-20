package com.jaasielsilva.erpcorporativo.app.config;

import java.util.Optional;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;

import com.jaasielsilva.erpcorporativo.app.config.properties.AppBootstrapProperties;
import com.jaasielsilva.erpcorporativo.app.model.ArticleVisibility;
import com.jaasielsilva.erpcorporativo.app.model.KnowledgeArticle;
import com.jaasielsilva.erpcorporativo.app.model.PlatformModule;
import com.jaasielsilva.erpcorporativo.app.model.Role;
import com.jaasielsilva.erpcorporativo.app.model.Tenant;
import com.jaasielsilva.erpcorporativo.app.model.Usuario;
import com.jaasielsilva.erpcorporativo.app.repository.knowledge.KnowledgeArticleRepository;
import com.jaasielsilva.erpcorporativo.app.repository.module.PlatformModuleRepository;
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

    @Override
    public void run(String... args) {
        Tenant platformTenant = ensurePlatformTenant();
        ensureSuperAdmin(platformTenant);
        platformSettingService.ensureDefaults();
        ensureDefaultModules();
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

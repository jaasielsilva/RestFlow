package com.jaasielsilva.erpcorporativo.app.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.bootstrap")
public class AppBootstrapProperties {

    private final Tenant tenant = new Tenant();
    private final SuperAdmin superAdmin = new SuperAdmin();

    @Getter
    @Setter
    public static class Tenant {
        private String nome = "Plataforma SaaS";
        private String slug = "platform";
    }

    @Getter
    @Setter
    public static class SuperAdmin {
        private String nome = "Super Admin";
        private String email = "admin@erpcorporativo.com";
        private String password = "123456";
    }
}

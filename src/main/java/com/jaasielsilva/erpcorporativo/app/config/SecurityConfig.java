package com.jaasielsilva.erpcorporativo.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.jaasielsilva.erpcorporativo.app.security.ApiAccessDeniedHandler;
import com.jaasielsilva.erpcorporativo.app.security.ApiAuthenticationEntryPoint;
import com.jaasielsilva.erpcorporativo.app.security.CustomAuthenticationFailureHandler;
import com.jaasielsilva.erpcorporativo.app.security.CustomAuthenticationSuccessHandler;
import com.jaasielsilva.erpcorporativo.app.security.LoginThrottleFilter;
import com.jaasielsilva.erpcorporativo.app.tenant.TenantRequestFilter;

@Configuration
public class SecurityConfig {

    private final TenantRequestFilter tenantRequestFilter;
    private final LoginThrottleFilter loginThrottleFilter;
    private final CustomAuthenticationSuccessHandler authenticationSuccessHandler;
    private final CustomAuthenticationFailureHandler authenticationFailureHandler;
    private final ApiAuthenticationEntryPoint apiAuthenticationEntryPoint;
    private final ApiAccessDeniedHandler apiAccessDeniedHandler;

    public SecurityConfig(
            TenantRequestFilter tenantRequestFilter,
            LoginThrottleFilter loginThrottleFilter,
            CustomAuthenticationSuccessHandler authenticationSuccessHandler,
            CustomAuthenticationFailureHandler authenticationFailureHandler,
            ApiAuthenticationEntryPoint apiAuthenticationEntryPoint,
            ApiAccessDeniedHandler apiAccessDeniedHandler
    ) {
        this.tenantRequestFilter = tenantRequestFilter;
        this.loginThrottleFilter = loginThrottleFilter;
        this.authenticationSuccessHandler = authenticationSuccessHandler;
        this.authenticationFailureHandler = authenticationFailureHandler;
        this.apiAuthenticationEntryPoint = apiAuthenticationEntryPoint;
        this.apiAccessDeniedHandler = apiAccessDeniedHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/",
                    "/login",
                    "/recuperar-senha",
                    "/webjars/**",
                    "/css/**",
                    "/js/**",
                    "/img/**",
                    "/favicon.ico",
                    "/api/v1/system/health"
                ).permitAll()
                .requestMatchers("/home").hasRole("SUPER_ADMIN")
                .requestMatchers("/app/conhecimento", "/app/conhecimento/**").hasAnyRole("ADMIN", "USER")
                .requestMatchers("/app/**").hasAnyRole("ADMIN", "USER")
                .requestMatchers("/admin/**").hasRole("SUPER_ADMIN")
                .requestMatchers("/api/v1/admin/**").hasRole("SUPER_ADMIN")
                .requestMatchers("/api/v1/tenant-admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .successHandler(authenticationSuccessHandler)
                .failureHandler(authenticationFailureHandler)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .sessionManagement(session -> session
                .sessionFixation(sessionFixation -> sessionFixation.migrateSession())
            )
            .exceptionHandling(exception -> exception
                .defaultAuthenticationEntryPointFor(
                        apiAuthenticationEntryPoint,
                        new AntPathRequestMatcher("/api/**")
                )
                .defaultAuthenticationEntryPointFor(
                        new LoginUrlAuthenticationEntryPoint("/login"),
                        new AntPathRequestMatcher("/**")
                )
                .defaultAccessDeniedHandlerFor(
                        apiAccessDeniedHandler,
                        new AntPathRequestMatcher("/api/**")
                )
            )
            .headers(headers -> headers
                .contentSecurityPolicy(csp -> csp.policyDirectives(
                        "default-src 'self'; " +
                        "connect-src 'self' https://cdn.jsdelivr.net https://cdnjs.cloudflare.com https://viacep.com.br; " +
                        "script-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net; " +
                        "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com https://cdnjs.cloudflare.com https://cdn.jsdelivr.net; " +
                        "font-src 'self' https://fonts.gstatic.com https://cdnjs.cloudflare.com; " +
                        "img-src 'self' data:; " +
                        "object-src 'none'; " +
                        "frame-ancestors 'none'; " +
                        "base-uri 'self'; " +
                        "form-action 'self'"
                ))
                .referrerPolicy(referrer -> referrer.policy(ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
            )
            .addFilterBefore(tenantRequestFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(loginThrottleFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

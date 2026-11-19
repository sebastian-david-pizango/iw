package com.collectiverse.collectiverse.security;

import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Seguridad de Collectiverse con DOS cadenas de filtros:
 *  1) /api/**  -> API REST sin estado (HTTP Basic). Es la configuracion que ya existia.
 *  2) el resto -> paginas web con formulario de login, sesion, CSRF y logout.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // ---------- Componentes reutilizados (equivalen a User / authenticate() de Django) ----------

    @Bean
    public UserDetailsManager userDetailsManager(DataSource dataSource) {
        return new JdbcUserDetailsManager(dataSource);   // usa las tablas users/authorities
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();               // hash con sal; nunca texto plano
    }

    // ---------- Cadena 1: API REST (sin cambios de comportamiento) ----------

    @Bean
    @Order(1)
    public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/api/**")
            .csrf(csrf -> csrf.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/api/auth/register").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/me").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/**").permitAll()
                .anyRequest().authenticated())
            // Responde 401 sin el header que hace aparecer el popup del navegador
            .httpBasic(basic -> basic.authenticationEntryPoint(
                (request, response, ex) -> response.sendError(401, "No autenticado")));
        return http.build();
    }

    // ---------- Cadena 2: paginas web con login por formulario y sesion ----------

    @Bean
    @Order(2)
    public SecurityFilterChain webFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // paginas publicas (equivalente a una vista sin @login_required)
                .requestMatchers("/", "/login", "/registro", "/css/**", "/error").permitAll()
                // solo administradores (equivalente a permisos/grupos de Django)
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // permisos: cada uno abre su propia pagina
                .requestMatchers("/vender/**").hasAnyRole("VENDEDOR", "ADMIN")
                .requestMatchers("/calificar/**").hasAnyRole("CALIFICADOR", "ADMIN")
                // cualquier otra pagina exige sesion (equivalente a @login_required)
                .anyRequest().authenticated())
            // equivalente a LoginView + LOGIN_URL + LOGIN_REDIRECT_URL
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/bienvenida", true)   // tras iniciar sesion siempre va a "Hola, ingresaste"
                .failureUrl("/login?error")
                .permitAll())
            // equivalente a LogoutView + LOGOUT_REDIRECT_URL (POST protegido con CSRF)
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll());
        // CSRF queda activado por defecto en esta cadena
        return http.build();
    }
}
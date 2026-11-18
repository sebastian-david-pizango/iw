package com.collectiverse.collectiverse;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/** Pruebas de la Actividad 4: acceso sin sesion, credenciales malas/buenas, logout y roles. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AutenticacionWebTests {

    private static final String PASS_EST = "Estudiante123!";
    private static final String PASS_ADMIN = "Admin12345!";

    @Autowired MockMvc mvc;
    @Autowired UserDetailsManager users;
    @Autowired PasswordEncoder encoder;
    @Autowired JdbcTemplate jdbc;

    @BeforeEach
    void crearUsuarios() {
        if (!users.userExists("estudiante1")) {
            users.createUser(User.withUsername("estudiante1")
                    .password(encoder.encode(PASS_EST)).roles("USER").build());
        }
        if (!users.userExists("admin_test")) {
            users.createUser(User.withUsername("admin_test")
                    .password(encoder.encode(PASS_ADMIN)).roles("ADMIN", "USER").build());
        }
    }

    // 1. Pagina publica accesible sin sesion
    @Test
    void paginaPublicaEsAccesible() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk());
        mvc.perform(get("/login")).andExpect(status().isOk());
    }

    // 2. Entrar al panel privado sin autenticarse -> redirige al login
    @Test
    void panelPrivadoSinSesionRedirigeAlLogin() throws Exception {
        mvc.perform(get("/privado"))
           .andExpect(status().isFound())
           .andExpect(redirectedUrlPattern("**/login"));
    }

    // 3. Credenciales incorrectas -> vuelve al login con error y sin sesion
    @Test
    void credencialesIncorrectasSonRechazadas() throws Exception {
        mvc.perform(formLogin("/login").user("estudiante1").password("incorrecta"))
           .andExpect(status().isFound())
           .andExpect(redirectedUrl("/login?error"))
           .andExpect(unauthenticated());
    }

    // 4-7. Login valido, acceso al panel, logout y nuevo intento de acceso
    @Test
    void flujoCompletoLoginPanelLogout() throws Exception {
        MockHttpSession session = (MockHttpSession) mvc
                .perform(formLogin("/login").user("estudiante1").password(PASS_EST))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/privado"))
                .andExpect(authenticated().withUsername("estudiante1"))
                .andReturn().getRequest().getSession(false);
        assertNotNull(session);

        // con sesion valida puede ver el panel
        mvc.perform(get("/privado").session(session))
           .andExpect(status().isOk());

        // cierra sesion (POST + CSRF)
        mvc.perform(post("/logout").with(csrf()).session(session))
           .andExpect(status().isFound())
           .andExpect(redirectedUrl("/login?logout"));

        // la sesion anterior ya no sirve: vuelve a pedir login
        mvc.perform(get("/privado").session(session))
           .andExpect(status().isFound())
           .andExpect(redirectedUrlPattern("**/login"));
    }

    // Logout por GET no esta permitido (solo POST con CSRF)
    @Test
    void logoutSinCsrfEsRechazado() throws Exception {
        mvc.perform(post("/logout"))
           .andExpect(status().isForbidden());
    }

    // Roles: un USER no entra al panel de administracion; un ADMIN si
    @Test
    @WithMockUser(username = "estudiante1", roles = "USER")
    void usuarioNormalNoEntraAAdmin() throws Exception {
        mvc.perform(get("/admin")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin_test", roles = {"ADMIN", "USER"})
    void adminEntraAAdmin() throws Exception {
        mvc.perform(get("/admin")).andExpect(status().isOk());
    }

    // Personalizacion: registro con perfil; la contrasena se guarda con hash
    @Test
    void registroGuardaHashYPerfil() throws Exception {
        mvc.perform(post("/registro").with(csrf())
                .param("username", "nuevo_user")
                .param("fullName", "Nuevo Usuario")
                .param("email", "nuevo@example.com")
                .param("password", "ClaveSegura1!"))
           .andExpect(status().isFound())
           .andExpect(redirectedUrl("/login?registrado"));

        String guardada = jdbc.queryForObject(
                "select password from users where username = ?", String.class, "nuevo_user");
        assertNotEquals("ClaveSegura1!", guardada);
        assertTrue(guardada.startsWith("$2"), "debe ser un hash BCrypt");

        // y ese usuario ya puede iniciar sesion
        mvc.perform(formLogin("/login").user("nuevo_user").password("ClaveSegura1!"))
           .andExpect(authenticated().withUsername("nuevo_user"));
    }

    @Test
    void registroConUsuarioRepetidoMuestraError() throws Exception {
        mvc.perform(post("/registro").with(csrf())
                .param("username", "estudiante1")
                .param("fullName", "Otro")
                .param("email", "otro@example.com")
                .param("password", "ClaveSegura1!"))
           .andExpect(status().isOk())          // vuelve al formulario, no redirige
           .andExpect(view().name("registro"));
    }

    // La API REST con HTTP Basic sigue funcionando igual que antes
    @Test
    void apiSigueConHttpBasic() throws Exception {
        mvc.perform(get("/api/me")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/me").with(httpBasic("estudiante1", PASS_EST)))
           .andExpect(status().isOk());
        mvc.perform(get("/api/admin/ping").with(httpBasic("estudiante1", PASS_EST)))
           .andExpect(status().isForbidden());
    }
}

package com.collectiverse.collectiverse.security.registro;

import java.util.Set;

/** Fila de la tabla de usuarios del panel de administracion. */
public class UsuarioAdmin {

    private final String username;
    private final Set<String> roles;   // sin el prefijo ROLE_

    public UsuarioAdmin(String username, Set<String> roles) {
        this.username = username;
        this.roles = roles;
    }

    public String getUsername() { return username; }
    public Set<String> getRoles() { return roles; }
}
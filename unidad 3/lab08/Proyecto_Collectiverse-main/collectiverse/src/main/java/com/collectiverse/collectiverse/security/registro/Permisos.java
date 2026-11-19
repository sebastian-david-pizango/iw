package com.collectiverse.collectiverse.security.registro;

import java.util.List;

/** Permisos (roles) que existen en Collectiverse. */
public final class Permisos {

    private Permisos() { }

    /** Base: todo usuario registrado lo tiene siempre. */
    public static final String USER = "USER";

    /** Los que cualquier persona puede elegir al registrarse (no son privilegiados). */
    public static final List<String> AUTOASIGNABLES = List.of("VENDEDOR", "CALIFICADOR");

    /** Los que un administrador puede asignar desde /admin. */
    public static final List<String> ASIGNABLES_POR_ADMIN = List.of("ADMIN", "VENDEDOR", "CALIFICADOR");
}
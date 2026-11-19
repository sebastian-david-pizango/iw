package com.collectiverse.collectiverse.security.registro;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Lista usuarios y edita sus permisos (tabla authorities). Solo se usa desde /admin. */
@Service
public class PermisosAdminService {

    private final JdbcTemplate jdbc;

    public PermisosAdminService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional(readOnly = true)
    public List<UsuarioAdmin> listar() {
        Map<String, Set<String>> mapa = new LinkedHashMap<>();
        jdbc.query("select username from users order by username",
                rs -> { mapa.put(rs.getString(1), new LinkedHashSet<>()); });
        jdbc.query("select username, authority from authorities order by authority",
                rs -> {
                    Set<String> roles = mapa.get(rs.getString(1));
                    if (roles != null) roles.add(rs.getString(2).replaceFirst("^ROLE_", ""));
                });
        List<UsuarioAdmin> lista = new ArrayList<>();
        mapa.forEach((u, r) -> lista.add(new UsuarioAdmin(u, r)));
        return lista;
    }

    /**
     * Reemplaza los permisos del usuario. USER se conserva siempre y solo se aceptan
     * permisos de la lista permitida. Devuelve false si el usuario no existe.
     */
    @Transactional
    public boolean actualizar(String username, List<String> pedidos) {
        Integer existe = jdbc.queryForObject("select count(*) from users where username = ?",
                Integer.class, username);
        if (existe == null || existe == 0) return false;

        Set<String> finales = new LinkedHashSet<>();
        finales.add(Permisos.USER);
        if (pedidos != null) {
            for (String p : pedidos) {
                if (Permisos.ASIGNABLES_POR_ADMIN.contains(p)) finales.add(p);
            }
        }
        jdbc.update("delete from authorities where username = ?", username);
        for (String rol : finales) {
            jdbc.update("insert into authorities (username, authority) values (?, ?)",
                    username, "ROLE_" + rol);
        }
        return true;
    }
}
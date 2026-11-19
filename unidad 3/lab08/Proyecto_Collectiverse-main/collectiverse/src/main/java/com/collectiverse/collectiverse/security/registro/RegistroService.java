package com.collectiverse.collectiverse.security.registro;

import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.collectiverse.collectiverse.cu.shared.exception.ConflictException;
import com.collectiverse.collectiverse.dominio.entity.UserProfile;
import com.collectiverse.collectiverse.dominio.repository.RepoUserProfile;

/**
 * Registro de usuarios: crea la cuenta con el UserDetailsManager de Spring Security
 * (equivalente a User.objects.create_user de Django) y guarda el perfil adicional.
 */
@Service
public class RegistroService {

    private final UserDetailsManager users;
    private final PasswordEncoder encoder;
    private final RepoUserProfile profiles;

    public RegistroService(UserDetailsManager users, PasswordEncoder encoder, RepoUserProfile profiles) {
        this.users = users;
        this.encoder = encoder;
        this.profiles = profiles;
    }

    @Transactional
    public void registrar(RegistroForm form, boolean quienRegistraEsAdmin) {
        String username = form.getUsername().trim();
        String email = form.getEmail().trim().toLowerCase();

        if (users.userExists(username)) {
            throw new ConflictException("El nombre de usuario ya esta en uso");
        }
        if (profiles.existsByEmail(email)) {
            throw new ConflictException("Ese correo ya esta registrado");
        }

        // Validacion en el SERVIDOR: el formulario no es de fiar (se puede alterar el HTML).
        boolean pideAdmin = "ADMIN".equals(form.getRole());
        if (pideAdmin && !quienRegistraEsAdmin) {
            throw new ConflictException("Solo un administrador puede crear usuarios con rol ADMIN");
        }
        // USER siempre; ADMIN solo si lo pide un admin; y los permisos autoasignables elegidos.
        Set<String> rolesFinales = new LinkedHashSet<>();
        rolesFinales.add(Permisos.USER);
        if (pideAdmin) rolesFinales.add("ADMIN");
        if (form.getPermisos() != null) {
            for (String perm : form.getPermisos()) {
                if (Permisos.AUTOASIGNABLES.contains(perm)) rolesFinales.add(perm);
            }
        }
        String[] roles = rolesFinales.toArray(new String[0]);

        users.createUser(User.withUsername(username)
                .password(encoder.encode(form.getPassword()))   // se guarda el hash BCrypt
                .roles(roles)
                .build());

        profiles.save(new UserProfile(username, email, form.getFullName().trim()));
    }
}
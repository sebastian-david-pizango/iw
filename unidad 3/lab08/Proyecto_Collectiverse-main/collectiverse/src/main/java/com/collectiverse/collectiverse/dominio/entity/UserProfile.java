package com.collectiverse.collectiverse.dominio.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

/**
 * Datos adicionales del usuario (personalizacion del componente de autenticacion).
 * La tabla "users" de Spring Security solo guarda username/password/enabled,
 * por eso el correo y el nombre completo viven aqui, enlazados por username.
 */
@Entity
@Table(name = "user_profile")
public class UserProfile {

    @Id
    @Column(name = "username", length = 50)
    private String username;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @CreationTimestamp
    @Column(name = "created", updatable = false, nullable = false)
    private LocalDateTime created;

    public UserProfile() { }

    public UserProfile(String username, String email, String fullName) {
        this.username = username;
        this.email = email;
        this.fullName = fullName;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public LocalDateTime getCreated() { return created; }
    public void setCreated(LocalDateTime created) { this.created = created; }
}

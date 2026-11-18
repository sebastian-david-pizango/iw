package com.collectiverse.collectiverse.security.registro;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Datos del formulario de registro (clase con getters/setters para poder usar th:field). */
public class RegistroForm {

    @NotBlank(message = "El usuario es obligatorio")
    @Pattern(regexp = "^[A-Za-z0-9_.-]{3,50}$",
             message = "Usuario: 3 a 50 caracteres (letras, numeros, . _ -)")
    private String username;

    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(max = 100, message = "El nombre completo admite maximo 100 caracteres")
    private String fullName;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato valido")
    @Size(max = 100, message = "El correo admite maximo 100 caracteres")
    private String email;

    @NotBlank(message = "La contrasena es obligatoria")
    @Size(min = 8, max = 72, message = "La contrasena debe tener entre 8 y 72 caracteres")
    private String password;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}

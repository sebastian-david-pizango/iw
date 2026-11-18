package com.collectiverse.collectiverse.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.collectiverse.collectiverse.cu.shared.exception.ConflictException;
import com.collectiverse.collectiverse.dominio.repository.RepoUserProfile;
import com.collectiverse.collectiverse.security.registro.RegistroForm;
import com.collectiverse.collectiverse.security.registro.RegistroService;

import jakarta.validation.Valid;

/** Paginas HTML: publica, login, registro, panel privado y panel de administracion. */
@Controller
public class PaginasController {

    private final RegistroService registroService;
    private final RepoUserProfile profiles;

    public PaginasController(RegistroService registroService, RepoUserProfile profiles) {
        this.registroService = registroService;
        this.profiles = profiles;
    }

    // Pagina publica
    @GetMapping("/")
    public String inicio() {
        return "inicio";
    }

    // Formulario de login (el POST /login lo procesa Spring Security, no este controlador)
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/registro")
    public String registroForm(Model model) {
        model.addAttribute("registro", new RegistroForm());
        return "registro";
    }

    @PostMapping("/registro")
    public String registrar(@Valid @ModelAttribute("registro") RegistroForm form, BindingResult result) {
        if (!result.hasErrors()) {
            try {
                registroService.registrar(form);
                return "redirect:/login?registrado";
            } catch (ConflictException e) {
                result.reject("duplicado", e.getMessage());
            }
        }
        return "registro";
    }

    // Pagina privada: la cadena de seguridad exige sesion iniciada
    @GetMapping("/privado")
    public String privado(Authentication auth, Model model) {
        model.addAttribute("username", auth.getName());
        model.addAttribute("roles", auth.getAuthorities());
        model.addAttribute("perfil", profiles.findById(auth.getName()).orElse(null));
        return "privado";
    }

    // Solo ADMIN (la regla esta en SecurityConfig)
    @GetMapping("/admin")
    public String admin(Authentication auth, Model model) {
        model.addAttribute("username", auth.getName());
        return "admin";
    }
}

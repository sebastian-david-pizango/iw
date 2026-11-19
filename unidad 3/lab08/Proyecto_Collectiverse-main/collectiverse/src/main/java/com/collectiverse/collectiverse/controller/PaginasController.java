package com.collectiverse.collectiverse.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.collectiverse.collectiverse.cu.shared.exception.ConflictException;
import com.collectiverse.collectiverse.dominio.repository.RepoUserProfile;
import java.util.List;

import com.collectiverse.collectiverse.security.registro.Permisos;
import com.collectiverse.collectiverse.security.registro.PermisosAdminService;
import com.collectiverse.collectiverse.security.registro.RegistroForm;
import com.collectiverse.collectiverse.security.registro.RegistroService;

import jakarta.validation.Valid;

/** Paginas HTML: publica, login, registro, panel privado y panel de administracion. */
@Controller
public class PaginasController {

    private final RegistroService registroService;
    private final RepoUserProfile profiles;
    private final PermisosAdminService permisosAdmin;

    public PaginasController(RegistroService registroService, RepoUserProfile profiles,
                             PermisosAdminService permisosAdmin) {
        this.registroService = registroService;
        this.profiles = profiles;
        this.permisosAdmin = permisosAdmin;
    }

    // Permisos que se ofrecen en el formulario de registro
    @ModelAttribute("permisosDisponibles")
    public List<String> permisosDisponibles() {
        return Permisos.AUTOASIGNABLES;
    }

    // Disponible en las vistas: solo un ADMIN autenticado ve la opcion de crear otro ADMIN
    @ModelAttribute("puedeElegirAdmin")
    public boolean puedeElegirAdmin(Authentication auth) {
        return esAdmin(auth);
    }

    private static boolean esAdmin(Authentication auth) {
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
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
    public String registrar(@Valid @ModelAttribute("registro") RegistroForm form,
                            BindingResult result, Authentication auth) {
        if (!result.hasErrors()) {
            try {
                registroService.registrar(form, esAdmin(auth));
                return "redirect:/login?registrado";   // vuelve a iniciar sesion
            } catch (ConflictException e) {
                result.reject("duplicado", e.getMessage());
            }
        }
        return "registro";
    }

    // Pagina a la que se llega justo despues de iniciar sesion
    @GetMapping("/bienvenida")
    public String bienvenida(Authentication auth, Model model) {
        model.addAttribute("username", auth.getName());
        return "bienvenida";
    }

    // Pagina del permiso VENDEDOR (la regla esta en SecurityConfig)
    @GetMapping("/vender")
    public String vender() {
        return "vender";
    }

    // Pagina del permiso CALIFICADOR (la regla esta en SecurityConfig)
    @GetMapping("/calificar")
    public String calificar() {
        return "calificar";
    }

    // Pagina privada: la cadena de seguridad exige sesion iniciada
    @GetMapping("/privado")
    public String privado(Authentication auth, Model model) {
        model.addAttribute("username", auth.getName());
        model.addAttribute("roles", auth.getAuthorities());
        model.addAttribute("perfil", profiles.findById(auth.getName()).orElse(null));
        return "privado";
    }

    // Solo ADMIN (la regla esta en SecurityConfig): lista usuarios y permite editar sus permisos
    @GetMapping("/admin")
    public String admin(Authentication auth, Model model) {
        model.addAttribute("username", auth.getName());
        model.addAttribute("usuarios", permisosAdmin.listar());
        model.addAttribute("asignables", Permisos.ASIGNABLES_POR_ADMIN);
        return "admin";
    }

    @PostMapping("/admin/usuarios/{username}/permisos")
    public String editarPermisos(@PathVariable String username,
                                 @RequestParam(name = "roles", required = false) List<String> roles,
                                 Authentication auth, RedirectAttributes flash) {
        List<String> pedidos = roles == null ? new java.util.ArrayList<>() : new java.util.ArrayList<>(roles);
        // Un admin no puede quitarse a si mismo el rol ADMIN (evita quedarse sin acceso)
        if (username.equals(auth.getName()) && !pedidos.contains("ADMIN")) {
            pedidos.add("ADMIN");
            flash.addFlashAttribute("aviso", "No puedes quitarte tu propio rol ADMIN.");
        }
        if (permisosAdmin.actualizar(username, pedidos)) {
            flash.addFlashAttribute("mensaje", "Permisos de " + username + " actualizados.");
        } else {
            flash.addFlashAttribute("aviso", "El usuario no existe.");
        }
        return "redirect:/admin";
    }
}
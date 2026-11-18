# Reutilización del componente de autenticación — Spring Security (Collectiverse)

Esta actividad pide reutilizar el componente de autenticación del framework. El enunciado usa Django (`django.contrib.auth`); Collectiverse usa **Java + Spring Boot**, así que el componente reutilizado es **Spring Security** (`spring-boot-starter-security`).

## 1. Equivalencias Django → Spring Security

| Django | Spring Security (en este proyecto) |
|---|---|
| `django.contrib.auth` | `spring-boot-starter-security` |
| Modelo `User` | `UserDetails` / `User` guardado por `JdbcUserDetailsManager` (tablas `users`, `authorities`) |
| `create_user()` | `UserDetailsManager.createUser()` con `PasswordEncoder` |
| `authenticate()` | `AuthenticationManager` + `DaoAuthenticationProvider` (se configuran solos al existir un `UserDetailsManager` y un `PasswordEncoder`) |
| `LoginView` + `LOGIN_URL` | `.formLogin()` + `UsernamePasswordAuthenticationFilter` |
| `LogoutView` | `.logout()` + `LogoutFilter` |
| `@login_required` | `.anyRequest().authenticated()` (o `@PreAuthorize`) |
| `LOGIN_REDIRECT_URL` / `?next=` | `.defaultSuccessUrl("/privado")` (vuelve a la página pedida si existía) |
| `Permission` / `Group` | Roles y *authorities* (`hasRole("ADMIN")`); grupos opcionales en `JdbcUserDetailsManager` |
| `AbstractUser` | Tabla propia enlazada por `username` (`user_profile`) o `UserDetails` propio |
| Middleware de sesión + CSRF | `SecurityContextHolderFilter`, `HttpSession`, `CsrfFilter` |
| Hash de contraseñas (PBKDF2) | `BCryptPasswordEncoder` |

## 2. Qué se implementó

**Archivos nuevos o modificados** (carpeta `collectiverse/`):

| Archivo | Función |
|---|---|
| `security/SecurityConfig.java` | Ahora define **dos cadenas de filtros**: `/api/**` (HTTP Basic sin estado, igual que antes) y el resto (login por formulario, sesión, CSRF, logout) |
| `controller/PaginasController.java` | Páginas `/`, `/login`, `/registro`, `/privado`, `/admin` |
| `security/registro/RegistroService.java` y `RegistroForm.java` | Registro: crea el usuario con `UserDetailsManager` y guarda el perfil |
| `dominio/entity/UserProfile.java`, `dominio/repository/RepoUserProfile.java` | Perfil adicional (nombre completo, correo) |
| `resources/templates/*.html` | `inicio`, `login`, `registro`, `privado`, `admin` (Thymeleaf) |
| `resources/application.properties` | Credenciales por variables de entorno; cookie de sesión `HttpOnly`, `SameSite=Lax`, 30 min |
| `docs/seguridad-schema.sql` | Crea `users`, `authorities` y `user_profile` en PostgreSQL |
| `src/test/.../AutenticacionWebTests.java` | Pruebas automáticas de la Actividad 4 (H2 en memoria) |

**Lo que se reutiliza y lo que se programa**

- Reutilizado de Spring Security: validación de credenciales, hash BCrypt, manejo de sesión, filtro de login, filtro de logout, protección CSRF, redirección de usuarios no autenticados, control de roles.
- Programado por nosotros: las plantillas HTML, la regla de qué URL es pública/privada/solo-admin, el formulario y servicio de registro y la entidad de perfil. No se escribió ninguna función que compare contraseñas ni que gestione la sesión.

**Cómo cumple los requisitos de la Actividad 2**

| Requisito | Dónde |
|---|---|
| Página pública | `/` |
| Página de login | `/login` |
| Página privada | `/privado` |
| Cerrar sesión | Botón en `/privado` → `POST /logout` |
| Al menos dos usuarios | `admin` (lo crea `AdminInicial` con `ADMIN_PASSWORD`) y cualquiera creado en `/registro` |
| Protección de páginas privadas | `anyRequest().authenticated()` y `/admin/**` con `hasRole("ADMIN")` |

## 3. Actividad 1 — Comprender el componente

**¿Qué es Spring Security?** El módulo de Spring que resuelve autenticación y autorización. Funciona como una cadena de filtros que se ejecuta antes de los controladores.

**¿Qué funcionalidades proporciona?** Autenticación (formulario, HTTP Basic, OAuth2, etc.), almacenamiento de usuarios (`UserDetailsService`, `JdbcUserDetailsManager`), codificación de contraseñas (`PasswordEncoder`), sesiones, protección CSRF, autorización por URL, rol o método, cierre de sesión y cabeceras de seguridad.

**Autenticación vs. autorización.** Autenticación responde *¿quién eres?* (usuario y contraseña correctos). Autorización responde *¿qué puedes hacer?* (un `USER` entra a `/privado`, solo un `ADMIN` entra a `/admin`).

**`LoginView` ↔ `formLogin()`.** Al declarar `.formLogin()`, Spring registra un filtro que atiende `POST /login`, valida usuario y contraseña contra el `UserDetailsManager` y, si son correctos, guarda la autenticación en la sesión. Nosotros solo aportamos la página `login.html`.

**`LogoutView` ↔ `logout()`.** Atiende `POST /logout`, invalida la sesión, borra la cookie `JSESSIONID` y redirige a `/login?logout`. Exige token CSRF, por eso el botón está dentro de un formulario `POST`.

**`login_required` ↔ `authenticated()`.** La regla `anyRequest().authenticated()` hace que, si no hay usuario autenticado, Spring guarde la URL pedida y redirija a `/login`. Después del login vuelve a esa URL.

**Almacenamiento de contraseñas.** `BCryptPasswordEncoder` genera un hash con sal aleatoria y factor de costo (10 por defecto). En la tabla `users` queda un valor tipo `$2a$10$...` (60 caracteres); la contraseña original no se guarda y el hash no se puede invertir. Para validar, Spring vuelve a aplicar BCrypt a lo escrito y compara.

**Sesiones.** Tras un login correcto el servidor crea una `HttpSession` y envía la cookie `JSESSIONID`. En cada petición el navegador devuelve la cookie y Spring recupera el `SecurityContext` guardado en esa sesión. Al autenticar se cambia el ID de sesión (defensa contra *session fixation*). La API `/api/**` es la excepción: no usa sesión, cada petición lleva credenciales Basic.

## 4. Actividad 3 — Personalización implementada

1. **Información adicional del usuario** (equivalente a un perfil con `AbstractUser`): la tabla `users` de Spring solo tiene usuario, contraseña y `enabled`; se creó `user_profile` (nombre completo y correo único) enlazada por `username` con `ON DELETE CASCADE`. Se llena en `/registro` y se muestra en `/privado`.
2. **Formulario de login personalizado**: `login.html` reemplaza la página por defecto y muestra mensajes de error, de cierre de sesión y de registro exitoso.
3. **Roles y permisos**: `USER` y `ADMIN`; `/admin` solo para `ADMIN`. El registro público siempre crea `USER`; el `ADMIN` solo lo crea `AdminInicial`.
4. **Dos cadenas de seguridad**: la web con sesión y CSRF, y la API sin estado con Basic, sin romper lo que ya existía.

## 5. Actividad 4 — Pruebas de seguridad

Prerrequisito: BD preparada con `docs/seguridad-schema.sql`, variables de entorno definidas y `./gradlew bootRun` en marcha. Crear primero un usuario en `/registro` (por ejemplo `estudiante1`).

| # | Prueba | Resultado esperado | Prueba automática | Captura |
|---|---|---|---|---|
| 1 | Abrir `http://localhost:8080/privado` sin sesión | Redirige a `/login` | `panelPrivadoSinSesionRedirigeAlLogin` | *(adjuntar)* |
| 2 | Login con contraseña incorrecta | Vuelve a `/login?error` con mensaje de error | `credencialesIncorrectasSonRechazadas` | *(adjuntar)* |
| 3 | Login con usuario válido | Redirige a `/privado` | `flujoCompletoLoginPanelLogout` | *(adjuntar)* |
| 4 | Ver `/privado` con sesión | Muestra usuario, roles y perfil | `flujoCompletoLoginPanelLogout` | *(adjuntar)* |
| 5 | Pulsar «Cerrar sesión» | Redirige a `/login?logout` | `flujoCompletoLoginPanelLogout` | *(adjuntar)* |
| 6 | Volver a abrir `/privado` | Redirige a `/login` | `flujoCompletoLoginPanelLogout` | *(adjuntar)* |
| 7 | `POST /logout` sin token CSRF | 403 | `logoutSinCsrfEsRechazado` | *(adjuntar)* |
| 8 | Usuario normal abre `/admin` | 403 | `usuarioNormalNoEntraAAdmin` | *(adjuntar)* |
| 9 | `admin` abre `/admin` | 200 | `adminEntraAAdmin` | *(adjuntar)* |
| 10 | Registro: la contraseña se guarda con hash | `select password from users` muestra `$2a$...` | `registroGuardaHashYPerfil` | *(adjuntar)* |
| 11 | Registro con usuario repetido | Vuelve al formulario con error | `registroConUsuarioRepetidoMuestraError` | *(adjuntar)* |
| 12 | API: `GET /api/me` sin credenciales / con Basic | 401 / 200 | `apiSigueConHttpBasic` | *(adjuntar)* |

Comandos para la prueba 12:

```bash
curl -i http://localhost:8080/api/me
curl -i -u estudiante1:SuClave http://localhost:8080/api/me
```

Ejecución de las pruebas automáticas: `./gradlew test` (el reporte queda en `collectiverse/build/reports/tests/test/index.html`; sirve como evidencia).

## 6. Actividad 5 — Análisis crítico

**Ventajas de reutilizar el componente.** Menos código propio, mecanismos probados y revisados por una comunidad grande, correcciones de seguridad que llegan al actualizar la dependencia, y tiempo para dedicarlo a la lógica de Collectiverse (coleccionables, publicaciones, intercambios).

**Problemas de seguridad que evitamos.** Guardar contraseñas en texto plano o con hashes débiles sin sal; comparaciones de contraseña vulnerables a ataques de tiempo; sesiones predecibles o sin renovar al autenticar (*session fixation*); cierre de sesión que no invalida el servidor; falta de protección CSRF; rutas protegidas olvidadas por una validación manual.

**Funcionalidades adicionales posibles.** Recuperación y cambio de contraseña por correo, verificación de correo, bloqueo temporal tras intentos fallidos, autenticación en dos pasos, «recordarme», login con Google (OAuth2), grupos de permisos (`JdbcUserDetailsManager` los soporta), auditoría de accesos y JWT si hubiera un frontend separado.

**Cuándo personalizar.** Cuando se necesita iniciar sesión con correo, guardar datos propios del usuario, usar un proveedor externo (OAuth2/LDAP), definir roles más finos o cumplir políticas de contraseñas y bloqueo de cuentas.

**Responsabilidades que siguen siendo del desarrollador.**
- Servir todo por HTTPS y activar `server.servlet.session.cookie.secure=true` en producción.
- Definir bien qué rutas son públicas y cuáles no (un error en `SecurityConfig` deja datos expuestos).
- Política de contraseñas: Spring no trae validadores como los de Django; aquí solo se exige un largo mínimo de 8.
- Protección contra fuerza bruta: **no está implementada**.
- Proteger secretos (credenciales de la base de datos, `ADMIN_PASSWORD`).
- Validar y autorizar a nivel de datos: que un usuario solo modifique lo suyo.
- Mantener las dependencias actualizadas.

## 7. Hallazgos al revisar el proyecto original

1. **Credenciales de la base de datos en el repositorio.** `application.properties` del zip contenía la URL, usuario y contraseña de Neon en texto plano, aunque el README pide variables de entorno. Se corrigió el archivo, pero si el repositorio ya se subió a GitHub la clave sigue en el historial: **cambie la contraseña en Neon** y use las variables `DB_URL`, `DB_USER`, `DB_PASSWORD`.
2. **`GlobalExceptionHandler` captura `RuntimeException`.** `AccessDeniedException` también es una `RuntimeException`; si en el futuro se usa `@PreAuthorize`, ese manejador la convertiría en un 500 en lugar de un 403. Por eso aquí la autorización se hizo por URL en `SecurityConfig`. Si se quiere usar `@PreAuthorize`, hay que añadir un manejador que relance `AccessDeniedException`.
3. **Las tablas `users` y `authorities` no tenían script de creación en el repositorio.** Se añadió `docs/seguridad-schema.sql`.
4. **HTTP Basic en la API** envía el usuario y la contraseña en cada petición; solo es aceptable sobre HTTPS.
5. El usuario `admin` creado por `AdminInicial` no tiene fila en `user_profile`; `/privado` lo maneja mostrando «sin perfil adicional».

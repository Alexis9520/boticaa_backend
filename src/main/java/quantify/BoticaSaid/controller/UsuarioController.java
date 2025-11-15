package quantify.BoticaSaid.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import quantify.BoticaSaid.dto.auth.UsuarioDto;
import quantify.BoticaSaid.model.Usuario;
import quantify.BoticaSaid.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // 1. Listar todos los usuarios
    @GetMapping
    public List<Usuario> listarUsuarios() {
        return usuarioService.listarTodos();
    }

    // 2. Obtener usuario por ID
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> obtenerUsuarioPorId(@PathVariable Long id) {
        Usuario usuario = usuarioService.obtenerPorId(id);
        if (usuario != null) {
            return ResponseEntity.ok(usuario);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 2b. Obtener usuario por DNI
    @GetMapping("/dni/{dni}")
    public ResponseEntity<Usuario> obtenerUsuarioPorDni(@PathVariable String dni) {
        Usuario usuario = usuarioService.obtenerPorDni(dni);
        if (usuario != null) {
            return ResponseEntity.ok(usuario);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 3. Editar usuario por ID
    @PutMapping("/{id}")
    public ResponseEntity<Usuario> editarUsuario(
            @PathVariable Long id,
            @RequestBody Usuario usuarioActualizado
    ) {
        Usuario actualizado = usuarioService.actualizarUsuario(id, usuarioActualizado);
        if (actualizado != null) {
            return ResponseEntity.ok(actualizado);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 4. Eliminar usuario por ID
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarUsuario(@PathVariable Long id) {
        boolean eliminado = usuarioService.eliminarUsuario(id);
        if (eliminado) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }


    @GetMapping("/me")
    public ResponseEntity<UsuarioDto> obtenerMiUsuario(Authentication authentication) {
        String dni = authentication.getName();
        Usuario usuario = usuarioService.obtenerPorDni(dni);
        if (usuario != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            UsuarioDto usuarioDto = new UsuarioDto(
                    usuario.getDni(),
                    usuario.getNombreCompleto(),
                    usuario.getRol().toString(),
                    usuario.getHorarioEntrada() != null ? usuario.getHorarioEntrada().format(formatter) : null,
                    usuario.getHorarioSalida() != null ? usuario.getHorarioSalida().format(formatter) : null,
                    usuario.getTurno()
            );
            return ResponseEntity.ok(usuarioDto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/cambiar-contrasena")
    public ResponseEntity<?> cambiarContrasena(
            @PathVariable Long id,
            @RequestBody CambiarContrasenaRequest request,
            Authentication authentication
    ) {
        String dniActual = authentication.getName();
        Usuario usuarioActual = usuarioService.obtenerPorDni(dniActual);
        Usuario usuarioEditar = usuarioService.obtenerPorId(id);

        if (usuarioEditar == null) {
            return ResponseEntity.notFound().build();
        }

        boolean esAdmin = usuarioActual.getRol().toString().equalsIgnoreCase("ADMINISTRADOR");
        boolean esElMismo = usuarioEditar.getDni().equals(dniActual);

        if (!esAdmin && !esElMismo) {
            return ResponseEntity.status(403).body(java.util.Map.of("message", "No autorizado"));
        }

        // Validar contraseña actual si no es admin
        if (!esAdmin) {
            if (request.getContrasenaActual() == null ||
                    !passwordEncoder.matches(request.getContrasenaActual(), usuarioEditar.getContrasena())) {
                return ResponseEntity.badRequest().body(java.util.Map.of("message", "Contraseña actual incorrecta"));
            }
        }

        if (request.getNuevaContrasena() == null || request.getNuevaContrasena().length() < 6) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", "La nueva contraseña debe tener al menos 6 caracteres"));
        }

        usuarioService.cambiarContrasena(usuarioEditar.getId(), request.getNuevaContrasena());

        return ResponseEntity.ok(java.util.Map.of("message", "Contraseña cambiada correctamente"));
    }

    public static class CambiarContrasenaRequest {
        private String contrasenaActual;
        private String nuevaContrasena;
        public String getContrasenaActual() { return contrasenaActual; }
        public void setContrasenaActual(String contrasenaActual) { this.contrasenaActual = contrasenaActual; }
        public String getNuevaContrasena() { return nuevaContrasena; }
        public void setNuevaContrasena(String nuevaContrasena) { this.nuevaContrasena = nuevaContrasena; }
    }
}
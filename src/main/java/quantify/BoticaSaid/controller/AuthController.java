package quantify.BoticaSaid.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import quantify.BoticaSaid.dto.auth.AuthRequest;
import quantify.BoticaSaid.dto.auth.AuthResponse;
import quantify.BoticaSaid.dto.auth.RegisterRequest;
import quantify.BoticaSaid.service.AuthService;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // Endpoint para login
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        System.out.println("Intentando login para DNI: " + request.getDni());
        try {
            AuthResponse response = authService.login(request);
            System.out.println("Login exitoso, JWT generado");
            return ResponseEntity.ok(response);
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "DNI no encontrado", "code", "AUTH_USER_NOT_FOUND"));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Contraseña incorrecta", "code", "AUTH_INVALID_PASSWORD"));
        } catch (ResponseStatusException e) {
            // Para errores como fuera de horario laboral
            return ResponseEntity.status(e.getStatusCode())
                    .body(Map.of("message", e.getReason(), "code", "AUTH_OUT_OF_SCHEDULE"));
        } catch (Exception e) {
            // Errores no esperados
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error inesperado", "code", "INTERNAL_ERROR"));
        }
    }

    // Endpoint para registro (puedes aplicar lógica similar si quieres)
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        System.out.println("Intentando registrar usuario con DNI: " + request.getDni());
        AuthResponse response = authService.register(request);
        System.out.println("Registro exitoso, JWT generado");
        return ResponseEntity.ok(response);
    }
}
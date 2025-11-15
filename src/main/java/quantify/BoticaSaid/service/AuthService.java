package quantify.BoticaSaid.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import quantify.BoticaSaid.dto.auth.AuthRequest;
import quantify.BoticaSaid.dto.auth.AuthResponse;
import quantify.BoticaSaid.dto.auth.RegisterRequest;
import quantify.BoticaSaid.dto.auth.UsuarioDto;
import quantify.BoticaSaid.enums.EstadoToken;
import quantify.BoticaSaid.enums.Rol;
import quantify.BoticaSaid.enums.TipoToken;
import quantify.BoticaSaid.jwt.JwtUtil;
import quantify.BoticaSaid.model.Token;
import quantify.BoticaSaid.model.Usuario;
import quantify.BoticaSaid.model.Caja;
import quantify.BoticaSaid.repository.TokenRepository;
import quantify.BoticaSaid.repository.UsuarioRepository;
import quantify.BoticaSaid.repository.CajaRepository;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
public class AuthService {

    @Autowired
    private UsuarioRepository usuarioRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private CajaRepository cajaRepository;

    public AuthResponse login(AuthRequest request) {
        Usuario usuario = usuarioRepo.findByDni(request.getDni())
                .orElseThrow(() -> new UsernameNotFoundException("DNI no encontrado"));

        if (!passwordEncoder.matches(request.getContrasena(), usuario.getContrasena())) {
            throw new BadCredentialsException("Contraseña incorrecta");
        }

        // ======= BLOQUEO DE ACCESO POR HORARIO (SOLO TRABAJADORES) =======
        if (usuario.getRol() == Rol.TRABAJADOR) {
            LocalTime ahora = LocalTime.now(ZoneId.of("America/Lima")); // <-- CORREGIDO PARA LIMA
            LocalTime entrada = usuario.getHorarioEntrada();
            LocalTime salida = usuario.getHorarioSalida();
            if (entrada != null && salida != null) {
                boolean turnoEsOvernight = entrada.isAfter(salida);
                boolean fueraDeHorario = (!turnoEsOvernight && (ahora.isBefore(entrada) || ahora.isAfter(salida)))
                        || (turnoEsOvernight && (ahora.isAfter(salida) && ahora.isBefore(entrada)));

                if (fueraDeHorario) {
                    boolean tieneCajaAbierta = cajaRepository.existsByUsuarioAndFechaCierreIsNull(usuario);
                    if (!tieneCajaAbierta) {
                        // No tiene caja abierta → bloquea login
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
                        String msg = String.format("No puedes ingresar fuera de tu horario laboral (%s - %s)",
                                entrada.format(formatter), salida.format(formatter));
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN, msg);
                    }
                    // Si tiene caja abierta, permite login para que pueda cerrar caja
                }
            }
        }
        // ================================================================

        // Generar token JWT usando el DNI
        String jwt = jwtUtil.generarToken(usuario.getDni());

        // Invalidar tokens previos
        invalidarTokensPrevios(usuario);

        // Guardar el nuevo token
        Token token = new Token();
        token.setToken(jwt);
        token.setUsuario(usuario);
        token.setTipoToken(TipoToken.BEARER);
        token.setEstadoToken(EstadoToken.VALIDO);
        tokenRepository.save(token);

        // Construir el UsuarioDto (convertir LocalTime a String)
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        UsuarioDto usuarioDto = new UsuarioDto(
                usuario.getDni(),
                usuario.getNombreCompleto(),
                usuario.getRol().toString(),
                usuario.getHorarioEntrada() != null ? usuario.getHorarioEntrada().format(timeFormatter) : null,
                usuario.getHorarioSalida() != null ? usuario.getHorarioSalida().format(timeFormatter) : null,
                usuario.getTurno()
        );

        AuthResponse response = new AuthResponse();
        response.setToken(jwt);
        response.setUsuario(usuarioDto);

        return response;
    }

    private void invalidarTokensPrevios(Usuario usuario) {
        var tokens = tokenRepository.findAllByUsuarioAndEstadoToken(usuario, EstadoToken.VALIDO);
        for (Token t : tokens) {
            t.setEstadoToken(EstadoToken.INVALIDO);
        }
        tokenRepository.saveAll(tokens);
    }

    public AuthResponse register(RegisterRequest request) {
        if (usuarioRepo.findByDni(request.getDni()).isPresent()) {
            throw new IllegalArgumentException("DNI ya registrado");
        }

        Usuario usuario = new Usuario();
        usuario.setDni(request.getDni());
        usuario.setNombreCompleto(request.getNombreCompleto());
        usuario.setContrasena(passwordEncoder.encode(request.getContrasena()));
        usuario.setTurno(request.getTurno());

        try {
            usuario.setRol(Rol.valueOf(request.getRol().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Rol inválido");
        }

        if (request.getHorarioEntrada() != null) {
            usuario.setHorarioEntrada(LocalTime.parse(request.getHorarioEntrada()));
        }
        if (request.getHorarioSalida() != null) {
            usuario.setHorarioSalida(LocalTime.parse(request.getHorarioSalida()));
        }

        // Guardar usuario y obtener el objeto guardado con ID
        Usuario usuarioGuardado = usuarioRepo.save(usuario);

        System.out.println("Se guarda el usuario :D");
        // Generar token JWT con el DNI guardado
        String jwt = jwtUtil.generarToken(usuarioGuardado.getDni());

        Token token = new Token();
        token.setToken(jwt);
        token.setUsuario(usuarioGuardado);
        token.setTipoToken(TipoToken.BEARER);
        token.setEstadoToken(EstadoToken.VALIDO);

        tokenRepository.save(token);

        AuthResponse response = new AuthResponse();
        response.setToken(jwt);

        return response;
    }
}
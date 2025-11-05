package quantify.BoticaSaid.service;

import java.util.Collections;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import quantify.BoticaSaid.model.Usuario;
import quantify.BoticaSaid.repository.UsuarioRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepo;

    public CustomUserDetailsService(UsuarioRepository usuarioRepo) {
        this.usuarioRepo = usuarioRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String dni) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepo.findByDni(dni)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con DNI: " + dni));

        // MODO A: rol con prefijo ROLE_
        return new User(
                usuario.getDni(),
                usuario.getContrasena(),
                true, true, true, true,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name()))
        );
    }
}
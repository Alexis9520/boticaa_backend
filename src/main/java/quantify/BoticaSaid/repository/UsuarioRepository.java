package quantify.BoticaSaid.repository;

import org.springframework.stereotype.Repository;
import quantify.BoticaSaid.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByDni(String dni);
}
package quantify.BoticaSaid.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import quantify.BoticaSaid.model.DetalleBoleta;

@Repository
public interface DetalleBoletaRepository extends JpaRepository<DetalleBoleta,Integer> {
}

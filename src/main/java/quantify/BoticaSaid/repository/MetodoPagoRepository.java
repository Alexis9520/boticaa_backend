package quantify.BoticaSaid.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import quantify.BoticaSaid.model.MetodoPago;

@Repository
public interface MetodoPagoRepository extends JpaRepository<MetodoPago,Integer> {
}

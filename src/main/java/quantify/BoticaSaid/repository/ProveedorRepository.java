package quantify.BoticaSaid.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import quantify.BoticaSaid.model.Proveedor;

import java.util.List;

@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {
    
    List<Proveedor> findByActivoTrue();
    
    List<Proveedor> findByRucContainingIgnoreCaseOrRazonComercialContainingIgnoreCaseAndActivoTrue(String ruc, String razonComercial);
}

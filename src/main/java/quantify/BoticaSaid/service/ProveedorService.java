package quantify.BoticaSaid.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import quantify.BoticaSaid.dto.proveedor.ProveedorRequest;
import quantify.BoticaSaid.dto.proveedor.ProveedorResponse;
import quantify.BoticaSaid.model.Proveedor;
import quantify.BoticaSaid.repository.ProveedorRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProveedorService {

    @Autowired
    private ProveedorRepository proveedorRepository;

    /**
     * Crear un nuevo proveedor
     */
    @Transactional
    public Proveedor crearProveedor(ProveedorRequest request) {
        if (request.getRuc() == null || request.getRuc().trim().isEmpty()) {
            throw new IllegalArgumentException("El RUC del proveedor es obligatorio.");
        }

        Proveedor proveedor = new Proveedor();
        proveedor.setRuc(request.getRuc());
        proveedor.setRazonComercial(request.getRazonComercial());
        proveedor.setNumero1(request.getNumero1());
        proveedor.setNumero2(request.getNumero2());
        proveedor.setCorreo(request.getCorreo());
        proveedor.setDireccion(request.getDireccion());
        proveedor.setActivo(true);

        return proveedorRepository.save(proveedor);
    }

    /**
     * Obtener proveedor por ID
     */
    public Proveedor buscarPorId(Long id) {
        Optional<Proveedor> proveedor = proveedorRepository.findById(id);
        return (proveedor.isPresent() && proveedor.get().isActivo()) ? proveedor.get() : null;
    }

    /**
     * Listar todos los proveedores activos
     */
    public List<Proveedor> listarTodos() {
        return proveedorRepository.findByActivoTrue();
    }

    /**
     * Buscar proveedores por RUC o razón comercial
     */
    public List<Proveedor> buscarPorRucORazonComercial(String query) {
        if (query == null || query.trim().isEmpty()) {
            return listarTodos();
        }
        return proveedorRepository.findByRucContainingIgnoreCaseOrRazonComercialContainingIgnoreCaseAndActivoTrue(query, query);
    }

    /**
     * Actualizar un proveedor
     */
    @Transactional
    public Proveedor actualizarPorId(Long id, ProveedorRequest request) {
        Optional<Proveedor> proveedorOpt = proveedorRepository.findById(id);
        
        if (proveedorOpt.isEmpty() || !proveedorOpt.get().isActivo()) {
            return null;
        }

        Proveedor proveedor = proveedorOpt.get();
        proveedor.setRuc(request.getRuc());
        proveedor.setRazonComercial(request.getRazonComercial());
        proveedor.setNumero1(request.getNumero1());
        proveedor.setNumero2(request.getNumero2());
        proveedor.setCorreo(request.getCorreo());
        proveedor.setDireccion(request.getDireccion());

        return proveedorRepository.save(proveedor);
    }

    /**
     * Eliminar un proveedor (borrado lógico)
     */
    @Transactional
    public boolean eliminarPorId(Long id) {
        Optional<Proveedor> proveedorOpt = proveedorRepository.findById(id);
        
        if (proveedorOpt.isEmpty() || !proveedorOpt.get().isActivo()) {
            return false;
        }

        Proveedor proveedor = proveedorOpt.get();
        proveedor.setActivo(false);
        proveedorRepository.save(proveedor);
        return true;
    }

    /**
     * Convertir Proveedor a ProveedorResponse
     */
    public ProveedorResponse toProveedorResponse(Proveedor proveedor) {
        if (proveedor == null) {
            return null;
        }

        ProveedorResponse response = new ProveedorResponse();
        response.setId(proveedor.getId());
        response.setRuc(proveedor.getRuc());
        response.setRazonComercial(proveedor.getRazonComercial());
        response.setNumero1(proveedor.getNumero1());
        response.setNumero2(proveedor.getNumero2());
        response.setCorreo(proveedor.getCorreo());
        response.setDireccion(proveedor.getDireccion());
        response.setFechaCreacion(proveedor.getFechaCreacion());
        response.setFechaActualizacion(proveedor.getFechaActualizacion());
        response.setActivo(proveedor.isActivo());

        return response;
    }
}

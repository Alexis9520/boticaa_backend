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
        if (request.getNombre() == null || request.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del proveedor es obligatorio.");
        }

        Proveedor proveedor = new Proveedor();
        proveedor.setNombre(request.getNombre());
        proveedor.setContacto(request.getContacto());
        proveedor.setTelefono(request.getTelefono());
        proveedor.setEmail(request.getEmail());
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
     * Buscar proveedores por nombre
     */
    public List<Proveedor> buscarPorNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return listarTodos();
        }
        return proveedorRepository.findByNombreContainingIgnoreCaseAndActivoTrue(nombre);
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
        proveedor.setNombre(request.getNombre());
        proveedor.setContacto(request.getContacto());
        proveedor.setTelefono(request.getTelefono());
        proveedor.setEmail(request.getEmail());
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
        response.setNombre(proveedor.getNombre());
        response.setContacto(proveedor.getContacto());
        response.setTelefono(proveedor.getTelefono());
        response.setEmail(proveedor.getEmail());
        response.setDireccion(proveedor.getDireccion());
        response.setFechaCreacion(proveedor.getFechaCreacion());
        response.setFechaActualizacion(proveedor.getFechaActualizacion());
        response.setActivo(proveedor.isActivo());

        return response;
    }
}

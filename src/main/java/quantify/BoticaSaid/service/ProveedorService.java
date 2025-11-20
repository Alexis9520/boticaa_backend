package quantify.BoticaSaid.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import quantify.BoticaSaid.dto.proveedor.ProveedorRequest;
import quantify.BoticaSaid.dto.proveedor.ProveedorResponse;
import quantify.BoticaSaid.model.Proveedor;
import quantify.BoticaSaid.repository.ProveedorRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProveedorService {

    @Autowired
    private ProveedorRepository proveedorRepository;

    /**
     * Crear un nuevo proveedor
     */
    @Transactional
    public ProveedorResponse crearProveedor(ProveedorRequest request) {
        if (request.getNombre() == null || request.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del proveedor es obligatorio.");
        }

        Proveedor proveedor = new Proveedor();
        proveedor.setNombre(request.getNombre());
        proveedor.setRuc(request.getRuc());
        proveedor.setTelefono(request.getTelefono());
        proveedor.setEmail(request.getEmail());
        proveedor.setDireccion(request.getDireccion());
        proveedor.setContacto(request.getContacto());
        proveedor.setActivo(true);

        Proveedor guardado = proveedorRepository.save(proveedor);
        return toProveedorResponse(guardado);
    }

    /**
     * Obtener todos los proveedores activos
     */
    public List<ProveedorResponse> obtenerProveedoresActivos() {
        return proveedorRepository.findByActivoTrue().stream()
                .map(this::toProveedorResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtener proveedor por ID
     */
    public Proveedor buscarPorId(Long id) {
        return proveedorRepository.findById(id).orElse(null);
    }

    /**
     * Obtener proveedores con paginación y filtros
     */
    public Page<ProveedorResponse> buscarProveedoresPaginados(String q, Boolean activo, Pageable pageable) {
        Page<Proveedor> page = proveedorRepository.findByFiltros(q, activo, pageable);
        return page.map(this::toProveedorResponse);
    }

    /**
     * Actualizar proveedor
     */
    @Transactional
    public ProveedorResponse actualizarProveedor(Long id, ProveedorRequest request) {
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado"));

        if (request.getNombre() != null && !request.getNombre().trim().isEmpty()) {
            proveedor.setNombre(request.getNombre());
        }
        proveedor.setRuc(request.getRuc());
        proveedor.setTelefono(request.getTelefono());
        proveedor.setEmail(request.getEmail());
        proveedor.setDireccion(request.getDireccion());
        proveedor.setContacto(request.getContacto());

        Proveedor actualizado = proveedorRepository.save(proveedor);
        return toProveedorResponse(actualizado);
    }

    /**
     * Eliminar proveedor (borrado lógico)
     */
    @Transactional
    public boolean eliminarProveedor(Long id) {
        Proveedor proveedor = proveedorRepository.findById(id).orElse(null);
        if (proveedor != null) {
            proveedor.setActivo(false);
            proveedorRepository.save(proveedor);
            return true;
        }
        return false;
    }

    /**
     * Convertir entidad a DTO
     */
    public ProveedorResponse toProveedorResponse(Proveedor proveedor) {
        ProveedorResponse response = new ProveedorResponse();
        response.setId(proveedor.getId());
        response.setNombre(proveedor.getNombre());
        response.setRuc(proveedor.getRuc());
        response.setTelefono(proveedor.getTelefono());
        response.setEmail(proveedor.getEmail());
        response.setDireccion(proveedor.getDireccion());
        response.setContacto(proveedor.getContacto());
        response.setActivo(proveedor.isActivo());
        return response;
    }
}

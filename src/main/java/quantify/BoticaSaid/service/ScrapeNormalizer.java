package quantify.BoticaSaid.service;

import org.springframework.stereotype.Component;
import quantify.BoticaSaid.dto.producto.ProductoResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class ScrapeNormalizer {

    /**
     * Normaliza la respuesta del microservicio de scraping a una lista de ProductoResponse.
     */
    @SuppressWarnings("unchecked")
    public List<ProductoResponse> normalize(Object raw) {
        if (raw == null) return List.of();

        Map<String, Object> root;
        if (raw instanceof Map) {
            root = (Map<String, Object>) raw;
            // Si existe clave "json", usar su contenido
            if (root.containsKey("json") && root.get("json") instanceof Map) {
                root = (Map<String, Object>) root.get("json");
            }
        } else {
            return List.of();
        }

        Object resultadosObj = root.get("resultados");
        if (!(resultadosObj instanceof List)) {
            return List.of();
        }

        List<Map<String, Object>> resultados = (List<Map<String, Object>>) resultadosObj;
        List<ProductoResponse> out = new ArrayList<>();

        for (Map<String, Object> r : resultados) {
            ProductoResponse p = new ProductoResponse();
            // Nombre del producto
            Object nombre = r.get("nombre_producto");
            if (nombre == null) nombre = r.get("nombre");
            if (nombre instanceof String) p.setNombre(((String) nombre).trim());

            // Presentacion
            Object presentacion = r.get("presentacion");
            if (presentacion instanceof String) p.setPresentacion(((String) presentacion).trim());

            // Principio activo / composicion
            Object composicion = r.get("composicion");
            if (composicion instanceof String) p.setPrincipioActivo(((String) composicion).trim());

            // Laboratorio / fabricante
            Object fabricante = r.get("fabricante");
            if (fabricante instanceof String) p.setLaboratorio(((String) fabricante).trim());

            // Categoria / forma farmaceutica / rubro
            Object forma = r.get("forma_farmaceutica");
            Object rubro = r.get("rubro");
            if (forma instanceof String && !((String) forma).isBlank()) {
                p.setCategoria(((String) forma).trim());
            } else if (rubro instanceof String) {
                p.setCategoria(((String) rubro).trim());
            }

            // Tipo medicamento heuristico a partir de rubro
            if (rubro instanceof String) {
                String rub = ((String) rubro).toLowerCase();
                if (rub.contains("generico")) {
                    p.setTipoMedicamento("GENERIC");
                } else {
                    p.setTipoMedicamento("MARCA");
                }
            }

            // Numero de registro sanitario: preferir 'rs' luego 'rs_anterior'
            String nro = null;
            Object rs = r.get("rs");
            Object rs_ant = r.get("rs_anterior");
            if (rs instanceof String && !((String) rs).isBlank()) nro = ((String) rs).trim();
            if ((nro == null || nro.isBlank()) && rs_ant instanceof String && !((String) rs_ant).isBlank()) nro = ((String) rs_ant).trim();
            p.setNroRegistroSanitario(nro);

            // Fecha de vencimiento -> se puede exponer en presentacion o en otro campo si se quiere
            Object fecha = r.get("fecha_vencimiento");
            if (fecha instanceof String) {
                // opcional: concatenar a presentacion si no existe
                if ((p.getPresentacion() == null || p.getPresentacion().isBlank())) {
                    p.setPresentacion(((String) fecha).trim());
                }
            }

            // Solo agregar si tiene nombre o nroRegistroSanitario
            if ((p.getNombre() != null && !p.getNombre().isBlank()) || (p.getNroRegistroSanitario() != null && !p.getNroRegistroSanitario().isBlank())) {
                out.add(p);
            }
        }

        return out;
    }
}

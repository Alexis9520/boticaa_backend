package quantify.BoticaSaid.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import quantify.BoticaSaid.dto.scraping.GenericErrorResponse;
import quantify.BoticaSaid.dto.scraping.ScrapeRequest;
import quantify.BoticaSaid.dto.scraping.SessionCreateRequest;
import quantify.BoticaSaid.dto.scraping.SessionCreateResponse;
import quantify.BoticaSaid.service.ScrapingClient;
import quantify.BoticaSaid.service.ProductoService;
import quantify.BoticaSaid.dto.producto.ProductoResponse;
import quantify.BoticaSaid.model.Producto;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/scraping")
public class ScrapingController {

    private final ScrapingClient client;
    @Autowired
    private ProductoService productoService;

    @Autowired
    public ScrapingController(ScrapingClient client) {
        this.client = client;
    }

    private String getIncomingAuth(HttpHeaders headers) {
        // No usar X-API-Key aquí: sólo considerar el header Authorization tal como viene
        String a = headers.getFirst("Authorization");
        if (!StringUtils.hasText(a)) return null;
        return a.trim();
    }

    @GetMapping("/health")
    public ResponseEntity<?> health(@RequestHeader HttpHeaders headers) {
        try {
            ResponseEntity<String> resp = client.health(getIncomingAuth(headers));
            return handleScrapeStringResponse(resp);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "error", "redis", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/session/create")
    public ResponseEntity<?> createSession(@RequestHeader HttpHeaders headers, @RequestBody(required = false) SessionCreateRequest body) {
        try {
            // Si el cliente no envía body, pasar null para que el cliente HTTP reenvíe la petición sin body
            ResponseEntity<SessionCreateResponse> resp = client.createSession(getIncomingAuth(headers), body);
            return ResponseEntity.status(resp.getStatusCode()).body(resp.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GenericErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/session/{sessionId}/status")
    public ResponseEntity<?> sessionStatus(@RequestHeader HttpHeaders headers, @PathVariable String sessionId) {
        try {
            ResponseEntity<String> resp = client.sessionStatus(getIncomingAuth(headers), sessionId);
            return handleScrapeStringResponse(resp);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GenericErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/session/{sessionId}/validate")
    public ResponseEntity<?> validateSession(@RequestHeader HttpHeaders headers, @PathVariable String sessionId) {
        try {
            ResponseEntity<String> resp = client.validateSession(getIncomingAuth(headers), sessionId);
            return handleScrapeStringResponse(resp);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GenericErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/session/{sessionId}/invalidate")
    public ResponseEntity<?> invalidateSession(@RequestHeader HttpHeaders headers, @PathVariable String sessionId) {
        try {
            ResponseEntity<String> resp = client.invalidateSession(getIncomingAuth(headers), sessionId);
            return handleScrapeStringResponse(resp);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GenericErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/admin/sessions")
    public ResponseEntity<?> adminSessions(@RequestHeader HttpHeaders headers) {
        try {
            ResponseEntity<String> resp = client.adminSessions(getIncomingAuth(headers));
            return handleScrapeStringResponse(resp);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GenericErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/admin/sessions/reset")
    public ResponseEntity<?> resetAdminSessions(@RequestHeader HttpHeaders headers) {
        try {
            ResponseEntity<String> resp = client.resetAdminSessions(getIncomingAuth(headers));
            return handleScrapeStringResponse(resp);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GenericErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/scrape")
    public ResponseEntity<?> scrape(@RequestHeader HttpHeaders headers, @RequestBody(required = false) ScrapeRequest body) {
        // DEBUG: imprimir headers y body para verificar que la petición llega al controlador
        System.out.println("[DEBUG] ScrapingController.scrape called. Headers: " + headers.toSingleValueMap());
        System.out.println("[DEBUG] ScrapingController.scrape body: " + body);

        // Manejar body nulo y evitar NPE
        if (body == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new GenericErrorResponse("Campo 'nro' es requerido"));
        }

        String nroParam = body.getNro();
        String nroRegistro = body.getNroDeRegistroSanitario();

        if ((nroParam == null || nroParam.isBlank()) && (nroRegistro == null || nroRegistro.isBlank())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new GenericErrorResponse("Campo 'nro' es requerido"));
        }

        // Si se proporcionó explícitamente nroDeRegistroSanitario, primero buscar en BD local
        if (nroRegistro != null && !nroRegistro.isBlank()) {
            Producto producto = productoService.buscarPorNroRegistroSanitario(nroRegistro);
            if (producto != null) {
                ProductoResponse resp = productoService.toProductoResponse(producto);
                return ResponseEntity.ok(Map.of("ok", true, "status", 200, "json", resp));
            }
            // si no se encuentra en BD, seguiremos con el scraping remoto
        }

        String nro = (nroParam != null && !nroParam.isBlank()) ? nroParam : nroRegistro;

        try {
            // Reenviar la petición al microservicio de scraping (si se pasó session_id se incluirá, si no el microservicio intentará usar una sesión valida automáticamente)
            Map<String, Object> req = new HashMap<>();
            req.put("nro", nro);
            if (body.getSession_id() != null && !body.getSession_id().isBlank()) {
                req.put("session_id", body.getSession_id());
            }
            ResponseEntity<String> resp = client.scrape(getIncomingAuth(headers), req);

            // Si el servicio remoto devolvió JSON, intentar parsearlo y manejar casos especiales
            if (resp != null && resp.getBody() != null && resp.getBody().startsWith("{")) {
                try {
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    Object parsed = mapper.readValue(resp.getBody(), Object.class);

                    if (parsed instanceof Map<?,?> parsedMap) {
                        // Comprobar errores tipo 'no validated session' en wrapper o en json
                        Object err = parsedMap.get("error");
                        if (err instanceof String errS && errS.toLowerCase().contains("no validated session")) {
                            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", errS));
                        }
                        Object jsonObj = parsedMap.get("json");
                        if (jsonObj instanceof Map<?,?> jm) {
                            Object err2 = jm.get("error");
                            if (err2 instanceof String errS2 && errS2.toLowerCase().contains("no validated session")) {
                                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", errS2));
                            }

                            // Manejo especial cuando el microservicio devuelve el wrapper {ok,status,json}
                            // y json contiene 'count' y 'resultados'
                            Object countObj = ((Map<?,?>) jm).get("count");
                            Object resultadosObj = ((Map<?,?>) jm).get("resultados");
                            if (resultadosObj instanceof java.util.List) {
                                int count = -1;
                                try {
                                    if (countObj instanceof Number) count = ((Number) countObj).intValue();
                                    else if (countObj instanceof String) count = Integer.parseInt((String) countObj);
                                } catch (Exception ignore) { }

                                @SuppressWarnings("unchecked")
                                java.util.List<Object> resultados = (java.util.List<Object>) resultadosObj;

                                // Caso 1: un solo resultado
                                if (count == 1 || resultados.size() == 1) {
                                    Object first = resultados.get(0);
                                    if (first instanceof Map<?,?> firstMap) {
                                        @SuppressWarnings("unchecked")
                                        Map<String,Object> jmMap = (Map<String,Object>) jm;
                                        // poner el resultado remoto y su versión normalizada
                                        jmMap.put("selected", firstMap);
                                        ProductoResponse selNorm = normalizeRemoteResultToProductoResponse(firstMap);
                                        jmMap.put("selected_normalized", selNorm);

                                        String rs = firstMap.get("rs") == null ? null : String.valueOf(firstMap.get("rs"));
                                        String situacion = firstMap.get("situacion") == null ? null : String.valueOf(firstMap.get("situacion"));
                                        // Si el rs coincide con el nro consultado, indicar que está vigente/estado
                                        if (nro != null && rs != null && rs.equalsIgnoreCase(nro)) {
                                            jmMap.put("note", "Registro sanitario consultado se encuentra: " + (situacion == null ? "UNKNOWN" : situacion));
                                        }
                                    }
                                    // devolver el wrapper original (modificado) manteniendo el status remoto
                                    return ResponseEntity.status(resp.getStatusCode()).body(parsedMap);
                                }

                                // Caso: varios resultados -> buscar el resultado cuyo 'rs' sea igual al nro consultado
                                if (resultados.size() > 1) {
                                    Map<String,Object> selected = null;
                                    Map<String,Object> newer = null;
                                    for (Object o : resultados) {
                                        if (!(o instanceof Map<?,?>)) continue;
                                        @SuppressWarnings("unchecked")
                                        Map<String,Object> m = (Map<String,Object>) o;
                                        String rs = m.get("rs") == null ? null : String.valueOf(m.get("rs"));
                                        String rs_anterior = m.get("rs_anterior") == null ? null : String.valueOf(m.get("rs_anterior"));
                                        if (nro != null && rs != null && rs.equalsIgnoreCase(nro)) {
                                            selected = m;
                                        }
                                        if (nro != null && rs_anterior != null && rs_anterior.equalsIgnoreCase(nro)) {
                                            newer = m; // este es un registro que refiere al nro consultado como rs_anterior
                                        }
                                    }

                                    if (selected != null) {
                                        @SuppressWarnings("unchecked")
                                        Map<String,Object> jmMap = (Map<String,Object>) jm;
                                        // incluir el resultado remoto original
                                        jmMap.put("selected", selected);
                                        // y su versión normalizada según nuestro DTO
                                        ProductoResponse selNorm = normalizeRemoteResultToProductoResponse(selected);
                                        jmMap.put("selected_normalized", selNorm);

                                        if (newer != null && newer.get("rs") != null) {
                                            String newerRs = String.valueOf(newer.get("rs"));
                                            String newerSit = newer.get("situacion") == null ? "UNKNOWN" : String.valueOf(newer.get("situacion"));
                                            jmMap.put("alert", "Existe un registro sanitario más actualizado: " + newerRs + " (" + newerSit + ")");
                                            ProductoResponse newNorm = normalizeRemoteResultToProductoResponse(newer);
                                            jmMap.put("newSelected", newer);
                                            jmMap.put("newSelected_normalized", newNorm);
                                        }
                                        return ResponseEntity.status(resp.getStatusCode()).body(parsedMap);
                                    }
                                }
                            }
                        }
                        // Si no encaja en nuestras reglas especiales, devolver parsedMap tal cual
                        return ResponseEntity.status(resp.getStatusCode()).body(parsedMap);
                    }
                } catch (Exception ignored) {
                    // parsing error: no-op, seguiremos al handler general
                }
            }

            return handleScrapeStringResponse(resp);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GenericErrorResponse(e.getMessage()));
        }
    }

    // Manejo centralizado para respuestas del scrape que vienen como String
    private ResponseEntity<?> handleScrapeStringResponse(ResponseEntity<String> resp) {
        if (resp == null) return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GenericErrorResponse("Respuesta vacía del servicio de scraping"));

        var statusCode = resp.getStatusCode();
        String body = resp.getBody();

        // Si no hay body, devolver el status remoto con un mensaje mínimo
        if (body == null || body.isBlank()) {
            return ResponseEntity.status(statusCode).body(Map.of("ok", false, "status", statusCode.value(), "json", Map.of("message", "Empty response from scraping service")));
        }

        // Intentar parsear como JSON y devolver el JSON parseado manteniendo el status remoto
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            Object parsed = mapper.readValue(body, Object.class);
            return ResponseEntity.status(statusCode).body(parsed);
        } catch (Exception e) {
            // No es JSON: devolver el texto tal cual con el status remoto
            return ResponseEntity.status(statusCode).body(body);
        }
    }

    // Normalizar un resultado remoto (map) a ProductoResponse según nuestras reglas
    private ProductoResponse normalizeRemoteResultToProductoResponse(Map<?,?> remote) {
        if (remote == null) return null;
        ProductoResponse pr = new ProductoResponse();
        try {
            Object nombre = remote.get("nombre_producto");
            Object laboratorio = remote.get("fabricante");
            Object presentacion = remote.get("presentacion");
            Object composicion = remote.get("composicion");
            Object rs = remote.get("rs");
            Object categoria = remote.get("categoria_titular");
            Object situacion = remote.get("situacion");

            pr.setNombre(nombre == null ? null : String.valueOf(nombre));
            pr.setLaboratorio(laboratorio == null ? null : String.valueOf(laboratorio));
            pr.setPresentacion(presentacion == null ? null : String.valueOf(presentacion));
            pr.setConcentracion(composicion == null ? null : String.valueOf(composicion));
            pr.setNroRegistroSanitario(rs == null ? null : String.valueOf(rs));
            pr.setCategoria(categoria == null ? null : String.valueOf(categoria));

            // Añadimos un campo extra en el DTO si existe 'situacion' -> lo ponemos en tipoMedicamento por falta de campo específico
            pr.setTipoMedicamento(situacion == null ? null : String.valueOf(situacion));

            // Otros campos no disponibles en el remote se dejan nulos o con valores por defecto
            pr.setCodigoBarras(null);
            pr.setCantidadGeneral(0);
            pr.setCantidadMinima(null);
            pr.setPrecioVentaUnd(null);
            pr.setDescuento(null);
            pr.setCantidadUnidadesBlister(null);
            pr.setPrecioVentaBlister(null);
            pr.setPrincipioActivo(null);

            // proveedores y stocks quedan vacíos
            pr.setProveedores(new java.util.ArrayList<>());
            pr.setStocks(new java.util.ArrayList<>());

            return pr;
        } catch (Exception e) {
            return pr;
        }
    }

}

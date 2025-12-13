package quantify.BoticaSaid.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import quantify.BoticaSaid.dto.scraping.SessionCreateRequest;
import quantify.BoticaSaid.dto.scraping.SessionCreateResponse;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class ScrapingClient {

    private static final Logger log = LoggerFactory.getLogger(ScrapingClient.class);

    private final RestTemplate restTemplate = new RestTemplate();
    private final String baseUrl;

    public ScrapingClient(
            @Value("${scraping.service.url:http://127.0.0.1:5000}") String baseUrl
    ) {
        this.baseUrl = baseUrl;
    }

    private HttpHeaders buildHeaders(String incomingAuth) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // Aceptar JSON y incluir User-Agent para ser más compatible con servidores que esperan UA
        headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));
        headers.set("User-Agent", "BoticaProxy/1.0");
        // No usar apiKey: si viene Authorization, reenviar tal cual
        if (incomingAuth != null && !incomingAuth.isBlank()) {
            headers.set("Authorization", incomingAuth.trim());
        }
        return headers;
    }

    public ResponseEntity<String> health(String incomingAuth) {
        String url = baseUrl + "/health";
        HttpEntity<Void> request = new HttpEntity<>(buildHeaders(incomingAuth));
        try {
            return restTemplate.exchange(url, HttpMethod.GET, request, String.class);
        } catch (HttpStatusCodeException ex) {
            return new ResponseEntity<>(ex.getResponseBodyAsString(), ex.getStatusCode());
        }
    }

    public ResponseEntity<SessionCreateResponse> createSession(String incomingAuth, SessionCreateRequest body) {
        String url = baseUrl + "/session/create";
        try {
            ResponseEntity<String> respStr;
            if (body == null) {
                HttpEntity<Void> request = new HttpEntity<>(buildHeaders(incomingAuth));
                respStr = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
            } else {
                HttpEntity<SessionCreateRequest> request = new HttpEntity<>(body, buildHeaders(incomingAuth));
                respStr = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
            }

            // Debug printing (already presente)
            if (respStr != null) {
                log.info("ScrapingClient.createSession -> remote status={}, body={}", respStr.getStatusCode(), respStr.getBody());
                System.out.println("[DEBUG] ScrapingClient.createSession -> remote status=" + respStr.getStatusCode() + " body=" + respStr.getBody());
            } else {
                log.warn("ScrapingClient.createSession -> remote response is null");
                System.out.println("[DEBUG] ScrapingClient.createSession -> remote response is null");
            }

            int statusCode = respStr.getStatusCode().value();
            String bodyStr = respStr.getBody();

            SessionCreateResponse out = new SessionCreateResponse();

            // Si el remote respondió 2xx, envolver su JSON directamente en out.json
            if (respStr.getStatusCode().is2xxSuccessful()) {
                out.setOk(true);
                out.setStatus(statusCode);
                if (bodyStr == null || bodyStr.isBlank()) {
                    out.setJson(null);
                } else {
                    try {
                        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                        Object parsed = mapper.readValue(bodyStr, Object.class);
                        if (parsed instanceof java.util.Map parsedMap) {
                            // si el remote devolvió el wrapper, extraer 'json' si existe
                            if (parsedMap.containsKey("json") && parsedMap.get("json") instanceof java.util.Map) {
                                //noinspection unchecked
                                out.setJson((java.util.Map<String, Object>) parsedMap.get("json"));
                            } else if (parsedMap.containsKey("ok") && parsedMap.containsKey("status") && parsedMap.containsKey("json")) {
                                // mapping fallback
                                com.fasterxml.jackson.databind.ObjectMapper m2 = new com.fasterxml.jackson.databind.ObjectMapper();
                                SessionCreateResponse mapped = m2.convertValue(parsedMap, SessionCreateResponse.class);
                                return new ResponseEntity<>(mapped, respStr.getStatusCode());
                            } else {
                                // remote devolvió objeto de sesión directamente
                                //noinspection unchecked
                                out.setJson((java.util.Map<String, Object>) parsedMap);
                            }
                        } else {
                            out.setJson(Map.of("value", parsed));
                        }
                    } catch (Exception e) {
                        out.setJson(Map.of("message", bodyStr));
                    }
                }
                return new ResponseEntity<>(out, respStr.getStatusCode());
            }

            // Si no fue 2xx, intentar mapear wrapper {ok,status,json} o devolver mensaje
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                Object parsed = mapper.readValue(bodyStr == null ? "" : bodyStr, Object.class);
                if (parsed instanceof java.util.Map parsedMap && parsedMap.containsKey("ok") && parsedMap.containsKey("status") && parsedMap.containsKey("json")) {
                    SessionCreateResponse mapped = mapper.convertValue(parsedMap, SessionCreateResponse.class);
                    return new ResponseEntity<>(mapped, respStr.getStatusCode());
                }
            } catch (Exception ignored) {
            }

            // fallback: devolver out con ok=false y json con mensaje bruto
            out.setOk(false);
            out.setStatus(statusCode);
            out.setJson(bodyStr == null ? null : Map.of("message", bodyStr));
            return new ResponseEntity<>(out, respStr.getStatusCode());

        } catch (HttpStatusCodeException ex) {
            SessionCreateResponse resp = new SessionCreateResponse();
            resp.setOk(false);
            resp.setStatus(ex.getStatusCode().value());
            String bodyStr = ex.getResponseBodyAsString();
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                Object parsed = mapper.readValue(bodyStr, Object.class);
                if (parsed instanceof java.util.Map) {
                    //noinspection unchecked
                    resp.setJson((java.util.Map<String, Object>) parsed);
                } else {
                    resp.setJson(Map.of("message", bodyStr));
                }
            } catch (Exception parseEx) {
                resp.setJson(Map.of("message", bodyStr));
            }
            resp.setStatus(ex.getStatusCode().value());
            return new ResponseEntity<>(resp, ex.getStatusCode());
        }
    }

    public ResponseEntity<String> sessionStatus(String incomingAuth, String sessionId) {
        String url = baseUrl + "/session/" + sessionId + "/status";
        HttpEntity<Void> request = new HttpEntity<>(buildHeaders(incomingAuth));
        try {
            return restTemplate.exchange(url, HttpMethod.GET, request, String.class);
        } catch (HttpStatusCodeException ex) {
            return new ResponseEntity<>(ex.getResponseBodyAsString(), ex.getStatusCode());
        }
    }

    public ResponseEntity<String> validateSession(String incomingAuth, String sessionId) {
        String url = baseUrl + "/session/" + sessionId + "/validate";
        HttpEntity<Void> request = new HttpEntity<>(buildHeaders(incomingAuth));
        try {
            return restTemplate.exchange(url, HttpMethod.POST, request, String.class);
        } catch (HttpStatusCodeException ex) {
            return new ResponseEntity<>(ex.getResponseBodyAsString(), ex.getStatusCode());
        }
    }

    public ResponseEntity<String> invalidateSession(String incomingAuth, String sessionId) {
        String url = baseUrl + "/session/" + sessionId + "/invalidate";
        HttpEntity<Void> request = new HttpEntity<>(buildHeaders(incomingAuth));
        try {
            return restTemplate.exchange(url, HttpMethod.POST, request, String.class);
        } catch (HttpStatusCodeException ex) {
            return new ResponseEntity<>(ex.getResponseBodyAsString(), ex.getStatusCode());
        }
    }

    public ResponseEntity<String> adminSessions(String incomingAuth) {
        String url = baseUrl + "/admin/sessions";
        HttpEntity<Void> request = new HttpEntity<>(buildHeaders(incomingAuth));
        try {
            return restTemplate.exchange(url, HttpMethod.GET, request, String.class);
        } catch (HttpStatusCodeException ex) {
            return new ResponseEntity<>(ex.getResponseBodyAsString(), ex.getStatusCode());
        }
    }

    public ResponseEntity<String> scrape(String incomingAuth, Map<String, Object> body) {
        String url = baseUrl + "/scrape";
        // Serializar el body a JSON String y enviarlo explícitamente como String
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            String json = mapper.writeValueAsString(body == null ? Map.of() : body);
            HttpEntity<String> request = new HttpEntity<>(json, buildHeaders(incomingAuth));
            ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

            // Logging
            if (resp != null) {
                log.info("ScrapingClient.scrape -> remote status={}, body={}", resp.getStatusCode(), resp.getBody());
                System.out.println("[DEBUG] ScrapingClient.scrape -> remote status=" + resp.getStatusCode() + " body=" + resp.getBody());
            }

            if (resp == null) return new ResponseEntity<>((String) null, HttpStatus.INTERNAL_SERVER_ERROR);

            String bodyStr = resp.getBody();
            if (resp.getStatusCode().is2xxSuccessful()) {
                if (bodyStr == null || bodyStr.isBlank()) return resp;
                try {
                    com.fasterxml.jackson.databind.ObjectMapper m2 = new com.fasterxml.jackson.databind.ObjectMapper();
                    Object parsed = m2.readValue(bodyStr, Object.class);
                    if (parsed instanceof java.util.Map parsedMap) {
                        if (parsedMap.containsKey("ok") && parsedMap.containsKey("status") && parsedMap.containsKey("json")) {
                            return resp;
                        }
                        Map<String, Object> wrapper = Map.of("ok", true, "status", resp.getStatusCode().value(), "json", parsedMap);
                        String out = m2.writeValueAsString(wrapper);
                        return new ResponseEntity<>(out, resp.getStatusCode());
                    } else {
                        Map<String, Object> wrapper = Map.of("ok", true, "status", resp.getStatusCode().value(), "json", Map.of("value", parsed));
                        String out = m2.writeValueAsString(wrapper);
                        return new ResponseEntity<>(out, resp.getStatusCode());
                    }
                } catch (Exception e) {
                    Map<String, Object> wrapper = Map.of("ok", true, "status", resp.getStatusCode().value(), "json", Map.of("message", bodyStr));
                    String out = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(wrapper);
                    return new ResponseEntity<>(out, resp.getStatusCode());
                }
            }

            return resp;
        } catch (HttpStatusCodeException ex) {
            return new ResponseEntity<>(ex.getResponseBodyAsString(), ex.getStatusCode());
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<String> resetAdminSessions(String incomingAuth) {
        String url = baseUrl + "/admin/sessions/reset";
        HttpEntity<Void> request = new HttpEntity<>(buildHeaders(incomingAuth));
        try {
            return restTemplate.exchange(url, HttpMethod.POST, request, String.class);
        } catch (HttpStatusCodeException ex) {
            return new ResponseEntity<>(ex.getResponseBodyAsString(), ex.getStatusCode());
        }
    }
}

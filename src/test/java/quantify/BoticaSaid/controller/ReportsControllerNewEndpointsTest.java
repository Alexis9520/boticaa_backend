package quantify.BoticaSaid.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ReportsControllerNewEndpointsTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    void testReceiveIdEndpointWithValidId() throws Exception {
        String jsonPayload = "{\"id\": 1}";

        mockMvc.perform(post("/api/reports/receive-id")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("ID recibido correctamente"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.received_at").exists());
    }

    @Test
    @WithMockUser
    void testReceiveIdEndpointWithStringId() throws Exception {
        String jsonPayload = "{\"id\": \"test-123\"}";

        mockMvc.perform(post("/api/reports/receive-id")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("ID recibido correctamente"))
                .andExpect(jsonPath("$.id").value("test-123"))
                .andExpect(jsonPath("$.received_at").exists());
    }

    @Test
    @WithMockUser
    void testReceiveIdEndpointWithMissingId() throws Exception {
        String jsonPayload = "{\"other_field\": 1}";

        mockMvc.perform(post("/api/reports/receive-id")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("El campo 'id' es requerido"));
    }

    @Test
    @WithMockUser
    void testReceiveIdEndpointWithEmptyBody() throws Exception {
        String jsonPayload = "{}";

        mockMvc.perform(post("/api/reports/receive-id")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("El campo 'id' es requerido"));
    }

    @Test
    @WithMockUser
    void testLotesJsonEndpointReturnsJson() throws Exception {
        String fechaInicio = LocalDate.now().minusDays(30).toString();
        String fechaFin = LocalDate.now().toString();

        mockMvc.perform(get("/api/reports/lotes-json")
                .param("fechaInicio", fechaInicio)
                .param("fechaFin", fechaFin))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser
    void testLotesJsonEndpointWithInvalidDatesReturnsBadRequest() throws Exception {
        // Test with invalid date format - should return 400 Bad Request
        mockMvc.perform(get("/api/reports/lotes-json")
                .param("fechaInicio", "invalid-date")
                .param("fechaFin", LocalDate.now().toString()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @WithMockUser
    void testLotesJsonEndpointWithMissingDatesReturnsBadRequest() throws Exception {
        // Test without required parameters
        mockMvc.perform(get("/api/reports/lotes-json"))
                .andExpect(status().isBadRequest());
    }
}

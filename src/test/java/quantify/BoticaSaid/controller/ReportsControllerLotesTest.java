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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ReportsControllerLotesTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    void testLotesEndpointReturnsExcel() throws Exception {
        String fechaInicio = LocalDate.now().minusDays(30).toString();
        String fechaFin = LocalDate.now().toString();

        mockMvc.perform(get("/api/reports/lotes")
                .param("fechaInicio", fechaInicio)
                .param("fechaFin", fechaFin))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", 
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .andExpect(header().exists("Content-Disposition"));
    }

    @Test
    @WithMockUser
    void testLotesEndpointWithInvalidDatesReturnsBadRequest() throws Exception {
        // Test with invalid date format - should return 400 Bad Request
        mockMvc.perform(get("/api/reports/lotes")
                .param("fechaInicio", "invalid-date")
                .param("fechaFin", LocalDate.now().toString()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"));
    }
}

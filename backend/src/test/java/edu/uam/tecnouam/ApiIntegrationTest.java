package edu.uam.tecnouam;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
      "app.jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=",
      "app.admin.password=IntegrationAdmin123!",
      "app.user.password=IntegrationUser123!"
    })
@AutoConfigureMockMvc
class ApiIntegrationTest {
  @Autowired MockMvc mvc;
  @Autowired ObjectMapper json;

  String login(String user, String password) throws Exception {
    return json.readTree(
            mvc.perform(
                    post("/api/auth/login")
                        .contentType("application/json")
                        .content(
                            json.writeValueAsString(
                                java.util.Map.of("username", user, "password", password))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString())
        .get("token")
        .asText();
  }

  @Test
  void completeBusinessAndSecurityFlow() throws Exception {
    mvc.perform(get("/api/equipos")).andExpect(status().isUnauthorized());
    mvc.perform(
            post("/api/auth/login")
                .contentType("application/json")
                .content("{\"username\":\"admin\",\"password\":\"wrong\"}"))
        .andExpect(status().isUnauthorized());
    String user = "Bearer " + login("user", "IntegrationUser123!"),
        admin = "Bearer " + login("admin", "IntegrationAdmin123!");
    mvc.perform(get("/api/equipos").header("Authorization", user)).andExpect(status().isOk());
    String equipment =
        "{\"codigoPatrimonial\":\"TEST-001\",\"nombre\":\"Equipo prueba\",\"tipo\":\"PORTATIL\"}";
    mvc.perform(
            post("/api/equipos")
                .header("Authorization", user)
                .contentType("application/json")
                .content(equipment))
        .andExpect(status().isForbidden());
    long id =
        json.readTree(
                mvc.perform(
                        post("/api/equipos")
                            .header("Authorization", admin)
                            .contentType("application/json")
                            .content(equipment))
                    .andExpect(status().isCreated())
                    .andReturn()
                    .getResponse()
                    .getContentAsString())
            .get("id")
            .asLong();
    mvc.perform(
            post("/api/equipos")
                .header("Authorization", admin)
                .contentType("application/json")
                .content(equipment))
        .andExpect(status().isConflict());
    mvc.perform(
            put("/api/equipos/" + id)
                .header("Authorization", admin)
                .contentType("application/json")
                .content(equipment.replace("Equipo prueba", "Equipo actualizado")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.nombre").value("Equipo actualizado"));
    String loan =
        "{\"equipoId\":"
            + id
            + ",\"solicitante\":\"Instructor\",\"fechaPrestamo\":\"2026-10-26\",\"fechaPrevistaDevolucion\":\"2026-10-28\"}";
    mvc.perform(
            post("/api/prestamos")
                .header("Authorization", admin)
                .contentType("application/json")
                .content(loan.replace("2026-10-28", "2026-10-20")))
        .andExpect(status().isBadRequest());
    long pid =
        json.readTree(
                mvc.perform(
                        post("/api/prestamos")
                            .header("Authorization", admin)
                            .contentType("application/json")
                            .content(loan))
                    .andExpect(status().isCreated())
                    .andReturn()
                    .getResponse()
                    .getContentAsString())
            .get("id")
            .asLong();
    mvc.perform(get("/api/equipos/" + id).header("Authorization", user))
        .andExpect(jsonPath("$.estado").value("PRESTADO"));
    mvc.perform(
            post("/api/prestamos")
                .header("Authorization", admin)
                .contentType("application/json")
                .content(loan))
        .andExpect(status().isConflict());
    mvc.perform(delete("/api/equipos/" + id).header("Authorization", admin))
        .andExpect(status().isConflict());
    mvc.perform(put("/api/prestamos/" + pid + "/devolver").header("Authorization", admin))
        .andExpect(status().isOk());
    mvc.perform(put("/api/prestamos/" + pid + "/devolver").header("Authorization", admin))
        .andExpect(status().isConflict());
    mvc.perform(get("/api/equipos/" + id).header("Authorization", user))
        .andExpect(jsonPath("$.estado").value("DISPONIBLE"));
    mvc.perform(delete("/api/equipos/" + id).header("Authorization", admin))
        .andExpect(status().isNoContent());
    mvc.perform(get("/api/equipos/" + id).header("Authorization", user))
        .andExpect(status().isNotFound());
    mvc.perform(get("/api/equipos").header("Authorization", admin + "invalid"))
        .andExpect(status().isUnauthorized());
  }
}

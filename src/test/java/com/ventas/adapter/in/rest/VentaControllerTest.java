package com.ventas.adapter.in.rest;

import com.ventas.application.port.in.ConsultarProductosUseCase;
import com.ventas.application.port.in.RegistrarVentaUseCase;
import com.ventas.model.Producto;
import com.ventas.model.Venta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba del adaptador REST aislado: los puertos de entrada se sustituyen por dobles en memoria,
 * sin base de datos ni Mockito.
 */
@WebMvcTest(VentaController.class)
class VentaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RegistroFalso registro;

    @BeforeEach
    void limpiar() {
        registro.ventas.clear();
    }

    @Test
    void registrarVentaValidaDevuelve201ConLaVentaPersistida() throws Exception {
        String cuerpo = "{\"partidas\":[{\"productoId\":101,\"cantidad\":2},{\"productoId\":102,\"cantidad\":1}]}";

        mockMvc.perform(post("/ventas").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fecha", notNullValue()))
                .andExpect(jsonPath("$.descuento").value(0.0))
                .andExpect(jsonPath("$.total").value(2050.0))
                .andExpect(jsonPath("$.partidas", hasSize(2)))
                .andExpect(jsonPath("$.partidas[0].productoId").value(101))
                .andExpect(jsonPath("$.partidas[0].nombre").value("Teclado mecánico"))
                .andExpect(jsonPath("$.partidas[0].cantidad").value(2))
                .andExpect(jsonPath("$.partidas[0].precioUnitario").value(850.0))
                .andExpect(jsonPath("$.partidas[0].subtotal").value(1700.0));

        assertEquals(1, registro.ventas.size(), "El controlador debe delegar en RegistrarVentaUseCase");
    }

    @Test
    void peticionSinPartidasDevuelve400() throws Exception {
        mockMvc.perform(post("/ventas").contentType(MediaType.APPLICATION_JSON).content("{\"partidas\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Entrada inválida"))
                .andExpect(jsonPath("$.detail", containsString("partidas")));

        assertTrue(registro.ventas.isEmpty());
    }

    @Test
    void peticionVaciaDevuelve400() throws Exception {
        mockMvc.perform(post("/ventas").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", containsString("partidas")));

        assertTrue(registro.ventas.isEmpty());
    }

    @Test
    void cantidadMenorAUnoDevuelve400() throws Exception {
        String cuerpo = "{\"partidas\":[{\"productoId\":101,\"cantidad\":0}]}";

        mockMvc.perform(post("/ventas").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", containsString("cantidad")));

        assertTrue(registro.ventas.isEmpty());
    }

    @Test
    void productoIdFaltanteDevuelve400() throws Exception {
        String cuerpo = "{\"partidas\":[{\"cantidad\":3}]}";

        mockMvc.perform(post("/ventas").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", containsString("productoId")));
    }

    @Test
    void productoInexistenteDevuelve400() throws Exception {
        String cuerpo = "{\"partidas\":[{\"productoId\":999,\"cantidad\":1}]}";

        mockMvc.perform(post("/ventas").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", containsString("999")));

        assertTrue(registro.ventas.isEmpty());
    }

    @Test
    void jsonMalFormadoDevuelve400() throws Exception {
        mockMvc.perform(post("/ventas").contentType(MediaType.APPLICATION_JSON).content("{\"partidas\":"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    static class RegistroFalso implements RegistrarVentaUseCase {
        final List<Venta> ventas = new ArrayList<>();

        @Override
        public Venta registrar(Venta venta) {
            ventas.add(venta);
            venta.asignarId((long) ventas.size());
            return venta;
        }
    }

    @TestConfiguration
    static class PuertosFalsos {

        @Bean
        RegistroFalso registrarVentaUseCase() {
            return new RegistroFalso();
        }

        @Bean
        ConsultarProductosUseCase consultarProductosUseCase() {
            return () -> List.of(
                    new Producto("101", "Teclado mecánico", 850.00, 12),
                    new Producto("102", "Mouse óptico", 350.00, 25));
        }
    }
}

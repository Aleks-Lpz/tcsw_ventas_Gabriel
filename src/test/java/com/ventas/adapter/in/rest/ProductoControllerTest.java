package com.ventas.adapter.in.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ventas.application.port.in.ConsultarProductosUseCase;
import com.ventas.model.Producto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

class ProductoControllerTest {
    private MockMvc mvc;
    private AtomicReference<Producto> registrado;
    private AtomicReference<String> consultado;
    private final ObjectMapper json = new ObjectMapper();

    @BeforeEach
    void configurar() {
        registrado = new AtomicReference<>();
        consultado = new AtomicReference<>();
        ConsultarProductosUseCase consulta = new ConsultarProductosUseCase() {
            public java.util.List<Producto> consultarTodos() {
                throw new AssertionError("El controlador debe consultar por identificador");
            }
            public Optional<Producto> consultarPorId(String codigo) {
                consultado.set(codigo);
                return "P001".equals(codigo)
                        ? Optional.of(new Producto(codigo, "Café", 20.5, 4)) : Optional.empty();
            }
        };
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mvc = MockMvcBuilders.standaloneSetup(new ProductoController(consulta, producto -> {
            registrado.set(producto);
            return new Producto(producto.getCodigo(), "Nombre persistido", producto.getPrecio(),
                    producto.getExistencia());
        })).setValidator(validator).build();
    }

    @Test
    void consultaPorCodigoYRespondeConDto() throws Exception {
        MvcResult result = mvc.perform(get("/productos/P001")).andReturn();
        assertEquals(200, result.getResponse().getStatus());
        assertEquals("P001", consultado.get());
        JsonNode body = json.readTree(result.getResponse().getContentAsByteArray());
        assertEquals("P001", body.get("codigo").asText());
        assertEquals("Café", body.get("nombre").asText());
        assertEquals(20.5, body.get("precio").asDouble());
        assertEquals(4, body.get("existencia").asInt());
    }

    @Test
    void productoInexistenteResponde404() throws Exception {
        assertEquals(404, mvc.perform(get("/productos/desconocido"))
                .andReturn().getResponse().getStatus());
    }

    @Test
    void registraYResponde201ConLocationYDatosPersistidos() throws Exception {
        MvcResult result = mvc.perform(post("/productos").contentType(MediaType.APPLICATION_JSON)
                .content("{\"codigo\":\"P002\",\"nombre\":\"Café\",\"precio\":25.75,\"existencia\":0}"))
                .andReturn();
        assertEquals(201, result.getResponse().getStatus());
        assertEquals("http://localhost/productos/P002", result.getResponse().getHeader("Location"));
        assertEquals("P002", registrado.get().getCodigo());
        assertEquals("Café", registrado.get().getNombre());
        assertEquals(25.75, registrado.get().getPrecio());
        assertEquals(0, registrado.get().getExistencia());
        JsonNode body = json.readTree(result.getResponse().getContentAsByteArray());
        assertEquals("Nombre persistido", body.get("nombre").asText());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{}",
            "{\"codigo\":\" \",\"nombre\":\"Café\",\"precio\":1,\"existencia\":0}",
            "{\"codigo\":\"P\",\"nombre\":\" \",\"precio\":1,\"existencia\":0}",
            "{\"codigo\":\"P\",\"nombre\":\"Café\",\"precio\":null,\"existencia\":0}",
            "{\"codigo\":\"P\",\"nombre\":\"Café\",\"precio\":1,\"existencia\":null}",
            "{\"codigo\":\"P\",\"nombre\":\"Café\",\"precio\":0,\"existencia\":0}",
            "{\"codigo\":\"P\",\"nombre\":\"Café\",\"precio\":-1,\"existencia\":0}",
            "{\"codigo\":\"P\",\"nombre\":\"Café\",\"precio\":1,\"existencia\":-1}",
            "{\"codigo\":\"P\",\"nombre\":\"Café\",\"precio\":\"abc\",\"existencia\":0}",
            "{malformed"
    })
    void entradaInvalidaResponde400SinInvocarRegistro(String body) throws Exception {
        assertEquals(400, mvc.perform(post("/productos").contentType(MediaType.APPLICATION_JSON)
                .content(body)).andReturn().getResponse().getStatus());
        assertNull(registrado.get());
    }
}

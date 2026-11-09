package com.ventas.adapter.in.web;

import com.ventas.adapter.in.rest.ProductoController;
import com.ventas.adapter.in.web.exception.GlobalExceptionHandler;
import com.ventas.application.port.in.ConsultarProductosUseCase;
import com.ventas.application.port.in.RegistrarProductoUseCase;
import com.ventas.model.Producto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductoController.class)
@Import(GlobalExceptionHandler.class)
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ConsultarProductosUseCase consultarProductosUseCase;

    @MockBean
    private RegistrarProductoUseCase registrarProductoUseCase;

    @Test
    void testGetProductoExistente() throws Exception {
        when(consultarProductosUseCase.consultarPorId(any()))
                .thenReturn(Optional.of(new Producto("Producto Test", "Descripcion", 100.0, 10)));

        mockMvc.perform(get("/productos/1"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetProductoInexistente() throws Exception {
        when(consultarProductosUseCase.consultarPorId(any()))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/productos/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testPostProductoPayloadInvalido() throws Exception {
        String payloadJson = "{\"nombre\": \"\", \"precio\": -10}";

        mockMvc.perform(post("/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Entrada inválida"));
    }
}

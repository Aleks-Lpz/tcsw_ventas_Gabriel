package com.ventas.adapter.in.rest;

import com.ventas.adapter.in.rest.dto.NuevaVentaPeticion;
import com.ventas.adapter.in.rest.dto.VentaRespuesta;
import com.ventas.application.port.in.ConsultarProductosUseCase;
import com.ventas.application.port.in.RegistrarVentaUseCase;
import com.ventas.model.Venta;

import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptador de entrada REST para el recurso /ventas (docs/openapi.yaml).
 *
 * <p>Solo habla con puertos de entrada: resuelve los productos con {@link ConsultarProductosUseCase}
 * y delega el registro a {@link RegistrarVentaUseCase}.
 */
@RestController("ventaRestController")
@RequestMapping("/ventas")
public class VentaController {

    private final RegistrarVentaUseCase registrarVentaUseCase;
    private final ConsultarProductosUseCase consultarProductosUseCase;

    public VentaController(RegistrarVentaUseCase registrarVentaUseCase,
                           ConsultarProductosUseCase consultarProductosUseCase) {
        this.registrarVentaUseCase = registrarVentaUseCase;
        this.consultarProductosUseCase = consultarProductosUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VentaRespuesta registrar(@Valid @RequestBody NuevaVentaPeticion peticion) {
        Venta venta = VentaRestMapper.aVenta(peticion, consultarProductosUseCase.consultarTodos());
        Venta registrada = registrarVentaUseCase.registrar(venta);
        return VentaRestMapper.aRespuesta(registrada);
    }
}

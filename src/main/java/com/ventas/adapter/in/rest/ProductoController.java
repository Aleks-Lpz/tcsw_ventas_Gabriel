package com.ventas.adapter.in.rest;

import com.ventas.application.port.in.ConsultarProductosUseCase;
import com.ventas.application.port.in.RegistrarProductoUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;

@RestController
@RequestMapping("/productos")
public class ProductoController {
    private final ConsultarProductosUseCase consultarProductos;
    private final RegistrarProductoUseCase registrarProducto;

    public ProductoController(ConsultarProductosUseCase consultarProductos, RegistrarProductoUseCase registrarProducto) {
        this.consultarProductos = consultarProductos;
        this.registrarProducto = registrarProducto;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponse> consultar(@PathVariable("id") String id) {
        return consultarProductos.consultarPorId(id)
                .map(ProductoRestMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ProductoResponse> registrar(@Valid @RequestBody ProductoRequest request) {
        ProductoResponse response = ProductoRestMapper.toResponse( registrarProducto.registrar(ProductoRestMapper.toDomain(request)));
        return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(response.getCodigo()).encode().toUri())
                .body(response);
    }
}

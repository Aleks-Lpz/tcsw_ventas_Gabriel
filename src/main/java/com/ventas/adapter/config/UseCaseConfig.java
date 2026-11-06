package com.ventas.adapter.config;

import com.ventas.application.port.in.ConsultarProductosUseCase;
import com.ventas.application.port.in.RegistrarProductoUseCase;
import com.ventas.application.port.out.ProductoRepository;
import com.ventas.application.service.ProductoService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {
    @Bean
    public ConsultarProductosUseCase consultarProductosUseCase(ProductoRepository productoRepository) {
        return new ProductoService(productoRepository);
    }

    @Bean
    public RegistrarProductoUseCase registrarProductoUseCase(ProductoRepository productoRepository) {
        return new ProductoService(productoRepository);
    }
}
package com.ventas.adapter.config;

import com.ventas.adapter.out.hibernate.ConfiguracionHibernate;
import com.ventas.adapter.out.hibernate.HibernateProductoRepository;
import com.ventas.application.port.in.ConsultarProductosUseCase;
import com.ventas.application.port.in.RegistrarProductoUseCase;
import com.ventas.application.port.out.ProductoRepository;
import com.ventas.application.service.ProductoService;
import javax.persistence.EntityManagerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public EntityManagerFactory entityManagerFactory() {
        ConfiguracionHibernate config = ConfiguracionHibernate.desdeEntorno()
            .orElseThrow(() -> new IllegalStateException("Faltan variables de entorno DB_URL, DB_USER o DB_PASSWORD"));
        return config.crearEntityManagerFactory();
    }

    @Bean 
    public ProductoRepository productoRepository(EntityManagerFactory emf) {
        return new HibernateProductoRepository(emf);
    }

    @Bean
    public ProductoService productoService(ProductoRepository productoRepository) {
        return new ProductoService(productoRepository);
    }

    @Bean
    public ConsultarProductosUseCase consultarProductosUseCase(ProductoService productoService) {
        return productoService;
    }

    @Bean
    public RegistrarProductoUseCase registrarProductoUseCase(ProductoService productoService) {
        return productoService;
    }
}

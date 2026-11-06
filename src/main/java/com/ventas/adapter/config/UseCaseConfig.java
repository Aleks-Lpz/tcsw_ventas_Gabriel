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

    // 1. Define a Bean for the EntityManagerFactory using your custom logic
    @Bean
    public EntityManagerFactory entityManagerFactory() {
        ConfiguracionHibernate config = ConfiguracionHibernate.desdeEntorno()
            .orElseThrow(() -> new IllegalStateException("Faltan variables de entorno DB_URL, DB_USER o DB_PASSWORD"));
        return config.crearEntityManagerFactory();
    }

    // 2. Spring will now inject the EntityManagerFactory created above
    @Bean 
    public ProductoRepository productoRepository(EntityManagerFactory emf) {
        return new HibernateProductoRepository(emf);
    }

    @Bean
    public ConsultarProductosUseCase consultarProductosUseCase(ProductoRepository productoRepository) {
        return new ProductoService(productoRepository);
    }

    @Bean
    public RegistrarProductoUseCase registrarProductoUseCase(ProductoRepository productoRepository) {
        return new ProductoService(productoRepository);
    }
}
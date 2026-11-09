package com.ventas.adapter.config;

import javax.persistence.EntityManagerFactory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ventas.adapter.out.hibernate.ConfiguracionHibernate;
import com.ventas.adapter.out.hibernate.HibernateProductoRepository;
import com.ventas.adapter.out.hibernate.HibernateVentaRepository;
import com.ventas.adapter.out.notification.ReciboObservador;
import com.ventas.application.port.in.RegistrarVentaUseCase;
import com.ventas.application.port.out.ProductoRepository;
import com.ventas.application.port.out.PublicadorDeEventos;
import com.ventas.application.port.out.VentaRepository;
import com.ventas.application.service.ProductoService;
import com.ventas.application.service.PublicadorSimple;
import com.ventas.application.service.RegistrarVentaService;
import com.ventas.model.PoliticaDescuento;

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

    // Única declaración necesaria. Spring la usará automáticamente 
    // para inyectar ConsultarProductosUseCase y RegistrarProductoUseCase.
    @Bean
    public ProductoService productoService(ProductoRepository productoRepository) {
        return new ProductoService(productoRepository);
    }

    @Bean
    public VentaRepository ventaRepository(EntityManagerFactory emf) {
        return new HibernateVentaRepository(emf);
    }

    @Bean
    public PoliticaDescuento politicaDescuento() {
        return venta -> venta.getPartidas().size() > 10 ? venta.calcularTotal() * 0.15 : 0.0;
    }

    @Bean
    public PublicadorDeEventos publicadorDeEventos() {
        PublicadorSimple publicador = new PublicadorSimple();
        publicador.agregarObservador(new ReciboObservador());
        return publicador;
    }

    @Bean
    public RegistrarVentaUseCase registrarVentaUseCase(VentaRepository ventaRepository,
                                                       PoliticaDescuento politicaDescuento,
                                                       PublicadorDeEventos publicadorDeEventos) {
        return new RegistrarVentaService(ventaRepository, politicaDescuento, publicadorDeEventos);
    }
}
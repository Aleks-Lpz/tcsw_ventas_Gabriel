package com.ventas.architecture;

import com.ventas.application.port.in.RegistrarVentaUseCase;
import com.ventas.application.service.RegistrarVentaService;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class HexagonalArchitectureTest {

    @Test
    void registrarVentaServiceDependeDelPuertoDeSalidaYNoDelAdaptadorConcreto() throws Exception {
        Path serviceFile = Paths.get("src/main/java/com/ventas/application/service/RegistrarVentaService.java");
        String serviceCode = Files.readString(serviceFile);

        assertTrue(serviceCode.contains("package com.ventas.application.service;"));
        assertTrue(serviceCode.contains("implements RegistrarVentaUseCase"));
        assertTrue(serviceCode.contains("import com.ventas.application.port.out.VentaRepository;"));
        assertFalse(serviceCode.contains("import com.ventas.adapter."));
        assertFalse(serviceCode.contains("com.ventas.adapter"));

        Field field = RegistrarVentaService.class.getDeclaredField("ventaRepository");
        assertEquals("com.ventas.application.port.out.VentaRepository", field.getType().getName());
        assertTrue(RegistrarVentaUseCase.class.getName().equals("com.ventas.application.port.in.RegistrarVentaUseCase"));
        assertTrue(RegistrarVentaService.class.getName().equals("com.ventas.application.service.RegistrarVentaService"));
    }

    @Test
    void elDominioNoDependeDeApplicationNiDeAdapter() throws IOException {
        Path modelDir = Paths.get("src/main/java/com/ventas/model");
        List<Path> files = Files.list(modelDir)
                .filter(path -> path.toString().endsWith(".java"))
                .collect(Collectors.toList());

        assertTrue(files.stream().anyMatch(path -> path.getFileName().toString().equals("Producto.java")));
        assertTrue(files.stream().anyMatch(path -> path.getFileName().toString().equals("Partida.java")));
        assertTrue(files.stream().anyMatch(path -> path.getFileName().toString().equals("Venta.java")));

        for (Path file : files) {
            String content = Files.readString(file);
            assertFalse(content.contains("com.ventas.application"),
                    "El dominio no debe depender de application: " + file.getFileName());
            assertFalse(content.contains("com.ventas.adapter"),
                    "El dominio no debe depender de adapter: " + file.getFileName());
        }
    }
}

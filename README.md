# P01. Ambiente reproducible y Producto 

Proyecto desarrollado para la experiencia educativa ***Tecnologías para la Construcción de Software**.
El proyecto ahora incluye el modelado de ventas en memoria con las clases Producto, Partida y Venta. 

## Prerrequisitos 
* java 11
* Maven 
* Git

## Instrucciones de clonación, compilacion y pruebas 

1. **Clonar el repositorio**
```bash
   git clone https://github.com/Aleks-Lpz/tcsw_ventas_Gabriel
   cd tcsw_ventas_Gabriel
```

2. **Compilar y ejecutar las pruebas**
```bash
 mvn clean test
```

3. **Resultado esperado**

```
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0 -- in com.ventas.model.VentaTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0 -- in com.ventas.model.ProductoTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS

```
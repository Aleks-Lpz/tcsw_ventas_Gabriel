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

# P05 - Núcleo hexagonal

## A. Objetivo

Se implementó el caso de uso Registrar Venta mediante una estructura hexagonal simple: dominio con clases existentes, capa de aplicación con puertos y caso de uso, y un adaptador en memoria para salida.

## B. Estructura real

```text
src/main/java/com/ventas/
├── adapter/
│   └── out/
│       └── memory/
│           └── InMemoryVentaRepository.java
├── application/
│   ├── port/
│   │   ├── in/
│   │   │   └── RegistrarVentaUseCase.java
│   │   └── out/
│   │       └── VentaRepository.java
│   └── service/
│       └── RegistrarVentaService.java
├── model/
│   ├── Partida.java
│   ├── Producto.java
│   └── Venta.java
└── ...
```

Las clases del modelo fueron mantenidas sin refactorización general. Los nuevos archivos de la contribución P05 son los que aparecen en `application` y `adapter`.

## C. Puerto de entrada

- Nombre exacto: `RegistrarVentaUseCase`
- Paquete: `com.ventas.application.port.in`
- Responsabilidad: definir la operación externa que el núcleo acepta para registrar una venta.
- Método público: `Venta registrar(Venta venta);`

## D. Puerto de salida

- Nombre exacto: `VentaRepository`
- Paquete: `com.ventas.application.port.out`
- Responsabilidad: abstraer la necesidad de persistencia de una venta sin obligar al caso de uso a depender de una infraestructura concreta.
- Método público: `Venta guardar(Venta venta);`

## E. Caso de uso

- Nombre: `RegistrarVentaService`
- Paquete: `com.ventas.application.service`
- Responsabilidad: ejecutar la operación de registro invocando el puerto de salida `VentaRepository`.
- Dependencia: depende de la interfaz `VentaRepository` y no conoce `InMemoryVentaRepository`.
- No depende del adaptador concreto; sólo depende de la abstracción del puerto de salida.

## F. Adaptador

- Nombre: `InMemoryVentaRepository`
- Paquete: `com.ventas.adapter.out.memory`
- Responsabilidad: almacenar ventas en memoria como implementación concreta del puerto de salida.
- Almacenamiento: `List<Venta>` con retorno inmutable para `obtenerTodas()`.

## G. Flujo

```text
Entrada
  ↓
RegistrarVentaUseCase
  ↓
RegistrarVentaService
  ↓
VentaRepository
  ↑
InMemoryVentaRepository
```

## H. Pruebas

Archivos reales de pruebas existentes:

- `src/test/java/com/ventas/application/service/RegistrarVentaServiceTest.java`
  - `unaVentaValidaPuedeRegistrarse`
  - `elServicioUsaElVentaRepositoryParaGuardarLaVenta`
  - `laVentaDevueltaCorrespondeALaVentaRegistrada`
  - `unaVentaNullEsRechazada`
  - `noSePuedeCrearElServicioSinVentaRepository`

- `src/test/java/com/ventas/adapter/out/memory/InMemoryVentaRepositoryTest.java`
  - `guardarUnaVenta`
  - `recuperarLaVentaGuardada`
  - `guardarVariasVentas`
  - `obtenerTodasNoPermiteModificarLaColeccionInterna`
  - `unaVentaNullEsRechazada`

- `src/test/java/com/ventas/architecture/HexagonalArchitectureTest.java`
  - `registrarVentaServiceDependeDelPuertoDeSalidaYNoDelAdaptadorConcreto`
  - `elDominioNoDependeDeApplicationNiDeAdapter`

## I. Verificación

Se ejecutó:

```bash
mvn clean test
```

Resultado real:

```text
[INFO] Tests run: 18, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

Se ejecutó:

```bash
git diff --check
```

Resultado real: no produjo salida, es decir, no hubo errores de formato ni espacios finales en el diff.

## J. Estado de Git

Se ejecutó:

```bash
git status --short
```

Resultado real en esta rama:

```text
?? src/main/java/com/ventas/adapter/
?? src/main/java/com/ventas/application/
?? src/test/java/com/ventas/adapter/
?? src/test/java/com/ventas/application/
?? src/test/java/com/ventas/architecture/
```

Esto indica que los archivos nuevos de la contribución P05 quedaron sin confirmar y que no se realizaron commits ni pushes. No se eliminaron archivos existentes ni se modificó el modelo para esta actividad.

## K. Decisiones arquitectónicas

- Existe un puerto de entrada porque el exterior necesita una operación clara para registrar una venta sin depender de detalles de infraestructura.
- Existe un puerto de salida porque el caso de uso necesita guardar una venta, pero esa persistencia puede implementarse de varias formas.
- `RegistrarVentaService` depende de la interfaz `VentaRepository` para cumplir con la inversión de dependencias y mantener el núcleo independiente de infraestructura.
- `InMemoryVentaRepository` es un adaptador porque implementa el puerto de salida con una estrategia concreta en memoria.
- El servicio evita depender de infraestructura concreta porque sólo conoce la interfaz y no nombre de clases del adaptador.

## Verificación P05

| Verificación | Estado | Evidencia |
|---|---|---|
| Puerto de entrada | VERIFICADO | `src/main/java/com/ventas/application/port/in/RegistrarVentaUseCase.java` |
| Puerto de salida | VERIFICADO | `src/main/java/com/ventas/application/port/out/VentaRepository.java` |
| Caso de uso | VERIFICADO | `src/main/java/com/ventas/application/service/RegistrarVentaService.java` |
| Adaptador en memoria | VERIFICADO | `src/main/java/com/ventas/adapter/out/memory/InMemoryVentaRepository.java` |
| Pruebas unitarias | VERIFICADO | `mvn clean test` con 18 pruebas ejecutadas y 0 fallos |
| Dependencias hexagonales | VERIFICADO | prueba de arquitectura en `src/test/java/com/ventas/architecture/HexagonalArchitectureTest.java` |
| `mvn clean test` | VERIFICADO | resultado real mostrado arriba |
| `git diff --check` | VERIFICADO | no produjo salida, sin errores de diff |

Se detectaron artefactos no tratados por la actividad en `src/main/java/com/ventas/model/Partida.javamicro` y `src/main/java/com/ventas/model/com/ventas/model/Producto.class`; no fueron modificados, siguiendo la restricción del proyecto.
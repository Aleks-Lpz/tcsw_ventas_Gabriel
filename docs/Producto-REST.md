# Adaptador REST de productos

Fecha: 8-nov-2026

## Implementación

El paquete `src/main/java/com/ventas/adapter/in/rest` contiene:

- `ProductoRequest`: entrada JSON con `codigo` y `nombre` (`@NotBlank`),
  `precio` (`@NotNull`, `@Positive`) y `existencia` (`@NotNull`, `@PositiveOrZero`).
  Los tipos numéricos envoltorio permiten detectar campos omitidos o nulos.
- `ProductoResponse`: salida con los cuatro campos, sin exponer el objeto de dominio.
- `ProductoRestMapper`: conversión explícita de request a dominio y de dominio a response.
- `ProductoController`: traducción HTTP y despacho a los puertos de entrada,
  inyectados por constructor. No contiene SQL, cálculos ni reglas de existencias.

El precio debe ser estrictamente positivo porque esa es la regla actual de `Producto`.
La existencia cero es válida. La validación de entrada ocurre antes del mapeo y del registro.

## Contrato HTTP

| Solicitud | Resultado |
|---|---|
| `GET /productos/{id}` | 200 con `ProductoResponse`; 404 si no existe |
| `POST /productos` | 201 con `ProductoResponse` y cabecera `Location` |
| POST con campos inválidos, omitidos, nulos o JSON malformado | 400, sin invocar el registro |

Ejemplo de entrada y forma de salida:

```json
{"codigo":"P001","nombre":"Café","precio":25.75,"existencia":0}
```

`id` corresponde al `codigo` de dominio; no se introduce otro identificador.
`ConsultarProductosUseCase.consultarPorId` retorna `Optional<Producto>` y ofrece
una implementación por defecto sobre `consultarTodos`, conservando los consumidores
existentes. El controlador delega la búsqueda al puerto.

Se incorporan Spring Web MVC y Bean Validation mediante starters Spring Boot 2.7.18,
compatibles con Java 11, y Spring Test 5.3.31 para verificar HTTP con MockMvc.
El adaptador requiere un contexto Spring MVC que registre el controlador y los beans
`ConsultarProductosUseCase` y `RegistrarProductoUseCase` (por ejemplo, la misma instancia
de `ProductoService` con el repositorio elegido). Este cambio no incorpora un lanzador
de servidor ni cambia el arranque Swing existente.

## Evidencia de pruebas

Pruebas de conversión:
`src/test/java/com/ventas/adapter/in/rest/ProductoRestMapperTest.java` (2 pruebas).
Comprueban los cuatro campos en ambas direcciones y la independencia de los objetos.

Pruebas HTTP:
`src/test/java/com/ventas/adapter/in/rest/ProductoControllerTest.java` (13 casos).
Comprueban GET, ausencia, POST, ubicación, respuesta del registro y rechazo de entradas
inválidas antes de invocar el caso de uso.

Se agregan dos pruebas de consulta por código en
`src/test/java/com/ventas/application/service/ConsultarProductosUseCaseTest.java`.

Comando ejecutado:

```bash
mvn -Djava.awt.headless=true test
```

Resultado:

```text
Tests run: 100, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

La ejecución inicial sin `java.awt.headless` falló en pruebas Swing porque el entorno
no dispone de pantalla X11. Con el modo sin interfaz gráfica la suite completa pasa.
Los reportes de cada clase quedan en `target/surefire-reports/`.
No se ejecutaron las pruebas de integración `*IT` que requieren PostgreSQL.

`git diff --check` también pasó sin errores.

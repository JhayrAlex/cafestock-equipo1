# CaféStock · Equipo 1 (Escenario A)

Aplicación de escritorio en Java Swing para que Rosa, administradora de la cafetería de la facultad, controle su inventario y registre sus ventas sin usar cuaderno.

## Equipo

| Integrante | Rol Scrum |
|---|---|
| Jhayr Alexander Pazmiño Mena | Scrum Master |
| Abigail Rocio Becerra Bolaños | Product Owner |
| Deysi Abigail Guachamin Guauna | Desarrollador |
| Jasson Camilo Quirola Chicaiza | Desarrollador |

## Enlaces

- **Tablero de Trello:** https://trello.com/b/qjU7HtJc/equipo-1-caf%C3%A9stock-escenario-a
- **Repositorio en GitHub:** https://github.com/JhayrAlex/cafestock-equipo1

## Historias de usuario del Sprint 1

- **HU-A1:** Registrar productos con validaciones y persistencia.
- **HU-A2:** Consultar, buscar y editar productos existentes.
- **HU-A3:** Registrar ventas con cálculo automático del total y descuento de stock.

## Cómo ejecutar la aplicación

### Desde el archivo ejecutable (`.jar`)
1. Abrir la carpeta `dist/`.
2. Ejecutar en terminal: `java -jar cafestock.jar` (requiere Java 17 o superior) o hacer doble clic sobre `cafestock.jar`.
3. Mantener la carpeta `datos/` junto a `cafestock.jar`: ahí se cargan y guardan `productos.csv` y `ventas.csv` con los datos de prueba.

### Desde código fuente
1. Compilar: `javac -d bin src/cafestock/modelo/*.java src/cafestock/datos/*.java src/cafestock/servicio/*.java src/cafestock/ui/*.java src/cafestock/Main.java`
2. Ejecutar: `java -cp bin cafestock.Main`

## Estructura del proyecto

- `src/cafestock/modelo`: clases del negocio (`Producto`, `Venta`).
- `src/cafestock/datos`: lectura, actualización y guardado en CSV (`ProductoRepositorio`, `VentaRepositorio`).
- `src/cafestock/servicio`: validaciones de entrada y negocio (`ValidadorProducto`, `ValidadorVenta`).
- `src/cafestock/ui`: pantallas gráficas en Swing (`FormularioProductos`, `FormularioVentas`).
- `datos/`: archivos `productos.csv` y `ventas.csv` con datos iniciales y ventas de varios días.
- `dist/`: ejecutable `cafestock.jar` listo para entrega junto con su carpeta `datos/`.

## Convenciones del equipo

- Todo mensaje de commit inicia con el código de la historia: `HU-A1:`, `HU-A2:` o `HU-A3:`.
- Formato de los archivos CSV: separador `;`, punto decimal (`.`) y fechas `yyyy-MM-dd`.
- Cada integrante tiene máximo una tarjeta en **En progreso** y una tarjeta pasa a **Hecho** únicamente tras ser probada por otro integrante.
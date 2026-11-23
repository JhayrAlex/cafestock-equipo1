package cafestock.servicio;

/** Validaciones de la pantalla de venta (HU-A3). Lanzan IllegalArgumentException con el mensaje para el usuario. */
public final class ValidadorVenta {

    private ValidadorVenta() { }

    /** Cantidad entera, mayor que 0 y no mayor que el stock disponible. */
    public static int validarCantidad(String texto, int stockDisponible) {
        if (texto == null || texto.trim().isEmpty()) {
            throw new IllegalArgumentException("Ingrese la cantidad a vender.");
        }
        int cantidad;
        try {
            cantidad = Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("La cantidad debe ser un número entero.");
        }
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
        }
        if (cantidad > stockDisponible) {
            throw new IllegalArgumentException(
                    "Stock insuficiente: solo hay " + stockDisponible + " unidades disponibles.");
        }
        return cantidad;
    }

    /** Total = precio unitario x cantidad, redondeado a 2 decimales. */
    public static double calcularTotal(double precioUnitario, int cantidad) {
        return Math.round(precioUnitario * cantidad * 100.0) / 100.0;
    }
}

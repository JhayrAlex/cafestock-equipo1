package cafestock.servicio;

import cafestock.datos.ProductoRepositorio;
import cafestock.datos.VentaRepositorio;
import cafestock.modelo.Producto;
import cafestock.modelo.Venta;

import java.io.IOException;
import java.time.LocalDate;

/**
 * HU-A3 · Guardar venta y descontar stock.
 * Una sola operación: valida, calcula el total, descuenta el stock en productos.csv
 * y guarda la venta con su fecha en ventas.csv.
 * Si algo falla, lanza IllegalArgumentException (dato inválido) o IOException (archivo)
 * y no deja el stock a medias.
 */
public class VentaServicio {

    private final ProductoRepositorio productos;
    private final VentaRepositorio ventas;

    public VentaServicio(ProductoRepositorio productos, VentaRepositorio ventas) {
        this.productos = productos;
        this.ventas = ventas;
    }

    public Venta registrarVenta(String codigo, int cantidad) throws IOException {
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new IllegalArgumentException("Debes elegir un producto.");
        }
        Producto p = productos.buscarPorCodigo(codigo);
        if (p == null) {
            throw new IllegalArgumentException("El producto \"" + codigo.trim() + "\" no existe.");
        }
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que 0.");
        }
        if (cantidad > p.getStock()) {
            throw new IllegalArgumentException("Stock insuficiente: de \"" + p.getNombre()
                    + "\" solo hay " + p.getStock() + " unidades.");
        }

        double total = Math.round(p.getPrecio() * cantidad * 100.0) / 100.0;
        int stockAnterior = p.getStock();

        p.setStock(stockAnterior - cantidad);
        productos.actualizar(p);
        try {
            return ventas.guardar(LocalDate.now(), p.getCodigo(), cantidad, total);
        } catch (IOException | RuntimeException e) {
            // Si la venta no se pudo guardar, se devuelve el stock a su valor anterior.
            p.setStock(stockAnterior);
            productos.actualizar(p);
            throw e;
        }
    }
}

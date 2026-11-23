package cafestock.datos;

import cafestock.modelo.Producto;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Guarda y lee los productos en datos/productos.csv (ruta relativa, junto al .jar).
 */
public class ProductoRepositorio {
    private static final String SEP = ";";
    private final Path archivo;

    public ProductoRepositorio() {
        this(Paths.get("datos", "productos.csv"));
    }

    public ProductoRepositorio(Path archivo) {
        this.archivo = archivo;
    }

    public List<Producto> listar() throws IOException {
        List<Producto> productos = new ArrayList<>();
        if (!Files.exists(archivo)) {
            return productos;
        }
        for (String linea : Files.readAllLines(archivo, StandardCharsets.UTF_8)) {
            if (linea.trim().isEmpty()) {
                continue;
            }
            String[] p = linea.split(SEP);
            productos.add(new Producto(
                    p[0],
                    p[1],
                    Double.parseDouble(p[2]),
                    Integer.parseInt(p[3]),
                    Integer.parseInt(p[4])));
        }
        return productos;
    }

    public boolean existe(String codigo) throws IOException {
        for (Producto p : listar()) {
            if (p.getCodigo().equalsIgnoreCase(codigo.trim())) {
                return true;
            }
        }
        return false;
    }

    /** Devuelve el producto con ese código (sin distinguir mayúsculas) o null si no existe. */
    public Producto buscarPorCodigo(String codigo) throws IOException {
        if (codigo == null) {
            return null;
        }
        for (Producto p : listar()) {
            if (p.getCodigo().equalsIgnoreCase(codigo.trim())) {
                return p;
            }
        }
        return null;
    }

    public void guardar(Producto p) throws IOException {
        Files.createDirectories(archivo.toAbsolutePath().getParent());
        String linea = formatear(p) + System.lineSeparator();
        // Si la última línea del archivo no termina en salto de línea,
        // se agrega uno para que el producto nuevo no se pegue a la anterior.
        if (Files.exists(archivo) && Files.size(archivo) > 0) {
            byte[] contenido = Files.readAllBytes(archivo);
            if (contenido[contenido.length - 1] != '\n') {
                linea = System.lineSeparator() + linea;
            }
        }
        Files.write(archivo, linea.getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    /**
     * Reemplaza en el CSV el producto que tiene el mismo código (HU-A3 lo usa para
     * descontar stock; HU-A2 Editar/Guardar cambios también). Reescribe todo el archivo.
     */
    public void actualizar(Producto p) throws IOException {
        List<Producto> todos = listar();
        boolean encontrado = false;
        for (int i = 0; i < todos.size(); i++) {
            if (todos.get(i).getCodigo().equalsIgnoreCase(p.getCodigo())) {
                todos.set(i, p);
                encontrado = true;
                break;
            }
        }
        if (!encontrado) {
            throw new IllegalArgumentException("No existe el producto con código " + p.getCodigo());
        }
        StringBuilder sb = new StringBuilder();
        for (Producto x : todos) {
            sb.append(formatear(x)).append(System.lineSeparator());
        }
        Files.write(archivo, sb.toString().getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private String formatear(Producto p) {
        return p.getCodigo() + SEP + p.getNombre() + SEP
                + String.format(Locale.US, "%.2f", p.getPrecio()) + SEP
                + p.getStock() + SEP + p.getStockMinimo();
    }
}

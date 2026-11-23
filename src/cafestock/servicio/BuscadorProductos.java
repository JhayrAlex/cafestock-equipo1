package cafestock.servicio;

import cafestock.modelo.Producto;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * HU-A2 · Buscar por código o por nombre.
 * Coincide si el texto está en el código o en cualquier parte del nombre,
 * sin distinguir mayúsculas ni tildes ("cafe" encuentra "Café con Leche").
 */
public class BuscadorProductos {

    public static List<Producto> buscar(String texto, List<Producto> productos) {
        List<Producto> resultado = new ArrayList<>();
        if (productos == null) {
            return resultado;
        }
        String buscado = normalizar(texto);
        if (buscado.isEmpty()) {
            return new ArrayList<>(productos); // sin texto = todos
        }
        for (Producto p : productos) {
            if (normalizar(p.getCodigo()).contains(buscado)
                    || normalizar(p.getNombre()).contains(buscado)) {
                resultado.add(p);
            }
        }
        return resultado;
    }

    private static String normalizar(String s) {
        if (s == null) {
            return "";
        }
        String sinTildes = Normalizer.normalize(s.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return sinTildes.toLowerCase(Locale.ROOT);
    }
}

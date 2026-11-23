package cafestock.ui;

import cafestock.datos.ProductoRepositorio;
import cafestock.modelo.Producto;
import cafestock.servicio.ValidadorProducto;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.util.Locale;

/** HU-A1 · Formulario de registro y lista de productos. */
public class FormularioProductos extends JFrame {

    private final ProductoRepositorio repo;

    private final JTextField txtCodigo = new JTextField(12);
    private final JTextField txtNombre = new JTextField(12);
    private final JTextField txtPrecio = new JTextField(12);
    private final JTextField txtStock = new JTextField(12);
    private final JTextField txtStockMinimo = new JTextField(12);

    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"Código", "Nombre", "Precio (USD)", "Stock", "Stock mínimo"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };

    public FormularioProductos(ProductoRepositorio repo) {
        super("CaféStock - Productos");
        this.repo = repo;
        construirPantalla();
        refrescarLista();
    }

    private void construirPantalla() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Nuevo producto"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;

        agregarFila(form, c, 0, "Código:", txtCodigo);
        agregarFila(form, c, 1, "Nombre:", txtNombre);
        agregarFila(form, c, 2, "Precio de venta (USD):", txtPrecio);
        agregarFila(form, c, 3, "Stock actual:", txtStock);
        agregarFila(form, c, 4, "Stock mínimo:", txtStockMinimo);

        JButton btnGuardar = new JButton("Guardar producto");
        btnGuardar.addActionListener(e -> guardar());
        c.gridx = 1; c.gridy = 5;
        form.add(btnGuardar, c);

        JScrollPane scroll = new JScrollPane(new JTable(modelo));
        scroll.setBorder(BorderFactory.createTitledBorder("Productos registrados"));

        add(form, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);

        setSize(620, 520);
        setLocationRelativeTo(null);
    }

    private void agregarFila(JPanel panel, GridBagConstraints c, int fila, String texto, JTextField campo) {
        c.gridx = 0; c.gridy = fila;
        panel.add(new JLabel(texto), c);
        c.gridx = 1;
        panel.add(campo, c);
    }

    private void guardar() {
        String codigo = txtCodigo.getText().trim();
        String nombre = txtNombre.getText().trim();
        String precioTxt = txtPrecio.getText().trim();
        String stockTxt = txtStock.getText().trim();
        String minimoTxt = txtStockMinimo.getText().trim();

        String errorDatos = ValidadorProducto.validarDatosProducto(
                codigo, nombre, precioTxt, stockTxt, minimoTxt);
        if (errorDatos != null) {
            aviso(errorDatos);
            return;
        }
        if (codigo.contains(";") || nombre.contains(";")) {
            aviso("El código y el nombre no pueden contener el carácter ;");
            return;
        }

        try {
            String errorCodigo = ValidadorProducto.validarCodigoUnico(codigo, repo.listar());
            if (errorCodigo != null) {
                aviso(errorCodigo);
                return;
            }
            double precio = Double.parseDouble(precioTxt.replace(',', '.'));
            int stock = Integer.parseInt(stockTxt);
            int minimo = Integer.parseInt(minimoTxt);

            repo.guardar(new Producto(codigo, nombre, precio, stock, minimo));
        } catch (NumberFormatException ex) {
            aviso("El precio debe ser un número y el stock y el stock mínimo, números enteros.");
            return;
        } catch (IllegalArgumentException ex) {
            aviso(ex.getMessage());
            return;
        } catch (IOException ex) {
            aviso("No se pudo leer o guardar el archivo de productos: " + ex.getMessage());
            return;
        }

        refrescarLista();
        limpiarCampos();
        JOptionPane.showMessageDialog(this, "Producto guardado correctamente.");
    }

    private void refrescarLista() {
        modelo.setRowCount(0);
        try {
            for (Producto p : repo.listar()) {
                modelo.addRow(new Object[]{
                        p.getCodigo(), p.getNombre(),
                        String.format(Locale.US, "%.2f", p.getPrecio()),
                        p.getStock(), p.getStockMinimo()});
            }
        } catch (IOException | RuntimeException ex) {
            aviso("No se pudo leer productos.csv: " + ex.getMessage());
        }
    }

    private void limpiarCampos() {
        txtCodigo.setText("");
        txtNombre.setText("");
        txtPrecio.setText("");
        txtStock.setText("");
        txtStockMinimo.setText("");
        txtCodigo.requestFocus();
    }

    private void aviso(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Aviso", JOptionPane.WARNING_MESSAGE);
    }
}
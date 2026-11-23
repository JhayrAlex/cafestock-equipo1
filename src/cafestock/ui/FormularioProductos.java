package cafestock.ui;

import cafestock.datos.ProductoRepositorio;
import cafestock.modelo.Producto;
import cafestock.servicio.BuscadorProductos;
import cafestock.servicio.ValidadorProducto;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

/** HU-A1 · Formulario de registro y lista de productos. */
public class FormularioProductos extends JFrame {

    private final ProductoRepositorio repo;

    private final JTextField txtCodigo = new JTextField(12);
    private final JTextField txtNombre = new JTextField(12);
    private final JTextField txtPrecio = new JTextField(12);
    private final JTextField txtStock = new JTextField(12);
    private final JTextField txtStockMinimo = new JTextField(12);
    private final JTextField txtBuscar = new JTextField(18);

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

        // HU-A2 · Búsqueda por código o por parte del nombre
        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        panelBusqueda.add(new JLabel("Buscar (código o nombre):"));
        panelBusqueda.add(txtBuscar);
        JButton btnBuscar = new JButton("Buscar");
        JButton btnTodos = new JButton("Ver todos");
        btnBuscar.addActionListener(e -> buscar());
        txtBuscar.addActionListener(e -> buscar()); // Enter también busca
        btnTodos.addActionListener(e -> {
            txtBuscar.setText("");
            refrescarLista();
        });
        panelBusqueda.add(btnBuscar);
        panelBusqueda.add(btnTodos);

        JScrollPane scroll = new JScrollPane(new JTable(modelo));
        JPanel panelLista = new JPanel(new BorderLayout());
        panelLista.setBorder(BorderFactory.createTitledBorder("Productos registrados"));
        panelLista.add(panelBusqueda, BorderLayout.NORTH);
        panelLista.add(scroll, BorderLayout.CENTER);

        add(form, BorderLayout.NORTH);
        add(panelLista, BorderLayout.CENTER);

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

        txtBuscar.setText("");
        refrescarLista();
        limpiarCampos();
        JOptionPane.showMessageDialog(this, "Producto guardado correctamente.");
    }

    private void refrescarLista() {
        try {
            mostrar(repo.listar());
        } catch (IOException | RuntimeException ex) {
            aviso("No se pudo leer productos.csv: " + ex.getMessage());
        }
    }

    private void buscar() {
        String texto = txtBuscar.getText().trim();
        if (texto.isEmpty()) {
            refrescarLista(); // sin texto se muestran todos
            return;
        }
        try {
            List<Producto> encontrados = BuscadorProductos.buscar(texto, repo.listar());
            mostrar(encontrados);
            if (encontrados.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No se encontraron productos con \"" + texto + "\".",
                        "Búsqueda", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (IOException | RuntimeException ex) {
            aviso("No se pudo leer productos.csv: " + ex.getMessage());
        }
    }

    private void mostrar(List<Producto> productos) {
        modelo.setRowCount(0);
        for (Producto p : productos) {
            modelo.addRow(new Object[]{
                    p.getCodigo(), p.getNombre(),
                    String.format(Locale.US, "%.2f", p.getPrecio()),
                    p.getStock(), p.getStockMinimo()});
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
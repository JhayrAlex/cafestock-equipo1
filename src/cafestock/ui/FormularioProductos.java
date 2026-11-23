package cafestock.ui;

import cafestock.datos.ProductoRepositorio;
import cafestock.datos.VentaRepositorio;
import cafestock.modelo.Producto;
import cafestock.servicio.ValidadorProducto;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** HU-A1 y HU-A2 · Registro, consulta, búsqueda y edición de productos. */
public class FormularioProductos extends JFrame {

    private final ProductoRepositorio repo;
    private List<Producto> productosCargados = new ArrayList<>();

    private final JTextField txtCodigo = new JTextField(12);
    private final JTextField txtNombre = new JTextField(12);
    private final JTextField txtPrecio = new JTextField(12);
    private final JTextField txtStock = new JTextField(12);
    private final JTextField txtStockMinimo = new JTextField(12);
    private final JTextField txtBuscar = new JTextField(18);

    private final JButton btnGuardar = new JButton("Guardar producto");
    private final JButton btnGuardarCambios = new JButton("Guardar cambios");
    private final JButton btnCancelarEdicion = new JButton("Cancelar / Nuevo");
    private final JButton btnAbrirVentas = new JButton("Registrar ventas");

    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"Código", "Nombre", "Precio (USD)", "Stock", "Stock mínimo"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };

    private final JTable tabla = new JTable(modelo);

    public FormularioProductos(ProductoRepositorio repo) {
        super("CaféStock - Productos");
        this.repo = repo;
        construirPantalla();
        refrescarLista();

        // Refresca la tabla automáticamente cuando se regresa de la pantalla de ventas
        addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowGainedFocus(WindowEvent e) {
                refrescarLista();
            }
        });
    }

    private void construirPantalla() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Panel superior: formulario de registro / edición
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Datos del producto"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;

        agregarFila(form, c, 0, "Código:", txtCodigo);
        agregarFila(form, c, 1, "Nombre:", txtNombre);
        agregarFila(form, c, 2, "Precio de venta (USD):", txtPrecio);
        agregarFila(form, c, 3, "Stock actual:", txtStock);
        agregarFila(form, c, 4, "Stock mínimo:", txtStockMinimo);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        btnGuardar.addActionListener(e -> guardar());
        btnGuardarCambios.addActionListener(e -> guardarCambios());
        btnCancelarEdicion.addActionListener(e -> limpiarCampos());
        btnAbrirVentas.addActionListener(e -> abrirVentas());
        btnGuardarCambios.setEnabled(false);

        panelBotones.add(btnGuardar);
        panelBotones.add(btnGuardarCambios);
        panelBotones.add(btnCancelarEdicion);
        panelBotones.add(btnAbrirVentas);

        c.gridx = 0;
        c.gridy = 5;
        c.gridwidth = 2;
        form.add(panelBotones, c);

        // Panel central: barra de búsqueda + tabla
        JPanel panelCentro = new JPanel(new BorderLayout(6, 6));
        panelCentro.setBorder(BorderFactory.createTitledBorder("Productos registrados (seleccione una fila para editar)"));

        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        panelBusqueda.add(new JLabel("Buscar por código o nombre:"));
        panelBusqueda.add(txtBuscar);
        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { filtrarTabla(); }
            @Override
            public void removeUpdate(DocumentEvent e) { filtrarTabla(); }
            @Override
            public void changedUpdate(DocumentEvent e) { filtrarTabla(); }
        });

        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarProductoSeleccionado();
            }
        });

        panelCentro.add(panelBusqueda, BorderLayout.NORTH);
        panelCentro.add(new JScrollPane(tabla), BorderLayout.CENTER);

        add(form, BorderLayout.NORTH);
        add(panelCentro, BorderLayout.CENTER);

        setSize(720, 560);
        setLocationRelativeTo(null);
    }

    private void abrirVentas() {
        new FormularioVentas(repo, new VentaRepositorio()).setVisible(true);
    }

    private void agregarFila(JPanel panel, GridBagConstraints c, int fila, String texto, JTextField campo) {
        c.gridx = 0;
        c.gridy = fila;
        c.gridwidth = 1;
        panel.add(new JLabel(texto), c);
        c.gridx = 1;
        panel.add(campo, c);
    }

    /** HU-A1 · Registrar producto nuevo. */
    private void guardar() {
        if (!txtCodigo.isEditable()) {
            aviso("Está editando un producto existente. Use \"Guardar cambios\" o \"Cancelar / Nuevo\".");
            return;
        }

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

    /** HU-A2 · Guardar cambios y revalidar producto seleccionado (sin modificar el código). */
    private void guardarCambios() {
        if (txtCodigo.isEditable()) {
            aviso("Seleccione primero un producto de la tabla para editarlo.");
            return;
        }

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
        if (nombre.contains(";")) {
            aviso("El nombre no puede contener el carácter ;");
            return;
        }

        try {
            double precio = Double.parseDouble(precioTxt.replace(',', '.'));
            int stock = Integer.parseInt(stockTxt);
            int minimo = Integer.parseInt(minimoTxt);

            repo.actualizar(new Producto(codigo, nombre, precio, stock, minimo));
        } catch (NumberFormatException ex) {
            aviso("El precio debe ser un número y el stock y el stock mínimo, números enteros.");
            return;
        } catch (IllegalArgumentException ex) {
            aviso(ex.getMessage());
            return;
        } catch (IOException ex) {
            aviso("No se pudo actualizar el archivo de productos: " + ex.getMessage());
            return;
        }

        refrescarLista();
        limpiarCampos();
        JOptionPane.showMessageDialog(this, "Cambios guardados correctamente.");
    }

    private void cargarProductoSeleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            return;
        }
        txtCodigo.setText(modelo.getValueAt(fila, 0).toString());
        txtNombre.setText(modelo.getValueAt(fila, 1).toString());
        txtPrecio.setText(modelo.getValueAt(fila, 2).toString());
        txtStock.setText(modelo.getValueAt(fila, 3).toString());
        txtStockMinimo.setText(modelo.getValueAt(fila, 4).toString());

        txtCodigo.setEditable(false);
        btnGuardar.setEnabled(false);
        btnGuardarCambios.setEnabled(true);
    }

    private void refrescarLista() {
        try {
            productosCargados = repo.listar();
            filtrarTabla();
        } catch (IOException | RuntimeException ex) {
            aviso("No se pudo leer productos.csv: " + ex.getMessage());
        }
    }

    private void filtrarTabla() {
        String filtro = txtBuscar.getText().trim().toLowerCase(Locale.ROOT);
        modelo.setRowCount(0);
        for (Producto p : productosCargados) {
            boolean coincide = filtro.isEmpty()
                    || p.getCodigo().toLowerCase(Locale.ROOT).contains(filtro)
                    || p.getNombre().toLowerCase(Locale.ROOT).contains(filtro);
            if (coincide) {
                modelo.addRow(new Object[]{
                        p.getCodigo(),
                        p.getNombre(),
                        String.format(Locale.US, "%.2f", p.getPrecio()),
                        p.getStock(),
                        p.getStockMinimo()
                });
            }
        }
    }

    private void limpiarCampos() {
        tabla.clearSelection();
        txtCodigo.setText("");
        txtNombre.setText("");
        txtPrecio.setText("");
        txtStock.setText("");
        txtStockMinimo.setText("");

        txtCodigo.setEditable(true);
        btnGuardar.setEnabled(true);
        btnGuardarCambios.setEnabled(false);
        txtCodigo.requestFocus();
    }

    private void aviso(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Aviso", JOptionPane.WARNING_MESSAGE);
    }
}
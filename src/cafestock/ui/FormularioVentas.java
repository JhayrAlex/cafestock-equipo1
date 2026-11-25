package cafestock.ui;

import cafestock.datos.ProductoRepositorio;
import cafestock.datos.VentaRepositorio;
import cafestock.modelo.Producto;
import cafestock.modelo.Venta;
import cafestock.servicio.VentaServicio;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** HU-A3 · Pantalla de registro de ventas y cálculo automático del total. */
public class FormularioVentas extends JFrame {

    private final ProductoRepositorio productoRepo;
    private final VentaRepositorio ventaRepo;
    private List<Producto> listaProductos = new ArrayList<>();

    // Por defecto toma la fecha de hoy, pero permite editarla (regla del proyecto)
    private final JTextField txtFecha = new JTextField(LocalDate.now().toString(), 12);
    private final JComboBox<String> cmbProducto = new JComboBox<>();
    private final JTextField txtCantidad = new JTextField(12);
    private final JLabel lblTotal = new JLabel("$ 0.00");

    private final DefaultTableModel modeloVentas = new DefaultTableModel(
            new String[]{"N°", "Fecha", "Producto", "Cantidad", "Total (USD)"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };

    public FormularioVentas(ProductoRepositorio productoRepo, VentaRepositorio ventaRepo) {
        super("CaféStock - Registrar Ventas");
        this.productoRepo = productoRepo;
        this.ventaRepo = ventaRepo;
        construirPantalla();
        cargarProductos();
        refrescarVentas();
    }

    private void construirPantalla() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Nueva venta"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;

        agregarFila(form, c, 0, "Fecha (AAAA-MM-DD):", txtFecha);
        agregarFila(form, c, 1, "Producto:", cmbProducto);
        agregarFila(form, c, 2, "Cantidad:", txtCantidad);

        c.gridx = 0;
        c.gridy = 3;
        form.add(new JLabel("Total a pagar:"), c);
        c.gridx = 1;
        lblTotal.setFont(new Font("SansSerif", Font.BOLD, 15));
        form.add(lblTotal, c);

        JButton btnConfirmar = new JButton("Confirmar venta");
        btnConfirmar.addActionListener(e -> confirmarVenta());
        c.gridx = 1;
        c.gridy = 4;
        form.add(btnConfirmar, c);

        // Actualiza el total automáticamente antes de confirmar
        cmbProducto.addActionListener(e -> actualizarTotal());
        txtCantidad.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { actualizarTotal(); }
            @Override
            public void removeUpdate(DocumentEvent e) { actualizarTotal(); }
            @Override
            public void changedUpdate(DocumentEvent e) { actualizarTotal(); }
        });

        JScrollPane scroll = new JScrollPane(new JTable(modeloVentas));
        scroll.setBorder(BorderFactory.createTitledBorder("Ventas registradas"));

        add(form, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);

        setSize(660, 500);
        setLocationRelativeTo(null);
    }

    private void agregarFila(JPanel panel, GridBagConstraints c, int fila, String texto, JComponent comp) {
        c.gridx = 0;
        c.gridy = fila;
        panel.add(new JLabel(texto), c);
        c.gridx = 1;
        panel.add(comp, c);
    }

    private void cargarProductos() {
        cmbProducto.removeAllItems();
        try {
            listaProductos = productoRepo.listar();
            for (Producto p : listaProductos) {
                cmbProducto.addItem(p.getCodigo() + " - " + p.getNombre()
                        + " ($" + String.format(Locale.US, "%.2f", p.getPrecio())
                        + " | Stock: " + p.getStock() + ")");
            }
        } catch (IOException ex) {
            aviso("No se pudieron cargar los productos: " + ex.getMessage());
        }
    }

    /** HU-A3 · Calcula y muestra precio × cantidad antes de confirmar. */
    private void actualizarTotal() {
        int idx = cmbProducto.getSelectedIndex();
        if (idx < 0 || idx >= listaProductos.size()) {
            lblTotal.setText("$ 0.00");
            return;
        }
        try {
            int cantidad = Integer.parseInt(txtCantidad.getText().trim());
            if (cantidad > 0) {
                double total = listaProductos.get(idx).getPrecio() * cantidad;
                lblTotal.setText(String.format(Locale.US, "$ %.2f", total));
            } else {
                lblTotal.setText("$ 0.00");
            }
        } catch (NumberFormatException e) {
            lblTotal.setText("$ 0.00");
        }
    }

    private void confirmarVenta() {
        String fechaTexto = txtFecha.getText().trim();
        if (fechaTexto.isEmpty()) {
            aviso("La fecha es obligatoria.");
            return;
        }
        LocalDate fecha;
        try {
            fecha = LocalDate.parse(fechaTexto);
        } catch (DateTimeParseException e) {
            aviso("Formato de fecha inválido. Use AAAA-MM-DD (ejemplo: " + LocalDate.now() + ").");
            return;
        }

        int idx = cmbProducto.getSelectedIndex();
        if (idx < 0 || idx >= listaProductos.size()) {
            aviso("Seleccione un producto válido.");
            return;
        }

        int cantidad;
        try {
            cantidad = Integer.parseInt(txtCantidad.getText().trim());
        } catch (NumberFormatException e) {
            aviso("La cantidad debe ser un número entero válido.");
            return;
        }

        Producto seleccionado = listaProductos.get(idx);
        if (cantidad <= 0) {
            aviso("La cantidad debe ser mayor que 0.");
            return;
        }
        if (cantidad > seleccionado.getStock()) {
            aviso("Stock insuficiente. Disponible: " + seleccionado.getStock() + " unidades.");
            return;
        }

        // Guarda la venta y descuenta el stock en una sola operación (VentaServicio).
        Venta venta;
        try {
            venta = new VentaServicio(productoRepo, ventaRepo)
                    .registrarVenta(fecha, seleccionado.getCodigo(), cantidad);
        } catch (IOException | RuntimeException ex) {
            aviso("Error al registrar la venta: " + ex.getMessage());
            return;
        }
        double total = venta.getTotal();

        txtCantidad.setText("");
        lblTotal.setText("$ 0.00");
        cargarProductos();
        refrescarVentas();
        JOptionPane.showMessageDialog(this, "Venta registrada. Total: $" + String.format(Locale.US, "%.2f", total));
    }

    private void refrescarVentas() {
        modeloVentas.setRowCount(0);
        try {
            for (Venta v : ventaRepo.listar()) {
                modeloVentas.addRow(new Object[]{
                        v.getNumero(),
                        v.getFecha(),
                        v.getCodigoProducto(),
                        v.getCantidad(),
                        String.format(Locale.US, "%.2f", v.getTotal())
                });
            }
        } catch (IOException | RuntimeException ex) {
            aviso("No se pudieron cargar las ventas: " + ex.getMessage());
        }
    }

    private void aviso(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Aviso", JOptionPane.WARNING_MESSAGE);
    }
}
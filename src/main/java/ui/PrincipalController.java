package ui;

import java.time.LocalDate;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import model.Client;
import model.Employee;
import model.Invoice;
import model.ReportRows;
import model.Reservation;
import model.Room;
import service.HotelService;

/** Controlador del panel principal. */
public class PrincipalController {

    // ---- Barra y pestanas
    @FXML private Label lblUsuario;
    @FXML private TabPane tabs;
    @FXML private Tab tabPanel;
    @FXML private Tab tabTrabajadores;
    @FXML private Tab tabReportes;

    // ---- Top de habitaciones (controlador de top.fxml, incluido con fx:id="top")
    @FXML private TopHabitacionesController topController;

    // ---- Panel
    @FXML private Label lblTotal;
    @FXML private Label lblOcupadas;
    @FXML private Label lblReservadas;
    @FXML private Label lblLibres;
    @FXML private Label lblMantenimiento;
    @FXML private Label lblNoDisponibles;
    @FXML private Label lblIngresos;
    @FXML private Label lblPendiente;
    @FXML private Label lblPorcentaje;
    @FXML private ProgressBar pbOcupacion;

    // ---- Reservas
    @FXML private ComboBox<Client> cmbCliente;
    @FXML private DatePicker dpEntrada;
    @FXML private DatePicker dpSalida;
    @FXML private Spinner<Integer> spPersonas;
    @FXML private ComboBox<Room> cmbHabitacion;
    @FXML private Label lblResumenReserva;
    @FXML private Label lblAviso;
    @FXML private TextField txtBuscarReserva;
    @FXML private ComboBox<String> cmbFiltroEstado;
    @FXML private ComboBox<Invoice.PaymentMethod> cmbMetodo;
    @FXML private TableView<Reservation> tblReservas;

    // ---- Clientes
    @FXML private TextField txtBuscarCliente;
    @FXML private TableView<Client> tblClientes;
    @FXML private TextField txtNombre;
    @FXML private TextField txtDocumento;
    @FXML private TextField txtCorreo;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtCiudad;
    @FXML private ComboBox<Client.Category> cmbCategoria;

    // ---- Habitaciones
    @FXML private DatePicker dpDesde;
    @FXML private DatePicker dpHasta;
    @FXML private Spinner<Integer> spPersonasBuscar;
    @FXML private TextField txtNumero;
    @FXML private ComboBox<Room.Type> cmbTipo;
    @FXML private Spinner<Integer> spCapacidad;
    @FXML private TextField txtPrecio;
    @FXML private Button btnAgregarHab;
    @FXML private Button btnMantenimiento;
    @FXML private TableView<Room> tblHabitaciones;

    // ---- Comprobantes
    @FXML private TextField txtAbono;
    @FXML private TableView<Invoice> tblComprobantes;

    // ---- Trabajadores
    @FXML private TableView<Employee> tblTrabajadores;
    @FXML private TextField txtEmpNombre;
    @FXML private TextField txtEmpDocumento;
    @FXML private TextField txtEmpCorreo;
    @FXML private TextField txtEmpTelefono;
    @FXML private TextField txtEmpCiudad;
    @FXML private ComboBox<Employee.Role> cmbEmpRol;
    @FXML private TextField txtEmpUsuario;
    @FXML private PasswordField txtEmpClave;

    private final HotelService servicio = Sesion.servicio;
    private final Employee usuario = Sesion.usuario;
    private final boolean esAdmin = usuario.getRole().isAtLeast(Employee.Role.ADMIN);

    private List<Reservation> reservas = List.of();
    private List<Client> clientes = List.of();

    // ------------------------------------------------------------------ inicio

    @FXML
    private void initialize() {
        String rol = Ui.es(usuario.getRole());
        lblUsuario.setText(usuario.getName().equalsIgnoreCase(rol) ? rol : usuario.getName() + "  ·  " + rol);

        // Lo exclusivo del administrador se oculta o se bloquea para el recepcionista.
        if (!esAdmin) {
            tabs.getTabs().remove(tabPanel);
            tabs.getTabs().remove(tabTrabajadores);
            tabs.getTabs().remove(tabReportes);
        }
        btnAgregarHab.setDisable(!esAdmin);
        btnMantenimiento.setDisable(!esAdmin);

        // Listas desplegables.
        cmbCategoria.getItems().addAll(Client.Category.values());
        cmbCategoria.setValue(Client.Category.NEW);
        cmbCategoria.setConverter(Ui.conversor());
        cmbMetodo.getItems().addAll(Invoice.PaymentMethod.values());
        cmbMetodo.setValue(Invoice.PaymentMethod.CASH);
        cmbMetodo.setConverter(Ui.conversor());
        cmbTipo.getItems().addAll(Room.Type.values());
        cmbTipo.setValue(Room.Type.SINGLE);
        cmbTipo.setConverter(Ui.conversor());
        cmbEmpRol.getItems().addAll(Employee.Role.values());
        cmbEmpRol.setValue(Employee.Role.RECEPTIONIST);
        cmbEmpRol.setConverter(Ui.conversor());
        cmbHabitacion.setConverter(Ui.conversorHabitacion());
        cmbFiltroEstado.getItems().addAll("Todos", "Confirmada", "En el hotel", "Finalizada", "Cancelada");
        cmbFiltroEstado.setValue("Todos");

        // Fechas por defecto.
        dpEntrada.setValue(LocalDate.now());
        dpSalida.setValue(LocalDate.now().plusDays(1));
        dpDesde.setValue(LocalDate.now());
        dpHasta.setValue(LocalDate.now().plusDays(1));

        // Interactividad: se recalcula todo al cambiar fechas, personas, habitacion o filtros.
        dpEntrada.valueProperty().addListener((o, a, b) -> actualizarLibres());
        dpSalida.valueProperty().addListener((o, a, b) -> actualizarLibres());
        spPersonas.valueProperty().addListener((o, a, b) -> actualizarLibres());
        cmbHabitacion.valueProperty().addListener((o, a, b) -> actualizarResumen());
        txtBuscarReserva.textProperty().addListener((o, a, b) -> filtrarReservas());
        cmbFiltroEstado.valueProperty().addListener((o, a, b) -> filtrarReservas());
        txtBuscarCliente.textProperty().addListener((o, a, b) -> filtrarClientes());

        // Que muestra cada columna (en el mismo orden que en el .fxml).
        Ui.columna(tblReservas, 0, r -> r.getId());
        Ui.columna(tblReservas, 1, r -> r.getClient().getName());
        Ui.columna(tblReservas, 2, r -> r.getRoom().getNumber());
        Ui.columna(tblReservas, 3, r -> Ui.fecha(r.getCheckIn()));
        Ui.columna(tblReservas, 4, r -> Ui.fecha(r.getCheckOut()));
        Ui.columna(tblReservas, 5, r -> r.getGuests());
        Ui.columna(tblReservas, 6, r -> Ui.dinero(r.getTotal()));
        Ui.columna(tblReservas, 7, r -> r.getEmployee() == null ? "Huésped" : r.getEmployee().getName());
        Ui.columnaEstado(tblReservas, 8, r -> Ui.es(r.getStatus()));

        Ui.columna(tblClientes, 0, c -> c.getId());
        Ui.columna(tblClientes, 1, c -> c.getName());
        Ui.columna(tblClientes, 2, c -> c.getDocumentNumber());
        Ui.columna(tblClientes, 3, c -> c.getEmail());
        Ui.columna(tblClientes, 4, c -> c.getPhone());
        Ui.columna(tblClientes, 5, c -> c.getCity());
        Ui.columna(tblClientes, 6, c -> Ui.es(c.getCategory()));

        Ui.columna(tblHabitaciones, 0, h -> h.getId());
        Ui.columna(tblHabitaciones, 1, h -> h.getNumber());
        Ui.columna(tblHabitaciones, 2, h -> Ui.es(h.getType()));
        Ui.columna(tblHabitaciones, 3, h -> h.getCapacity());
        Ui.columna(tblHabitaciones, 4, h -> Ui.dinero(h.getPricePerNight()));
        Ui.columna(tblHabitaciones, 5, h -> h.isAvailable() ? "Si" : "No");
        Ui.columna(tblHabitaciones, 6, h -> h.isUnderMaintenance() ? "Si" : "No");

        Ui.columna(tblComprobantes, 0, i -> i.getId());
        Ui.columna(tblComprobantes, 1, i -> i.getReservation().getId());
        Ui.columna(tblComprobantes, 2, i -> i.getReservation().getClient().getName());
        Ui.columna(tblComprobantes, 3, i -> Ui.fecha(i.getIssueDate()));
        Ui.columna(tblComprobantes, 4, i -> Ui.dinero(i.getAmount()));
        Ui.columna(tblComprobantes, 5, i -> Ui.dinero(i.getPaidAmount()));
        Ui.columna(tblComprobantes, 6, i -> Ui.dinero(i.getBalance()));
        Ui.columna(tblComprobantes, 7, i -> Ui.es(i.getPaymentMethod()));
        Ui.columnaEstado(tblComprobantes, 8, i -> Ui.es(i.getStatus()));

        Ui.columna(tblTrabajadores, 0, e -> e.getId());
        Ui.columna(tblTrabajadores, 1, e -> e.getName());
        Ui.columna(tblTrabajadores, 2, e -> e.getDocumentNumber());
        Ui.columna(tblTrabajadores, 3, e -> e.getEmail());
        Ui.columna(tblTrabajadores, 4, e -> e.getPhone());
        Ui.columna(tblTrabajadores, 5, e -> e.getCity());
        Ui.columna(tblTrabajadores, 6, e -> Ui.es(e.getRole()));
        Ui.columna(tblTrabajadores, 7, e -> e.getUsername());

        refrescar();
    }

    // ------------------------------------------------------------------ panel

    @FXML
    private void actualizarPanel() {
        if (!esAdmin) {
            return;
        }
        Ui.ejecutar(() -> {
            ReportRows.BasicReport r = servicio.getBasicReport(usuario);
            lblTotal.setText(String.valueOf(r.totalRooms()));
            lblOcupadas.setText(String.valueOf(r.occupiedRooms()));
            lblReservadas.setText(String.valueOf(r.reservedRooms()));
            lblLibres.setText(String.valueOf(r.freeRooms()));
            lblMantenimiento.setText(String.valueOf(r.maintenanceRooms()));
            lblNoDisponibles.setText(String.valueOf(r.unavailableRooms()));
            lblIngresos.setText(Ui.dinero(r.income()));
            lblPendiente.setText(Ui.dinero(r.pendingBalance()));
            double ocupacion = r.totalRooms() == 0 ? 0 : (double) r.occupiedRooms() / r.totalRooms();
            pbOcupacion.setProgress(ocupacion);
            lblPorcentaje.setText(String.format("%.0f%% de las habitaciones ocupadas hoy", ocupacion * 100));
        });
    }

    // ------------------------------------------------------------------ reservas

    @FXML
    private void crearReserva() {
        Ui.ejecutar(() -> {
            Client cliente = cmbCliente.getValue();
            Room habitacion = cmbHabitacion.getValue();
            if (cliente == null || habitacion == null) {
                throw new IllegalArgumentException("Elige un cliente y una habitacion.");
            }
            Reservation r = servicio.createReservation(cliente.getId(), habitacion.getId(),
                    dpEntrada.getValue(), dpSalida.getValue(), spPersonas.getValue(), usuario);
            double base = r.getPricePerNight() * r.nights();
            Ui.info("Reserva #" + r.getId() + " creada correctamente.\n\n"
                    + "Noches: " + r.nights() + "\n"
                    + "Precio sin descuento: " + Ui.dinero(base) + "\n"
                    + "Descuento aplicado: " + Ui.dinero(base - r.getTotal()) + "\n"
                    + "Total a pagar: " + Ui.dinero(r.getTotal()));
            refrescar();
        });
    }

    @FXML
    private void registrarLlegada() {
        Ui.conSeleccion(tblReservas, r -> {
            servicio.checkIn(r.getId());
            Ui.info("Llegada registrada. La habitación queda ocupada.");
            refrescar();
        });
    }

    @FXML
    private void registrarSalida() {
        Ui.conSeleccion(tblReservas, r -> {
            servicio.checkOut(r.getId());
            Ui.info("Salida registrada. La habitación queda libre."
                    + (servicio.hasPayments(r.getId()) ? "" : "\n\nAviso: esta reserva no tiene abonos registrados."));
            refrescar();
        });
    }

    @FXML
    private void cancelarReserva() {
        Ui.conSeleccion(tblReservas, r -> {
            if (Ui.confirmar("Cancelar la reserva #" + r.getId() + " de " + r.getClient().getName() + "?")) {
                boolean conPagos = servicio.hasPayments(r.getId());
                servicio.cancelReservation(r.getId(), usuario);
                Ui.info("Reserva cancelada."
                        + (conPagos ? "\n\nEl comprobante pagado permanece registrado. No se realiza un reembolso automático." : ""));
                refrescar();
            }
        });
    }

    @FXML
    private void generarComprobante() {
        Ui.conSeleccion(tblReservas, r -> {
            Invoice factura = servicio.issueInvoice(r.getId(), cmbMetodo.getValue());
            Ui.info("Comprobante #" + factura.getId() + " generado por " + Ui.dinero(factura.getAmount()) + ".");
            refrescar();
        });
    }

    private void filtrarReservas() {
        String texto = txtBuscarReserva.getText() == null ? "" : txtBuscarReserva.getText().trim().toLowerCase();
        String estado = cmbFiltroEstado.getValue();
        tblReservas.getItems().setAll(reservas.stream()
                .filter(r -> estado == null || estado.equals("Todos") || estado.equals(Ui.es(r.getStatus())))
                .filter(r -> texto.isEmpty()
                        || r.getClient().getName().toLowerCase().contains(texto)
                        || String.valueOf(r.getRoom().getNumber()).toLowerCase().contains(texto))
                .toList());
    }

    private void actualizarLibres() {
        try {
            cmbHabitacion.getItems().setAll(servicio.findAvailableRooms(
                    dpEntrada.getValue(), dpSalida.getValue(), spPersonas.getValue()));
            lblAviso.setText(cmbHabitacion.getItems().isEmpty() ? "No hay habitaciones libres para esos datos." : "");
        } catch (RuntimeException error) {
            cmbHabitacion.getItems().clear();
            lblAviso.setText(Ui.mensaje(error));
        }
        actualizarResumen();
    }

    private void actualizarResumen() {
        Room h = cmbHabitacion.getValue();
        long noches = Ui.noches(dpEntrada.getValue(), dpSalida.getValue());
        if (h == null || noches <= 0) {
            lblResumenReserva.setText("");
            return;
        }
        lblResumenReserva.setText(noches + " noche(s) x " + Ui.dinero(h.getPricePerNight()) + " = "
                + Ui.dinero(noches * h.getPricePerNight())
                + "  (los descuentos por estadia larga o categoria se aplican al confirmar)");
    }

    // ------------------------------------------------------------------ clientes

    @FXML
    private void registrarCliente() {
        Ui.ejecutar(() -> {
            servicio.registerClient(txtNombre.getText(), txtDocumento.getText(), txtCorreo.getText(),
                    txtTelefono.getText(), txtCiudad.getText(), cmbCategoria.getValue());
            txtNombre.clear();
            txtDocumento.clear();
            txtCorreo.clear();
            txtTelefono.clear();
            txtCiudad.clear();
            cmbCategoria.setValue(Client.Category.NEW);
            Ui.info("Cliente registrado.");
            refrescar();
        });
    }

    private void filtrarClientes() {
        String texto = txtBuscarCliente.getText() == null ? "" : txtBuscarCliente.getText().trim().toLowerCase();
        tblClientes.getItems().setAll(clientes.stream()
                .filter(c -> texto.isEmpty()
                        || c.getName().toLowerCase().contains(texto)
                        || c.getDocumentNumber().toLowerCase().contains(texto)
                        || c.getEmail().toLowerCase().contains(texto))
                .toList());
    }

    // ------------------------------------------------------------------ habitaciones

    @FXML
    private void buscarLibres() {
        Ui.ejecutar(() -> tblHabitaciones.getItems().setAll(servicio.findAvailableRooms(
                dpDesde.getValue(), dpHasta.getValue(), spPersonasBuscar.getValue())));
    }

    @FXML
    private void verTodas() {
        Ui.ejecutar(() -> tblHabitaciones.getItems().setAll(servicio.getRooms()));
    }

    @FXML
    private void agregarHabitacion() {
        Ui.ejecutar(() -> {
            servicio.registerRoom(txtNumero.getText(), cmbTipo.getValue(), spCapacidad.getValue(),
                    Ui.leerNumero(txtPrecio.getText(), "El precio"), usuario);
            txtNumero.clear();
            txtPrecio.clear();
            refrescar();
        });
    }

    @FXML
    private void mantenimiento() {
        Ui.conSeleccion(tblHabitaciones, h -> {
            servicio.setRoomMaintenance(h.getId(), !h.isUnderMaintenance(), usuario);
            refrescar();
        });
    }

    // ------------------------------------------------------------------ comprobantes

    @FXML
    private void registrarAbono() {
        Ui.conSeleccion(tblComprobantes, i -> {
            servicio.payInvoice(i.getId(), Ui.leerNumero(txtAbono.getText(), "El abono"));
            txtAbono.clear();
            refrescar();
        });
    }

    // ------------------------------------------------------------------ trabajadores

    @FXML
    private void registrarTrabajador() {
        Ui.ejecutar(() -> {
            servicio.registerEmployee(txtEmpNombre.getText(), txtEmpDocumento.getText(), txtEmpCorreo.getText(),
                    txtEmpTelefono.getText(), txtEmpCiudad.getText(), cmbEmpRol.getValue(),
                    txtEmpUsuario.getText(), txtEmpClave.getText(), usuario);
            txtEmpNombre.clear();
            txtEmpDocumento.clear();
            txtEmpCorreo.clear();
            txtEmpTelefono.clear();
            txtEmpCiudad.clear();
            txtEmpUsuario.clear();
            txtEmpClave.clear();
            cmbEmpRol.setValue(Employee.Role.RECEPTIONIST);
            Ui.info("Trabajador registrado.");
            refrescar();
        });
    }

    // ------------------------------------------------------------------ general

    @FXML
    private void cerrarSesion() {
        Sesion.usuario = null;
        AppFxml.irBienvenida();
    }

    private void refrescar() {
        Ui.ejecutar(() -> {
            reservas = servicio.getReservations();
            clientes = servicio.getClients();
            filtrarReservas();
            filtrarClientes();
            tblHabitaciones.getItems().setAll(servicio.getRooms());
            tblComprobantes.getItems().setAll(servicio.getInvoices());
            cmbCliente.getItems().setAll(clientes.stream().filter(Client::isActive).toList());
            actualizarLibres();
            topController.actualizar();
            if (esAdmin) {
                tblTrabajadores.getItems().setAll(servicio.getEmployees(usuario));
                actualizarPanel();
            }
        });
    }
}

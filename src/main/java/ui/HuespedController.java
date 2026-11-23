package ui;

import java.time.LocalDate;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import model.Client;
import model.Reservation;
import model.Room;
import service.HotelService;

/** Controlador de la seccion de huespedes. */
public class HuespedController {

    @FXML private TextField txtDocumento;
    @FXML private Label lblSaludo;

    // Buscar y reservar
    @FXML private DatePicker dpEntrada;
    @FXML private DatePicker dpSalida;
    @FXML private Spinner<Integer> spPersonas;
    @FXML private Label lblResumen;
    @FXML private TableView<Room> tblLibres;

    // Mis reservas
    @FXML private TableView<Reservation> tblMisReservas;

    // Registrarme
    @FXML private TextField txtNombre;
    @FXML private TextField txtDocRegistro;
    @FXML private TextField txtCorreo;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtCiudad;

    private final HotelService servicio = Sesion.servicio;

    @FXML
    private void initialize() {
        dpEntrada.setValue(LocalDate.now());
        dpSalida.setValue(LocalDate.now().plusDays(1));

        // Las habitaciones libres se recalculan solas al cambiar fechas o personas.
        dpEntrada.valueProperty().addListener((o, a, b) -> buscar());
        dpSalida.valueProperty().addListener((o, a, b) -> buscar());
        spPersonas.valueProperty().addListener((o, a, b) -> buscar());

        Ui.columna(tblLibres, 0, h -> h.getNumber());
        Ui.columna(tblLibres, 1, h -> Ui.es(h.getType()));
        Ui.columna(tblLibres, 2, h -> h.getCapacity());
        Ui.columna(tblLibres, 3, h -> Ui.dinero(h.getPricePerNight()));
        Ui.columna(tblLibres, 4, h -> Ui.dinero(h.getPricePerNight() * Ui.noches(dpEntrada.getValue(), dpSalida.getValue())));

        Ui.columna(tblMisReservas, 0, r -> r.getId());
        Ui.columna(tblMisReservas, 1, r -> r.getRoom().getNumber());
        Ui.columna(tblMisReservas, 2, r -> Ui.fecha(r.getCheckIn()));
        Ui.columna(tblMisReservas, 3, r -> Ui.fecha(r.getCheckOut()));
        Ui.columna(tblMisReservas, 4, r -> r.getGuests());
        Ui.columna(tblMisReservas, 5, r -> Ui.dinero(r.getTotal()));
        Ui.columnaEstado(tblMisReservas, 6, r -> Ui.es(r.getStatus()));

        buscar();
    }

    // ------------------------------------------------------------------ identificacion

    @FXML
    private void ingresar() {
        Ui.ejecutar(() -> {
            Sesion.huesped = servicio.findClientByDocument(txtDocumento.getText());
            mostrarSaludo();
            actualizarMisReservas();
        });
    }

    private void mostrarSaludo() {
        Client c = Sesion.huesped;
        String descuento = switch (c.getCategory()) {
            case FREQUENT -> " - 5% de descuento";
            case VIP -> " - 15% de descuento";
            default -> "";
        };
        lblSaludo.setText("Hola, " + c.getName() + " (cliente " + Ui.es(c.getCategory()) + descuento + ")");
    }

    private Client requerirHuesped() {
        if (Sesion.huesped == null) {
            throw new IllegalArgumentException("Primero escribe tu documento y pulsa Ingresar "
                    + "(si es tu primera vez, usa la pestana Registrarme).");
        }
        return Sesion.huesped;
    }

    // ------------------------------------------------------------------ buscar y reservar

    private void buscar() {
        try {
            List<Room> libres = servicio.findAvailableRooms(
                    dpEntrada.getValue(), dpSalida.getValue(), spPersonas.getValue());
            tblLibres.getItems().setAll(libres);
            tblLibres.refresh();
            long noches = Ui.noches(dpEntrada.getValue(), dpSalida.getValue());
            lblResumen.setText(libres.isEmpty()
                    ? "No hay habitaciones libres para esos datos."
                    : libres.size() + " habitacion(es) libre(s) para " + noches
                      + " noche(s). Selecciona una y pulsa Reservar.");
        } catch (RuntimeException error) {
            tblLibres.getItems().clear();
            lblResumen.setText(Ui.mensaje(error));
        }
    }

    @FXML
    private void reservar() {
        Ui.conSeleccion(tblLibres, habitacion -> {
            Client cliente = requerirHuesped();
            long noches = Ui.noches(dpEntrada.getValue(), dpSalida.getValue());
            String pregunta = "Reservar la habitacion " + habitacion.getNumber() + " del "
                    + Ui.fecha(dpEntrada.getValue()) + " al " + Ui.fecha(dpSalida.getValue())
                    + " (" + noches + " noche(s)) para " + spPersonas.getValue() + " persona(s)?";
            if (!Ui.confirmar(pregunta)) {
                return;
            }
            Reservation r = servicio.createReservation(cliente.getId(), habitacion.getId(),
                    dpEntrada.getValue(), dpSalida.getValue(), spPersonas.getValue(), null);
            double base = r.getPricePerNight() * r.nights();
            Ui.info("Reserva #" + r.getId() + " creada correctamente.\n\n"
                    + "Noches: " + r.nights() + "\n"
                    + "Precio sin descuento: " + Ui.dinero(base) + "\n"
                    + "Descuento aplicado: " + Ui.dinero(base - r.getTotal()) + "\n"
                    + "Total a pagar: " + Ui.dinero(r.getTotal()) + "\n\n"
                    + "Para pagar, acércate a recepción.");
            buscar();
            actualizarMisReservas();
        });
    }

    // ------------------------------------------------------------------ mis reservas

    @FXML
    private void actualizarMisReservas() {
        if (Sesion.huesped == null) {
            tblMisReservas.getItems().clear();
            return;
        }
        Ui.ejecutar(() -> tblMisReservas.getItems().setAll(servicio.getReservationsOf(Sesion.huesped.getId())));
    }

    @FXML
    private void cancelarReserva() {
        Ui.conSeleccion(tblMisReservas, r -> {
            Client cliente = requerirHuesped();
            if (Ui.confirmar("Cancelar tu reserva #" + r.getId() + " (habitacion " + r.getRoom().getNumber() + ")?")) {
                servicio.cancelGuestReservation(cliente.getId(), r.getId());
                Ui.info("Reserva cancelada.");
                buscar();
                actualizarMisReservas();
            }
        });
    }

    // ------------------------------------------------------------------ registrarme

    @FXML
    private void registrarme() {
        Ui.ejecutar(() -> {
            Client c = servicio.registerGuest(txtNombre.getText(), txtDocRegistro.getText(),
                    txtCorreo.getText(), txtTelefono.getText(), txtCiudad.getText());
            Ui.info("Registro exitoso. Tu codigo de cliente es " + c.getId() + ".\nYa puedes reservar.");
            txtDocumento.setText(c.getDocumentNumber());
            Sesion.huesped = c;
            mostrarSaludo();
            actualizarMisReservas();
            txtNombre.clear();
            txtDocRegistro.clear();
            txtCorreo.clear();
            txtTelefono.clear();
            txtCiudad.clear();
        });
    }

    @FXML
    private void volver() {
        Sesion.huesped = null;
        AppFxml.irBienvenida();
    }
}

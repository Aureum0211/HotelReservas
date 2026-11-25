package ui;

import java.time.LocalDate;
import java.util.List;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
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
    @FXML private FlowPane flpLibres;

    // Mis reservas
    @FXML private VBox vbMisReservas;

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

        buscar();
        actualizarMisReservas();
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
            long noches = Ui.noches(dpEntrada.getValue(), dpSalida.getValue());
            flpLibres.getChildren().clear();
            if (libres.isEmpty()) {
                lblResumen.setText("");
                flpLibres.getChildren().add(vacio("No hay habitaciones libres para esos datos",
                        "Prueba con otras fechas o con menos personas."));
                return;
            }
            lblResumen.setText(libres.size() + " habitación(es) libre(s) para " + noches + " noche(s).");
            for (Room habitacion : libres) {
                flpLibres.getChildren().add(tarjetaHabitacion(habitacion, noches));
            }
        } catch (RuntimeException error) {
            flpLibres.getChildren().clear();
            lblResumen.setText(Ui.mensaje(error));
        }
    }

    /** Tarjeta de una habitacion libre: numero, tipo, capacidad, precio por noche, total y boton Reservar. */
    private Node tarjetaHabitacion(Room h, long noches) {
        Label caption = new Label("HABITACIÓN");
        caption.getStyleClass().add("hab-caption");
        Label numero = new Label(h.getNumber());
        numero.getStyleClass().add("hab-numero");
        Label tipo = new Label(Ui.es(h.getType()));
        tipo.getStyleClass().add("hab-tipo");
        VBox titulos = new VBox(3, caption, numero, tipo);
        titulos.setAlignment(Pos.CENTER_LEFT);
        Region cama = new Region();
        cama.getStyleClass().addAll("ico", "ico-enorme", "ico-cama", "hab-ico-grande");
        Region relleno = new Region();
        HBox.setHgrow(relleno, Priority.ALWAYS);
        HBox portada = new HBox(titulos, relleno, cama);
        portada.setAlignment(Pos.CENTER_LEFT);
        portada.getStyleClass().add("hab-portada");

        Label precio = new Label(Ui.dinero(h.getPricePerNight()));
        precio.getStyleClass().add("hab-precio");
        Label porNoche = new Label(" / noche");
        porNoche.getStyleClass().add("hab-dato");
        HBox filaPrecio = new HBox(2, precio, porNoche);
        filaPrecio.setAlignment(Pos.BASELINE_LEFT);

        Region persona = new Region();
        persona.getStyleClass().addAll("ico", "ico-pequeno", "ico-persona", "ico-dorado");
        Label capacidad = new Label("Hasta " + h.getCapacity() + (h.getCapacity() == 1 ? " persona" : " personas"));
        capacidad.getStyleClass().add("hab-dato");
        HBox filaCapacidad = new HBox(8, persona, capacidad);
        filaCapacidad.setAlignment(Pos.CENTER_LEFT);

        Label total = new Label("Estadía de " + noches + (noches == 1 ? " noche: " : " noches: ")
                + Ui.dinero(h.getPricePerNight() * noches));
        total.getStyleClass().add("hab-total");
        Label sinDescuento = new Label("sin descuentos");
        sinDescuento.getStyleClass().add("hab-dato");

        Button reservar = new Button("Reservar");
        reservar.getStyleClass().addAll("boton-principal", "boton-ancho");
        reservar.setMaxWidth(Double.MAX_VALUE);
        reservar.setPrefHeight(38);
        reservar.setOnAction(e -> reservar(h));

        VBox cuerpo = new VBox(8, filaPrecio, filaCapacidad, total, sinDescuento, reservar);
        cuerpo.getStyleClass().add("hab-cuerpo");
        VBox.setMargin(reservar, new Insets(6, 0, 0, 0));

        VBox tarjeta = new VBox(portada, cuerpo);
        tarjeta.getStyleClass().add("hab-card");
        tarjeta.setPrefWidth(255);
        tarjeta.setMinWidth(255);
        return tarjeta;
    }

    /** Aviso con borde punteado cuando no hay nada que mostrar. */
    private Node vacio(String titulo, String texto) {
        Label t = new Label(titulo);
        t.getStyleClass().add("vacio-titulo");
        Label d = new Label(texto);
        d.getStyleClass().add("nota");
        d.setWrapText(true);
        VBox caja = new VBox(6, t, d);
        caja.setAlignment(Pos.CENTER);
        caja.getStyleClass().add("vacio");
        caja.setPrefWidth(520);
        return caja;
    }

    private void reservar(Room habitacion) {
        Ui.ejecutar(() -> {
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
        vbMisReservas.getChildren().clear();
        if (Sesion.huesped == null) {
            vbMisReservas.getChildren().add(vacio("Ingresa con tu documento",
                    "Escribe tu documento arriba y pulsa Ingresar para ver tus reservas. "
                            + "Si es tu primera vez, usa la pestaña Registrarme."));
            return;
        }
        Ui.ejecutar(() -> {
            List<Reservation> mias = servicio.getReservationsOf(Sesion.huesped.getId());
            if (mias.isEmpty()) {
                vbMisReservas.getChildren().add(vacio("Aún no tienes reservas",
                        "Cuando reserves una habitación, aparecerá aquí."));
                return;
            }
            for (Reservation r : mias) {
                vbMisReservas.getChildren().add(tarjetaReserva(r));
            }
        });
    }

    /** Tarjeta de una reserva del huesped; el boton Cancelar solo se ofrece en reservas confirmadas. */
    private Node tarjetaReserva(Reservation r) {
        String estado = Ui.es(r.getStatus());
        Region franja = new Region();
        franja.getStyleClass().addAll("res-franja", "res-franja-" + switch (Ui.claseEstado(estado)) {
            case "badge-ok" -> "ok";
            case "badge-aviso" -> "aviso";
            case "badge-malo" -> "malo";
            default -> "neutro";
        });

        Label titulo = new Label("Habitación " + r.getRoom().getNumber());
        titulo.getStyleClass().add("res-titulo");
        Label etiqueta = new Label(estado);
        etiqueta.getStyleClass().addAll("badge", Ui.claseEstado(estado));
        HBox cabecera = new HBox(10, titulo, etiqueta);
        cabecera.setAlignment(Pos.CENTER_LEFT);

        long noches = Ui.noches(r.getCheckIn(), r.getCheckOut());
        Label fechas = new Label(Ui.fecha(r.getCheckIn()) + "  →  " + Ui.fecha(r.getCheckOut()));
        fechas.getStyleClass().add("res-fechas");
        Label detalle = new Label(noches + (noches == 1 ? " noche" : " noches") + "  ·  " + r.getGuests()
                + (r.getGuests() == 1 ? " persona" : " personas") + "  ·  Reserva #" + r.getId());
        detalle.getStyleClass().add("nota");
        VBox izquierda = new VBox(5, cabecera, fechas, detalle);
        izquierda.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(izquierda, Priority.ALWAYS);

        Label total = new Label(Ui.dinero(r.getTotal()));
        total.getStyleClass().add("res-total");
        Label totalTexto = new Label("Total");
        totalTexto.getStyleClass().add("nota");
        VBox derecha = new VBox(4, totalTexto, total);
        derecha.setAlignment(Pos.CENTER_RIGHT);
        if (r.getStatus() == Reservation.Status.ACTIVE) {
            Button cancelar = new Button("Cancelar reserva");
            cancelar.getStyleClass().add("boton-peligro");
            cancelar.setOnAction(e -> cancelarReserva(r));
            derecha.getChildren().add(cancelar);
            VBox.setMargin(cancelar, new Insets(6, 0, 0, 0));
        }

        HBox cuerpo = new HBox(20, izquierda, derecha);
        cuerpo.setAlignment(Pos.CENTER_LEFT);
        cuerpo.setPadding(new Insets(14, 20, 14, 18));
        HBox.setHgrow(cuerpo, Priority.ALWAYS);
        HBox tarjeta = new HBox(franja, cuerpo);
        tarjeta.getStyleClass().add("res-card");
        return tarjeta;
    }

    private void cancelarReserva(Reservation r) {
        Ui.ejecutar(() -> {
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

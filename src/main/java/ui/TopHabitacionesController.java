package ui;

import java.time.LocalDate;
import java.util.List;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import model.ReportRows;
import service.HotelService;

/** Controlador del ranking de habitaciones. */
public class TopHabitacionesController {
    @FXML private ComboBox<String> cmbCantidad;
    @FXML private ComboBox<String> cmbPeriodo;
    @FXML private Label lblNota;
    @FXML private VBox vbTop;

    private final HotelService servicio = Sesion.servicio;

    @FXML
    private void initialize() {
        cmbCantidad.getItems().addAll("Top 5", "Top 10", "Todas");
        cmbCantidad.setValue("Top 5");
        cmbPeriodo.getItems().addAll("Todo el tiempo", "Últimos 30 días", "Últimos 90 días", "Este año");
        cmbPeriodo.setValue("Todo el tiempo");
        cmbCantidad.valueProperty().addListener((o, a, b) -> actualizar());
        cmbPeriodo.valueProperty().addListener((o, a, b) -> actualizar());
        actualizar();
    }

    @FXML
    void actualizar() {
        Ui.ejecutar(() -> {
            LocalDate hoy = LocalDate.now();
            LocalDate desde = switch (String.valueOf(cmbPeriodo.getValue())) {
                case "Últimos 30 días" -> hoy.minusDays(30);
                case "Últimos 90 días" -> hoy.minusDays(90);
                case "Este año" -> LocalDate.of(hoy.getYear(), 1, 1);
                default -> null;
            };
            Integer limite = switch (String.valueOf(cmbCantidad.getValue())) {
                case "Top 5" -> 5;
                case "Top 10" -> 10;
                default -> null;
            };

            List<ReportRows.RoomRanking> ranking = servicio.getRoomRanking(desde, limite);
            vbTop.getChildren().clear();
            if (ranking.isEmpty() || ranking.stream().noneMatch(row -> row.reservations() > 0)) {
                lblNota.setText("Todavía no hay reservas en este periodo para armar el ranking.");
                return;
            }

            int maximo = Math.max(1, ranking.stream().mapToInt(ReportRows.RoomRanking::reservations).max().orElse(1));
            lblNota.setText("Se cuenta cada reserva que no fue cancelada. Mientras más larga la barra, más veces se usó la habitación.");
            for (int i = 0; i < ranking.size(); i++) {
                ReportRows.RoomRanking row = ranking.get(i);
                vbTop.getChildren().add(fila(i + 1, row, maximo));
            }
        });
    }

    private HBox fila(int puesto, ReportRows.RoomRanking room, int maximo) {
        Label rank = new Label(String.valueOf(puesto));
        rank.getStyleClass().add("rank");
        if (puesto == 1) rank.getStyleClass().add("rank-1");

        Label nombre = new Label("Habitación " + room.roomNumber() + " · " + Ui.es(room.type()));
        nombre.getStyleClass().add("top-nombre");
        nombre.setMinWidth(210);

        Region barra = new Region();
        barra.getStyleClass().add("barra-top");
        if (puesto == 1) barra.getStyleClass().add("barra-top-1");
        barra.setPrefHeight(22);
        barra.setMaxHeight(22);
        StackPane pista = new StackPane(barra);
        pista.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(pista, Priority.ALWAYS);
        double fraccion = Math.max(0.012, (double) room.reservations() / maximo);
        barra.maxWidthProperty().bind(pista.widthProperty().multiply(fraccion));

        Label cifras = new Label(room.reservations() + (room.reservations() == 1 ? " reserva" : " reservas")
                + "  ·  " + room.nights() + (room.nights() == 1 ? " noche" : " noches"));
        cifras.getStyleClass().add("top-valor");
        cifras.setMinWidth(170);
        HBox fila = new HBox(12, rank, nombre, pista, cifras);
        fila.setAlignment(Pos.CENTER_LEFT);
        return fila;
    }
}

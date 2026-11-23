package ui;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;

/** Controlador de la pantalla de reportes. */
public class ReportesController {

    @FXML private ComboBox<String> cmbReporte;
    @FXML private DatePicker dpDesde;
    @FXML private DatePicker dpHasta;
    @FXML private Label lblResumen;
    @FXML private TableView<String[]> tblReporte;

    private Reportes.Reporte actual;

    @FXML
    private void initialize() {
        cmbReporte.getItems().addAll(Reportes.TIPOS);
        cmbReporte.setValue(Reportes.TIPOS.get(0));
        lblResumen.setText("Elige un reporte y pulsa \"Generar reporte\". Las fechas son opcionales.");
    }

    @FXML
    private void generar() {
        Ui.ejecutar(() -> {
            actual = Reportes.generar(cmbReporte.getValue(), Sesion.servicio, Sesion.usuario, dpDesde.getValue(), dpHasta.getValue());
            tblReporte.getColumns().clear();
            for (int i = 0; i < actual.columnas().length; i++) {
                final int indice = i;
                TableColumn<String[], String> columna = new TableColumn<>(actual.columnas()[i]);
                columna.setCellValueFactory(celda -> new SimpleStringProperty(celda.getValue()[indice]));
                tblReporte.getColumns().add(columna);
            }
            tblReporte.getItems().setAll(actual.filas());
            lblResumen.setText(actual.resumen());
        });
    }

    @FXML
    private void quitarFechas() {
        dpDesde.setValue(null);
        dpHasta.setValue(null);
    }

    @FXML
    private void exportar() {
        if (actual == null) {
            Ui.info("Primero genera un reporte.");
            return;
        }
        FileChooser selector = new FileChooser();
        selector.setTitle("Guardar reporte");
        selector.setInitialFileName("reporte-" + cmbReporte.getValue().toLowerCase().replace(' ', '-')
                + "-" + LocalDate.now() + ".csv");
        selector.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivo CSV (Excel)", "*.csv"));
        File archivo = selector.showSaveDialog(Sesion.ventana);
        if (archivo == null) {
            return;
        }
        Ui.ejecutar(() -> {
            StringBuilder csv = new StringBuilder("\uFEFF");   // marca UTF-8 para que Excel muestre bien las tildes
            csv.append(linea(actual.columnas()));
            for (String[] fila : actual.filas()) {
                csv.append(linea(fila));
            }
            try {
                Files.writeString(archivo.toPath(), csv.toString(), StandardCharsets.UTF_8);
            } catch (IOException error) {
                throw new UncheckedIOException(error);
            }
            Ui.info("Reporte guardado en:\n" + archivo.getAbsolutePath());
        });
    }

    private String linea(String[] celdas) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < celdas.length; i++) {
            if (i > 0) {
                sb.append(';');
            }
            sb.append('"').append(celdas[i].replace("\"", "\"\"")).append('"');
        }
        return sb.append("\r\n").toString();
    }
}

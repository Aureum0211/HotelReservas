package ui;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.sql.Connection;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import persistence.Database;
import service.HotelService;

/** Configura la aplicacion y muestra la pantalla inicial. */
public class AppFxml extends Application {

    @Override
    public void start(Stage stage) {
        Sesion.ventana = stage;
        stage.setTitle("Hotel - Sistema de reservas");
        try {
            Sesion.servicio = crearServicio();
        } catch (RuntimeException error) {
            Alert alerta = new Alert(Alert.AlertType.ERROR, Ui.mensaje(error), ButtonType.OK);
            alerta.setHeaderText("No se pudo conectar a la base de datos");
            alerta.showAndWait();
            Platform.exit();
            return;
        }
        stage.getIcons().add(new Image(AppFxml.class.getResourceAsStream("/ui/images/icono.png")));
        stage.setMinWidth(1000);
        stage.setMinHeight(640);
        irBienvenida();
        stage.show();
    }

    // ----------------- pantallas

    private static final double ANCHO_INICIAL = 1280;
    private static final double ALTO_INICIAL = 780;

    static void irBienvenida() { mostrar("/ui/bienvenida.fxml"); }

    static void irHuesped() { mostrar("/ui/huesped.fxml"); }

    static void irPrincipal() { mostrar("/ui/principal.fxml"); }

    private static void mostrar(String ruta) {
        try {
            Parent raiz = FXMLLoader.load(AppFxml.class.getResource(ruta));
            boolean primera = Sesion.ventana.getScene() == null;
            double ancho = primera ? ANCHO_INICIAL : Sesion.ventana.getScene().getWidth();
            double alto = primera ? ALTO_INICIAL : Sesion.ventana.getScene().getHeight();
            Scene escena = new Scene(raiz, ancho, alto);
            escena.getStylesheets().add(AppFxml.class.getResource("/ui/estilos.css").toExternalForm());
            Sesion.ventana.setScene(escena);
            if (primera) {
                Sesion.ventana.centerOnScreen();
            }
        } catch (IOException error) {
            throw new UncheckedIOException(error);
        }
    }

    private HotelService crearServicio() {
        Connection connection = Database.open(Path.of("db.properties"));
        try {
            return HotelService.forConnection(connection);
        } catch (RuntimeException error) {
            try {
                connection.close();
            } catch (java.sql.SQLException closeError) {
                error.addSuppressed(closeError);
            }
            throw error;
        }
    }

    @Override
    public void stop() throws Exception {
        if (Sesion.servicio != null) {
            Sesion.servicio.close();
        }
    }
}

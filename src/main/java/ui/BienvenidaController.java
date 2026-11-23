package ui;

import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/** Controlador de la pantalla de bienvenida. */
public class BienvenidaController {

    @FXML private HBox raiz;
    @FXML private VBox panelMarca;
    @FXML private StackPane panelFoto;
    @FXML private ImageView imgMascota;
    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtClave;
    @FXML private Label lblMensaje;

    @FXML
    private void initialize() {
        // Columna de marca = 33% del ancho, mascota = 30%; el inicio de sesion ocupa el resto.
        panelMarca.prefWidthProperty().bind(raiz.widthProperty().multiply(0.33));
        panelFoto.prefWidthProperty().bind(raiz.widthProperty().multiply(0.30));
        cubrir(imgMascota, panelFoto);
    }

    @FXML
    private void entrar() {
        try {
            Sesion.usuario = Sesion.servicio.login(txtUsuario.getText(), txtClave.getText());
            AppFxml.irPrincipal();
        } catch (RuntimeException error) {
            lblMensaje.setText(Ui.mensaje(error));
        }
    }

    @FXML
    private void irHuesped() {
        Sesion.huesped = null;
        AppFxml.irHuesped();
    }

    private void cubrir(ImageView vista, Region panel) {
        Image imagen = vista.getImage();
        Runnable ajustar = () -> {
            double ancho = panel.getWidth();
            double alto = panel.getHeight();
            if (ancho <= 0 || alto <= 0) {
                return;
            }
            double proporcionPanel = ancho / alto;
            double proporcionImagen = imagen.getWidth() / imagen.getHeight();
            double anchoVisible;
            double altoVisible;
            if (proporcionImagen > proporcionPanel) {          // la imagen es mas ancha: se recortan los lados
                altoVisible = imagen.getHeight();
                anchoVisible = altoVisible * proporcionPanel;
            } else {                                           // es mas alta: se recorta abajo
                anchoVisible = imagen.getWidth();
                altoVisible = anchoVisible / proporcionPanel;
            }
            double x = (imagen.getWidth() - anchoVisible) / 2;
            vista.setViewport(new Rectangle2D(x, 0, anchoVisible, altoVisible));
            vista.setFitWidth(ancho);
            vista.setFitHeight(alto);
        };
        panel.widthProperty().addListener((o, a, b) -> ajustar.run());
        panel.heightProperty().addListener((o, a, b) -> ajustar.run());
        ajustar.run();
    }
}

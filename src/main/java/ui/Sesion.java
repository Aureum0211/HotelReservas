package ui;

import javafx.stage.Stage;
import model.Client;
import model.Employee;
import service.HotelService;

/** Datos compartidos entre las pantallas. */
final class Sesion {
    static HotelService servicio;
    static Employee usuario;   // trabajador con sesion iniciada (null en la zona de huespedes)
    static Client huesped;     // huesped identificado por su documento (null si no se ha identificado)
    static Stage ventana;

    private Sesion() { }
}

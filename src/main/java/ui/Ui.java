package ui;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.StringConverter;
import model.Room;

/** Metodos de apoyo para las pantallas. */
final class Ui {

    private static final Locale CO = Locale.forLanguageTag("es-CO");
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private Ui() { }

    // ------------------------------------------------------------------ formato

    static String dinero(double valor) {
        return String.format(CO, "$%,.0f", valor);
    }

    static String fecha(LocalDate fecha) {
        return fecha == null ? "" : fecha.format(FECHA);
    }

    static long noches(LocalDate entrada, LocalDate salida) {
        return (entrada == null || salida == null) ? 0 : ChronoUnit.DAYS.between(entrada, salida);
    }

    static String mensaje(Throwable error) {
        String texto = error.getMessage() == null ? error.toString() : error.getMessage();
        return texto
                .replaceAll("(?i)que ya tuvo check-in", "que ya tiene la llegada registrada")
                .replaceAll("(?i)hacer check-in", "registrar la llegada")
                .replaceAll("(?i)hacer check-out", "registrar la salida")
                .replaceAll("(?i)con check-in", "con llegada registrada")
                .replaceAll("(?i)check-in", "llegada")
                .replaceAll("(?i)check-out", "salida");
    }

    static String es(Object valor) {
        if (!(valor instanceof Enum<?> e)) {
            return String.valueOf(valor);
        }
        return switch (e.name()) {
            case "ACTIVE" -> "Confirmada";
            case "CHECKED_IN" -> "En el hotel";
            case "CHECKED_OUT" -> "Finalizada";
            case "CANCELLED" -> "Cancelada";
            case "PENDING" -> "Pendiente";
            case "PARTIALLY_PAID" -> "Pago parcial";
            case "PAID" -> "Pagado";
            case "REFUNDED" -> "Reembolsado";
            case "VOIDED" -> "Anulado";
            case "CASH" -> "Efectivo";
            case "CARD" -> "Tarjeta";
            case "TRANSFER" -> "Transferencia";
            case "NEW" -> "Nuevo";
            case "FREQUENT" -> "Frecuente";
            case "VIP" -> "VIP";
            case "SINGLE" -> "Sencilla";
            case "DOUBLE" -> "Doble";
            case "SUITE" -> "Suite";
            case "RECEPTIONIST" -> "Recepcionista";
            case "ADMIN" -> "Administrador";
            default -> e.name();
        };
    }

    static <E extends Enum<E>> StringConverter<E> conversor() {
        return new StringConverter<>() {
            @Override
            public String toString(E valor) {
                return valor == null ? "" : es(valor);
            }

            @Override
            public E fromString(String texto) {
                return null;
            }
        };
    }

    static String habitacion(Room r) {
        return r.getNumber() + " - " + es(r.getType()) + " - hasta " + r.getCapacity()
                + " personas - " + dinero(r.getPricePerNight()) + "/noche";
    }

    static StringConverter<Room> conversorHabitacion() {
        return new StringConverter<>() {
            @Override
            public String toString(Room r) {
                return r == null ? "" : habitacion(r);
            }

            @Override
            public Room fromString(String texto) {
                return null;
            }
        };
    }

    // ------------------------------------------------------------------ avisos

    static void info(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION, mensaje, ButtonType.OK);
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }

    static boolean confirmar(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION, mensaje, ButtonType.YES, ButtonType.NO);
        alerta.setHeaderText(null);
        return alerta.showAndWait().filter(b -> b == ButtonType.YES).isPresent();
    }

    static void ejecutar(Runnable accion) {
        try {
            accion.run();
        } catch (RuntimeException error) {
            Alert alerta = new Alert(Alert.AlertType.WARNING, mensaje(error), ButtonType.OK);
            alerta.setHeaderText("No se pudo completar la accion");
            alerta.showAndWait();
        }
    }

    // ------------------------------------------------------------------ tablas

    static <T> void columna(TableView<T> tabla, int indice, Function<T, Object> valor) {
        @SuppressWarnings("unchecked")
        TableColumn<T, String> col = (TableColumn<T, String>) tabla.getColumns().get(indice);
        col.setCellValueFactory(celda -> new SimpleStringProperty(String.valueOf(valor.apply(celda.getValue()))));
    }

    static <T> void columnaEstado(TableView<T> tabla, int indice, Function<T, Object> valor) {
        columna(tabla, indice, valor);
        @SuppressWarnings("unchecked")
        TableColumn<T, String> col = (TableColumn<T, String>) tabla.getColumns().get(indice);
        col.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(String texto, boolean vacio) {
                super.updateItem(texto, vacio);
                if (vacio || texto == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                Label etiqueta = new Label(texto);
                etiqueta.getStyleClass().addAll("badge", claseEstado(texto));
                setText(null);
                setGraphic(etiqueta);
            }
        });
    }

    private static String claseEstado(String estado) {
        return switch (estado) {
            case "En el hotel", "Pagado" -> "badge-ok";
            case "Confirmada", "Pendiente" -> "badge-aviso";
            case "Pago parcial" -> "badge-medio";
            case "Cancelada", "Anulado" -> "badge-malo";
            default -> "badge-neutro";
        };
    }

    static <T> void conSeleccion(TableView<T> tabla, Consumer<T> accion) {
        T elegido = tabla.getSelectionModel().getSelectedItem();
        if (elegido == null) {
            info("Primero selecciona una fila de la tabla.");
            return;
        }
        ejecutar(() -> accion.accept(elegido));
    }

    static double leerNumero(String texto, String nombre) {
        try {
            return Double.parseDouble(texto.trim().replace(",", "."));
        } catch (RuntimeException error) {
            throw new IllegalArgumentException(nombre + " debe ser un numero.");
        }
    }
}

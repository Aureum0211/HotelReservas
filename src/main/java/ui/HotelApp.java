package ui;

import exceptions.HotelException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;
import model.Client;
import model.Invoice;
import model.Reservation;
import model.Room;
import service.HotelService;

public class HotelApp {
    private final Scanner scanner = new Scanner(System.in);
    private final HotelService hotel = new HotelService();

    public void start() {
        boolean running = true;
        System.out.println("=== HOTEL RESERVAS - DEMO ===");
        System.out.println("Datos de ejemplo cargados en memoria. No se usa base de datos.");
        while (running) {
            printMenu();
            String option = scanner.nextLine().trim();
            try {
                switch (option) {
                    case "1" -> showClients();
                    case "2" -> showRooms();
                    case "3" -> createReservation();
                    case "4" -> showReservations();
                    case "5" -> issueInvoice();
                    case "6" -> showInvoices();
                    case "7" -> payInvoice();
                    case "8" -> cancelReservation();
                    case "0" -> running = false;
                    default -> System.out.println("Opcion no valida.");
                }
            } catch (HotelException | IllegalArgumentException e) {
                System.out.println("No se pudo completar la operacion: " + e.getMessage());
            }
            System.out.println();
        }
        System.out.println("Programa finalizado.");
    }

    private void printMenu() {
        System.out.println("""

                1. Ver clientes de ejemplo
                2. Ver habitaciones
                3. Crear reserva
                4. Ver reservas
                5. Emitir comprobante
                6. Ver comprobantes
                7. Pagar comprobante
                8. Cancelar reserva
                0. Salir
                """);
        System.out.print("Seleccione una opcion: ");
    }

    private void showClients() {
        for (Client client : hotel.getClients()) {
            System.out.println(client.describe());
        }
    }

    private void showRooms() {
        for (Room room : hotel.getRooms()) {
            System.out.printf("ID %d | %s | Capacidad: %d | $%.0f por noche | %s%n",
                    room.getId(), room, room.getCapacity(), room.getPricePerNight(),
                    room.isAvailable() ? "Habilitada" : "No disponible");
        }
    }

    private void createReservation() {
        showClients();
        int clientId = readInt("ID del cliente: ");
        showRooms();
        int roomId = readInt("ID de la habitacion: ");
        LocalDate checkIn = readDate("Fecha de entrada (AAAA-MM-DD): ");
        LocalDate checkOut = readDate("Fecha de salida (AAAA-MM-DD): ");
        System.out.print("¿Aplicar 10% de descuento desde 7 noches? (s/n): ");
        boolean discount = scanner.nextLine().trim().equalsIgnoreCase("s");

        Reservation reservation = hotel.createReservation(clientId, roomId, checkIn, checkOut, discount);
        System.out.println("Reserva creada correctamente:");
        System.out.println(reservation);
        System.out.println("Noches: " + reservation.nights());
    }

    private void showReservations() {
        if (hotel.getReservations().isEmpty()) {
            System.out.println("Todavia no hay reservas.");
            return;
        }
        hotel.getReservations().forEach(System.out::println);
    }

    private void issueInvoice() {
        showReservations();
        int reservationId = readInt("ID de la reserva que desea facturar: ");
        System.out.println("Metodo de pago: 1) Efectivo  2) Tarjeta  3) Transferencia");
        int method = readInt("Seleccione: ");
        Invoice.PaymentMethod paymentMethod = switch (method) {
            case 1 -> Invoice.PaymentMethod.CASH;
            case 2 -> Invoice.PaymentMethod.CARD;
            case 3 -> Invoice.PaymentMethod.TRANSFER;
            default -> throw new IllegalArgumentException("Metodo de pago no valido.");
        };
        Invoice invoice = hotel.issueInvoice(reservationId, paymentMethod);
        System.out.println("Comprobante emitido: " + invoice);
    }

    private void showInvoices() {
        if (hotel.getInvoices().isEmpty()) {
            System.out.println("Todavia no hay comprobantes.");
            return;
        }
        hotel.getInvoices().forEach(System.out::println);
    }

    private void payInvoice() {
        showInvoices();
        int id = readInt("ID del comprobante que desea pagar: ");
        hotel.payInvoice(id);
        System.out.println("Pago registrado correctamente.");
    }

    private void cancelReservation() {
        showReservations();
        int id = readInt("ID de la reserva que desea cancelar: ");
        hotel.cancelReservation(id);
        System.out.println("Reserva cancelada.");
    }

    private int readInt(String prompt) {
        System.out.print(prompt);
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Debe escribir un numero entero.");
        }
    }

    private LocalDate readDate(String prompt) {
        System.out.print(prompt);
        try {
            return LocalDate.parse(scanner.nextLine().trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Use el formato AAAA-MM-DD.");
        }
    }
}

package ui;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import model.Employee;
import model.ReportRows;
import service.HotelService;

/** Prepara los resultados de reportes para mostrarlos. */
final class Reportes {
    record Reporte(String[] columnas, List<String[]> filas, String resumen) { }

    static final List<String> TIPOS = List.of(
            "Reservas por estado",
            "Ingresos por mes",
            "Uso de habitaciones",
            "Mejores clientes",
            "Saldos pendientes");

    private Reportes() { }

    static Reporte generar(String tipo, HotelService servicio, Employee actor, LocalDate desde, LocalDate hasta) {
        if (tipo == null) {
            throw new IllegalArgumentException("Elige el reporte que quieres generar.");
        }
        return switch (tipo) {
            case "Reservas por estado" -> reservasPorEstado(servicio, actor, desde, hasta);
            case "Ingresos por mes" -> ingresosPorMes(servicio, actor, desde, hasta);
            case "Uso de habitaciones" -> usoDeHabitaciones(servicio, actor, desde, hasta);
            case "Mejores clientes" -> mejoresClientes(servicio, actor, desde, hasta);
            case "Saldos pendientes" -> saldosPendientes(servicio, actor, desde, hasta);
            default -> throw new IllegalArgumentException("Reporte desconocido: " + tipo);
        };
    }

    private static Reporte reservasPorEstado(HotelService servicio, Employee actor, LocalDate desde, LocalDate hasta) {
        List<ReportRows.ReservationsByStatus> datos = servicio.reservationsByStatus(actor, desde, hasta);
        List<String[]> filas = new ArrayList<>();
        int total = 0;
        for (ReportRows.ReservationsByStatus row : datos) {
            total += row.count();
            filas.add(new String[] {Ui.es(row.status()), String.valueOf(row.count()), Ui.dinero(row.totalValue())});
        }
        return new Reporte(new String[] {"Estado", "Reservas", "Valor total"}, filas,
                total + " reserva(s) con entrada en el periodo.");
    }

    private static Reporte ingresosPorMes(HotelService servicio, Employee actor, LocalDate desde, LocalDate hasta) {
        List<ReportRows.MonthlyIncome> datos = servicio.monthlyIncome(actor, desde, hasta);
        List<String[]> filas = new ArrayList<>();
        double netoTotal = 0;
        for (ReportRows.MonthlyIncome row : datos) {
            netoTotal += row.net();
            filas.add(new String[] {
                    String.format("%02d/%d", row.month().getMonthValue(), row.month().getYear()),
                    String.valueOf(row.invoiceCount()), Ui.dinero(row.collected()),
                    Ui.dinero(row.refunded()), Ui.dinero(row.net())});
        }
        return new Reporte(new String[] {"Mes", "Comprobantes", "Cobrado", "Reembolsado", "Ingreso neto"},
                filas, "Ingreso neto total del periodo: " + Ui.dinero(netoTotal));
    }

    private static Reporte usoDeHabitaciones(HotelService servicio, Employee actor,
                                              LocalDate desde, LocalDate hasta) {
        List<ReportRows.RoomUsage> datos = servicio.roomUsage(actor, desde, hasta);
        List<String[]> filas = new ArrayList<>();
        for (ReportRows.RoomUsage row : datos) {
            filas.add(new String[] {row.roomNumber(), Ui.es(row.type()), String.valueOf(row.timesUsed()),
                    String.valueOf(row.nights()), Ui.dinero(row.reservedValue())});
        }
        return new Reporte(new String[] {"Habitación", "Tipo", "Veces usada", "Noches", "Valor reservado"},
                filas, "Se cuentan las reservas no canceladas, ordenadas de la más a la menos usada.");
    }

    private static Reporte mejoresClientes(HotelService servicio, Employee actor,
                                            LocalDate desde, LocalDate hasta) {
        List<ReportRows.TopClient> datos = servicio.topClients(actor, desde, hasta);
        List<String[]> filas = new ArrayList<>();
        for (ReportRows.TopClient row : datos) {
            filas.add(new String[] {row.name(), Ui.es(row.category()), String.valueOf(row.reservationCount()),
                    String.valueOf(row.nights()), Ui.dinero(row.reservedValue())});
        }
        return new Reporte(new String[] {"Cliente", "Categoría", "Reservas", "Noches", "Valor reservado"},
                filas, "Los 10 clientes con mayor valor reservado (sin contar reservas canceladas).");
    }

    private static Reporte saldosPendientes(HotelService servicio, Employee actor,
                                             LocalDate desde, LocalDate hasta) {
        List<ReportRows.PendingBalance> datos = servicio.pendingBalances(actor, desde, hasta);
        List<String[]> filas = new ArrayList<>();
        double total = 0;
        for (ReportRows.PendingBalance row : datos) {
            total += row.balance();
            filas.add(new String[] {String.valueOf(row.invoiceId()), row.clientName(), Ui.fecha(row.issueDate()),
                    Ui.dinero(row.amount()), Ui.dinero(row.paid()), Ui.dinero(row.balance()), Ui.es(row.status())});
        }
        return new Reporte(new String[] {"Comprobante", "Cliente", "Fecha", "Monto", "Pagado", "Saldo", "Estado"},
                filas, filas.size() + " comprobante(s) con saldo pendiente. Total por cobrar: " + Ui.dinero(total));
    }
}

package model;

import java.time.LocalDate;
import java.time.YearMonth;

/** Datos utilizados para mostrar los reportes. */
public final class ReportRows {
    private ReportRows() { }

    public record BasicReport(
            int totalRooms,
            int occupiedRooms,
            int reservedRooms,
            int freeRooms,
            int maintenanceRooms,
            int unavailableRooms,
            double income,
            double pendingBalance) { }

    public record ReservationsByStatus(
            Reservation.Status status,
            int count,
            double totalValue) { }

    public record MonthlyIncome(
            YearMonth month,
            int invoiceCount,
            double collected,
            double refunded,
            double net) { }

    public record RoomUsage(
            String roomNumber,
            Room.Type type,
            int timesUsed,
            int nights,
            double reservedValue) { }

    public record TopClient(
            String name,
            Client.Category category,
            int reservationCount,
            int nights,
            double reservedValue) { }

    public record PendingBalance(
            int invoiceId,
            String clientName,
            LocalDate issueDate,
            double amount,
            double paid,
            double balance,
            Invoice.Status status) { }

    public record RoomRanking(
            int id,
            String roomNumber,
            Room.Type type,
            int reservations,
            int nights) { }
}

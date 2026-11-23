package persistence;

import exceptions.BusinessRuleException;
import exceptions.InvalidDataException;
import exceptions.ResourceNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import model.Client;
import model.Invoice;
import model.ReportRows;
import model.Reservation;
import model.Room;
import oracle.jdbc.OracleTypes;

/** Centraliza las llamadas a los paquetes PL/SQL de Oracle. */
public final class OracleHotelProcedures {

    private static final List<String> REQUIRED_PACKAGES =
            List.of("PKG_RESERVAS", "PKG_PERSONAS", "PKG_HABITACIONES", "PKG_REPORTES");

    private static final Set<Integer> NOT_FOUND = Set.of(20030, 20038, 20042, 20069);

    private static final Set<Integer> INVALID_DATA =
            Set.of(20001, 20004, 20035, 20040, 20060, 20064, 20065, 20067, 20071, 20080);

    private static final int UNIQUE_VIOLATION = 1;

    /** Prepara los parametros de entrada de una llamada. */
    @FunctionalInterface
    private interface Binder {
        void bind(CallableStatement call) throws SQLException;
    }

    /** Convierte una fila de un cursor en un objeto. */
    @FunctionalInterface
    private interface RowReader<T> {
        T read(ResultSet rs) throws SQLException;
    }

    private final Connection connection;
    private final RowMapper<Room> roomMapper = SqlMappers.rooms();

    public OracleHotelProcedures(Connection connection) {
        this.connection = connection;
        try {
            connection.setAutoCommit(true);
        } catch (SQLException error) {
            throw fail("preparar la conexion", error);
        }
    }

    public static void requireInstalled(Connection connection) {
        String sql = "SELECT object_name FROM user_objects WHERE object_type = 'PACKAGE BODY' "
                + "AND status = 'VALID' AND object_name IN ('PKG_RESERVAS', 'PKG_PERSONAS', "
                + "'PKG_HABITACIONES', 'PKG_REPORTES')";
        List<String> found = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) found.add(rs.getString(1));
        } catch (SQLException error) {
            throw fail("comprobar los paquetes de Oracle", error);
        }
        List<String> missing = REQUIRED_PACKAGES.stream().filter(name -> !found.contains(name)).toList();
        if (!missing.isEmpty()) {
            throw new UncheckedIOException("Faltan paquetes de Oracle o estan invalidos: " + String.join(", ", missing)
                    + ". Ejecuta los scripts de la carpeta oracle/ en el orden indicado en LEEME.md.",
                    new IOException("paquetes no instalados"));
        }
    }

    // ------------------------------------------------------------------ permisos

    public void requireAdmin(int workerId) {
        run("comprobar permisos", "{ call sp_exigir_admin(?) }", call -> call.setInt(1, workerId));
    }

    // ------------------------------------------------------------------ PKG_PERSONAS

    public int registerClient(String name, String document, String email, String phone, String city,
                              String category) {
        return runReturningId("registrar el cliente",
                "{ call pkg_personas.sp_registrar_cliente(?, ?, ?, ?, ?, ?, ?) }", 7, call -> {
                    call.setString(1, name);
                    call.setString(2, document);
                    call.setString(3, email);
                    call.setString(4, phone);
                    call.setString(5, city);
                    call.setString(6, category);
                });
    }

    public int registerGuest(String name, String document, String email, String phone, String city) {
        return runReturningId("registrar al huesped",
                "{ call pkg_personas.sp_registrar_huesped(?, ?, ?, ?, ?, ?) }", 6, call -> {
                    call.setString(1, name);
                    call.setString(2, document);
                    call.setString(3, email);
                    call.setString(4, phone);
                    call.setString(5, city);
                });
    }

    public int registerEmployee(int actorId, String name, String document, String email, String phone,
                                String city, String role, String username, String password) {
        return runReturningId("registrar el trabajador",
                "{ call pkg_personas.sp_registrar_trabajador(?, ?, ?, ?, ?, ?, ?, ?, ?, ?) }", 10, call -> {
                    call.setInt(1, actorId);
                    call.setString(2, name);
                    call.setString(3, document);
                    call.setString(4, email);
                    call.setString(5, phone);
                    call.setString(6, city);
                    call.setString(7, role);
                    call.setString(8, username);
                    call.setString(9, password);
                });
    }

    public int login(String username, String password) {
        return callNumber("iniciar sesion", "{ ? = call pkg_personas.fn_login(?, ?) }", call -> {
            call.setString(2, username);
            call.setString(3, password);
        });
    }

    public int findClientIdByDocument(String document) {
        return callNumber("buscar al cliente",
                "{ ? = call pkg_personas.fn_buscar_cliente_por_documento(?) }", call -> call.setString(2, document));
    }

    // ------------------------------------------------------------------ PKG_HABITACIONES

    public int registerRoom(int actorId, String number, String type, int capacity, BigDecimal price) {
        return runReturningId("registrar la habitacion",
                "{ call pkg_habitaciones.sp_registrar_habitacion(?, ?, ?, ?, ?, ?) }", 6, call -> {
                    call.setInt(1, actorId);
                    call.setString(2, number);
                    call.setString(3, type);
                    call.setInt(4, capacity);
                    call.setBigDecimal(5, price);
                });
    }

    public void startMaintenance(int actorId, int roomId) {
        run("iniciar el mantenimiento", "{ call pkg_habitaciones.sp_iniciar_mantenimiento(?, ?) }", call -> {
            call.setInt(1, actorId);
            call.setInt(2, roomId);
        });
    }

    public void finishMaintenance(int actorId, int roomId) {
        run("finalizar el mantenimiento", "{ call pkg_habitaciones.sp_finalizar_mantenimiento(?, ?) }", call -> {
            call.setInt(1, actorId);
            call.setInt(2, roomId);
        });
    }

    // ------------------------------------------------------------------ PKG_RESERVAS

    public int createReservation(int clientId, Integer workerId, int roomId, String checkIn, String checkOut,
                                 int guests) {
        return runReturningId("crear la reserva",
                "{ call pkg_reservas.sp_crear_reserva(?, ?, ?, ?, ?, ?, ?) }", 7, call -> {
                    call.setInt(1, clientId);
                    if (workerId == null) call.setNull(2, Types.NUMERIC);
                    else call.setInt(2, workerId);
                    call.setInt(3, roomId);
                    call.setString(4, checkIn);
                    call.setString(5, checkOut);
                    call.setInt(6, guests);
                });
    }

    public void cancelReservation(int reservationId, int workerId) {
        run("cancelar la reserva", "{ call pkg_reservas.sp_cancelar_reserva(?, ?) }", call -> {
            call.setInt(1, reservationId);
            call.setInt(2, workerId);
        });
    }

    public void cancelGuestReservation(int clientId, int reservationId) {
        run("cancelar la reserva", "{ call pkg_reservas.sp_cancelar_reserva_huesped(?, ?) }", call -> {
            call.setInt(1, clientId);
            call.setInt(2, reservationId);
        });
    }

    public void checkIn(int reservationId) {
        run("registrar la llegada", "{ call pkg_reservas.sp_checkin(?) }", call -> call.setInt(1, reservationId));
    }

    public void checkOut(int reservationId) {
        run("registrar la salida", "{ call pkg_reservas.sp_checkout(?) }", call -> call.setInt(1, reservationId));
    }

    public int issueInvoice(int reservationId, String paymentMethod) {
        return runReturningId("generar el comprobante",
                "{ call pkg_reservas.sp_generar_comprobante(?, ?, ?) }", 3, call -> {
                    call.setInt(1, reservationId);
                    call.setString(2, paymentMethod);
                });
    }

    public void payInvoice(int invoiceId, BigDecimal amount) {
        run("registrar el pago", "{ call pkg_reservas.sp_registrar_pago(?, ?) }", call -> {
            call.setInt(1, invoiceId);
            call.setBigDecimal(2, amount);
        });
    }

    public BigDecimal calculateTotal(BigDecimal nightlyPrice, int nights, String category) {
        return callDecimal("calcular el total", "{ ? = call pkg_reservas.fn_calcular_total(?, ?, ?) }", call -> {
            call.setBigDecimal(2, nightlyPrice);
            call.setInt(3, nights);
            call.setString(4, category);
        });
    }

    public boolean hasPayments(int reservationId) {
        return callNumber("consultar los pagos", "{ ? = call pkg_reservas.fn_tiene_pagos(?) }",
                call -> call.setInt(2, reservationId)) == 1;
    }

    public List<Room> freeRooms(String checkIn, String checkOut, int guests) {
        return queryCursor("buscar habitaciones libres", "{ ? = call pkg_reservas.fn_habitaciones_libres(?, ?, ?) }",
                call -> {
                    call.setString(2, checkIn);
                    call.setString(3, checkOut);
                    call.setInt(4, guests);
                }, roomMapper::read);
    }

    // ------------------------------------------------------------------ PKG_REPORTES

    public ReportRows.BasicReport generalStats(int actorId) {
        List<ReportRows.BasicReport> rows = queryCursor("consultar las estadisticas",
                "{ ? = call pkg_reportes.fn_estadisticas_generales(?) }", call -> call.setInt(2, actorId),
                rs -> new ReportRows.BasicReport(rs.getInt("total_habitaciones"), rs.getInt("ocupadas"),
                        rs.getInt("reservadas"), rs.getInt("libres"), rs.getInt("mantenimiento"),
                        rs.getInt("no_disponibles"), rs.getDouble("ingresos"), rs.getDouble("saldo_pendiente")));
        return rows.get(0);
    }

    public List<ReportRows.ReservationsByStatus> reservationsByStatus(int actorId, String from, String to) {
        return queryCursor("generar el reporte", "{ ? = call pkg_reportes.fn_reservas_por_estado(?, ?, ?) }",
                rangeBinder(actorId, from, to),
                rs -> new ReportRows.ReservationsByStatus(Reservation.Status.valueOf(rs.getString("estado")),
                        rs.getInt("cantidad"), rs.getDouble("valor_total")));
    }

    public List<ReportRows.MonthlyIncome> monthlyIncome(int actorId, String from, String to) {
        return queryCursor("generar el reporte", "{ ? = call pkg_reportes.fn_ingresos_por_mes(?, ?, ?) }",
                rangeBinder(actorId, from, to),
                rs -> new ReportRows.MonthlyIncome(YearMonth.parse(rs.getString("mes")), rs.getInt("comprobantes"),
                        rs.getDouble("cobrado"), rs.getDouble("reembolsado"), rs.getDouble("neto")));
    }

    public List<ReportRows.RoomUsage> roomUsage(int actorId, String from, String to) {
        return queryCursor("generar el reporte", "{ ? = call pkg_reportes.fn_uso_habitaciones(?, ?, ?) }",
                rangeBinder(actorId, from, to),
                rs -> new ReportRows.RoomUsage(rs.getString("numero"), Room.Type.valueOf(rs.getString("tipo")),
                        rs.getInt("veces"), rs.getInt("noches"), rs.getDouble("valor")));
    }

    public List<ReportRows.TopClient> topClients(int actorId, String from, String to) {
        return queryCursor("generar el reporte", "{ ? = call pkg_reportes.fn_mejores_clientes(?, ?, ?) }",
                rangeBinder(actorId, from, to),
                rs -> new ReportRows.TopClient(rs.getString("nombre"),
                        Client.Category.valueOf(rs.getString("categoria")), rs.getInt("cant_reservas"),
                        rs.getInt("noches"), rs.getDouble("valor")));
    }

    public List<ReportRows.PendingBalance> pendingBalances(int actorId, String from, String to) {
        return queryCursor("generar el reporte", "{ ? = call pkg_reportes.fn_saldos_pendientes(?, ?, ?) }",
                rangeBinder(actorId, from, to),
                rs -> new ReportRows.PendingBalance(rs.getInt("comprobante_id"), rs.getString("cliente"),
                        LocalDate.parse(rs.getString("fecha_emision")), rs.getDouble("monto"),
                        rs.getDouble("pagado"), rs.getDouble("saldo"),
                        Invoice.Status.valueOf(rs.getString("estado"))));
    }

    public List<ReportRows.RoomRanking> roomRanking(String from, Integer limit) {
        return queryCursor("consultar el ranking", "{ ? = call pkg_reportes.fn_top_habitaciones(?, ?) }", call -> {
            call.setString(2, from);
            if (limit == null) call.setNull(3, Types.NUMERIC);
            else call.setInt(3, limit);
        }, rs -> new ReportRows.RoomRanking(rs.getInt("id"), rs.getString("numero"),
                Room.Type.valueOf(rs.getString("tipo")), rs.getInt("veces"), rs.getInt("noches")));
    }

    private static Binder rangeBinder(int actorId, String from, String to) {
        return call -> {
            call.setInt(2, actorId);
            call.setString(3, from);
            call.setString(4, to);
        };
    }

    // ------------------------------------------------------------------ llamadas JDBC

    private void run(String action, String sql, Binder in) {
        try (CallableStatement call = connection.prepareCall(sql)) {
            in.bind(call);
            call.execute();
        } catch (SQLException error) {
            throw translate(action, error);
        }
    }

    private int runReturningId(String action, String sql, int outIndex, Binder in) {
        try (CallableStatement call = connection.prepareCall(sql)) {
            in.bind(call);
            call.registerOutParameter(outIndex, Types.NUMERIC);
            call.execute();
            return call.getInt(outIndex);
        } catch (SQLException error) {
            throw translate(action, error);
        }
    }

    private BigDecimal callDecimal(String action, String sql, Binder in) {
        try (CallableStatement call = connection.prepareCall(sql)) {
            call.registerOutParameter(1, Types.NUMERIC);
            in.bind(call);
            call.execute();
            BigDecimal value = call.getBigDecimal(1);
            if (value == null) {
                throw new SQLException("Oracle devolvio un resultado numerico nulo para " + action + ".");
            }
            return value;
        } catch (SQLException error) {
            throw translate(action, error);
        }
    }

    private int callNumber(String action, String sql, Binder in) {
        try (CallableStatement call = connection.prepareCall(sql)) {
            call.registerOutParameter(1, Types.NUMERIC);
            in.bind(call);
            call.execute();
            return call.getInt(1);
        } catch (SQLException error) {
            throw translate(action, error);
        }
    }

    private <T> List<T> queryCursor(String action, String sql, Binder in, RowReader<T> reader) {
        try (CallableStatement call = connection.prepareCall(sql)) {
            call.registerOutParameter(1, OracleTypes.CURSOR);
            in.bind(call);
            call.execute();
            List<T> rows = new ArrayList<>();
            try (ResultSet rs = (ResultSet) call.getObject(1)) {
                while (rs.next()) rows.add(reader.read(rs));
            }
            return rows;
        } catch (SQLException error) {
            throw translate(action, error);
        }
    }

    // ------------------------------------------------------------------ errores

    private static RuntimeException translate(String action, SQLException error) {
        int code = error.getErrorCode();
        if (code >= 20000 && code <= 20999) {
            String message = oracleMessage(error.getMessage(), code);
            if (NOT_FOUND.contains(code)) return new ResourceNotFoundException(message);
            if (INVALID_DATA.contains(code)) return new InvalidDataException(message);
            return new BusinessRuleException(message);
        }
        if (code == UNIQUE_VIOLATION) {
            return new BusinessRuleException("Ya existe un registro con esos datos.");
        }
        return fail(action, error);
    }

    static String oracleMessage(String raw, int code) {
        if (raw == null) return "Error de la base de datos (ORA-" + code + ").";
        String prefix = "ORA-" + code + ": ";
        int start = raw.indexOf(prefix);
        String text = start >= 0 ? raw.substring(start + prefix.length()) : raw;
        int lineEnd = text.indexOf('\n');
        if (lineEnd >= 0) text = text.substring(0, lineEnd);
        return text.trim();
    }

    private static UncheckedIOException fail(String action, SQLException error) {
        return new UncheckedIOException("No se pudo " + action + ": " + error.getMessage(), new IOException(error));
    }
}

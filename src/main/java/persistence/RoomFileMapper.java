package persistence;

import java.util.List;
import model.Room;

/**
 * Convierte habitaciones entre objetos Java y registros de texto delimitados por punto y
 * coma.
 */
public class RoomFileMapper implements EntityFileMapper<Room> {

    /**
     * Convierte los datos de la habitacion en una linea delimitada para almacenarla.
     * @param room habitacion de la reserva.
     * @return representacion textual de la entidad.
     */
    @Override
    public String toLine(Room room) {
        return DelimitedText.join(Integer.toString(room.getId()), room.getNumber(), room.getType().name(),
                Integer.toString(room.getCapacity()), Double.toString(room.getPricePerNight()),
                Boolean.toString(room.isAvailable()));
    }

    /**
     * Reconstruye una habitacion a partir de una linea del archivo.
     * @param line linea de texto que se va a interpretar.
     * @return entidad reconstruida a partir de la linea.
     */
    @Override
    public Room fromLine(String line) {
        List<String> f = DelimitedText.requireFieldCount(line, 6);
        return new Room(Integer.parseInt(f.get(0)), f.get(1), Room.Type.valueOf(f.get(2)),
                Integer.parseInt(f.get(3)), Double.parseDouble(f.get(4)), Boolean.parseBoolean(f.get(5)));
    }
}

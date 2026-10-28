package persistence;

import java.util.List;
import model.Room;

public class RoomFileMapper implements EntityFileMapper<Room> {
    @Override
    public String toLine(Room room) {
        return DelimitedText.join(Integer.toString(room.getId()), room.getNumber(), room.getType().name(),
                Integer.toString(room.getCapacity()), Double.toString(room.getPricePerNight()),
                Boolean.toString(room.isAvailable()));
    }

    @Override
    public Room fromLine(String line) {
        List<String> f = DelimitedText.requireFieldCount(line, 6);
        return new Room(Integer.parseInt(f.get(0)), f.get(1), Room.Type.valueOf(f.get(2)),
                Integer.parseInt(f.get(3)), Double.parseDouble(f.get(4)), Boolean.parseBoolean(f.get(5)));
    }
}

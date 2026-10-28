package persistence;

import java.util.List;
import model.Client;

public class ClientFileMapper implements EntityFileMapper<Client> {
    @Override
    public String toLine(Client client) {
        return DelimitedText.join(
                Integer.toString(client.getId()), client.getName(), client.getDocumentNumber(),
                client.getEmail(), client.getPhone(), client.getCity(), client.getCategory().name(),
                Boolean.toString(client.isActive()));
    }

    @Override
    public Client fromLine(String line) {
        List<String> f = DelimitedText.requireFieldCount(line, 8);
        Client client = new Client(Integer.parseInt(f.get(0)), f.get(1), f.get(2), f.get(3),
                f.get(4), f.get(5), Client.Category.valueOf(f.get(6)));
        if (!Boolean.parseBoolean(f.get(7))) client.deactivate();
        return client;
    }
}

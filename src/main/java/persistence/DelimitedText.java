package persistence;

import java.util.ArrayList;
import java.util.List;

public final class DelimitedText {

    private DelimitedText() { }

    public static String join(String... fields) {
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) line.append(';');
            String value = fields[i] == null ? "" : fields[i];
            for (int j = 0; j < value.length(); j++) {
                char character = value.charAt(j);
                switch (character) {
                    case '\\' -> line.append("\\\\");
                    case ';' -> line.append("\\;");
                    case '\n' -> line.append("\\n");
                    case '\r' -> line.append("\\r");
                    default -> line.append(character);
                }
            }
        }
        return line.toString();
    }

    public static List<String> split(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean escaped = false;

        for (int i = 0; i < line.length(); i++) {
            char character = line.charAt(i);
            if (escaped) {
                switch (character) {
                    case 'n' -> field.append('\n');
                    case 'r' -> field.append('\r');
                    default -> field.append(character);
                }
                escaped = false;
            } else if (character == '\\') {
                escaped = true;
            } else if (character == ';') {
                fields.add(field.toString());
                field.setLength(0);
            } else {
                field.append(character);
            }
        }
        if (escaped) field.append('\\');
        fields.add(field.toString());
        return fields;
    }

    public static List<String> requireFieldCount(String line, int expected) {
        List<String> fields = split(line);
        if (fields.size() != expected) {
            throw new IllegalArgumentException("Se esperaban " + expected + " campos, pero se encontraron " + fields.size() + ".");
        }
        return fields;
    }
}

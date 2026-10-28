package persistence;

import model.Identifiable;

public interface EntityFileMapper<T extends Identifiable> {
    String toLine(T entity);
    T fromLine(String line);
}

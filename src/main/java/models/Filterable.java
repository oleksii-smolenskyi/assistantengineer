package models;

import java.util.Set;

public interface Filterable {
    Set<String> getFilterColumnItems(int idColumn);
    void addFilter(int idColumn, Set<String> items);
}

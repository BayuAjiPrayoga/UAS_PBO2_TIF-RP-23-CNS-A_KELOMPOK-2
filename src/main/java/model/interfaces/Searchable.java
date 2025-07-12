package model.interfaces;

import java.util.List;

/**
 * Interface untuk entity yang dapat dicari
 * Implementasi prinsip Polymorphism dengan Generic Type
 */
public interface Searchable<T> {
    List<T> search(String query);
    List<T> filterByCategory(String category);
    List<T> filterByStatus(String status);
}

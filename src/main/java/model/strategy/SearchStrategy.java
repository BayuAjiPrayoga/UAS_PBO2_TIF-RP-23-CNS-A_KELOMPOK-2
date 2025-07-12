package model.strategy;

import java.util.List;

/**
 * Strategy interface untuk pencarian barang
 * Implementasi Strategy Pattern untuk polymorphism
 */
public interface SearchStrategy<T> {
    List<T> search(List<T> items, String keyword);
}

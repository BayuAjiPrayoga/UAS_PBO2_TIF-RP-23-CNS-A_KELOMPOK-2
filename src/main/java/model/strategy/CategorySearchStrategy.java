package model.strategy;

import model.Barang;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Concrete Strategy untuk pencarian berdasarkan kategori
 */
public class CategorySearchStrategy implements SearchStrategy<Barang> {
    @Override
    public List<Barang> search(List<Barang> items, String keyword) {
        return items.stream()
                .filter(barang -> barang.getKategori() != null && 
                               barang.getKategori().toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toList());
    }
}

package model.strategy;

import model.Barang;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Concrete Strategy untuk pencarian berdasarkan harga range
 */
public class PriceRangeSearchStrategy implements SearchStrategy<Barang> {
    private final double minPrice;
    private final double maxPrice;
    
    public PriceRangeSearchStrategy(double minPrice, double maxPrice) {
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
    }
    
    @Override
    public List<Barang> search(List<Barang> items, String keyword) {
        return items.stream()
                .filter(barang -> barang.getHarga() >= minPrice && barang.getHarga() <= maxPrice)
                .collect(Collectors.toList());
    }
}

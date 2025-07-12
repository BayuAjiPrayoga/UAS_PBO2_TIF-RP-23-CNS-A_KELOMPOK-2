package model.strategy;

import model.Barang;
import java.util.List;

/**
 * Context class untuk strategy pattern
 */
public class SearchContext {
    private SearchStrategy<Barang> strategy;
    
    public SearchContext(SearchStrategy<Barang> strategy) {
        this.strategy = strategy;
    }
    
    public void setStrategy(SearchStrategy<Barang> strategy) {
        this.strategy = strategy;
    }
    
    public List<Barang> executeSearch(List<Barang> items, String keyword) {
        return strategy.search(items, keyword);
    }
}

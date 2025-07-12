package controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import config.DatabaseConnection;
import model.Barang;
import model.interfaces.Searchable;
import utils.AlertUtils;

/**
 * BarangController yang mengextend BaseController (Inheritance)
 * dan mengimplementasi Searchable interface (Polymorphism)
 */
public class BarangController extends BaseController<Barang> implements Searchable<Barang> {

    // Implementasi abstract methods dari BaseController (Polymorphism)
    @Override
    protected boolean performSave(Barang barang) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "INSERT INTO barang (nama_barang, stok, harga, kategori) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, barang.getNamaBarang());
                stmt.setInt(2, barang.getStok());
                stmt.setDouble(3, barang.getHarga());
                stmt.setString(4, barang.getKategori() != null ? barang.getKategori() : "Umum");
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected boolean performUpdate(Barang barang) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "UPDATE barang SET nama_barang=?, stok=?, harga=?, kategori=? WHERE id=?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, barang.getNamaBarang());
                stmt.setInt(2, barang.getStok());
                stmt.setDouble(3, barang.getHarga());
                stmt.setString(4, barang.getKategori() != null ? barang.getKategori() : "Umum");
                stmt.setInt(5, barang.getId());
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected boolean performDelete(int id) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Cek apakah barang memiliki transaksi
            String checkQuery = "SELECT COUNT(*) FROM transaksi WHERE id_barang = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                checkStmt.setInt(1, id);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    showErrorMessage("Barang tidak dapat dihapus karena memiliki riwayat transaksi!");
                    return false;
                }
            }
            
            String query = "DELETE FROM barang WHERE id=?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, id);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected List<Barang> performGetAll() {
        List<Barang> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM barang ORDER BY nama_barang ASC";
            try (PreparedStatement stmt = conn.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    Barang b = createBarangFromResultSet(rs);
                    list.add(b);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    protected Barang performGetById(int id) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM barang WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return createBarangFromResultSet(rs);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Helper method untuk membuat object Barang dari ResultSet
    private Barang createBarangFromResultSet(ResultSet rs) throws SQLException {
        Barang b = new Barang();
        b.setId(rs.getInt("id"));
        b.setNamaBarang(rs.getString("nama_barang"));
        b.setStok(rs.getInt("stok"));
        b.setHarga(rs.getDouble("harga"));
        
        // Handle kategori yang mungkin null di database lama
        String kategori = rs.getString("kategori");
        b.setKategori(kategori != null ? kategori : "Umum");
        
        return b;
    }

    // Implementasi interface Searchable (Polymorphism)
    @Override
    public List<Barang> search(String query) {
        return getAllEntities().stream()
                .filter(barang -> barang.getNamaBarang().toLowerCase()
                        .contains(query.toLowerCase()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Barang> filterByCategory(String category) {
        return getAllEntities().stream()
                .filter(barang -> barang.getKategori().equalsIgnoreCase(category))
                .collect(Collectors.toList());
    }

    @Override
    public List<Barang> filterByStatus(String status) {
        return getAllEntities().stream()
                .filter(barang -> barang.getStokStatus().equalsIgnoreCase(status))
                .collect(Collectors.toList());
    }

    // Business methods dengan polymorphism
    public List<Barang> getLowStockItems() {
        return getAllEntities().stream()
                .filter(Barang::isLowStock)
                .collect(Collectors.toList());
    }

    public List<Barang> getOutOfStockItems() {
        return getAllEntities().stream()
                .filter(Barang::isOutOfStock)
                .collect(Collectors.toList());
    }

    // Method untuk update stok dengan validation
    public boolean updateStok(int idBarang, int newStok) {
        Barang barang = getEntityById(idBarang);
        if (barang == null) {
            showErrorMessage("Barang tidak ditemukan!");
            return false;
        }
        
        barang.setStok(newStok);
        return updateEntity(barang);
    }

    // Method untuk mengurangi stok (untuk transaksi)
    public boolean reduceStock(int idBarang, int quantity) {
        Barang barang = getEntityById(idBarang);
        if (barang == null) {
            showErrorMessage("Barang tidak ditemukan!");
            return false;
        }
        
        try {
            barang.reduceStock(quantity);
            return updateEntity(barang);
        } catch (IllegalArgumentException e) {
            showErrorMessage(e.getMessage());
            return false;
        }
    }

    // Method untuk menambah stok
    public boolean addStock(int idBarang, int quantity) {
        Barang barang = getEntityById(idBarang);
        if (barang == null) {
            showErrorMessage("Barang tidak ditemukan!");
            return false;
        }
        
        barang.addStock(quantity);
        return updateEntity(barang);
    }

    // Method untuk cek ketersediaan stok
    public boolean cekStokTersedia(int idBarang, int jumlahDiminta) {
        Barang barang = getEntityById(idBarang);
        return barang != null && barang.canReduceStock(jumlahDiminta);
    }

    // Method untuk statistik stok
    public StokStatistik getStokStatistik() {
        StokStatistik stats = new StokStatistik();
        List<Barang> allBarang = getAllEntities();
        
        stats.setTotalProduk(allBarang.size());
        stats.setStokHabis((int) allBarang.stream().filter(Barang::isOutOfStock).count());
        stats.setStokMenipis((int) allBarang.stream().filter(Barang::isLowStock).count());
        stats.setNilaiTotalStok(allBarang.stream().mapToDouble(Barang::getTotalValue).sum());
        
        return stats;
    }

    // Backward compatibility methods (delegation to base class)
    public List<Barang> getAllBarang() {
        return getAllEntities();
    }

    public boolean tambahBarang(Barang barang) {
        return saveEntity(barang);
    }

    public boolean updateBarang(Barang barang) {
        return updateEntity(barang);
    }

    public boolean deleteBarang(int id) {
        Barang barang = getBarangById(id);
        if (barang != null) {
            return deleteEntity(barang);
        }
        return false;
    }

    public Barang getBarangById(int id) {
        return getEntityById(id);
    }

    public List<Barang> getBarangByStatusStok(String status) {
        return filterByStatus(status);
    }

    public List<Barang> getBarangByKategori(String kategori) {
        return filterByCategory(kategori);
    }

    // Inner class untuk statistik stok
    public static class StokStatistik {
        private int totalProduk;
        private int stokHabis;
        private int stokMenipis;
        private double nilaiTotalStok;
        
        // Getters and setters
        public int getTotalProduk() { return totalProduk; }
        public void setTotalProduk(int totalProduk) { this.totalProduk = totalProduk; }
        
        public int getStokHabis() { return stokHabis; }
        public void setStokHabis(int stokHabis) { this.stokHabis = stokHabis; }
        
        public int getStokMenipis() { return stokMenipis; }
        public void setStokMenipis(int stokMenipis) { this.stokMenipis = stokMenipis; }
        
        public double getNilaiTotalStok() { return nilaiTotalStok; }
        public void setNilaiTotalStok(double nilaiTotalStok) { this.nilaiTotalStok = nilaiTotalStok; }
    }
}

package model;

import model.interfaces.Validatable;
import model.interfaces.Exportable;
import config.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

/**
 * Barang class yang mengextend BaseEntity (Inheritance)
 * dan mengimplementasi interfaces (Polymorphism)
 */
public class Barang extends BaseEntity implements Validatable, Exportable {
    private String namaBarang;
    private int stok;
    private double harga;
    private String kategori;

    public Barang() {
        super();
        this.kategori = "Umum";
    }

    public Barang(int id, String namaBarang, int stok, double harga) {
        super(id);
        this.namaBarang = namaBarang;
        this.stok = stok;
        this.harga = harga;
        this.kategori = "Umum";
    }

    public Barang(int id, String namaBarang, int stok, double harga, String kategori) {
        super(id);
        this.namaBarang = namaBarang;
        this.stok = stok;
        this.harga = harga;
        this.kategori = kategori;
    }

    // Implementasi abstract methods dari BaseEntity (Polymorphism)
    @Override
    public boolean isValid() {
        return namaBarang != null && !namaBarang.trim().isEmpty() &&
               stok >= 0 && harga > 0;
    }

    @Override
    public String getDisplayName() {
        return namaBarang != null ? namaBarang : "Unknown Product";
    }

    @Override
    public String getEntityType() {
        return "Barang";
    }

    @Override
    protected boolean performSave() {
        // Implementasi langsung ke database tanpa circular call
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query;
            if (this.getId() == 0) {
                // Insert new record
                query = "INSERT INTO barang (nama_barang, stok, harga, kategori) VALUES (?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setString(1, this.getNamaBarang());
                    stmt.setInt(2, this.getStok());
                    stmt.setDouble(3, this.getHarga());
                    stmt.setString(4, this.getKategori() != null ? this.getKategori() : "Umum");
                    return stmt.executeUpdate() > 0;
                }
            } else {
                // Update existing record
                query = "UPDATE barang SET nama_barang=?, stok=?, harga=?, kategori=? WHERE id=?";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setString(1, this.getNamaBarang());
                    stmt.setInt(2, this.getStok());
                    stmt.setDouble(3, this.getHarga());
                    stmt.setString(4, this.getKategori() != null ? this.getKategori() : "Umum");
                    stmt.setInt(5, this.getId());
                    return stmt.executeUpdate() > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    protected boolean performDelete() {
        // Implementasi delete langsung ke database
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "DELETE FROM barang WHERE id=?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, this.getId());
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Implementasi interface Validatable (Polymorphism)
    @Override
    public List<String> getValidationErrors() {
        List<String> errors = new ArrayList<>();
        
        if (namaBarang == null || namaBarang.trim().isEmpty()) {
            errors.add("Nama barang tidak boleh kosong");
        }
        if (stok < 0) {
            errors.add("Stok tidak boleh negatif");
        }
        if (harga <= 0) {
            errors.add("Harga harus lebih dari 0");
        }
        
        return errors;
    }

    // Implementasi interface Exportable (Polymorphism)
    @Override
    public void exportToCSV(String filePath) {
        // Implementasi export ke CSV
        String csvData = getExportData();
        // Logic untuk write ke file CSV
    }

    @Override
    public void exportToPDF(String filePath) {
        // Implementasi export ke PDF
        String data = getExportData();
        // Logic untuk write ke file PDF
    }

    @Override
    public String getExportData() {
        return String.format("%d,%s,%d,%.2f,%s", 
                getId(), namaBarang, stok, harga, kategori);
    }

    // Business methods specific to Barang
    public String getStokStatus() {
        if (stok == 0) {
            return "Habis";
        } else if (stok <= 10) {
            return "Menipis";
        } else {
            return "Tersedia";
        }
    }

    public boolean isLowStock() {
        return stok <= 10;
    }

    public boolean isOutOfStock() {
        return stok == 0;
    }

    public boolean canReduceStock(int quantity) {
        return stok >= quantity;
    }

    public void reduceStock(int quantity) {
        if (canReduceStock(quantity)) {
            this.stok -= quantity;
            markAsUpdated();
        } else {
            throw new IllegalArgumentException("Stok tidak mencukupi");
        }
    }

    public void addStock(int quantity) {
        if (quantity > 0) {
            this.stok += quantity;
            markAsUpdated();
        }
    }

    public double getTotalValue() {
        return stok * harga;
    }

    // Getters and Setters
    public String getNamaBarang() { return namaBarang; }
    public void setNamaBarang(String namaBarang) { 
        this.namaBarang = namaBarang;
        markAsUpdated();
    }

    public int getStok() { return stok; }
    public void setStok(int stok) { 
        this.stok = stok;
        markAsUpdated();
    }

    public double getHarga() { return harga; }
    public void setHarga(double harga) { 
        this.harga = harga;
        markAsUpdated();
    }

    public String getKategori() { return kategori; }
    public void setKategori(String kategori) { 
        this.kategori = kategori;
        markAsUpdated();
    }
}

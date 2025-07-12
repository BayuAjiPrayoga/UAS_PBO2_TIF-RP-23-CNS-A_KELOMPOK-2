package controller;

import config.DatabaseConnection;
import model.Transaksi;
import utils.AlertUtils;


import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import model.TransaksiRiwayat;

public class TransaksiController {

    public void simpanTransaksi(Transaksi transaksi) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "INSERT INTO transaksi (id_barang, id_kasir, jumlah, total_harga, waktu) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, transaksi.getIdBarang());
            stmt.setInt(2, transaksi.getIdKasir());
            stmt.setInt(3, transaksi.getJumlah());
            stmt.setDouble(4, transaksi.getTotalHarga());
            stmt.setTimestamp(5, Timestamp.valueOf(transaksi.getWaktu()));
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public List<TransaksiRiwayat> getRiwayatTransaksiLengkap() {
    List<TransaksiRiwayat> riwayatList = new ArrayList<>();

    String query = "SELECT t.jumlah, t.total_harga, t.waktu, " +
               "b.nama_barang, u.username AS kasir " +
               "FROM transaksi t " +
               "JOIN barang b ON t.id_barang = b.id " +
               "JOIN users u ON t.id_kasir = u.id " +
               "ORDER BY t.waktu DESC";


    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(query);
         ResultSet rs = stmt.executeQuery()) {

        while (rs.next()) {
            String namaBarang = rs.getString("nama_barang");
            String kasir = rs.getString("kasir");
            int jumlah = rs.getInt("jumlah");
            double total = rs.getDouble("total_harga");
            LocalDateTime waktu = rs.getTimestamp("waktu").toLocalDateTime();

            TransaksiRiwayat riwayat = new TransaksiRiwayat(namaBarang, kasir, jumlah, total, waktu);
            riwayatList.add(riwayat);
        }

    } catch (SQLException e) {
        System.err.println("Gagal mengambil data riwayat transaksi: " + e.getMessage());
        e.printStackTrace();
    }

    return riwayatList;
}



    public List<Transaksi> getAllTransaksi() {
        List<Transaksi> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM transaksi ORDER BY waktu DESC";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                Transaksi t = new Transaksi();
                t.setId(rs.getInt("id"));
                t.setIdBarang(rs.getInt("id_barang"));
                t.setIdKasir(rs.getInt("id_kasir"));
                t.setJumlah(rs.getInt("jumlah"));
                t.setTotalHarga(rs.getDouble("total_harga"));
                t.setWaktu(rs.getTimestamp("waktu").toLocalDateTime());
                list.add(t);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Transaksi> getTransaksiByKasir(int idKasir) {
        List<Transaksi> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM transaksi WHERE id_kasir = ? ORDER BY waktu DESC";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, idKasir);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Transaksi t = new Transaksi();
                t.setId(rs.getInt("id"));
                t.setIdBarang(rs.getInt("id_barang"));
                t.setIdKasir(rs.getInt("id_kasir"));
                t.setJumlah(rs.getInt("jumlah"));
                t.setTotalHarga(rs.getDouble("total_harga"));
                t.setWaktu(rs.getTimestamp("waktu").toLocalDateTime());
                list.add(t);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    
    public boolean deleteAllTransactions() {
        String query = "DELETE FROM transaksi";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            int deletedCount = stmt.executeUpdate();
            System.out.println("Deleted " + deletedCount + " transactions");
            return true;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean deleteTransactionsByPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        String query = "DELETE FROM transaksi WHERE waktu >= ? AND waktu <= ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setTimestamp(1, Timestamp.valueOf(startDate));
            stmt.setTimestamp(2, Timestamp.valueOf(endDate));
            
            int deletedCount = stmt.executeUpdate();
            System.out.println("Deleted " + deletedCount + " transactions in period");
            return true;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean deleteTransactionsByUser(int userId) {
        String query = "DELETE FROM transaksi WHERE id_kasir = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, userId);
            
            int deletedCount = stmt.executeUpdate();
            System.out.println("Deleted " + deletedCount + " transactions for user " + userId);
            return true;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean deleteTransactionsByProduct(int productId) {
        String query = "DELETE FROM transaksi WHERE id_barang = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, productId);
            
            int deletedCount = stmt.executeUpdate();
            System.out.println("Deleted " + deletedCount + " transactions for product " + productId);
            return true;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public int getTransactionCount() {
        String query = "SELECT COUNT(*) FROM transaksi";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return 0;
    }
}

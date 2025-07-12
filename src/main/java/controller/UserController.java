package controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import config.DatabaseConnection;
import model.User;

public class UserController {
    
    public boolean createUser(User user) {
        String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getRole());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean updateUser(User user) {
        String sql;
        
        // Jika password kosong, jangan update password
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            sql = "UPDATE users SET username = ?, role = ? WHERE id = ?";
        } else {
            sql = "UPDATE users SET username = ?, password = ?, role = ? WHERE id = ?";
        }
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, user.getUsername());
            
            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                stmt.setString(2, user.getPassword());
                stmt.setString(3, user.getRole());
                stmt.setInt(4, user.getId());
            } else {
                stmt.setString(2, user.getRole());
                stmt.setInt(3, user.getId());
            }
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean deleteUser(int userId) {
        // Coba soft delete dulu, jika gagal gunakan hard delete
        String softDeleteSql = "UPDATE users SET is_active = false WHERE id = ?";
        String hardDeleteSql = "DELETE FROM users WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            
            // Coba soft delete dulu
            try (PreparedStatement stmt = conn.prepareStatement(softDeleteSql)) {
                stmt.setInt(1, userId);
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("User soft deleted successfully");
                    return true;
                }
            } catch (SQLException e) {
                // Kolom is_active tidak ada, gunakan hard delete
                System.out.println("is_active column not found, using hard delete...");
                
                // Cek apakah user memiliki transaksi
                if (hasTransactions(userId)) {
                    System.out.println("Cannot delete user: has transactions");
                    return false;
                }
                
                try (PreparedStatement stmt = conn.prepareStatement(hardDeleteSql)) {
                    stmt.setInt(1, userId);
                    int rowsAffected = stmt.executeUpdate();
                    return rowsAffected > 0;
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        
        return false;
    }
    
    public boolean restoreUser(int userId) {
        // Restore user: set is_active = true
        String sql = "UPDATE users SET is_active = true WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean hasTransactions(int userId) {
        String sql = "SELECT COUNT(*) FROM transaksi WHERE id_kasir = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    public User getUserById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setRole(rs.getString("role"));
                // Cek apakah kolom is_active ada
                try {
                    user.setActive(rs.getBoolean("is_active"));
                } catch (SQLException e) {
                    user.setActive(true); // default jika kolom belum ada
                }
                return user;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY username";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setRole(rs.getString("role"));
                // Cek apakah kolom is_active ada
                try {
                    boolean isActive = rs.getBoolean("is_active");
                    user.setActive(isActive);
                    // Hanya tampilkan user yang aktif jika kolom is_active ada
                    if (!isActive) {
                        continue;
                    }
                } catch (SQLException e) {
                    user.setActive(true); // default jika kolom belum ada
                }
                users.add(user);
            }
            
        } catch (SQLException e) {
            // Jika error karena kolom is_active tidak ada, coba query lama
            System.out.println("Trying fallback query without is_active column...");
            return getAllUsersLegacy();
        }
        
        return users;
    }
    
    private List<User> getAllUsersLegacy() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY username";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setRole(rs.getString("role"));
                user.setActive(true); // default untuk database lama
                users.add(user);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return users;
    }
    
    public List<User> getAllUsersIncludingInactive() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY username";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setRole(rs.getString("role"));
                // Cek apakah kolom is_active ada
                try {
                    user.setActive(rs.getBoolean("is_active"));
                } catch (SQLException e) {
                    user.setActive(true); // default jika kolom belum ada
                }
                users.add(user);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return users;
    }
    
    public boolean isUsernameExists(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    public User authenticate(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            stmt.setString(2, password);
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setRole(rs.getString("role"));
                // Cek apakah kolom is_active ada dan user aktif
                try {
                    boolean isActive = rs.getBoolean("is_active");
                    user.setActive(isActive);
                    // Jika user tidak aktif, return null
                    if (!isActive) {
                        return null;
                    }
                } catch (SQLException e) {
                    user.setActive(true); // default jika kolom belum ada
                }
                return user;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }
}

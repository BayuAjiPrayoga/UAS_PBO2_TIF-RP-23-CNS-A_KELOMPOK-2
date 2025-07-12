package utils;

import config.DatabaseConnection;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseSetup {
    
    public static boolean addIsActiveColumn() {
        System.out.println("Checking and adding is_active column if needed...");
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Cek apakah kolom is_active sudah ada
            try {
                stmt.executeQuery("SELECT is_active FROM users LIMIT 1");
                System.out.println("✅ Column 'is_active' already exists.");
                return true;
            } catch (SQLException e) {
                // Kolom belum ada, tambahkan
                System.out.println("📝 Adding 'is_active' column...");
                stmt.executeUpdate("ALTER TABLE users ADD COLUMN is_active BOOLEAN DEFAULT TRUE");
                stmt.executeUpdate("UPDATE users SET is_active = TRUE WHERE is_active IS NULL");
                System.out.println("✅ Column 'is_active' added successfully.");
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Failed to add is_active column: " + e.getMessage());
            return false;
        }
    }
}

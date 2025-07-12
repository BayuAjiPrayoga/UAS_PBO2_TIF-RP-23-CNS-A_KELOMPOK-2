package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/ngawarung";
    private static final String USER = "root"; // Ganti jika user MySQL kamu berbeda
    private static final String PASSWORD = ""; // Ganti jika pakai password

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // Driver MySQL terbaru
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver MySQL tidak ditemukan: " + e.getMessage());
        }
    }
}

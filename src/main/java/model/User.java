package model;

import model.interfaces.Validatable;
import model.interfaces.Auditable;
import controller.UserController;
import config.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

/**
 * User class yang mengextend BaseEntity (Inheritance)
 * dan mengimplementasi interfaces (Polymorphism)
 */
public class User extends BaseEntity implements Validatable, Auditable {
    private String username;
    private String password;
    private String role; // "admin" atau "kasir"
    private String createdBy;
    private String lastModifiedBy;

    public User() {
        super();
        this.createdBy = "system";
        this.lastModifiedBy = "system";
    }

    public User(int id, String username, String password, String role) {
        super(id);
        this.username = username;
        this.password = password;
        this.role = role;
        this.createdBy = "system";
        this.lastModifiedBy = "system";
    }

    // Implementasi abstract methods dari BaseEntity (Polymorphism)
    @Override
    public boolean isValid() {
        return username != null && !username.trim().isEmpty() &&
               password != null && !password.trim().isEmpty() &&
               role != null && (role.equals("admin") || role.equals("kasir"));
    }

    @Override
    public String getDisplayName() {
        return username != null ? username : "Unknown User";
    }

    @Override
    public String getEntityType() {
        return "User";
    }

    @Override
    protected boolean performSave() {
        // Implementasi langsung ke database tanpa circular call
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query;
            if (this.getId() == 0) {
                // Insert new record
                query = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setString(1, this.getUsername());
                    stmt.setString(2, this.getPassword());
                    stmt.setString(3, this.getRole());
                    return stmt.executeUpdate() > 0;
                }
            } else {
                // Update existing record
                query = "UPDATE users SET username=?, password=?, role=? WHERE id=?";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setString(1, this.getUsername());
                    stmt.setString(2, this.getPassword());
                    stmt.setString(3, this.getRole());
                    stmt.setInt(4, this.getId());
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
            String query = "DELETE FROM users WHERE id=?";
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
        
        if (username == null || username.trim().isEmpty()) {
            errors.add("Username tidak boleh kosong");
        }
        if (password == null || password.trim().isEmpty()) {
            errors.add("Password tidak boleh kosong");
        }
        if (role == null || (!role.equals("admin") && !role.equals("kasir"))) {
            errors.add("Role harus admin atau kasir");
        }
        
        return errors;
    }

    // Implementasi interface Auditable (Polymorphism)
    @Override
    public String getCreatedBy() {
        return createdBy;
    }

    @Override
    public void setCreatedBy(String username) {
        this.createdBy = username;
    }

    @Override
    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    @Override
    public void setLastModifiedBy(String username) {
        this.lastModifiedBy = username;
        markAsUpdated();
    }

    @Override
    public String getAuditInfo() {
        return String.format("Created by: %s at %s, Last modified by: %s at %s", 
                createdBy, getCreatedAt(), lastModifiedBy, getUpdatedAt());
    }

    // Business methods specific to User
    public boolean isAdmin() {
        return "admin".equals(role);
    }

    public boolean isKasir() {
        return "kasir".equals(role);
    }

    public boolean canAccess(String feature) {
        if (isAdmin()) {
            return true; // Admin dapat akses semua fitur
        }
        
        // Kasir hanya dapat akses fitur tertentu
        return feature.equals("transaction") || feature.equals("view_products");
    }

    // Getters & Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { 
        this.username = username;
        markAsUpdated();
    }

    public String getPassword() { return password; }
    public void setPassword(String password) { 
        this.password = password;
        markAsUpdated();
    }

    public String getRole() { return role; }
    public void setRole(String role) { 
        this.role = role;
        markAsUpdated();
    }
}

package model.factory;

import model.BaseEntity;
import model.User;
import model.Barang;

/**
 * Factory Pattern untuk membuat instance entity
 * Implementasi prinsip Polymorphism dan Abstraction
 */
public class EntityFactory {
    
    // Factory method untuk membuat entity berdasarkan type
    public static BaseEntity createEntity(String entityType) {
        switch (entityType.toLowerCase()) {
            case "user":
                return new User();
            case "barang":
                return new Barang();
            default:
                throw new IllegalArgumentException("Unknown entity type: " + entityType);
        }
    }
    
    // Factory method dengan parameter untuk User
    public static User createUser(String username, String password, String role) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRole(role);
        return user;
    }
    
    // Factory method dengan parameter untuk Barang
    public static Barang createBarang(String nama, int stok, double harga, String kategori) {
        Barang barang = new Barang();
        barang.setNamaBarang(nama);
        barang.setStok(stok);
        barang.setHarga(harga);
        barang.setKategori(kategori);
        return barang;
    }
    
    // Factory method untuk create default entities
    public static User createDefaultAdmin() {
        return createUser("admin", "admin123", "admin");
    }
    
    public static User createDefaultKasir() {
        return createUser("kasir", "kasir123", "kasir");
    }
    
    // Builder pattern untuk complex object creation
    public static class UserBuilder {
        private User user;
        
        public UserBuilder() {
            this.user = new User();
        }
        
        public UserBuilder setUsername(String username) {
            user.setUsername(username);
            return this;
        }
        
        public UserBuilder setPassword(String password) {
            user.setPassword(password);
            return this;
        }
        
        public UserBuilder setRole(String role) {
            user.setRole(role);
            return this;
        }
        
        public UserBuilder setCreatedBy(String createdBy) {
            user.setCreatedBy(createdBy);
            return this;
        }
        
        public User build() {
            if (!user.isValid()) {
                throw new IllegalStateException("User data is not valid");
            }
            return user;
        }
    }
    
    public static class BarangBuilder {
        private Barang barang;
        
        public BarangBuilder() {
            this.barang = new Barang();
        }
        
        public BarangBuilder setNama(String nama) {
            barang.setNamaBarang(nama);
            return this;
        }
        
        public BarangBuilder setStok(int stok) {
            barang.setStok(stok);
            return this;
        }
        
        public BarangBuilder setHarga(double harga) {
            barang.setHarga(harga);
            return this;
        }
        
        public BarangBuilder setKategori(String kategori) {
            barang.setKategori(kategori);
            return this;
        }
        
        public Barang build() {
            if (!barang.isValid()) {
                throw new IllegalStateException("Barang data is not valid");
            }
            return barang;
        }
    }
    
    // Static factory methods untuk Builder pattern
    public static UserBuilder userBuilder() {
        return new UserBuilder();
    }
    
    public static BarangBuilder barangBuilder() {
        return new BarangBuilder();
    }
}

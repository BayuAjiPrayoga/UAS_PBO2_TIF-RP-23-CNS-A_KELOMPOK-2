package model;

import java.time.LocalDateTime;
import model.observer.EntitySubject;
import model.observer.EntityObserver;
import model.observer.EntityEventManager;

/**
 * Abstract base class untuk semua entity dalam sistem
 * Implementasi prinsip Inheritance dan menyediakan common functionality
 */
public abstract class BaseEntity implements EntitySubject {
    protected int id;
    protected LocalDateTime createdAt;
    protected LocalDateTime updatedAt;
    protected boolean isActive;
    protected String createdBy;
    protected String updatedBy;
    
    // Observer pattern implementation
    private static EntityEventManager eventManager = new EntityEventManager();

    public BaseEntity() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.isActive = true;
    }

    public BaseEntity(int id) {
        this();
        this.id = id;
    }

    // Abstract methods yang harus diimplementasi oleh subclass (Polymorphism)
    public abstract boolean isValid();
    public abstract String getDisplayName();
    public abstract String getEntityType();

    // Common methods untuk semua entity
    public void markAsUpdated() {
        this.updatedAt = LocalDateTime.now();
    }

    public void softDelete() {
        this.isActive = false;
        markAsUpdated();
    }

    public void restore() {
        this.isActive = true;
        markAsUpdated();
    }

    @Override
    public void addObserver(EntityObserver observer) {
        eventManager.addObserver(observer);
    }
    
    @Override
    public void removeObserver(EntityObserver observer) {
        eventManager.removeObserver(observer);
    }
    
    @Override
    public void notifyObservers(String event, Object entity) {
        eventManager.notifyObservers(event, entity);
    }
    
    // Template method pattern dengan Observer integration
    public final boolean save() {
        if (!isValid()) {
            return false;
        }
        
        boolean isNewEntity = (this.id == 0);
        boolean result = performSave();
        
        if (result) {
            updateTimestamp();
            if (isNewEntity) {
                notifyObservers("CREATE", this);
            } else {
                notifyObservers("UPDATE", this);
            }
        }
        
        return result;
    }
    
    public final boolean delete() {
        boolean result = performDelete();
        
        if (result) {
            notifyObservers("DELETE", this);
        }
        
        return result;
    }

    // Hook method untuk subclass override
    protected abstract boolean performSave();
    protected abstract boolean performDelete();

    // Template method untuk update timestamp
    private void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters untuk audit fields
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { this.isActive = active; }

    @Override
    public String toString() {
        return getEntityType() + "{" +
                "id=" + id +
                ", displayName='" + getDisplayName() + '\'' +
                ", isActive=" + isActive +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        BaseEntity that = (BaseEntity) obj;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}

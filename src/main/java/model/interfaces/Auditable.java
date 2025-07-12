package model.interfaces;

/**
 * Interface untuk entity yang dapat diaudit
 * Implementasi prinsip Polymorphism
 */
public interface Auditable {
    String getCreatedBy();
    String getLastModifiedBy();
    void setCreatedBy(String username);
    void setLastModifiedBy(String username);
    String getAuditInfo();
}

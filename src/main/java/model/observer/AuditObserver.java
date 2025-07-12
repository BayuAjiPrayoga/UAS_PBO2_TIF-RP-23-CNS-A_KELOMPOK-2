package model.observer;

/**
 * Concrete Observer untuk audit trail
 */
public class AuditObserver implements EntityObserver {
    @Override
    public void onEntityCreated(Object entity) {
        // Implement audit trail for creation
        System.out.println("AUDIT: New entity created at " + java.time.LocalDateTime.now());
    }
    
    @Override
    public void onEntityUpdated(Object entity) {
        // Implement audit trail for updates
        System.out.println("AUDIT: Entity modified at " + java.time.LocalDateTime.now());
    }
    
    @Override
    public void onEntityDeleted(Object entity) {
        // Implement audit trail for deletion
        System.out.println("AUDIT: Entity deleted at " + java.time.LocalDateTime.now());
    }
}

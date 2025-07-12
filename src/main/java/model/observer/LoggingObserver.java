package model.observer;

/**
 * Concrete Observer untuk logging
 */
public class LoggingObserver implements EntityObserver {
    @Override
    public void onEntityCreated(Object entity) {
        System.out.println("LOG: Entity created - " + entity.getClass().getSimpleName());
    }
    
    @Override
    public void onEntityUpdated(Object entity) {
        System.out.println("LOG: Entity updated - " + entity.getClass().getSimpleName());
    }
    
    @Override
    public void onEntityDeleted(Object entity) {
        System.out.println("LOG: Entity deleted - " + entity.getClass().getSimpleName());
    }
}

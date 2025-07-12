package model.observer;

/**
 * Observer interface untuk event handling
 * Implementasi Observer Pattern
 */
public interface EntityObserver {
    void onEntityCreated(Object entity);
    void onEntityUpdated(Object entity);
    void onEntityDeleted(Object entity);
}

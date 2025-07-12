package model.observer;

/**
 * Subject interface untuk Observable pattern
 */
public interface EntitySubject {
    void addObserver(EntityObserver observer);
    void removeObserver(EntityObserver observer);
    void notifyObservers(String event, Object entity);
}

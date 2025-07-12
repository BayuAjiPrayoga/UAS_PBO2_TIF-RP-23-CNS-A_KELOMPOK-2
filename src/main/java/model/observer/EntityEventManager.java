package model.observer;

import java.util.ArrayList;
import java.util.List;

/**
 * Concrete implementation of Subject
 */
public class EntityEventManager implements EntitySubject {
    private List<EntityObserver> observers = new ArrayList<>();
    
    @Override
    public void addObserver(EntityObserver observer) {
        observers.add(observer);
    }
    
    @Override
    public void removeObserver(EntityObserver observer) {
        observers.remove(observer);
    }
    
    @Override
    public void notifyObservers(String event, Object entity) {
        for (EntityObserver observer : observers) {
            switch (event) {
                case "CREATE":
                    observer.onEntityCreated(entity);
                    break;
                case "UPDATE":
                    observer.onEntityUpdated(entity);
                    break;
                case "DELETE":
                    observer.onEntityDeleted(entity);
                    break;
            }
        }
    }
}

package model.interfaces;

import java.util.List;

/**
 * Interface untuk entity yang dapat divalidasi
 * Implementasi prinsip Polymorphism
 */
public interface Validatable {
    boolean isValid();
    List<String> getValidationErrors();
}

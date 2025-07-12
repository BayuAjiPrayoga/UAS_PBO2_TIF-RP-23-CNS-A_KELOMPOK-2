package controller;

import model.BaseEntity;
import model.interfaces.Validatable;
import utils.AlertUtils;
import java.util.List;

/**
 * Abstract base controller untuk semua controllers
 * Implementasi prinsip Inheritance dan Template Method Pattern
 */
public abstract class BaseController<T extends BaseEntity> {
    
    // Template method pattern - algoritma umum untuk CRUD operations
    public final boolean saveEntity(T entity) {
        if (!validateEntity(entity)) {
            return false;
        }
        
        // Use entity's save method which includes observer notifications
        boolean result = entity.save();
        if (result) {
            showSuccessMessage("Data berhasil disimpan!");
        } else {
            showErrorMessage("Gagal menyimpan data!");
        }
        return result;
    }

    public final boolean updateEntity(T entity) {
        if (!validateEntity(entity)) {
            return false;
        }
        
        boolean result = entity.save(); // Uses entity's save method with observer notifications
        if (!result) {
            showErrorMessage("Gagal mengupdate data!");
        }
        // No success message popup for updates
        return result;
    }

    public final boolean deleteEntity(T entity) {
        boolean result = entity.delete(); // Uses entity's delete method with observer notifications
        if (result) {
            showSuccessMessage("Data berhasil dihapus!");
        } else {
            showErrorMessage("Gagal menghapus data!");
        }
        return result;
    }

    // Abstract methods yang harus diimplementasi oleh subclass (Polymorphism)
    protected abstract boolean performSave(T entity);
    protected abstract boolean performUpdate(T entity);
    protected abstract boolean performDelete(int id);
    protected abstract List<T> performGetAll();
    protected abstract T performGetById(int id);

    // Common validation method
    protected boolean validateEntity(T entity) {
        if (entity == null) {
            showErrorMessage("Entity tidak boleh null!");
            return false;
        }

        if (!entity.isValid()) {
            if (entity instanceof Validatable) {
                List<String> errors = ((Validatable) entity).getValidationErrors();
                showValidationErrors(errors);
            } else {
                showErrorMessage("Data tidak valid!");
            }
            return false;
        }

        return true;
    }

    // Common utility methods
    protected void showSuccessMessage(String message) {
        AlertUtils.showInfo(message);
    }

    protected void showErrorMessage(String message) {
        AlertUtils.showError(message);
    }

    protected void showWarningMessage(String message) {
        AlertUtils.showWarning(message);
    }

    protected void showValidationErrors(List<String> errors) {
        StringBuilder errorMessage = new StringBuilder("Validasi gagal:\n");
        for (String error : errors) {
            errorMessage.append("- ").append(error).append("\n");
        }
        showErrorMessage(errorMessage.toString());
    }

    // Public interface methods that use template methods
    public final List<T> getAllEntities() {
        try {
            return performGetAll();
        } catch (Exception e) {
            showErrorMessage("Gagal mengambil data: " + e.getMessage());
            return List.of(); // Return empty list
        }
    }

    public final T getEntityById(int id) {
        try {
            return performGetById(id);
        } catch (Exception e) {
            showErrorMessage("Gagal mengambil data dengan ID " + id + ": " + e.getMessage());
            return null;
        }
    }

    // Hook method - dapat di-override oleh subclass jika diperlukan
    protected void beforeSave(T entity) {
        // Default implementation - do nothing
    }

    protected void afterSave(T entity) {
        // Default implementation - do nothing
    }

    protected void beforeUpdate(T entity) {
        // Default implementation - do nothing
    }

    protected void afterUpdate(T entity) {
        // Default implementation - do nothing
    }

    protected void beforeDelete(int id) {
        // Default implementation - do nothing
    }

    protected void afterDelete(int id) {
        // Default implementation - do nothing
    }
}

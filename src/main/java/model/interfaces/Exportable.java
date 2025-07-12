package model.interfaces;

/**
 * Interface untuk entity yang dapat diexport
 * Implementasi prinsip Polymorphism
 */
public interface Exportable {
    void exportToCSV(String filePath);
    void exportToPDF(String filePath);
    String getExportData();
}

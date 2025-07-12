package model;

import java.time.LocalDateTime;

public class Transaksi {
    private int id;
    private int idBarang;
    private int idKasir;
    private int jumlah;
    private double totalHarga;
    private LocalDateTime waktu;

    public Transaksi() {}

    public Transaksi(int id, int idBarang, int idKasir, int jumlah, double totalHarga, LocalDateTime waktu) {
        this.id = id;
        this.idBarang = idBarang;
        this.idKasir = idKasir;
        this.jumlah = jumlah;
        this.totalHarga = totalHarga;
        this.waktu = waktu;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdBarang() {
        return idBarang;
    }

    public void setIdBarang(int idBarang) {
        this.idBarang = idBarang;
    }

    public int getIdKasir() {
        return idKasir;
    }

    public void setIdKasir(int idKasir) {
        this.idKasir = idKasir;
    }

    public int getJumlah() {
        return jumlah;
    }

    public void setJumlah(int jumlah) {
        this.jumlah = jumlah;
    }

    public double getTotalHarga() {
        return totalHarga;
    }

    public void setTotalHarga(double totalHarga) {
        this.totalHarga = totalHarga;
    }

    public LocalDateTime getWaktu() {
        return waktu;
    }

    public void setWaktu(LocalDateTime waktu) {
        this.waktu = waktu;
    }
}

package model;

import java.time.LocalDateTime;

public class TransaksiRiwayat {
    private String namaBarang;
    private String namaKasir;
    private int jumlah;
    private double totalHarga;
    private LocalDateTime waktu;

    public TransaksiRiwayat(String namaBarang, String namaKasir, int jumlah, double totalHarga, LocalDateTime waktu) {
        this.namaBarang = namaBarang;
        this.namaKasir = namaKasir;
        this.jumlah = jumlah;
        this.totalHarga = totalHarga;
        this.waktu = waktu;
    }

    public String getNamaBarang() { return namaBarang; }
    public String getNamaKasir() { return namaKasir; }
    public int getJumlah() { return jumlah; }
    public double getTotalHarga() { return totalHarga; }
    public LocalDateTime getWaktu() { return waktu; }
}

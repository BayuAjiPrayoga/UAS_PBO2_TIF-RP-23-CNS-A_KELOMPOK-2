package utils;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;

/**
 * Utility class untuk memainkan sound effects dalam aplikasi
 */
public class SoundUtils {
    
    // Enum untuk jenis suara yang tersedia
    public enum SoundType {
        PAYMENT_SUCCESS("payment_success.wav"),
        LOGIN_SUCCESS("login_dashboard.wav"),
        ERROR("error.wav"),
        NOTIFICATION("notification.wav"),
        BEEP("beep.wav");
        
        private final String fileName;
        
        SoundType(String fileName) {
            this.fileName = fileName;
        }
        
        public String getFileName() {
            return fileName;
        }
    }
    
    /**
     * Memainkan suara berdasarkan type yang ditentukan
     * @param soundType Jenis suara yang akan dimainkan
     */
    public static void playSound(SoundType soundType) {
        try {
            // Coba load dari resources folder
            URL soundURL = SoundUtils.class.getResource("/sounds/" + soundType.getFileName());
            
            if (soundURL == null) {
                // Jika file tidak ditemukan, buat suara beep default
                playDefaultBeep();
                return;
            }
            
            // Play sound file
            playSoundFile(soundURL);
            
        } catch (Exception e) {
            System.err.println("Error playing sound: " + e.getMessage());
            // Fallback ke system beep
            playDefaultBeep();
        }
    }
    
    /**
     * Memainkan file suara dari URL
     * @param soundURL URL file suara
     */
    private static void playSoundFile(URL soundURL) {
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundURL);
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            
            // Play sound
            clip.start();
            
            // Optional: Add listener untuk cleanup setelah selesai
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    clip.close();
                }
            });
            
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Error playing sound file: " + e.getMessage());
            playDefaultBeep();
        }
    }
    
    /**
     * Memainkan suara beep default sistem
     */
    public static void playDefaultBeep() {
        try {
            // Generate simple beep sound
            java.awt.Toolkit.getDefaultToolkit().beep();
        } catch (Exception e) {
            System.err.println("Cannot play system beep: " + e.getMessage());
        }
    }
    
    /**
     * Memainkan suara sukses pembayaran
     * Suara yang menyenangkan untuk feedback positif
     */
    public static void playPaymentSuccess() {
        playSound(SoundType.PAYMENT_SUCCESS);
    }
    
    /**
     * Memainkan suara login dashboard berhasil
     * Suara welcome saat kasir berhasil login
     */
    public static void playLoginSuccess() {
        playSound(SoundType.LOGIN_SUCCESS);
    }
    
    /**
     * Memainkan suara error
     * Suara peringatan untuk error atau kesalahan
     */
    public static void playError() {
        playSound(SoundType.ERROR);
    }
    
    /**
     * Memainkan suara notifikasi umum
     */
    public static void playNotification() {
        playSound(SoundType.NOTIFICATION);
    }
    
    /**
     * Generate suara beep programmatically
     * Sebagai fallback jika file suara tidak tersedia
     */
    public static void generateSuccessBeep() {
        try {
            // Create a simple success sound pattern
            Thread soundThread = new Thread(() -> {
                try {
                    // Success pattern: 3 short beeps with increasing pitch
                    for (int i = 0; i < 3; i++) {
                        java.awt.Toolkit.getDefaultToolkit().beep();
                        Thread.sleep(100);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            soundThread.start();
        } catch (Exception e) {
            System.err.println("Error generating success beep: " + e.getMessage());
        }
    }
    
    /**
     * Check if audio system is available
     * @return true jika audio system tersedia
     */
    public static boolean isAudioSystemAvailable() {
        try {
            return AudioSystem.getMixerInfo().length > 0;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Check apakah file audio tersedia
     * @param soundType Jenis suara yang akan dicek
     * @return true jika file audio tersedia
     */
    public static boolean isAudioFileAvailable(SoundType soundType) {
        try {
            URL soundURL = SoundUtils.class.getResource("/sounds/" + soundType.getFileName());
            return soundURL != null;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Print status semua file audio
     */
    public static void printAudioStatus() {
        System.out.println("🎵 ===== AUDIO SYSTEM STATUS =====");
        System.out.println("Audio System Available: " + isAudioSystemAvailable());
        System.out.println();
        
        for (SoundType type : SoundType.values()) {
            boolean available = isAudioFileAvailable(type);
            String status = available ? "✅ FOUND" : "❌ MISSING";
            System.out.println(String.format("%-20s: %s (%s)", 
                type.name(), status, type.getFileName()));
        }
        
        System.out.println();
        System.out.println("📁 Place your WAV files in: src/main/resources/sounds/");
        System.out.println("🔧 See AUDIO_PLACEMENT_GUIDE.md for details");
        System.out.println("=================================");
    }
}

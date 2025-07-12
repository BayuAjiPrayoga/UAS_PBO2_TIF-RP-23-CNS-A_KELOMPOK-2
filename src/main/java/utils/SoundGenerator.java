package utils;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.File;

/**
 * Generator untuk membuat file suara sederhana secara programmatic
 */
public class SoundGenerator {
    
    /**
     * Generate file suara success untuk pembayaran
     */
    public static void generateSoundFiles() {
        try {
            File soundsDir = new File("src/main/resources/sounds");
            if (!soundsDir.exists()) {
                soundsDir.mkdirs();
            }
            
            // Generate success sound (ascending tone)
            generateSuccessSound();
            
            System.out.println("Sound files generated successfully!");
            
        } catch (Exception e) {
            System.err.println("Error generating sound files: " + e.getMessage());
        }
    }
    
    /**
     * Generate success sound dengan pola nada yang menyenangkan
     */
    private static void generateSuccessSound() {
        try {
            // Parameters for sound generation
            float sampleRate = 44100;
            int sampleSizeInBits = 16;
            int channels = 1;
            boolean signed = true;
            boolean bigEndian = false;
            
            AudioFormat audioFormat = new AudioFormat(
                sampleRate, sampleSizeInBits, channels, signed, bigEndian);
            
            // Generate success tone sequence
            generateSuccessTone(audioFormat);
            
            System.out.println("Success sound generated!");
            
        } catch (Exception e) {
            System.err.println("Error generating success sound: " + e.getMessage());
        }
    }
    
    /**
     * Generate byte array untuk success tone
     */
    private static byte[] generateSuccessTone(AudioFormat audioFormat) {
        float sampleRate = audioFormat.getSampleRate();
        int duration = 1; // 1 second
        int numSamples = (int) (duration * sampleRate);
        byte[] audioData = new byte[numSamples * 2]; // 16-bit samples
        
        // Generate pleasant success sound (major chord progression)
        double[] frequencies = {523.25, 659.25, 783.99}; // C5, E5, G5 (C major chord)
        
        for (int i = 0; i < numSamples; i++) {
            double time = i / sampleRate;
            double amplitude = 0.3 * Math.exp(-time * 2); // Fade out
            
            double sample = 0;
            for (double freq : frequencies) {
                sample += Math.sin(2 * Math.PI * freq * time) / frequencies.length;
            }
            sample *= amplitude;
            
            // Convert to 16-bit sample
            short sample16 = (short) (sample * Short.MAX_VALUE);
            audioData[i * 2] = (byte) (sample16 & 0xFF);
            audioData[i * 2 + 1] = (byte) ((sample16 >> 8) & 0xFF);
        }
        
        return audioData;
    }
    
    /**
     * Play generated success sound directly
     */
    public static void playGeneratedSuccessSound() {
        try {
            float sampleRate = 44100;
            int sampleSizeInBits = 16;
            int channels = 1;
            boolean signed = true;
            boolean bigEndian = false;
            
            AudioFormat audioFormat = new AudioFormat(
                sampleRate, sampleSizeInBits, channels, signed, bigEndian);
            
            byte[] audioData = generateSuccessTone(audioFormat);
            
            ByteArrayInputStream bais = new ByteArrayInputStream(audioData);
            AudioInputStream audioInputStream = new AudioInputStream(bais, audioFormat, audioData.length / audioFormat.getFrameSize());
            
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
            
            // Cleanup after playing
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    clip.close();
                }
            });
            
        } catch (Exception e) {
            System.err.println("Error playing generated sound: " + e.getMessage());
            // Fallback to system beep
            java.awt.Toolkit.getDefaultToolkit().beep();
        }
    }
}

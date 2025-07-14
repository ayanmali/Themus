package com.delphi.delphi.services;

import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EncryptionService {

    private final SecretKey secretKey;
    // private final String salt;
    // private static final int KEY_SIZE = 256; // 256 bits
    private static final int DATA_LENGTH = 128; // 128 bits
    private static final String ALGORITHM = "AES/GCM/NoPadding"; // AES/GCM/NoPadding

    public EncryptionService(@Value("${encryption.secret-key}") String secretKey) {
        this.secretKey = new SecretKeySpec(secretKey.getBytes(), "AES");
        //this.salt = salt;
    }

    // Encrypt a string (github PAT) using AES/GCM
    public String encrypt(String data) throws Exception {
        // Get Cipher Instance
        Cipher cipher = Cipher.getInstance(ALGORITHM);

        // Create IV (Initialization Vector)
        byte[] iv = new byte[12];
        SecureRandom.getInstanceStrong().nextBytes(iv);

        // Create GCMParameterSpec
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(DATA_LENGTH, iv);

        // Initialize Cipher for ENCRYPT_MODE
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmParameterSpec);

        // Perform Encryption
        byte[] cipherText = cipher.doFinal(data.getBytes());

        // Concatenate IV and ciphertext
        byte[] encryptedData = new byte[iv.length + cipherText.length];
        System.arraycopy(iv, 0, encryptedData, 0, iv.length);
        System.arraycopy(cipherText, 0, encryptedData, iv.length, cipherText.length);

        return Base64.getEncoder().encodeToString(encryptedData);
    }

    // Decrypt a string (github PAT) using AES/GCM
    public String decrypt(String encryptedData) throws Exception {
        // Get Cipher Instance
        Cipher cipher = Cipher.getInstance(ALGORITHM);

        // Decode the encrypted data
        byte[] decoded = Base64.getDecoder().decode(encryptedData);

        // Extract IV
        byte[] iv = new byte[12];
        System.arraycopy(decoded, 0, iv, 0, iv.length);

        // Extract ciphertext
        byte[] cipherText = new byte[decoded.length - iv.length];
        System.arraycopy(decoded, iv.length, cipherText, 0, cipherText.length);

        // Create GCMParameterSpec
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(DATA_LENGTH, iv);

        // Initialize Cipher for DECRYPT_MODE
        cipher.init(Cipher.DECRYPT_MODE,  secretKey, gcmParameterSpec);

        // Perform Decryption
        return new String(cipher.doFinal(cipherText));
    }
    
}
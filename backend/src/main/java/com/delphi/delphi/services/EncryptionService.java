package com.delphi.delphi.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
// TODO: implement encryption service
public class EncryptionService {

    private final String secretKey;
    private final String salt;

    public EncryptionService(@Value("${encryption.secret-key}") String secretKey, @Value("${encryption.salt}") String salt) {
        this.secretKey = secretKey;
        this.salt = salt;
    }

    public String encrypt(String value) {
        return value;
    }

    public String decrypt(String value) {
        return value;
    }
    
}

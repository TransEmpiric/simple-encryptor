package com.transempiric.simpleEncryptor.property;

import com.transempiric.simpleEncryptor.SimpleEncryptorException;
import org.springframework.security.crypto.encrypt.TextEncryptor;

/**
 * @author Ulises Bocchio
 * @author SlowBurner
 */
public class SimpleEncryptorPropertyResolver {
    private TextEncryptor encryptor;
    private SimpleEncryptorPropertyDetector detector;

    public SimpleEncryptorPropertyResolver(TextEncryptor encryptor) {
        this.encryptor = encryptor;
        this.detector = new SimpleEncryptorPropertyDetector();
    }

    public String resolvePropertyValue(String value) {
        String actualValue = value;
        if (detector.isEncrypted(value)) {
            try {
                actualValue = encryptor.decrypt(detector.unwrapEncryptedValue(value.trim()));
            } catch (Exception e) {
                throw new SimpleEncryptorException("Decryption of Properties failed.", e);
            }
        }
        return actualValue;
    }
}
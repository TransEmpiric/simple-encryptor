package com.transempiric.simpleEncryptor;

/**
 * @author SlowBurner
 */
public class SimpleEncryptorException extends RuntimeException {
    public SimpleEncryptorException(String msg) {
        super(msg);
    }

    public SimpleEncryptorException(Exception innerException) {
        super(innerException.getMessage(), innerException);
    }

    public SimpleEncryptorException(String msg, Exception innerException) {
        super(msg, innerException);
    }
}
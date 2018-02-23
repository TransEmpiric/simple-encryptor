package com.transempiric.simpleEncryptor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.rsa.crypto.RsaSecretEncryptor;
import org.springframework.util.StreamUtils;

import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * *******************************GENERATE PEM files with openssl************************************
 * Generate a RSA PRIVATE KEY:
 * openssl genrsa -out local_enc_private_key.pem 512
 * **************************************************************************************************
 * ***Generate a RSA PUBLIC KEY from the RSA PRIVATE KEY:
 * ***openssl rsa -in local_enc_private_key.pem -outform PEM -pubout -out local_enc_public_key.pem
 * **************************************************************************************************
 * ***Needs Java Cryptography
 * ***Extension (JCE) Unlimited Strength Jurisdiction Policy Files in this jvm.
 * **************************************************************************************************
 */

/**
 * @author SlowBurner
 */
public class SimpleEncryptorFactoryBean extends AbstractFactoryBean {
    public static final String SIMPLE_ENCRYPTOR_SECRET_PROPERTY_NAME = "simple.encryptor.secret";
    public static final String SIMPLE_ENCRYPTOR_SALT_PROPERTY_NAME = "simple.encryptor.salt";
    private static final String RSA_PRIVATE_PATTERN = "RSA PRIVATE KEY";
    private static final String RSA_PUBLIC_PATTERN = "PUBLIC KEY";
    private static final String RSA_ANTI_PATTERN = "ssh-rsa";
    private static final String SIMPLE_ENCRYPTOR_DEFAULT_SALT = "!I am not a beautiful and unique snowflake!";
    private static Log logger = LogFactory.getLog(SimpleEncryptorFactoryBean.class);
    private final DefaultResourceLoader defaultResourceLoader = new DefaultResourceLoader();

    private TextEncryptor encryptor;
    private boolean deleteKeyFiles;
    private boolean clearKeyFileContents;

    private Map<String, Resource> keyStore = new HashMap<String, Resource>();

    public SimpleEncryptorFactoryBean() {
    }

    public SimpleEncryptorFactoryBean rsaEncryptor(final String path) {
        final String rsaPublicKey = getRawAsciiKey(path, RSA_PUBLIC_PATTERN);
        return createRsaEncryptor(rsaPublicKey, RSA_PUBLIC_PATTERN);
    }

    public SimpleEncryptorFactoryBean rsaDecryptor(final String path) {
        final String rsaPrivateKey = getRawAsciiKey(path, RSA_PRIVATE_PATTERN);
        return createRsaEncryptor(rsaPrivateKey, RSA_PRIVATE_PATTERN);
    }

    public SimpleEncryptorFactoryBean hexEncodingTextEncryptor(String secret, String salt) {
        if (salt == null) {
            salt = SIMPLE_ENCRYPTOR_DEFAULT_SALT;
            logger.warn("Could not find " + SIMPLE_ENCRYPTOR_SALT_PROPERTY_NAME
                    + " System property. Using SIMPLE_ENCRYPTOR_DEFAULT_SALT.");
        }

        if (secret == null) {
            throw new SimpleEncryptorException("Could not find " + SIMPLE_ENCRYPTOR_SECRET_PROPERTY_NAME
                    + " System property. Please set the property.");
        }

        this.encryptor = Encryptors.text(secret, salt);
        return this;
    }

    private SimpleEncryptorFactoryBean createRsaEncryptor(final String data, String pattern) {
        if (data.contains(RSA_ANTI_PATTERN)) {
            throw new SimpleEncryptorException("Invalid key format for " + pattern + ". "
                    + "The key contains " + RSA_ANTI_PATTERN + "."
                    + "Please use a PEM format."
            );
        }

        if (!data.contains(pattern)) {
            throw new SimpleEncryptorException("Invalid key format for " + pattern + ". "
                    + "The key fie does not contain " + pattern + "."
            );
        }

        try {
            this.encryptor = new RsaSecretEncryptor(data.replaceAll("\\n", "").replaceAll("\\r", ""));
        } catch (IllegalArgumentException e) {
            throw new SimpleEncryptorException(e);
        }

        return this;
    }

    // e.g. classpath:com/app/private_key.pem or file:/tmp/private_key.pem
    private String getRawAsciiKey(final String path, String pattern) {
        final String key;
        try {

            Resource keyResource = defaultResourceLoader.getResource(path);
            key = StreamUtils.copyToString((keyResource).getInputStream(), Charset.forName("ASCII"));
            keyStore.put(path, keyResource);

        } catch (Exception e) {
            throw new SimpleEncryptorException("Could not get " + pattern + " from file (" + path + ").", e);
        }

        return key;
    }

    private void doClearKeyFileContents() {
        logger.debug("Clearing key file contents.");
        Iterator it = keyStore.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Resource> entry = (Map.Entry) it.next();
            Resource resource = entry.getValue();
            try {
                new PrintWriter(resource.getFile()).close();
            } catch (Exception e) {
                throw new SimpleEncryptorException("Could not clear key file contents (" + entry.getKey() + ").", e);
            } finally {
                it.remove();
            }
        }
    }

    private void doDeleteKeyFilesl() {
        logger.debug("Deleting key files.");
        Iterator it = keyStore.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Resource> entry = (Map.Entry) it.next();
            Resource resource = entry.getValue();
            try {
                if (resource.getFile().delete()) {
                    logger.info("Deleted key file (" + entry.getKey() + ").");
                }
            } catch (Exception e) {
                throw new SimpleEncryptorException("Could not delete key file (" + entry.getKey() + ").", e);
            }
        }
    }

    public boolean isDeleteKeyFiles() {
        return deleteKeyFiles;
    }

    public SimpleEncryptorFactoryBean deleteKeyFiles(boolean deleteKeyFiles) {
        this.deleteKeyFiles = deleteKeyFiles;
        return this;
    }

    public boolean isClearKeyFileContents() {
        return clearKeyFileContents;
    }

    public SimpleEncryptorFactoryBean clearKeyFileContents(boolean clearKeyFileContents) {
        this.clearKeyFileContents = clearKeyFileContents;
        return this;
    }

    @Override
    public Class<?> getObjectType() {
        return TextEncryptor.class;
    }

    @Override
    public TextEncryptor createInstance() throws Exception {
        if (this.clearKeyFileContents) doClearKeyFileContents();
        if (this.deleteKeyFiles) doDeleteKeyFilesl();
        return this.encryptor;
    }
}


package com.transempiric.simpleEncryptor.property.wrapper;

import com.transempiric.simpleEncryptor.property.SimpleEncryptorPropertyResolver;
import com.transempiric.simpleEncryptor.property.SimpleEncryptorPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.util.Assert;

/**
 * <p>Wrapper for {@link PropertySource} instances that simply delegates the {@link #getProperty} method
 * to the {@link PropertySource} delegate instance to retrieve properties, while checking if the resulting
 * property is encrypted or not using the convention of surrounding encrypted values with "ENC()".</p>
 * <p>When an encrypted property is detected, it is decrypted using the provided {@link TextEncryptor}</p>
 *
 * @author Ulises Bocchio
 * @author SlowBurner
 */
public class SimpleEncryptorPropertySourceWrapper<T> extends PropertySource<T> implements SimpleEncryptorPropertySource<T> {
    private final PropertySource<T> delegate;
    SimpleEncryptorPropertyResolver resolver;

    public SimpleEncryptorPropertySourceWrapper(PropertySource<T> delegate, SimpleEncryptorPropertyResolver resolver) {
        super(delegate.getName(), delegate.getSource());
        Assert.notNull(delegate, "PropertySource delegate cannot be null");
        Assert.notNull(resolver, "EncryptablePropertyResolver cannot be null");
        this.delegate = delegate;
        this.resolver = resolver;
    }

    @Override
    public Object getProperty(String name) {
        return getProperty(resolver, delegate, name);
    }

    @Override
    public PropertySource<T> getDelegate() {
        return delegate;
    }
}
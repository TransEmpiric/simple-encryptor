package com.transempiric.simpleEncryptor.property.wrapper;

import com.transempiric.simpleEncryptor.property.SimpleEncryptorPropertyResolver;
import com.transempiric.simpleEncryptor.property.SimpleEncryptorPropertySource;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.util.Assert;

/**
 * @author Ulises Bocchio
 * @author SlowBurner
 */
public class SimpleEncryptorEnumerablePropertySourceWrapper<T> extends EnumerablePropertySource<T> implements SimpleEncryptorPropertySource<T> {
    private final EnumerablePropertySource<T> delegate;
    private final SimpleEncryptorPropertyResolver resolver;

    public SimpleEncryptorEnumerablePropertySourceWrapper(EnumerablePropertySource<T> delegate, SimpleEncryptorPropertyResolver resolver) {
        super(delegate.getName(), delegate.getSource());
        Assert.notNull(delegate, "PropertySource delegate cannot be null");
        Assert.notNull(resolver, "SimpleEncryptorPropertyResolver cannot be null");
        this.delegate = delegate;
        this.resolver = resolver;
    }

    @Override
    public Object getProperty(String name) {
        return getProperty(resolver, delegate, name);
    }

    @Override
    public String[] getPropertyNames() {
        return delegate.getPropertyNames();
    }

    @Override
    public PropertySource<T> getDelegate() {
        return delegate;
    }
}
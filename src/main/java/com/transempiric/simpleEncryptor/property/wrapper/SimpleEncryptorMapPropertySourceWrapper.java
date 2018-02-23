package com.transempiric.simpleEncryptor.property.wrapper;

import com.transempiric.simpleEncryptor.property.SimpleEncryptorPropertyResolver;
import com.transempiric.simpleEncryptor.property.SimpleEncryptorPropertySource;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.util.Assert;

import java.util.Map;

/**
 * @author Ulises Bocchio
 * @author SlowBurner
 */
public class SimpleEncryptorMapPropertySourceWrapper extends MapPropertySource implements SimpleEncryptorPropertySource<Map<String, Object>> {
    SimpleEncryptorPropertyResolver resolver;
    private MapPropertySource delegate;

    public SimpleEncryptorMapPropertySourceWrapper(MapPropertySource delegate, SimpleEncryptorPropertyResolver resolver) {
        super(delegate.getName(), delegate.getSource());
        Assert.notNull(delegate, "PropertySource delegate cannot be null");
        Assert.notNull(resolver, "SimpleEncryptorPropertyResolver cannot be null");
        this.resolver = resolver;
        this.delegate = delegate;
    }

    @Override
    public Object getProperty(String name) {
        return getProperty(resolver, delegate, name);
    }

    @Override
    public PropertySource<Map<String, Object>> getDelegate() {
        return delegate;
    }

    @Override
    public boolean containsProperty(String name) {
        return delegate.containsProperty(name);
    }

    @Override
    public String[] getPropertyNames() {
        return delegate.getPropertyNames();
    }
}
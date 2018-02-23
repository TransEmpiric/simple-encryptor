package com.transempiric.simpleEncryptor.property;

import org.springframework.core.env.PropertySource;

/**
 * @author Ulises Bocchio
 * @author SlowBurner
 */
public interface SimpleEncryptorPropertySource<T> {

    PropertySource<T> getDelegate();

    default Object getProperty(SimpleEncryptorPropertyResolver resolver, PropertySource<T> source, String name) {
        Object value = source.getProperty(name);
        if (value instanceof String) {
            String stringValue = String.valueOf(value);
            return resolver.resolvePropertyValue(stringValue);
        }
        return value;
    }
}
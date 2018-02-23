package com.transempiric.simpleEncryptor.property;

import com.transempiric.simpleEncryptor.property.wrapper.SimpleEncryptorEnumerablePropertySourceWrapper;
import com.transempiric.simpleEncryptor.property.wrapper.SimpleEncryptorMapPropertySourceWrapper;
import com.transempiric.simpleEncryptor.property.wrapper.SimpleEncryptorPropertySourceWrapper;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.core.env.*;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;

/**
 * @author Ulises Bocchio
 * @author SlowBurner
 */
public class SimpleEncryptorEnvironment extends StandardEnvironment implements ConfigurableEnvironment {
    private final SimpleEncryptorPropertyResolver resolver;
    private final SimpleEncryptorInterceptionMode interceptionMode;
    private MutablePropertySources encryptablePropertySources;
    private MutablePropertySources originalPropertySources;

    public SimpleEncryptorEnvironment(SimpleEncryptorInterceptionMode interceptionMode, SimpleEncryptorPropertyResolver resolver) {
        this.interceptionMode = interceptionMode;
        this.resolver = resolver;
        actuallyCustomizePropertySources();
    }

    public static void convertPropertySources(SimpleEncryptorInterceptionMode interceptionMode, SimpleEncryptorPropertyResolver propertyResolver, MutablePropertySources propSources) {
        StreamSupport.stream(propSources.spliterator(), false)
                .filter(ps -> !(ps instanceof SimpleEncryptorPropertySource))
                .map(ps -> makeEncryptable(interceptionMode, propertyResolver, ps))
                .collect(toList())
                .forEach(ps -> propSources.replace(ps.getName(), ps));
    }

    @SuppressWarnings("unchecked")
    public static <T> PropertySource<T> makeEncryptable(SimpleEncryptorInterceptionMode interceptionMode, SimpleEncryptorPropertyResolver propertyResolver, PropertySource<T> propertySource) {
        if (propertySource instanceof SimpleEncryptorPropertySource) {
            return propertySource;
        }
        PropertySource<T> encryptablePropertySource = convertPropertySource(interceptionMode, propertyResolver, propertySource);
        //log.info("Converting PropertySource {} [{}] to {}", propertySource.getName(), propertySource.getClass().getName(), AopUtils.isAopProxy(encryptablePropertySource) ? "AOP Proxy" : encryptablePropertySource.getClass().getSimpleName());
        return encryptablePropertySource;
    }

    private static <T> PropertySource<T> convertPropertySource(SimpleEncryptorInterceptionMode interceptionMode, SimpleEncryptorPropertyResolver propertyResolver, PropertySource<T> propertySource) {
        return interceptionMode == SimpleEncryptorInterceptionMode.PROXY
                ? proxyPropertySource(propertySource, propertyResolver) : instantiatePropertySource(propertySource, propertyResolver);
    }

    public static MutablePropertySources proxyPropertySources(SimpleEncryptorInterceptionMode interceptionMode, SimpleEncryptorPropertyResolver propertyResolver, MutablePropertySources propertySources) {
        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setTarget(MutablePropertySources.class);
        proxyFactory.setProxyTargetClass(true);
        proxyFactory.addInterface(PropertySources.class);
        proxyFactory.setTarget(propertySources);
        proxyFactory.addAdvice(new SimpleEncryptorMutablePropertySourcesInterceptor(interceptionMode, propertyResolver));
        return (MutablePropertySources) proxyFactory.getProxy();
    }

    @SuppressWarnings("unchecked")
    public static <T> PropertySource<T> proxyPropertySource(PropertySource<T> propertySource, SimpleEncryptorPropertyResolver resolver) {
        if (CommandLinePropertySource.class.isAssignableFrom(propertySource.getClass())) {
            return instantiatePropertySource(propertySource, resolver);
        }
        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setTargetClass(propertySource.getClass());
        proxyFactory.setProxyTargetClass(true);
        proxyFactory.addInterface(SimpleEncryptorPropertySource.class);
        proxyFactory.setTarget(propertySource);
        proxyFactory.addAdvice(new SimpleEncryptorPropertySourceMethodInterceptor<>(propertySource, resolver));
        return (PropertySource<T>) proxyFactory.getProxy();
    }

    @SuppressWarnings("unchecked")
    public static <T> PropertySource<T> instantiatePropertySource(PropertySource<T> propertySource, SimpleEncryptorPropertyResolver resolver) {
        PropertySource<T> encryptablePropertySource;
        if (needsProxyAnyway(propertySource)) {
            encryptablePropertySource = proxyPropertySource(propertySource, resolver);
        } else if (propertySource instanceof MapPropertySource) {
            encryptablePropertySource = (PropertySource<T>) new SimpleEncryptorMapPropertySourceWrapper((MapPropertySource) propertySource, resolver);
        } else if (propertySource instanceof EnumerablePropertySource) {
            encryptablePropertySource = new SimpleEncryptorEnumerablePropertySourceWrapper<>((EnumerablePropertySource) propertySource, resolver);
        } else {
            encryptablePropertySource = new SimpleEncryptorPropertySourceWrapper<>(propertySource, resolver);
        }
        return encryptablePropertySource;
    }

    @SuppressWarnings("unchecked")
    private static boolean needsProxyAnyway(PropertySource<?> ps) {
        return needsProxyAnyway((Class<? extends PropertySource<?>>) ps.getClass());
    }

    private static boolean needsProxyAnyway(Class<? extends PropertySource<?>> psClass) {
        boolean needsProxy = needsProxyAnyway(psClass.getName());
        if (needsProxy) System.out.println("needsProxyAnyways: " + psClass.getName() + "...");
        return needsProxy;
    }

    private static boolean needsProxyAnyway(String className) {
        return Stream.of(
                "org.springframework.boot.context.config.ConfigFileApplicationListener$ConfigurationPropertySources",
                "org.springframework.boot.context.properties.source.ConfigurationPropertySourcesPropertySource"
        ).anyMatch(className::equals);
    }

    @Override
    protected void customizePropertySources(MutablePropertySources propertySources) {
        super.customizePropertySources(propertySources);
        this.originalPropertySources = propertySources;
    }

    protected void actuallyCustomizePropertySources() {
        convertPropertySources(interceptionMode, resolver, originalPropertySources);
        encryptablePropertySources = proxyPropertySources(interceptionMode, resolver, originalPropertySources);
    }

    @Override
    public MutablePropertySources getPropertySources() {
        return encryptablePropertySources;
    }
}
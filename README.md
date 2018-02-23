# simple-encryptor
Spring Framework encryption extension.
SimpleEncryptor supports property encryption via TextEncryptor, with optional use of [spring-security-rsa](https://github.com/dsyer/spring-security-rsa) RSA (PUBLIC and PRIVATE keys).
RSA (PUBLIC and PRIVATE keys) can be deleted or cleared after TextEncryptor instantiation using the SimpleEncryptorFactoryBean.

SimpleEncryptor provides encryption support for property sources in Spring Boot Applications and plain old Spring.<br/>

## How to get use.
1.  Add the simple-encryptor dependency to your project (Maven Central Coming soon):

2.  Spring Boot property example:
	```java
    @SpringBootApplication
    public class WebTemplateApplication {
        public static void main(String[] args) throws Exception {
    
            TextEncryptor rsaDecryptor = new SimpleEncryptorFactoryBean()
                    .rsaDecryptor("classPath:local_enc_private_key.pem")
                    .createInstance();
            
            //TODO: Clean up and make use of the SimpleEncryptorFactoryBean.
            SimpleEncryptorPropertyResolver resolver =  new SimpleEncryptorPropertyResolver(rsaDecryptor);
            SimpleEncryptorEnvironment env =  new SimpleEncryptorEnvironment(SimpleEncryptorInterceptionMode.WRAPPER, resolver);
            
            new SpringApplicationBuilder()
                    .environment(env)
                    .sources(WebTemplateApplication.class)
                    .run(args);
    
        }
    }
	```
	
    Encryptable properties will be enabled across the entire Spring Environment (This means any system property, environment property, command line argument, application.properties, yaml properties, and any other custom property sources can contain encrypted properties)

2.  Spring Bean example for encryptors:
	```java
    import com.transempiric.simpleEncryptor.SimpleEncryptorFactoryBean;
    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.Configuration;
    import org.springframework.security.crypto.encrypt.TextEncryptor;
    
    import static com.transempiric.simpleEncryptor.SimpleEncryptorFactoryBean.SIMPLE_ENCRYPTOR_SALT_PROPERTY_NAME;
    import static com.transempiric.simpleEncryptor.SimpleEncryptorFactoryBean.SIMPLE_ENCRYPTOR_SECRET_PROPERTY_NAME;
    
    @Configuration
    public class SimpleEncryptorConfigExample {
    
        @Bean
        public TextEncryptor rsaSimpleEncryptor() throws Exception {
            return new SimpleEncryptorFactoryBean()
                    .rsaEncryptor("classPath:local_enc_public_key.pem")
                    // .clearKeyFileContents(false)
                    // .deleteKeyFiles(false)
                    .createInstance();
        }
    
        @Bean
        public TextEncryptor rsaSimpleDecryptor() throws Exception {
            return new SimpleEncryptorFactoryBean()
                    .rsaDecryptor("classPath:local_enc_private_key.pem")
                    // .clearKeyFileContents(true)
                    // .deleteKeyFiles(true)
                    .createInstance();
        }
    
        
        @Bean
            public TextEncryptor hexEncodingSimpleEncryptor() throws Exception {
                return new SimpleEncryptorFactoryBean()
                                .hexEncodingTextEncryptor(
                                        System.getProperty(SIMPLE_ENCRYPTOR_SECRET_PROPERTY_NAME),
                                        System.getProperty(SIMPLE_ENCRYPTOR_SALT_PROPERTY_NAME)
                                )
                                .createInstance();
            }
    
        @Bean
        public String spaceMonkey(
                TextEncryptor hexEncodingSimpleEncryptor,
                TextEncryptor rsaSimpleEncryptor,
                TextEncryptor rsaSimpleDecryptor
        ) {
            /*
            
            System.out.println("**************** SimpleEncryptorConfigExample Test *************************");
            System.out.println(rsaSimpleEncryptor.encrypt("rupertDurden"));
            System.out.println(rsaSimpleEncryptor.encrypt("rupertDurden"));
    
            System.out.println(rsaSimpleDecryptor.decrypt(rsaSimpleEncryptor.encrypt("rupert")));
            System.out.println(rsaSimpleDecryptor.decrypt(rsaSimpleEncryptor.encrypt("durden")));
    
            System.out.println(hexEncodingSimpleEncryptor.encrypt("rupert"));
            System.out.println(hexEncodingSimpleEncryptor.encrypt("durden"));
    
            System.out.println(hexEncodingSimpleEncryptor.decrypt(hexEncodingSimpleEncryptor.encrypt("rupert")));
            System.out.println(hexEncodingSimpleEncryptor.decrypt(hexEncodingSimpleEncryptor.encrypt("durden")));
    
            System.out.println("**************** SimpleEncryptorConfigExample Test *************************");
    
            */
    
            return "SpaceMonkey";
        }
    }
	```
	
## Major Props to some Spring peeps
[Dave Syer](https://github.com/dsyer/spring-security-rsa) for [spring-security-rsa](https://github.com/dsyer/spring-security-rsa) and [spring-cloud-config](https://github.com/spring-cloud/spring-cloud-config).
[Ulises Bocchio](https://github.com/ulisesbocchio) for [jasypt-spring-boot](https://github.com/ulisesbocchio/jasypt-spring-boot).
	
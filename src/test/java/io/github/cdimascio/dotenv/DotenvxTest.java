package io.github.cdimascio.dotenv;

import io.github.cdimascio.ecies.ECKeyPair;
import io.github.cdimascio.ecies.Ecies;
import jakarta.config.Loader;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

public class DotenvxTest {

    @Test
    public void testGenerateKeyPair() throws Exception {
        ECKeyPair keyPair = Ecies.generateEcKeyPair();
        System.out.println("Public Key: " + keyPair.getPublicHex(true));
        System.out.println("Private Key: " + keyPair.getPrivateHex());
    }

    @Test
    public void testDotenv() {
        Dotenv dotenv = Dotenvx.load();
        final String hello = dotenv.get("HELLO");
        assertThat(hello).isEqualTo("World");
    }

    @Test
    public void testDotenvx() {
        DotenvxBuilder builder = Dotenvx.configure()
                .ignoreIfMissing()
                .systemProperties();
        Dotenv dotenv = builder.load();
        System.out.println("HELLO: " + dotenv.get("HELLO"));
    }

    @Test
    public void testLoadProperties() {
        final Properties properties = new DotenvxPropertiesBuilder().filename("classpath:application.properties").load();
        System.out.println(properties.get("hello"));
    }

    @Test
    public void testJakartaConfig() throws Exception {
        final Loader loader = Loader.bootstrap();
        DemoConfig config = loader
                .path(".env")
                .load(DemoConfig.class);
        System.out.println(config.getHello());
    }

    @Test
    public void testJakartaConfigInterface() throws Exception {
        final Loader loader = Loader.bootstrap();
        UserConfig config = loader
                .path(".env")
                .load(UserConfig.class);
        System.out.println(config.hello());
        System.out.println(config.world());
    }

    @Test
    public void testJakartaConfigRecord() throws Exception {
        final Loader loader = Loader.bootstrap();
        DemoRecordConfig config = loader
                .path(".env")
                .load(DemoRecordConfig.class);
        System.out.println(config.hello());
    }
}

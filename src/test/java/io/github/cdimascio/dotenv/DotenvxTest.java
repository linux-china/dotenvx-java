package io.github.cdimascio.dotenv;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DotenvxTest {

    @Test
    public void testDotenv() {
        Dotenv dotenv = Dotenvx.load();
        final String hello = dotenv.get("HELLO");
        assertThat(hello).isEqualTo("World");
    }

    @Test
    public void testDotenvx() {
        DotenvxBuilder builder = Dotenvx.configure()
                .filename(".env")
                .ignoreIfMissing()
                .systemProperties();
        Dotenv dotenv = builder.load();
        final String hello = dotenv.get("HELLO");
        System.out.println("HELLO: " + hello);
    }
}

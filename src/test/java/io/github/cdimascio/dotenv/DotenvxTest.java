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
}

package io.github.cdimascio.dotenv;

public interface UserConfig {
    String hello();

    default String world() {
        return "World";
    }
}

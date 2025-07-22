package io.github.cdimascio.dotenv;

/**
 * Creates and configures a new Dotenv instance with dotenvx support.
 */
public interface Dotenvx {

    /**
     * Configures a new {@link Dotenv} instance with dotenvx support.
     *
     * @return a new {@link Dotenv} instance with dotenvx support
     */
    static DotenvxBuilder configure() {
        return new DotenvxBuilder();
    }

    static Dotenv load() {
        return new DotenvxBuilder().load();
    }
}

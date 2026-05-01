package io.github.cdimascio.dotenv;

import org.jspecify.annotations.NonNull;

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

    @NonNull
    static Dotenv load() {
        return new DotenvxBuilder().load();
    }
}

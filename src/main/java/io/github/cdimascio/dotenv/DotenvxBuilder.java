package io.github.cdimascio.dotenv;


import io.github.cdimascio.dotenv.internal.DotenvParser;
import io.github.cdimascio.dotenv.internal.DotenvReader;
import io.github.cdimascio.ecies.Ecies;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds and loads and {@link Dotenv} instance with dotenvx support.
 *
 * @see Dotenvx#configure()
 */
@SuppressWarnings("unused")
public class DotenvxBuilder extends DotenvBuilder {
    private String privateKeyHex = null;
    private String filename = ".env";
    private String directoryPath = "./";
    private boolean systemProperties = false;
    private boolean throwIfMissing = true;
    private boolean throwIfMalformed = true;

    /**
     * Sets the directory containing the .env file.
     *
     * @param path the directory containing the .env file
     * @return this {@link DotenvxBuilder}
     */
    public DotenvxBuilder directory(final String path) {
        this.directoryPath = path;
        return this;
    }

    /**
     * Sets the name of the .env file. The default is .env.
     *
     * @param name the filename
     * @return this {@link DotenvxBuilder}
     */
    public DotenvxBuilder filename(final String name) {
        filename = name;
        return this;
    }

    /**
     * Does not throw an exception when .env is missing.
     *
     * @return this {@link DotenvxBuilder}
     */
    public DotenvxBuilder ignoreIfMissing() {
        throwIfMissing = false;
        return this;
    }

    /**
     * Does not throw an exception when .env is malformed.
     *
     * @return this {@link DotenvxBuilder}
     */
    public DotenvxBuilder ignoreIfMalformed() {
        throwIfMalformed = false;
        return this;
    }

    /**
     * Sets each environment variable as system properties.
     *
     * @return this {@link DotenvxBuilder}
     */
    public DotenvxBuilder systemProperties() {
        systemProperties = true;
        return this;
    }

    /**
     * set the private key
     *
     * @param privateKeyHex private key in hexadecimal format
     * @return this {@link DotenvxBuilder}
     */
    public DotenvxBuilder privateKey(String privateKeyHex) {
        this.privateKeyHex = privateKeyHex;
        return this;
    }

    /**
     * Load the contents of .env into the virtual environment.
     *
     * @return a new {@link Dotenv} instance
     * @throws DotenvException when an error occurs
     */
    public Dotenv load() throws DotenvException {
        final DotenvParser parser = new DotenvParser(
                new DotenvReader(directoryPath, filename),
                throwIfMissing, throwIfMalformed);
        List<DotenvEntry> entries = parser.parse();
        String profileName = null;
        if (filename.contains(".env.")) {
            profileName = filename.substring(filename.indexOf(".env.") + 5);
        }
        boolean isEncrypted = entries.stream()
                .anyMatch(entry -> entry.getValue().startsWith("encrypted:"));
        if (isEncrypted) {
            String privateKey = getDotenvxPrivateKey(profileName);
            if (privateKey == null || privateKey.isEmpty()) {
                throw new DotenvException("No DOTENV_PRIVATE_KEY found in environment variables or .env.keys file.");
            }
            List<DotenvEntry> decryptedEntries = new ArrayList<>();
            for (DotenvEntry entry : entries) {
                if (entry.getValue().startsWith("encrypted:")) {
                    final String encryptedText = entry.getValue().substring(10);
                    decryptedEntries.add(new DotenvEntry(entry.getKey(), decryptItem(privateKey, encryptedText)));
                } else {
                    decryptedEntries.add(entry);
                }
            }
            entries = decryptedEntries;
        }
        if (systemProperties) {
            entries.forEach(it -> System.setProperty(it.getKey(), it.getValue()));
        }
        return new DotenvImpl(entries);
    }

    private String getDotenvxPrivateKey(String profileName) {
        if (this.privateKeyHex != null && !this.privateKeyHex.isEmpty()) {
            return this.privateKeyHex;
        } else {
            String privateKeyEnvName = "DOTENV_PRIVATE_KEY";
            if (profileName != null && !profileName.isEmpty()) {
                privateKeyEnvName = "DOTENV_PRIVATE_KEY_" + profileName.toUpperCase();
            }
            String privateKey = System.getenv(privateKeyEnvName);
            if (privateKey == null || privateKey.isEmpty()) {
                if (Files.exists(Paths.get(".env.keys"))) { // Check in the current directory
                    final Dotenv keysEnv = Dotenv.configure().filename(".env.keys").load();
                    privateKey = keysEnv.get(privateKeyEnvName);
                } else if (Files.exists(Paths.get(System.getProperty("user.home"), ".env.keys"))) { // Check in the user's home directory
                    final Dotenv keysEnv = Dotenv.configure().directory(System.getProperty("user.home")).filename(".env.keys").load();
                    privateKey = keysEnv.get(privateKeyEnvName);
                }
            }
            this.privateKeyHex = privateKey;
        }
        return this.privateKeyHex;
    }

    private String decryptItem(String privateKeyHex, String item) throws DotenvException {
        try {
            return Ecies.decrypt(privateKeyHex, item);
        } catch (Exception e) {
            throw new DotenvException("Failed to decrypt item: " + item);
        }
    }

}

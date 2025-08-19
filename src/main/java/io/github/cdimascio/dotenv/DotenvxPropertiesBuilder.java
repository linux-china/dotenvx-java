package io.github.cdimascio.dotenv;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.ecies.Ecies;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Builds and loads properties instance with dotenvx support.
 *
 */
@SuppressWarnings("unused")
public class DotenvxPropertiesBuilder implements DotenvxBaseBuilder {
    public static final ObjectMapper objectMapper = new ObjectMapper();
    private String privateKeyHex = null;
    private String filename = "application.properties";
    private String directoryPath = ".";

    /**
     * Sets the directory containing the .properties file.
     *
     * @param path the directory containing the .properties file
     */
    public DotenvxPropertiesBuilder directory(final String path) {
        this.directoryPath = path;
        return this;
    }

    /**
     * Sets the name of the .properties file. The default is application.properties.
     *
     * @param name the filename or classpath resource,, such as `classpath:application.properties`
     */
    public DotenvxPropertiesBuilder filename(final String name) {
        filename = name;
        return this;
    }

    /**
     * set the private key
     *
     * @param privateKeyHex private key in hexadecimal format
     */
    public DotenvxPropertiesBuilder privateKey(String privateKeyHex) {
        this.privateKeyHex = privateKeyHex;
        return this;
    }

    /**
     * Load the contents of .properties into the virtual environment.
     *
     * @return a new {@link Dotenv} instance
     * @throws DotenvException when an error occurs
     */
    public Properties load() throws DotenvException {
        Properties properties = new Properties();
        if (this.filename.startsWith("classpath:")) {
            String classpathFile = this.filename.substring("classpath:".length());
            try (var inputStream = getClass().getClassLoader().getResourceAsStream(classpathFile)) {
                if (inputStream == null) {
                    throw new DotenvException("File not found in classpath: " + classpathFile);
                }
                properties.load(inputStream);
            } catch (Exception e) {
                throw new DotenvException("Failed to load properties from classpath: " + classpathFile);
            }
        } else {
            Path filePath = Paths.get(this.directoryPath, this.filename);
            if (!Files.exists(filePath)) {
                throw new DotenvException("File not found: " + filePath);
            }
            try (var inputStream = Files.newInputStream(filePath)) {
                properties.load(inputStream);
            } catch (Exception e) {
                throw new DotenvException("Failed to load properties from file: " + filePath);
            }
        }
        String publicKeyHex = getPublicKeyHex(properties);
        String profileName = null;
        if (filename.contains("-")) {
            profileName = filename.substring(filename.indexOf("-") + 1, filename.lastIndexOf('.'));
        }
        boolean isEncrypted = properties.entrySet().stream()
                .anyMatch(entry -> {
                    String value = entry.getValue().toString();
                    return value.startsWith("encrypted:");
                });
        if (isEncrypted) {
            String privateKey = getDotenvxPrivateKey(profileName, publicKeyHex);
            if (privateKey == null || privateKey.isEmpty()) {
                throw new DotenvException("No DOTENV_PRIVATE_KEY found in environment variables or .env.keys file.");
            }
            List<DotenvEntry> decryptedEntries = new ArrayList<>();
            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                String value = entry.getValue().toString();
                if (value.startsWith("encrypted:")) {
                    final String encryptedText = value.substring(10);
                    properties.put(entry.getKey(), decryptItem(privateKey, encryptedText));
                }
            }
        }
        return properties;
    }

    public String getPublicKeyHex(Properties properties) {
        for (String key : properties.stringPropertyNames()) {
            if (key.startsWith("dotenv.public.key")) {
                String publicKeyHex = properties.getProperty(key);
                if (publicKeyHex != null && !publicKeyHex.isEmpty()) {
                    return publicKeyHex;
                }
            }
        }
        return null;
    }

    public String getDotenvxPrivateKey(String profileName, String publicKeyHex) {
        if (this.privateKeyHex != null && !this.privateKeyHex.isEmpty()) {
            return this.privateKeyHex;
        } else {
            // load the private key from the global store: .env.keys.json
            if (publicKeyHex != null && !publicKeyHex.isEmpty()) {
                String privateKey = getPrivateKeyFromGlobalStore(publicKeyHex);
                if (privateKey != null && !privateKey.isEmpty()) {
                    this.privateKeyHex = privateKey;
                    return this.privateKeyHex;
                }
            }
            // load from environment variables
            String privateKeyEnvName = "DOTENV_PRIVATE_KEY";
            if (profileName != null && !profileName.isEmpty()) {
                privateKeyEnvName = "DOTENV_PRIVATE_KEY_" + profileName.toUpperCase();
            }
            String privateKey = System.getenv(privateKeyEnvName);
            // load from .env.keys file
            if (privateKey == null || privateKey.isEmpty()) {
                if (this.directoryPath != null && Files.exists(Paths.get(this.directoryPath, ".env.keys"))) { // Check in the specified directory
                    final Dotenv keysEnv = Dotenv.configure().directory(this.directoryPath).filename(".env.keys").load();
                    privateKey = keysEnv.get(privateKeyEnvName);
                } else if (Files.exists(Paths.get(".env.keys"))) { // Check in the current directory
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

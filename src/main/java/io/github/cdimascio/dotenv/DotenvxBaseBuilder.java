package io.github.cdimascio.dotenv;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;


@SuppressWarnings("unchecked")
public interface DotenvxBaseBuilder {
    ObjectMapper objectMapper = new ObjectMapper();

    @Nullable
    default String getPrivateKeyFromGlobalStore(@NonNull String publicKeyHex) {
        final Path globalFileStore = Paths.get(System.getProperty("user.home"), ".dotenvx", ".env.keys.json");
        if (Files.exists(globalFileStore)) {
            try {
                Map<String, Object> globalStore = objectMapper.readValue(globalFileStore.toFile(), Map.class);
                if (globalStore.containsKey("version") && globalStore.containsKey("keys")) { // new file format
                    globalStore = (Map<String, Object>) globalStore.get("keys");
                }
                if (globalStore.containsKey(publicKeyHex)) {
                    final Object keyPair = globalStore.get(publicKeyHex);
                    if (keyPair instanceof Map) {
                        final Map<String, Object> keyPairMap = (Map<String, Object>) keyPair;
                        return trimPrivateKey(keyPairMap.get("private_key").toString());
                    }
                }
            } catch (Exception ignore) {

            }
        }
        return null;
    }

    @Nullable
    default String trimPrivateKey(@Nullable String privateKeyHex) {
        if (privateKeyHex != null && privateKeyHex.contains("{")) {
            return privateKeyHex.substring(0, privateKeyHex.indexOf("{"));
        }
        return privateKeyHex;
    }

}

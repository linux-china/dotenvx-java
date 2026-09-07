package io.github.cdimascio.ecies;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EciesTest {
    @Test
    public void testEncrypt() throws Exception {
        String message = "Hello World!";
        String encryptedBase64 = Ecies.encrypt("02b4972559803fa3c2464e93858f80c3a4c86f046f725329f8975e007b393dc4f0", message);
        String decryptedBase64 = Ecies.decrypt("9e70188d351c25d0714929205df9b8f4564b6b859966bdae7aef7f752a749d8b", encryptedBase64);
        Assertions.assertEquals(message, decryptedBase64);
    }
}

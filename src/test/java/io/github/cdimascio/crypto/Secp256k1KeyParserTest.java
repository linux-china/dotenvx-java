package io.github.cdimascio.crypto;

import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.Test;

public class Secp256k1KeyParserTest {

    @Test
    public void parseKeys() throws Exception {
        String privateKey = "419f823553ee434c8a80a809c9874306593734173a01330e00f0c118da2b0f48";
        String publicKey = "03437763be709bbb8b253435c210d4f2d01f966195d9faaad7845edf0fde74040f";
        Secp256k1KeyParser.parseSecp256k1PrivateKey(Hex.decode(privateKey));
        Secp256k1KeyParser.parseSecp256k1CompressedPublicKey(Hex.decode(publicKey));
    }
}

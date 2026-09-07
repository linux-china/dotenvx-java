package io.github.cdimascio.crypto;

import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.Test;

public class Secp256k1KeyParserTest {

    @Test
    public void parseKeys() throws Exception {
        String privateKey = "9e70188d351c25d0714929205df9b8f4564b6b859966bdae7aef7f752a749d8b";
        String publicKey = "02b4972559803fa3c2464e93858f80c3a4c86f046f725329f8975e007b393dc4f0";
        Secp256k1KeyParser.parseSecp256k1PrivateKey(Hex.decode(privateKey));
        Secp256k1KeyParser.parseSecp256k1CompressedPublicKey(Hex.decode(publicKey));
    }
}

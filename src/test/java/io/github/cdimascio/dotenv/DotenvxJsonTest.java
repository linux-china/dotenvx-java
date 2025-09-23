package io.github.cdimascio.dotenv;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.github.cdimascio.json.DotenvxGlobalJsonSerializer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class DotenvxJsonTest {
    private static ObjectMapper objectMapper = new ObjectMapper();

    @BeforeAll
    static void setup() throws Exception {
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(new DotenvxGlobalJsonSerializer("02b4972559803fa3c2464e93858f80c3a4c86f046f725329f8975e007b393dc4f0"));
        //simpleModule.addDeserializer(String.class, new DotenvxGlobalJsonDeserializer(privateKey));
        objectMapper.registerModule(simpleModule);
    }

    @Test
    public void testJsonSerialize() throws IOException {
        Map<String, String> info = new HashMap<>();
        info.put("email", "private:demo@example.com");
        info.put("nick", "Jackie");
        final String jsonText = objectMapper.writeValueAsString(info);
        System.out.println(jsonText);
    }
}

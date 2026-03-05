---
name: dotenvx-java
description: Dotenvx Java SDK - decrypt your .env and properties files with Spring Boot support
---

## Get started

Add the following dependency to your `pom.xml`:

```xml

<dependency>
    <groupId>org.mvnsearch</groupId>
    <artifactId>dotenvx-java</artifactId>
    <version>0.2.4</version>
</dependency>
```

And use `Dotenvx.load()` to `.env` file in your Java application:

```java
import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.Dotenvx;

Dotenv dotenv = Dotenvx.load();
final String hello = dotenv.get("HELLO");
```

**Tips**: you can use `Dotenvx.configure()` to customize the loading of the `.env` file and private key.

If you want to use a properties file, please use `DotenvxPropertiesBuilder`, example:

```
    @Test
    public void testLoadProperties() {
        final Properties properties = new DotenvxPropertiesBuilder().filename("classpath:application.properties").load();
        System.out.println(properties.get("hello"));
    }
```

## FAQ

### private key loading

dotenvx-java loads private key from the following sources in order:

- Global key store file: `$HOME/.dotenvx/.env.keys.json`
- `DOTENV_PRIVATE_KEY` environment variable
- `.env.keys` file in working directory
- `$HOME/.env.keys` file

### How to integrate Jackson with Dotenvx?

You can integrate Jackson with Dotenvx to protect some sensitive fields, such as SSN, email or phone number.

```java
  ObjectMapper getDotenvxObjectMapper() {
    SimpleModule simpleModule = new SimpleModule();
    simpleModule.addSerializer(new DotenvxGlobalJsonSerializer(publicKey));
    return JsonMapper.builder().addModules(simpleModule).build();
}

@Test
public void testJsonSerialize() throws IOException {
    Map<String, String> info = new HashMap<>();
    info.put("email", "private:demo@example.com");
    info.put("nick", "Jackie");
    final String jsonText = objectMapper.writeValueAsString(info);
    System.out.println(jsonText);
}
```

If a text value prefixed with `private:`, and the value will be encrypted.



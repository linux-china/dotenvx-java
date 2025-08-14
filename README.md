Dotenvx Java SDK
==================

[Dotenvx](https://dotenvx.com/) encrypts your .env files, limiting their attack vector while retaining their benefits.

# Get started

Add the following dependency to your `pom.xml`:

```xml

<dependency>
    <groupId>org.mvnsearch</groupId>
    <artifactId>dotenvx-java</artifactId>
    <version>0.2.0</version>
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

# Load properties file

If you want to use a properties file, please use `DotenvxPropertiesBuilder`, example:

```
    @Test
    public void testLoadProperties() {
        final Properties properties = new DotenvxPropertiesBuilder().filename("classpath:application.properties").load();
        System.out.println(properties.get("hello"));
    }
```

# Jakarta Configuration

Dotenvx-java is compatible with [Jakarta Configuration](, you can use it as follows:

For example, you can define a configuration class:

```java
public class DemoConfig {
    private String hello;

    public String getHello() {
        return hello;
    }

    public void setHello(String hello) {
        this.hello = hello;
    }
}
```

And then load the configuration using `Loader`:

```java

@Test
public void testJakartaConfig() throws Exception {
    final Loader loader = Loader.bootstrap();
    DemoConfig config = loader
            .path(".env")
            .load(DemoConfig.class);
    System.out.println(config.getHello());
}

```

You can use POJO as a configuration class, and you can use config interface or record as well.

```java
public interface UserConfig {
    String hello();
}

public record DemoRecordConfig(String hello) {
}
```

Restrictions:

- Now only `String`, `Integer`, `Long`, `Double`, `Boolean` field types are supported
- Naming conventions:
    - `hello` to `HELLO` in .env file and `hello` in properties file
    - `jdbcUrl` to `JDBC_URL` or `jdbc.url`

# How dotenvx works?

### private key loading

dotenvx-java loads private key from the following sources in order:

- Global key store file: `$HOME/.dotenvx/.env.keys.json`
- `DOTENV_PRIVATE_KEY` environment variable
- `.env.keys` file in working directory
- `$HOME/.env.keys` file

# Credits

- ecies-java: https://github.com/ecies/java
- dotenv-java: https://github.com/cdimascio/dotenv-java

# References

* Dotenvx: https://dotenvx.com/
* Dotenvx Python SDK: https://github.com/dotenvx/python-dotenvx
* Dotenvx Node.js SDK: https://github.com/dotenvx/dotenvx
* Jakarta Configuration: https://github.com/jakartaee/config
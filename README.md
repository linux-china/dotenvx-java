Dotenvx Java SDK
==================

[Dotenvx](https://dotenvx.com/) encrypts your .env files, limiting their attack vector while retaining their benefits.

# Get started

Add the following dependency to your `pom.xml`:

```xml

<dependency>
    <groupId>org.mvnsearch</groupId>
    <artifactId>dotenvx-java</artifactId>
    <version>0.1.1</version>
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
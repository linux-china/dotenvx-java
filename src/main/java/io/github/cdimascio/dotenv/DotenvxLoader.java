package io.github.cdimascio.dotenv;

import jakarta.config.Loader;
import jakarta.config.TypeToken;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Dotenvx Loader is a Jakarta Config Loader implementation
 *
 * @author linux_china
 */
public class DotenvxLoader implements Loader {
    private String configFilePath;
    private String extName;

    @Override
    public <T> T load(Class<T> clazz) {
        if (clazz == Loader.class) {
            return (T) this;
        }
        try {
            Map<String, String> entries = entries();
            // check the clazz is an interface or not
            if (clazz.isInterface()) {
                Class<?>[] interfaces = new Class[]{clazz};
                return (T) Proxy.newProxyInstance(
                        clazz.getClassLoader(),
                        interfaces,
                        new DotenvxJakartaConfigInvocationHandler(entries)
                );
            }
            // check the clazz is a record or not
            if (clazz.isRecord()) {
                // If the class is a record, we need to create an instance using the record constructor
                RecordComponent[] components = clazz.getRecordComponents();
                Class<?>[] componentTypes = new Class[components.length];
                Object[] args = new Object[components.length];
                for (int i = 0; i < components.length; i++) {
                    String fieldName = components[i].getName();
                    componentTypes[i] = components[i].getType();
                    String value = entries.get(getConfigKeyName(fieldName));
                    if (value != null) {
                        args[i] = convertValue(value, componentTypes[i]);
                    }
                }
                return clazz.getConstructor(componentTypes).newInstance(args);
            }
            // normal POJO
            final T instance = clazz.getDeclaredConstructor().newInstance();
            // inject fields to Map instance
            if (instance instanceof Map) {
                // If the class is a Map, populate it directly
                Map<String, String> mapInstance = (Map<String, String>) instance;
                mapInstance.putAll(entries);
            }
            // inject fields to the class instance
            for (Field declaredField : clazz.getDeclaredFields()) {
                final String fieldName = declaredField.getName();
                final String value = entries.get(getConfigKeyName(fieldName));
                if (value != null) {
                    declaredField.setAccessible(true);
                    Object fieldValue = convertValue(value, declaredField.getType());
                    declaredField.set(instance, fieldValue);
                }
            }
            return instance;
        } catch (Exception ignore) {

        }
        return null;
    }

    @Override
    public <T> T load(TypeToken<T> type) {
        final Type javaType = type.type();
        try {
            final Class<?> clazz = Class.forName(javaType.getTypeName());
            return load((Class<T>) clazz);
        } catch (Exception ignore) {

        }
        return null;
    }

    @Override
    public Loader path(String path) {
        this.configFilePath = path;
        if (configFilePath.endsWith(".properties")) {
            this.extName = "properties";
        } else if (configFilePath.startsWith(".env")) {
            this.extName = "env";
        }
        return this;
    }

    private Object convertValue(String value, Class<?> type) {
        if (type == String.class) {
            return value;
        } else if (type == int.class || type == Integer.class) {
            return Integer.parseInt(value);
        } else if (type == boolean.class || type == Boolean.class) {
            return Boolean.parseBoolean(value);
        } else if (type == long.class || type == Long.class) {
            return Long.parseLong(value);
        } else if (type == double.class || type == Double.class) {
            return Double.parseDouble(value);
        }
        throw new UnsupportedOperationException("Unsupported type: " + type.getName());
    }

    private String getConfigKeyName(String fieldName) {
        StringBuilder sb = new StringBuilder();
        if ("properties".equals(extName)) {
            for (char c : fieldName.toCharArray()) {
                if (Character.isUpperCase(c)) {
                    sb.append('.').append(Character.toLowerCase(c));
                } else {
                    sb.append(c);
                }
            }
        } else {
            for (char c : fieldName.toCharArray()) {
                if (Character.isUpperCase(c)) {
                    sb.append('_');
                }
                sb.append(Character.toUpperCase(c));
            }
        }
        return sb.toString();
    }

    private Map<String, String> entries() throws Exception {
        if (this.configFilePath == null) {
            return Dotenvx.load().entries().stream()
                    .collect(java.util.stream.Collectors.toMap(DotenvEntry::getKey, DotenvEntry::getValue));
        } else if (configFilePath.startsWith("classpath:") && configFilePath.endsWith(".properties")) {
            Properties properties = new DotenvxPropertiesBuilder()
                    .filename(configFilePath).load();
            return properties.entrySet().stream()
                    .collect(Collectors.toMap(
                            entry -> entry.getKey().toString(),
                            entry -> entry.getValue().toString()
                    ));
        } else {
            final Path filePath = Paths.get(configFilePath).toAbsolutePath();
            if (!filePath.toFile().exists()) {
                throw new IllegalArgumentException("File not found: " + filePath);
            }
            String directory = filePath.getParent().toString();
            String fileName = filePath.toFile().getName();
            if (this.configFilePath.endsWith(".properties")) {
                final Properties properties = new DotenvxPropertiesBuilder().directory(directory).filename(fileName).load();
                return properties.entrySet().stream()
                        .collect(Collectors.toMap(
                                entry -> entry.getKey().toString(),
                                entry -> entry.getValue().toString()
                        ));
            } else {
                DotenvxBuilder builder = Dotenvx.configure()
                        .directory(directory)
                        .filename(fileName)
                        .ignoreIfMissing()
                        .systemProperties();
                final Dotenv dotenv = builder.load();
                return dotenv.entries().stream()
                        .collect(java.util.stream.Collectors.toMap(DotenvEntry::getKey, DotenvEntry::getValue));
            }

        }
    }

    public class DotenvxJakartaConfigInvocationHandler implements InvocationHandler {
        public Map<String, String> entries;

        public DotenvxJakartaConfigInvocationHandler(Map<String, String> entries) {
            this.entries = entries;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            // Handle default methods in interfaces
            if (method.isDefault()) {
                return MethodHandles.lookup().unreflectSpecial(method, method.getDeclaringClass())
                        .bindTo(proxy).invokeWithArguments(args);
            }
            String methodName = method.getName();
            String keyName = getConfigKeyName(methodName);
            if (!entries.containsKey(keyName)) {
                return null;
            }
            String value = entries.get(keyName);
            return convertValue(value, method.getReturnType());
        }
    }

}

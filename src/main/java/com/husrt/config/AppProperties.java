package com.husrt.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class AppProperties {

    private static final Properties PROPS = new Properties();

    static {
        try (InputStream in = AppProperties.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (in != null) {
                PROPS.load(in);
            }
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private AppProperties() {
    }

    public static String get(String key, String defaultValue) {
        String env = System.getenv(envKey(key));
        if (env != null && !env.isBlank()) {
            return env.trim();
        }
        return PROPS.getProperty(key, defaultValue);
    }

    private static String envKey(String key) {
        return key.toUpperCase().replace('.', '_');
    }

    public static String jdbcUrl() {
        String host = get("db.host", "127.0.0.1");
        String port = get("db.port", "3306");
        String name = get("db.name", "husrt");
        return "jdbc:mysql://" + host + ":" + port + "/" + name
                + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Bogota";
    }

    public static String dbUser() {
        return get("db.user", "husrt");
    }

    public static String dbPassword() {
        return get("db.password", "husrt_secret");
    }
}

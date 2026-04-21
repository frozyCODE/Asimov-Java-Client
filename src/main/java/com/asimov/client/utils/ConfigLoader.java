package com.asimov.client.utils;

import java.io.InputStream;
import java.util.Properties;

/**
 * Utilitaire pour charger les configurations depuis le fichier config.properties.
 */
public class ConfigLoader {
    private static final Properties properties = new Properties();

    static {
        try (InputStream input = ConfigLoader.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                System.err.println("Désolé, impossible de trouver config.properties");
            } else {
                properties.load(input);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }
}
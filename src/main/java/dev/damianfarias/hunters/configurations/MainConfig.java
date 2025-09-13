package dev.damianfarias.hunters.configurations;

import dev.damianfarias.hunters.model.YamlConfig;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

public class MainConfig {
    private final YamlConfig yamlConfig;
    private final Map<String, Object> configurations = new HashMap<>();

    public MainConfig(YamlConfig yamlConfig) {
        this.yamlConfig = yamlConfig;
        reload();
    }

    public void reload() {
        configurations.clear();
        loadSection("", yamlConfig.getConfig());
    }

    private void loadSection(String prefix, ConfigurationSection section) {
        for (String key : section.getKeys(false)) {
            String fullKey = prefix.isEmpty() ? key : prefix + "." + key;
            Object value = section.get(key);

            if (value instanceof ConfigurationSection) {
                loadSection(fullKey, (ConfigurationSection) value);
            } else {
                configurations.put(fullKey, value);
            }
        }
    }

    public String getString(String key) {
        Object value = configurations.get(key);
        return value instanceof String ? (String) value : "null";
    }

    public int getInt(String key) {
        Object value = configurations.get(key);
        if (value == null) return 0;
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public int getInt(String key, int def) {
        Object value = configurations.get(key);
        if (value == null) return def;
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return def;
        }
    }

    public float getFloat(String key) {
        Object value = configurations.get(key);
        if (value == null) return 0;
        try {
            return Float.parseFloat(value.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public boolean getBoolean(String key) {
        Object value = configurations.get(key);
        return value != null && Boolean.parseBoolean(value.toString());
    }

    public List<String> getList(String key) {
        Object value = configurations.get(key);
        if (value instanceof List<?>) {
            List<String> safeList = new ArrayList<>();
            for (Object obj : (List<?>) value) {
                if (obj != null) safeList.add(obj.toString());
            }
            return safeList;
        }
        return new ArrayList<>();
    }

    public String getString(String key, Map<String, String> placeholders) {
        return applyPlaceholders(getString(key), placeholders);
    }

    public List<String> getList(String key, Map<String, String> placeholders) {
        return getList(key).stream()
                .map(line -> applyPlaceholders(line, placeholders))
                .toList();
    }

    private String applyPlaceholders(String string, Map<String, String> placeholders) {
        if (string == null) return "null";
        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                string = string.replace("%" + entry.getKey() + "%", entry.getValue());
            }
        }
        Object prefix = configurations.get("prefix");
        if (prefix instanceof String) {
            string = string.replace("%prefix%", (String) prefix);
        }
        return string;
    }

    public ConfigurationSection getSection(String path) {
        return yamlConfig.getConfig().getConfigurationSection(path);
    }

    public void set(String path, Object value) {
        yamlConfig.set(path, value);
        configurations.put(path, value); // Actualizar el mapa interno
    }
}

package dev.damianfarias.hunters.model;

import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.logging.Level;

public class YamlConfig {

    private final JavaPlugin plugin;
    private final String fileName;
    private final File configFile;

    @Getter
    private FileConfiguration config;
    private FileConfiguration defaultConfig;

    public YamlConfig(JavaPlugin plugin, String fileName) {
        if (plugin == null) throw new IllegalArgumentException("Plugin cannot be null");

        this.plugin = plugin;
        this.fileName = fileName;
        this.configFile = new File(plugin.getDataFolder(), fileName);
    }

    public void register() {
        if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();

        if (!configFile.exists()) plugin.saveResource(fileName, false);

        this.config = YamlConfiguration.loadConfiguration(configFile);

        try (InputStream stream = plugin.getResource(fileName)) {
            if (stream != null) {
                this.defaultConfig = YamlConfiguration.loadConfiguration(
                        new InputStreamReader(stream, StandardCharsets.UTF_8)
                );
                config.setDefaults(defaultConfig);
                config.options().copyDefaults(true);
                save();
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not load default config: " + fileName, e);
        }
    }

    public void save() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save config to " + configFile, e);
        }
    }

    public void reload() {
        this.config = YamlConfiguration.loadConfiguration(configFile);
        if (defaultConfig != null) {
            config.setDefaults(defaultConfig);
            config.options().copyDefaults(true);
        }
    }

    // ---- Métodos utilitarios ----

    public String getString(String path) {
        return config.getString(path, "String not found");
    }

    public String getString(String path, String def) {
        if(getString(path) == null) return def;
        return getString(path);
    }

    public int getInt(String path) {
        return config.getInt(path, 0);
    }

    public int getInt(String path, int def) {
        return config.getInt(path, def);
    }


    public boolean getBoolean(String path) {
        return config.getBoolean(path, false);
    }

    public List<String> getStringList(String path) {
        return config.getStringList(path);
    }

    public void set(String path, Object value) {
        config.set(path, value);
    }
}
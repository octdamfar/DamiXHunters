package dev.damianfarias.hunters.managers;

import dev.damianfarias.hunters.model.ConfigGUI;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static dev.damianfarias.hunters.utils.DamiUtils.sendMessage;

public class CustomGUIManager {

    @Getter
    private static final CustomGUIManager instance = new CustomGUIManager();

    @Getter
    private final List<ConfigGUI> guis = new ArrayList<>();

    private CustomGUIManager() {}

    public void load(@Nullable ConfigurationSection s) {
        guis.clear();
        if (s == null) {
            warn("Container section of GUIs is null.");
            return;
        }
        debug("Loading GUIs...");
        for (String key : s.getKeys(false)) {
            ConfigurationSection sec = s.getConfigurationSection(key);
            if (sec == null) continue;
            guis.add(ConfigGUI.formConfig(sec));
        }
        debug( guis.size() + " GUIs loaded.");
    }

    private static void warn(String s) {
        sendMessage(Bukkit.getConsoleSender(), "&c[GUI Loader] " + s);
    }

    private static void debug(String s) {
        sendMessage(Bukkit.getConsoleSender(), "&a[GUI Loader] " + s);
    }
}

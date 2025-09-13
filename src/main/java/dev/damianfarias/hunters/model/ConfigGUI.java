package dev.damianfarias.hunters.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@AllArgsConstructor
@Data
public class ConfigGUI {
    private final String name, title;
    private final Set<GUIItem> items;
    private int rows;

    public static ConfigGUI formConfig(@NotNull ConfigurationSection s){
        String name = s.getName();
        Set<GUIItem> items = new HashSet<>();

        if(s.isConfigurationSection("items")){
            for (String key : Objects.requireNonNull(s.getConfigurationSection("items")).getKeys(false)) {
                ConfigurationSection sec = s.getConfigurationSection("items."+key);
                if(sec == null) continue;
                items.add(GUIItem.formConfig(sec));
            }
        }

        String title = s.getString("title", "Non title");
        int rows = s.getInt("rows", 6);

        return new ConfigGUI(name, title, items, rows);
    }
}
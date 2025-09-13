package dev.damianfarias.hunters.model;

import dev.damianfarias.hunters.utils.ItemSerialization;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Data
public class GUIItem {
    private final String id;
    private final ItemStack item;
    private final List<String> actions;
    private int slot;

    public static GUIItem formConfig(@NotNull ConfigurationSection s){
        String id = s.getName();
        ItemStack item = ItemSerialization.deserializeItemStack(s);
        List<String> actions = new ArrayList<>(s.getStringList("actions"));
        int slot = s.getInt("slot", 0);
        return new GUIItem(id, item, actions, slot);
    }

}
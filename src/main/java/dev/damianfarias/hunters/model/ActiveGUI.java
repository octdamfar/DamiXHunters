package dev.damianfarias.hunters.model;

import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import static dev.damianfarias.hunters.utils.DamiUtils.colorize;
import static dev.damianfarias.hunters.utils.DamiUtils.sendMessage;

@Data
public class ActiveGUI implements InventoryHolder {

    private final Inventory inventory;
    private final Player viewer;
    private final ConfigGUI gui;
    private final Map<Integer, GUIItem> registeredItems = new HashMap<>();

    @SuppressWarnings("deprecation")
    public ActiveGUI(Player viewer, ConfigGUI gui) {
        this.gui = gui;
        this.viewer = viewer;
        this.inventory = Bukkit.createInventory(this, gui.getRows() * 9, colorize(gui.getTitle()));

        writeItems();
    }

    public void open(){
        if(!viewer.isOnline()) return;
        viewer.openInventory(inventory);
    }

    private void writeItems() {
        for (GUIItem item : gui.getItems()) {
            if(registeredItems.containsKey(item.getSlot())){
                sendMessage(Bukkit.getConsoleSender(), "&c[Active GUI] Item with repeated slot: "+item.getId()+" in GUI "+gui.getName()+", slot: "+item.getSlot());
                continue;
            }

            registeredItems.put(item.getSlot(), item);
            inventory.setItem(item.getSlot(), item.getItem());
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}

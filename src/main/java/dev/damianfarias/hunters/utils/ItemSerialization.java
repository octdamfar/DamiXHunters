package dev.damianfarias.hunters.utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

import static dev.damianfarias.hunters.utils.DamiUtils.sendMessage;

@SuppressWarnings("deprecation")
public class ItemSerialization {

    public static ItemStack deserializeItemStack(ConfigurationSection s) {
        debug("Deserializing ItemStack in section: " + s.getCurrentPath());

        String typeName = s.getString("type", "STONE");
        Material mat = Material.matchMaterial(typeName);
        if (mat == null) {
            warn("Invalid item type: " + typeName + " -> Using STONE");
            mat = Material.STONE;
        } else {
            debug("Material: " + mat.name());
        }

        int amount = Math.max(1, Math.min(mat.getMaxStackSize(), s.getInt("amount", 1)));
        debug("Amount: " + amount);

        ItemBuilder ib = ItemBuilder.of(mat).withAmount(amount);

        if (s.contains("name")) {
            String name = s.getString("name");
            ib.withName(name);
            debug("Name applied: " + name);
        }

        List<String> lore = s.getStringList("lore");
        if (!lore.isEmpty()) {
            ib.withLore(lore);
            debug("Lore applied: " + lore.size() + " lines");
        }

        if (s.isConfigurationSection("enchants")) {
            ConfigurationSection es = s.getConfigurationSection("enchants");
            for (String key : es.getKeys(false)) {
                Enchantment enchantment = DamiUtils.getEnchantmentUniversal(key);
                if (enchantment == null) {
                    warn("Unknown enchantment: " + key);
                    continue;
                }

                int level = es.getInt(key, 1);
                if (level <= 0) {
                    warn("Invalid enchantment level: " + key + " -> " + es.getString(key));
                    continue;
                }

                ib.withEnchantment(enchantment, level);
                debug("Enchantment applied: " + enchantment.getKey() + " Level: " + level);
            }
        }

        if (s.isList("flags")) {
            List<String> flagsList = s.getStringList("flags");
            boolean hideAll = flagsList.stream().anyMatch(f -> f.equalsIgnoreCase("HIDE_ALL"));
            if (hideAll) {
                ib.hideFlags(ItemFlag.values());
                debug("Applied HIDE_ALL to flags");
            } else {
                for (String flagName : flagsList) {
                    try {
                        ItemFlag flag = ItemFlag.valueOf(flagName.toUpperCase());
                        ib.hideFlags(flag);
                        debug("Flag applied: " + flag.name());
                    } catch (IllegalArgumentException e) {
                        warn("Unknown ItemFlag: " + flagName);
                    }
                }
            }
        }

        if (s.getBoolean("unbreakable", false)) {
            ib.setUnbreakable(true);
            debug("Item is unbreakable");
        }

        if (s.contains("model")) {
            int model = s.getInt("model");
            ib.setCustomModelData(model);
            debug("CustomModelData applied: " + model);
        }

        if (s.contains("durability")) {
            int durability = s.getInt("durability", 0);
            ib.setDurability((short) durability);
            debug("Durability applied: " + durability);
        }

        if (s.contains("head")) {
            String head = s.getString("head");
            if (head != null) {
                if (head.length() > 50) {
                    ib.setCustomHead(head);
                    debug("Custom base64 head applied");
                } else {
                    ib.setPlayerHead(head);
                    debug("Player head applied: " + head);
                }
            }
        }

        ItemStack finalItem = ib.toItemStack();
        debug("ItemStack successfully deserialized: " + finalItem.getType() + " x" + finalItem.getAmount());
        return finalItem;
    }

    public static void serialize(@NotNull ConfigurationSection s, ItemStack item) {
        if (item == null || item.getType().isAir()) {
            if(s.getParent() != null) s.getParent().set(s.getName(), null);
            debug("Item is null or air, deleting section: " + s.getName());
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            warn("Could not get ItemMeta to serialize item. Serialization may be incomplete.");
        }

        // Type and Amount
        s.set("type", item.getType().name());
        s.set("amount", item.getAmount());
        debug("Serializing item of type: " + item.getType().name() + " x" + item.getAmount());

        if (meta != null && meta.hasDisplayName()) {
            s.set("name", meta.getDisplayName());
            debug("Name saved: " + meta.getDisplayName());
        } else {
            s.set("name", null);
        }

        if (meta != null && meta.hasLore()) {
            List<String> strippedLore = meta.getLore().stream()
                    .map(line -> line.replace('§', '&'))
                    .collect(Collectors.toList());
            s.set("lore", strippedLore);
            debug("Lore saved: " + meta.getLore().size() + " lines");
        } else {
            s.set("lore", null);
        }

        if (meta != null && !meta.getEnchants().isEmpty()) {
            ConfigurationSection es = s.createSection("enchants");
            for (Enchantment enchantment : meta.getEnchants().keySet()) {
                es.set(enchantment.getKey().getKey(), meta.getEnchantLevel(enchantment));
                debug("Enchantment saved: " + enchantment.getKey().getKey() + " Level: " + meta.getEnchantLevel(enchantment));
            }
        } else {
            s.set("enchants", null);
        }

        if (meta != null && !meta.getItemFlags().isEmpty()) {
            List<String> flagsList = meta.getItemFlags().stream().map(Enum::name).toList();
            s.set("flags", flagsList);
            debug("Flags saved: " + flagsList.size());
        } else {
            s.set("flags", null);
        }

        if (meta != null) {
            s.set("unbreakable", meta.isUnbreakable());
        }

        if (meta != null && meta.hasCustomModelData()) {
            s.set("model", meta.getCustomModelData());
            debug("CustomModelData saved: " + meta.getCustomModelData());
        } else {
            s.set("model", null);
        }

        if (meta instanceof org.bukkit.inventory.meta.Damageable damageable) {
            if (damageable.hasDamage()) {
                s.set("durability", damageable.getDamage());
                debug("Durability saved: " + damageable.getDamage());
            } else {
                s.set("durability", null);
            }
        } else {
            s.set("durability", null);
        }

        if (item.getType() == Material.PLAYER_HEAD && meta instanceof org.bukkit.inventory.meta.SkullMeta skullMeta) {
            if (skullMeta.hasOwner()) {
                s.set("head", skullMeta.getOwningPlayer().getName());
                debug("Player head saved: " + skullMeta.getOwningPlayer().getName());
            }
        } else {
            s.set("head", null);
        }

        debug("ItemStack serialized in section: " + s.getCurrentPath());
    }

    private static void warn(String s){
        sendMessage(Bukkit.getConsoleSender(), "&c[Item Serializer] " + s);
    }

    private static void debug(String s){
        sendMessage(Bukkit.getConsoleSender(), "&e[Item Serializer] " + s);
    }
}
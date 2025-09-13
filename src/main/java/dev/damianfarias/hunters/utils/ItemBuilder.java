package dev.damianfarias.hunters.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static dev.damianfarias.hunters.utils.DamiUtils.colorize;

@SuppressWarnings({"deprecation", "unused", "UnusedReturnValue"})
public class ItemBuilder {
    private final ItemStack itemStack;

    private ItemBuilder(Material material) {
        this.itemStack = new ItemStack(material);
    }

    private ItemBuilder(ItemStack itemStack) {
        this.itemStack = itemStack.clone();
    }

    public static ItemBuilder of(Material material) {
        return new ItemBuilder(material);
    }

    public static ItemBuilder of(ItemStack itemStack) {
        return new ItemBuilder(itemStack);
    }

    private ItemBuilder editMeta(Consumer<ItemMeta> action) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            action.accept(meta);
            itemStack.setItemMeta(meta);
        }
        return this;
    }

    public ItemBuilder withName(String name) {
        return editMeta(meta -> meta.setDisplayName(colorize(name)));
    }

    public ItemBuilder withLore(String... lore) {
        List<String> translated = Arrays.stream(lore)
                .map(DamiUtils::colorize)
                .collect(Collectors.toList());
        return editMeta(meta -> meta.setLore(translated));
    }

    public ItemBuilder withLore(List<String> lore) {
        List<String> translated = lore.stream()
                .map(DamiUtils::colorize)
                .collect(Collectors.toList());
        return editMeta(meta -> meta.setLore(translated));
    }

    public ItemBuilder addLore(String... lore) {
        List<String> translated = Arrays.stream(lore)
                .map(DamiUtils::colorize)
                .toList();
        return editMeta(meta -> {
            List<String> current = meta.hasLore() && meta.getLore() != null ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
            current.addAll(translated);
            meta.setLore(current);
        });
    }

    public ItemBuilder withAmount(int amount) {
        itemStack.setAmount(amount);
        return this;
    }

    public ItemBuilder withEnchantment(Enchantment enchantment, int level) {
        return editMeta(meta -> meta.addEnchant(enchantment, level, true));
    }

    public ItemBuilder removeEnchantment(Enchantment enchantment) {
        return editMeta(meta -> meta.removeEnchant(enchantment));
    }

    public ItemBuilder setUnbreakable(boolean unbreakable) {
        return editMeta(meta -> meta.setUnbreakable(unbreakable));
    }

    public ItemBuilder hideFlags(ItemFlag... flags) {
        return editMeta(meta -> meta.addItemFlags(flags));
    }

    public ItemBuilder setCustomModelData(Integer data) {
        return editMeta(meta -> meta.setCustomModelData(data));
    }

    public ItemBuilder setDurability(short durability) {
        return editMeta(meta -> {
            if (meta instanceof Damageable) ((Damageable) meta).setDamage(durability);
        });
    }

    public ItemBuilder setPlayerHead(String playerName) {
        if (itemStack.getType() != Material.PLAYER_HEAD) itemStack.setType(Material.PLAYER_HEAD);
        return editMeta(meta -> {
            if (meta instanceof SkullMeta skullMeta) {
                OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
                skullMeta.setOwningPlayer(player);
            }
        });
    }

    public ItemBuilder setCustomHead(String base64Texture) {
        if (itemStack.getType() != Material.PLAYER_HEAD) itemStack.setType(Material.PLAYER_HEAD);
        return editMeta(meta -> {
            if (meta instanceof SkullMeta skullMeta) {
                GameProfile profile = new GameProfile(UUID.randomUUID(), null);
                profile.getProperties().put("textures", new Property("textures", base64Texture));
                try {
                    Field profileField = skullMeta.getClass().getDeclaredField("profile");
                    profileField.setAccessible(true);
                    profileField.set(skullMeta, profile);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public ItemBuilder modifyMeta(Consumer<ItemMeta> metaConsumer) {
        return editMeta(metaConsumer);
    }

    public ItemStack toItemStack() {
        return itemStack;
    }
}

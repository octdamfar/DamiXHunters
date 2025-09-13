package dev.damianfarias.hunters.configurations;

import dev.damianfarias.hunters.model.YamlConfig;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.*;

import static dev.damianfarias.hunters.utils.DamiUtils.sendMessage;

@SuppressWarnings("deprecation")
public class MessagesConfig {

    private final YamlConfig yamlConfig;
    private final Map<String, Object> messages = new HashMap<>();

    public MessagesConfig(YamlConfig yamlConfig) {
        this.yamlConfig = yamlConfig;
        reload();
    }

    public void reload() {
        messages.clear();
        for (String key : yamlConfig.getConfig().getKeys(false)) {
            Object value = yamlConfig.getConfig().get(key);

            if (value instanceof String) {
                messages.put(key, value);
            } else if (value instanceof List<?>) {
                messages.put(key, value);
            }
        }
    }

    public String getString(String key) {
        Object value = messages.get(key);
        return value instanceof String ? applyPlaceholders((String) value, Map.of()) : ChatColor.RED + "Missing message: " + key;
    }

    public List<String> getList(String key) {
        Object value = messages.get(key);
        if (value instanceof List<?>) {
            List<String> safeList = new ArrayList<>();
            for (Object obj : (List<?>) value) {
                if (obj != null) safeList.add(applyPlaceholders(obj.toString(), Map.of()));
            }
            return safeList;
        }
        return Collections.singletonList(ChatColor.RED + "Missing message list: " + key);
    }

    public String getString(String key, Map<String, String> placeholders) {
        return applyPlaceholders(getString(key), placeholders);
    }

    public List<String> getList(String key, Map<String, String> placeholders) {
        return getList(key).stream()
                .map(line -> applyPlaceholders(line, placeholders))
                .toList();
    }

    public void send(CommandSender sender, String key) {
        sendMessage(sender, getString(key));
    }

    public void send(CommandSender sender, String key, Map<String, String> placeholders) {
        sendMessage(sender, getString(key, placeholders));
    }

    public void sendList(CommandSender sender, String key, Map<String, String> placeholders) {
        for (String line : getList(key, placeholders)) {
            sendMessage(sender, line);
        }
    }

    public void sendList(CommandSender sender, String key) {
        for (String line : getList(key)) {
            sendMessage(sender, line);
        }
    }

    private String applyPlaceholders(String message, Map<String, String> placeholders) {
        if (message == null) return ChatColor.RED + "Null message";
        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                message = message.replace("%" + entry.getKey() + "%", entry.getValue());
            }
        }
        Object prefix = messages.get("prefix");
        if (prefix instanceof String) {
            message = message.replace("%prefix%", (String) prefix);
        }
        return message;
    }
}

package dev.damianfarias.hunters.listeners;

import dev.damianfarias.hunters.DamiXHunters;
import dev.damianfarias.hunters.configurations.MainConfig;
import dev.damianfarias.hunters.managers.GameManager;
import dev.damianfarias.hunters.model.GameState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;

import static dev.damianfarias.hunters.utils.DamiUtils.colorize;

public class GroupChatListener implements Listener {
    private final MainConfig config = DamiXHunters.getConfigurationManager().getMainConfig();
    private final GameManager manager = DamiXHunters.getGameManager();

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        GameState currentState = manager.getState();

        if (currentState == GameState.PLAYING && config.getBoolean("only-group-chat")) {
            event.getRecipients().clear();
            String chatFormat = getChatFormat(player, event.getMessage()); // MEJORA: Se pasa el mensaje para el placeholder
            event.setFormat(chatFormat);
            addRecipientsByGroup(event, player);
            return;
        }

        event.setFormat(colorize(
                config.getString("global-chat-format",
                        Map.of(
                                "player", player.getName(),
                                "message", event.getMessage(),
                                "group_prefix", getPrefix(player)
                        )
                )
        ));
    }

    private void addRecipientsByGroup(AsyncPlayerChatEvent event, Player sender) {
        Set<Player> recipients = event.getRecipients();
        if (manager.isHunter(sender.getUniqueId())) {
            recipients.addAll(manager.getOnlineHunters());
        } else if (manager.isRunner(sender.getUniqueId())) {
            recipients.addAll(manager.getOnlineRunners());
        } else if (manager.isSpectator(sender.getUniqueId())) {
            recipients.addAll(manager.getOnlineSpectators());
        } else {
            recipients.add(sender);
        }
    }

    private String getChatFormat(@NotNull Player player, @NotNull String message) {
        String formatPath;
        if (manager.isHunter(player.getUniqueId())) {
            formatPath = "hunters-chat-format";
        } else if (manager.isRunner(player.getUniqueId())) {
            formatPath = "runners-chat-format";
        } else {
            formatPath = "spectators-chat-format";
        }
        return colorize(config.getString(formatPath,
                Map.of("player", player.getName(), "message", message))
        );
    }

    @NotNull
    private String getPrefix(@NotNull Player player) {
        String path;
        if (manager.isHunter(player.getUniqueId())) {
            path = "hunter-prefix";
        } else if (manager.isRunner(player.getUniqueId())) {
            path = "runner-prefix";
        } else if (manager.isSpectator(player.getUniqueId())){
            path = "spectator-prefix";
        } else {
            path = "null-prefix";
        }
        return colorize(config.getString(path));
    }
}
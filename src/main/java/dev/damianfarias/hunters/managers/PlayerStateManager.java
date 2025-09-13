package dev.damianfarias.hunters.managers;

import dev.damianfarias.hunters.DamiXHunters;
import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("deprecation")
@Data
public class PlayerStateManager {

    private final ConfigurationManager cm = DamiXHunters.getConfigurationManager();
    private final Map<UUID, Integer> lives = new HashMap<>();

    public PlayerStateManager() {
        ConfigurationSection livesSection = cm.getDataYaml().getConfig().getConfigurationSection("lives");
        if (livesSection != null) {
            for (String uuidStr : livesSection.getKeys(false)) {
                try {
                    UUID playerUUID = UUID.fromString(uuidStr);
                    lives.put(playerUUID, livesSection.getInt(uuidStr));
                } catch (IllegalArgumentException e) {
                    DamiXHunters.getInstance().getLogger().warning("Invalid UUID found in lives data: " + uuidStr);
                }
            }
        }
    }

    public void setupPlayerAsSpectator(Player player){
        DamiXHunters.getGameManager().addSpectator(player.getUniqueId());

        player.setGameMode(GameMode.SPECTATOR);
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);

        DamiXHunters.getGameManager().getOnlineRunners().stream()
                .findFirst()
                .ifPresentOrElse(
                        runner -> player.teleport(runner.getLocation()),
                        () -> {
                            player.teleport(DamiXHunters.getGameManager().getOverworld() == null ? Bukkit.getWorlds().getFirst().getSpawnLocation() : DamiXHunters.getGameManager().getOverworld().getSpawnLocation());
                            DamiXHunters.getInstance().getLogger().warning("No runners were found. Teleporting " + player.getName() + " to the main world spawn.");
                        }
                );
    }

    public void setupPlayerAsHunter(OfflinePlayer player){
        DamiXHunters.getGameManager().addHunter(player.getUniqueId());
        DamiXHunters.getGameManager().manageJoinPlayer(player);
    }

    public void setupPlayerAsRunner(OfflinePlayer player){
        DamiXHunters.getGameManager().addRunner(player.getUniqueId());
        int defaultLives = DamiXHunters.getConfigurationManager().getMainConfig().getInt("default-lives", 1);
        lives.put(player.getUniqueId(), defaultLives > 0 ? defaultLives : 1);
        saveLives();
    }

    private void saveLives() {
        ConfigurationSection livesSection = cm.getDataYaml().getConfig().createSection("lives");
        for (Map.Entry<UUID, Integer> entry : lives.entrySet()) {
            livesSection.set(entry.getKey().toString(), entry.getValue());
        }
        cm.getDataYaml().save();
    }

    public int getLives(Player player){
        return lives.getOrDefault(player.getUniqueId(), 0);
    }

    public void setLives(UUID playerId, int amount){
        lives.put(playerId, amount);
        saveLives();
    }

    public void decrementLives(Player player) {
        setLives(player.getUniqueId(), getLives(player) - 1);
    }

    public void removeRunner(UUID playerId) {
        DamiXHunters.getGameManager().removeRunner(playerId);
        lives.remove(playerId);
        saveLives();
    }
}
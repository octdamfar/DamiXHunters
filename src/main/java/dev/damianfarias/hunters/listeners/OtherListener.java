package dev.damianfarias.hunters.listeners;

import dev.damianfarias.hunters.DamiXHunters;
import dev.damianfarias.hunters.managers.GameManager;
import dev.damianfarias.hunters.model.GameState;
import dev.damianfarias.hunters.utils.DamiUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;

import static dev.damianfarias.hunters.utils.DamiUtils.colorize;

@SuppressWarnings("deprecation")
public class OtherListener implements Listener {
    private final GameManager gm = DamiXHunters.getGameManager();

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        if(gm.getState() == GameState.PLAYING){
            e.setRespawnLocation(e.getPlayer().getRespawnLocation() != null ? e.getPlayer().getRespawnLocation() : gm.getOverworld() != null ? gm.getOverworld().getSpawnLocation() : e.getPlayer().getLocation());
            if (gm.isHunter(e.getPlayer().getUniqueId())) {
                DamiXHunters.getCompassManager().give(e.getPlayer());
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        if (gm.isRunner(e.getPlayer().getUniqueId()) && gm.getState() == GameState.PLAYING) {
            DamiXHunters.getPlayerStateManager().decrementLives(e.getPlayer());
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                DamiXHunters.getConfigurationManager().getLangConfig().send(onlinePlayer, "runner-death", Map.of("runner", e.getPlayer().getName(), "lives", String.valueOf(DamiXHunters.getPlayerStateManager().getLives(e.getPlayer()))));
                DamiUtils.sendSerializedSound(onlinePlayer, DamiXHunters.getConfigurationManager().getMainConfig().getString("runner-dead-sound"), "runner-dead-sound");
            }
            if (DamiXHunters.getPlayerStateManager().getLives(e.getPlayer()) < 1) {
                DamiXHunters.getPlayerStateManager().removeRunner(e.getPlayer().getUniqueId());
                DamiXHunters.getPlayerStateManager().setupPlayerAsSpectator(e.getPlayer());
            }
            gm.checkLives();
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (gm.getState() == GameState.PLAYING) {
            Player player = e.getPlayer();
            if(gm.isHunter(player.getUniqueId()) || gm.isRunner(player.getUniqueId())){
                gm.manageJoinPlayer(player);
            }

            if(!player.getWorld().getName().startsWith(gm.getWorldPrefix())){
                player.teleport(gm.getOverworld().getSpawnLocation());
            }
            
            if(gm.getBossBar() != null) gm.getBossBar().addPlayer(player);

            if (gm.isHunter(player.getUniqueId())) {
                if(gm.getToRemoveBlindness().contains(player.getUniqueId())){
                    player.removePotionEffect(PotionEffectType.BLINDNESS);
                }

                if(gm.getCountdownSeconds() > 0 && !player.hasPotionEffect(PotionEffectType.BLINDNESS) ){
                    player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 300000, 4, false, false, false));
                }

                e.setJoinMessage(colorize(DamiXHunters.getConfigurationManager().getMainConfig().getString("hunter-join-message", Map.of("player", player.getName()))));
            } else if (gm.isRunner(player.getUniqueId())) {
                e.setJoinMessage(colorize(DamiXHunters.getConfigurationManager().getMainConfig().getString("runner-join-message", Map.of("player", player.getName()))));
            } else {
                DamiXHunters.getPlayerStateManager().setupPlayerAsSpectator(player);
                e.setJoinMessage(colorize(DamiXHunters.getConfigurationManager().getMainConfig().getString("spectator-join-message", Map.of("player", player.getName()))));
            }
        }else{
            e.setJoinMessage(colorize(DamiXHunters.getConfigurationManager().getMainConfig().getString("global-join-message", Map.of("player", e.getPlayer().getName()))));
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (gm.getState() == GameState.PLAYING) {
            if (gm.isHunter(e.getPlayer().getUniqueId()) && gm.getCountdownSeconds() > 0) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e){
        if (gm.getState() == GameState.PLAYING || gm.getState() == GameState.STARTING) {
            Player player = e.getPlayer();
            if(gm.getBossBar() != null) gm.getBossBar().removePlayer(e.getPlayer());
            if (gm.isHunter(player.getUniqueId())) {
                e.setQuitMessage(colorize(DamiXHunters.getConfigurationManager().getMainConfig().getString("hunter-quit-message", Map.of("player", player.getName()))));
            } else if (gm.isRunner(player.getUniqueId())) {
                e.setQuitMessage(colorize(DamiXHunters.getConfigurationManager().getMainConfig().getString("runner-quit-message", Map.of("player", player.getName()))));
            } else {
                player.setGameMode(GameMode.SURVIVAL);
                player.setHealth(player.getMaxHealth());
                player.setLevel(0);
                player.setExp(0);
                player.getInventory().clear();
                player.getInventory().setArmorContents(null);
                player.setFoodLevel(24);
                player.setAllowFlight(false);
                player.setFlying(false);
                player.teleport(Bukkit.getWorlds().getFirst().getSpawnLocation());
                gm.removeSpectator(player.getUniqueId());

                e.setQuitMessage(colorize(DamiXHunters.getConfigurationManager().getMainConfig().getString("spectator-quit-message", Map.of("player", player.getName()))));
            }
        }else{
            e.setQuitMessage(colorize(DamiXHunters.getConfigurationManager().getMainConfig().getString("global-quit-message", Map.of("player", e.getPlayer().getName()))));
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent e) {
        DamiXHunters.getInstance().getLogger().info(e.getPlayer().getName() + " - "+e.getFrom().getWorld()+" - "+e.getTo() + " - "+e.getCause().name());

        if (gm.getState() == GameState.PLAYING && gm.isRunner(e.getPlayer().getUniqueId()) &&
                e.getFrom().getWorld().getEnvironment() == World.Environment.THE_END && e.getCause() == PlayerTeleportEvent.TeleportCause.UNKNOWN) {
            gm.handleRunnersWin();

        }
    }
}
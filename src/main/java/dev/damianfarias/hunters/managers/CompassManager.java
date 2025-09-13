package dev.damianfarias.hunters.managers;

import dev.damianfarias.hunters.DamiXHunters;
import dev.damianfarias.hunters.configurations.MessagesConfig;
import dev.damianfarias.hunters.model.GameState;
import dev.damianfarias.hunters.utils.DamiUtils;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CompassManager implements Listener {
    private final GameManager gm;
    @Getter
    private final Map<UUID, UUID> trackingTargets = new HashMap<>();
    private int compassTaskID = -1;
    private final ItemStack itemBase;

    public CompassManager(GameManager gm, ItemStack itemBase) {
        this.gm = gm;
        this.itemBase = itemBase;
    }

    public void give(Player player){
        ItemStack item = itemBase.clone();
        item.setAmount(1);
        item.setType(Material.COMPASS);
        player.getInventory().addItem(item);
    }

    private MessagesConfig m(){
        return DamiXHunters.getConfigurationManager().getLangConfig();
    }

    public void startTrackingTask() {
        if (compassTaskID != -1) return;

        compassTaskID = new BukkitRunnable() {
            @Override
            public void run() {
                if (gm.getState() != GameState.PLAYING) {
                    stopTrackingTask();
                    return;
                }

                for (Player hunter : gm.getOnlineHunters()) {
                    UUID targetUUID = trackingTargets.get(hunter.getUniqueId());

                    if (targetUUID == null) {
                        @Nullable Player nearestRunner = getNearestRunner(hunter);
                        if (nearestRunner != null) {
                            trackingTargets.put(hunter.getUniqueId(), nearestRunner.getUniqueId());
                            targetUUID = nearestRunner.getUniqueId();
                            m().send(hunter, "compass-tracking-new-runner", Map.of("runner", nearestRunner.getName()));
                        } else {
                            DamiUtils.sendActionBar(hunter, m().getString("compass-no-runners-found"));
                            continue;
                        }
                    }

                    Player runner = Bukkit.getPlayer(targetUUID);

                    if (runner != null && runner.isOnline()) {
                        updateHunterActionBar(hunter, runner);
                    } else {
                        DamiUtils.sendActionBar(hunter, m().getString("compass-target-offline"));
                        trackingTargets.remove(hunter.getUniqueId());
                    }
                }
            }
        }.runTaskTimer(DamiXHunters.getInstance(), 10L, 10L).getTaskId();
    }

    public void stopTrackingTask() {
        if (compassTaskID != -1) {
            Bukkit.getScheduler().cancelTask(compassTaskID);
            compassTaskID = -1;
            trackingTargets.clear();
        }
    }

    @EventHandler
    public void onPlayerRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (gm.isHunter(player.getUniqueId()) && (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR)) {
            if (player.getInventory().getItemInMainHand().getType() != Material.COMPASS) {
                return;
            }

            @Nullable Player nearestRunner = getNearestRunner(player);

            if (nearestRunner == null) {
                DamiUtils.sendActionBar(player, m().getString("compass-no-runners-found"));
                trackingTargets.remove(player.getUniqueId());
                return;
            }

            if (!trackingTargets.containsKey(player.getUniqueId()) || !trackingTargets.get(player.getUniqueId()).equals(nearestRunner.getUniqueId())) {
                trackingTargets.put(player.getUniqueId(), nearestRunner.getUniqueId());
                m().send(player, "compass-tracking-new-runner", Map.of("runner", nearestRunner.getName()));
            }

            updateHunterCompass(player, nearestRunner);
        }
    }

    private void updateHunterCompass(Player hunter, Player runner) {
        if (hunter.getWorld().equals(runner.getWorld())) {
            ItemStack compass = hunter.getInventory().getItemInMainHand();
            if (compass.getType() == Material.COMPASS) {
                CompassMeta compassMeta = (CompassMeta) compass.getItemMeta();
                if (compassMeta != null) {
                    compassMeta.setLodestoneTracked(false);
                    compassMeta.setLodestone(runner.getLocation());
                    compass.setItemMeta(compassMeta);
                    hunter.getInventory().setItemInMainHand(compass);
                }
            }
        }
    }

    private void updateHunterActionBar(Player hunter, Player runner) {
        Location hunterLocation = hunter.getLocation();
        Location runnerLocation = runner.getLocation();

        if (!hunterLocation.getWorld().equals(runnerLocation.getWorld())) {
            DamiUtils.sendActionBar(hunter, m().getString("compass-target-different-world", Map.of("runner", runner.getName())));
        } else {
            double distance = hunter.getLocation().distance(runner.getLocation());
            String directionArrow = getDirectionArrow(hunter, runner);

            String message = m().getString("compass-tracking-runner",
                    Map.of("runner", runner.getName(), "distance", String.format("%.1f", distance), "direction", directionArrow));
            DamiUtils.sendActionBar(hunter, message);
        }
    }

    private String getDirectionArrow(Player hunter, Player runner) {
        double degrees = getDegrees(hunter, runner);

        if (degrees >= -22.5 && degrees < 22.5) {
            return "⬆";
        } else if (degrees >= 22.5 && degrees < 67.5) {
            return "⬉";
        } else if (degrees >= 67.5 && degrees < 112.5) {
            return "⮕";
        } else if (degrees >= 112.5 && degrees < 157.5) {
            return "⬋";
        } else if (degrees >= 157.5 || degrees < -157.5) {
            return "⬇";
        } else if (degrees >= -157.5 && degrees < -112.5) {
            return "⬊";
        } else if (degrees >= -112.5 && degrees < -67.5) {
            return "⬅";
        } else if (degrees >= -67.5) {
            return "⬈";
        }
        return " ";
    }

    private static double getDegrees(Player hunter, Player runner) {
        Location hunterLoc = hunter.getLocation();
        Location runnerLoc = runner.getLocation();

        double dx = runnerLoc.getX() - hunterLoc.getX();
        double dz = runnerLoc.getZ() - hunterLoc.getZ();

        double angleToRunner = Math.atan2(dx, dz);
        double playerYaw = Math.toRadians(-hunter.getLocation().getYaw());
        double relativeAngle = angleToRunner - playerYaw;

        while (relativeAngle > Math.PI) relativeAngle -= 2 * Math.PI;
        while (relativeAngle < -Math.PI) relativeAngle += 2 * Math.PI;

        return Math.toDegrees(relativeAngle);
    }

    private @Nullable Player getNearestRunner(Player hunter) {
        Player nearestRunner = null;
        double nearestDistanceSquared = Double.MAX_VALUE;

        for (Player runner : gm.getOnlineRunners()) {
            double distanceSquared = hunter.getLocation().distanceSquared(runner.getLocation());
            if (distanceSquared < nearestDistanceSquared) {
                nearestDistanceSquared = distanceSquared;
                nearestRunner = runner;
            }
        }
        return nearestRunner;
    }
}
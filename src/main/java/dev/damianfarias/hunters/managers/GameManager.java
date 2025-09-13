package dev.damianfarias.hunters.managers;

import dev.damianfarias.hunters.DamiXHunters;
import dev.damianfarias.hunters.model.GameState;
import dev.damianfarias.hunters.utils.DamiUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;
import org.mvplugins.multiverse.core.world.MultiverseWorld;
import org.mvplugins.multiverse.core.world.options.CreateWorldOptions;
import org.mvplugins.multiverse.core.world.options.DeleteWorldOptions;

import java.util.*;
import java.util.stream.Collectors;

import static dev.damianfarias.hunters.utils.DamiUtils.colorize;

@SuppressWarnings("deprecation")
public class GameManager {

    private static final ConfigurationManager cm = DamiXHunters.getConfigurationManager();
    @Getter
    private final String worldPrefix = "speedrun";

    @Getter
    private GameState state = GameState.UNSTARTED;

    @Getter
    private final Set<UUID> hunters = new HashSet<>();
    @Getter
    private final Set<UUID> runners = new HashSet<>();
    @Getter
    private final Set<UUID> spectators = new HashSet<>();
    @Getter
    private final Set<Integer> tasksId = new HashSet<>();

    @Nullable @Getter
    private BossBar bossBar;
    @Getter
    private int totalSeconds = 0;
    @Getter
    private int countdownSeconds;
    @Getter @Setter
    private MultiverseWorld overworld, nether, end;

    @Getter
    private final Set<UUID> alreadyPlayed = new HashSet<>(), toRemoveBlindness = new HashSet<>();

    public GameManager() {
    }

    public void loadGameState() {
        if(DamiXHunters.getMvAPI().getWorldManager().getWorld(getWorldPrefix()).getOrNull() != null && DamiXHunters.getMvAPI().getWorldManager().getWorld(getWorldPrefix()).getOrNull().isLoaded()){
            setOverworld(DamiXHunters.getMvAPI().getWorldManager().getWorld(getWorldPrefix()).get());
            setNether(DamiXHunters.getMvAPI().getWorldManager().getWorld(getWorldPrefix()+"_nether").get());
            setEnd(DamiXHunters.getMvAPI().getWorldManager().getWorld(getWorldPrefix()+"_the_end").get());
        }

        String stateString = cm.getDataYaml().getString("state", "UNSTARTED");
        this.state = GameState.valueOf(stateString.toUpperCase());

        this.alreadyPlayed.addAll(cm.getDataYaml().getStringList("alreadyPlayed").stream().map(UUID::fromString).toList());
        this.toRemoveBlindness.addAll(cm.getDataYaml().getStringList("toRemoveBlindness").stream().map(UUID::fromString).toList());
        this.hunters.addAll(cm.getDataYaml().getStringList("hunters").stream().map(UUID::fromString).toList());
        this.runners.addAll(cm.getDataYaml().getStringList("runners").stream().map(UUID::fromString).toList());
        this.totalSeconds = cm.getDataYaml().getInt("speedrun-time", 0);

        if (state == GameState.PLAYING) {
            DamiXHunters.getInstance().getLogger().info("A previous game was found. Attempting to start the game again...");
            createBossBar();
            scheduleCounter();
            DamiXHunters.getCompassManager().startTrackingTask();
        } else {
            cleanUpWorlds();
        }
    }

    public void saveGameState() {
        cm.getDataYaml().set("state", state.name());
        cm.getDataYaml().set("hunters", hunters.stream().map(UUID::toString).toList());
        cm.getDataYaml().set("runners", runners.stream().map(UUID::toString).toList());
        cm.getDataYaml().set("alreadyPlayed", alreadyPlayed.stream().map(UUID::toString).toList());
        cm.getDataYaml().set("toRemoveBlindness", toRemoveBlindness.stream().map(UUID::toString).toList());
        cm.getDataYaml().set("speedrun-time", totalSeconds);
        cm.getDataYaml().save();
    }

    public void addHunter(UUID playerUUID) {
        hunters.add(playerUUID);
        saveGameState();
    }

    public void setState(GameState s) {
        this.state = s;
        saveGameState();
    }

    public void removeHunter(UUID playerUUID) {
        hunters.remove(playerUUID);
        saveGameState();
    }

    public boolean isHunter(UUID playerUUID) {
        return hunters.contains(playerUUID);
    }

    public void addRunner(UUID playerUUID) {
        runners.add(playerUUID);
        saveGameState();
    }

    public void removeRunner(UUID playerUUID) {
        runners.remove(playerUUID);
        saveGameState();
    }

    public boolean isRunner(UUID playerUUID) {
        return runners.contains(playerUUID);
    }

    public void addSpectator(UUID playerUUID) {
        spectators.add(playerUUID);
    }

    public void removeSpectator(UUID playerUUID) {
        spectators.remove(playerUUID);
    }

    public boolean isSpectator(UUID playerUUID) {
        return spectators.contains(playerUUID);
    }

    public Set<Player> getOnlineHunters() {
        return hunters.stream().map(Bukkit::getPlayer).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    public Set<Player> getOnlineRunners() {
        return runners.stream().map(Bukkit::getPlayer).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    public Set<Player> getOnlineSpectators() {
        return spectators.stream().map(Bukkit::getPlayer).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    public Set<Player> getOnlineParticipants() {
        Set<Player> participants = new HashSet<>();
        participants.addAll(getOnlineHunters());
        participants.addAll(getOnlineRunners());
        return participants;
    }

    public void cleanUpWorlds() {
        if(state == GameState.PLAYING) return;
        unloadAndDeleteWorld(overworld, nether, end);
    }

    private void unloadAndDeleteWorld(MultiverseWorld... worlds) {
        for (MultiverseWorld world : worlds) {
            if(world == null) continue;
            if(!world.isLoaded()) continue;
            if(Bukkit.getWorld(world.getName()) != null) {
                for (Player player : Objects.requireNonNull(Bukkit.getWorld(world.getName())).getPlayers()) {
                    player.teleport(Bukkit.getWorlds().getFirst().getSpawnLocation());
                }
            }

            DamiXHunters.getMvAPI().getWorldManager()
                    .deleteWorld(DeleteWorldOptions.world(world))
                    .onFailure(reason -> DamiXHunters.getInstance().getLogger().severe("Could not delete world " + world.getName()+": "+ reason.get()))
                    .onSuccess(w -> DamiXHunters.getInstance().getLogger().info("Successfully deleted world " + w));

        }
        overworld = null;
        nether = null;
        end = null;
        alreadyPlayed.clear();
        totalSeconds = 0;
        countdownSeconds = cm.getMainConfig().getInt("starting-countdown", 20);
        toRemoveBlindness.clear();
    }

    public void startNewGame(Long seed) {
        Bukkit.broadcastMessage(colorize(cm.getLangConfig().getString("starting-phase1")));
        cleanUpWorlds();
        createWorlds(worldPrefix, seed);

        setState(GameState.STARTING);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (overworld != null && nether != null && end != null) {
                    teleportPlayersToWorld();
                    DamiXHunters.getCompassManager().startTrackingTask();
                    setState(GameState.PLAYING);
                    this.cancel();
                }
            }
        }.runTaskTimer(DamiXHunters.getInstance(), 20L, 20L);
    }

    public void createWorlds(String worldPrefix, Long seed) {
        int radius = cm.getMainConfig().getInt("preload-radius", 3);

        if(seed != null) {
            DamiXHunters.getMvAPI().getWorldManager()
                    .createWorld(CreateWorldOptions.worldName(worldPrefix)
                            .environment(World.Environment.NORMAL).seed(seed))
                    .onSuccess(world -> {
                        overworld = world;
                        DamiXHunters.getInstance().getLogger().info("Overworld created!");
                        preloadWorld(world.getBukkitWorld().get(), radius, worldPrefix);
                    });

            DamiXHunters.getMvAPI().getWorldManager()
                    .createWorld(CreateWorldOptions.worldName(worldPrefix + "_nether")
                            .environment(World.Environment.NETHER).seed(seed))
                    .onSuccess(world -> {
                        nether = world;
                        DamiXHunters.getInstance().getLogger().info("Nether created!");
                        preloadWorld(world.getBukkitWorld().get(), radius, worldPrefix + "_nether");
                    });

            DamiXHunters.getMvAPI().getWorldManager()
                    .createWorld(CreateWorldOptions.worldName(worldPrefix + "_the_end")
                            .environment(World.Environment.THE_END).seed(seed))
                    .onSuccess(world -> {
                        end = world;
                        DamiXHunters.getInstance().getLogger().info("End created!");
                        preloadWorld(world.getBukkitWorld().get(), radius, worldPrefix + "_the_end");
                    });
        }else{
            DamiXHunters.getMvAPI().getWorldManager()
                    .createWorld(CreateWorldOptions.worldName(worldPrefix)
                            .environment(World.Environment.NORMAL))
                    .onSuccess(world -> {
                        overworld = world;
                        DamiXHunters.getInstance().getLogger().info("Overworld created!");
                        preloadWorld(world.getBukkitWorld().get(), radius, worldPrefix);
                    });

            DamiXHunters.getMvAPI().getWorldManager()
                    .createWorld(CreateWorldOptions.worldName(worldPrefix + "_nether")
                            .environment(World.Environment.NETHER))
                    .onSuccess(world -> {
                        nether = world;
                        DamiXHunters.getInstance().getLogger().info("Nether created!");
                        preloadWorld(world.getBukkitWorld().get(), radius, worldPrefix + "_nether");
                    });

            DamiXHunters.getMvAPI().getWorldManager()
                    .createWorld(CreateWorldOptions.worldName(worldPrefix + "_the_end")
                            .environment(World.Environment.THE_END))
                    .onSuccess(world -> {
                        end = world;
                        DamiXHunters.getInstance().getLogger().info("End created!");
                        preloadWorld(world.getBukkitWorld().get(), radius, worldPrefix + "_the_end");
                    });
        }
        Bukkit.getScheduler().runTaskLater(DamiXHunters.getInstance(), () -> {
            if (overworld != null && nether != null && end != null) {
                DamiXHunters.getMvNP().addWorldLink(overworld.getName(), nether.getName(), PortalType.NETHER);
                DamiXHunters.getMvNP().addWorldLink(overworld.getName(), end.getName(), PortalType.ENDER);
                DamiXHunters.getInstance().getLogger().info("Portals linked successfully!");
            }
        }, 100L);
    }

    private void preloadWorld(World world, int radius, String logName) {
        new BukkitRunnable() {
            int x = -radius, z = -radius;

            @Override
            public void run() {
                if (x > radius) {
                    DamiXHunters.getInstance().getLogger().info("Finished preloading " + logName);
                    cancel();
                    return;
                }

                world.getChunkAt(x, z).load(true);
                z++;
                if (z > radius) {
                    z = -radius;
                    x++;
                }
            }
        }.runTaskTimer(DamiXHunters.getInstance(), 20L, 1L);
    }

    public void manageJoinPlayer(OfflinePlayer player) {
        if(player.getPlayer() != null && state == GameState.PLAYING){
            managePlayer(player.getPlayer());
        }
    }

    private void managePlayer(Player player) {
        if(!alreadyPlayed.contains(player.getUniqueId())){
            player.setGameMode(GameMode.SURVIVAL);
            player.getInventory().clear();
            player.setHealth(player.getMaxHealth());
            player.getInventory().setArmorContents(null);
            player.setFoodLevel(20);
            player.setSaturation(5.0f);
            player.setExp(0);
            player.setLevel(0);
            for (PotionEffect effect : player.getActivePotionEffects()) {
                player.removePotionEffect(effect.getType());
            }

            if (bossBar != null) {
                bossBar.addPlayer(player);
            }

            player.setAllowFlight(false);
            player.setFlying(false);
            if (isHunter(player.getUniqueId())) {
                DamiXHunters.getCompassManager().give(player);
            }
            alreadyPlayed.add(player.getUniqueId());
            saveGameState();
        }

    }

    private void teleportPlayersToWorld() {
        Location spawnLocation = overworld.getSpawnLocation();
        createBossBar();
        scheduleCounter();
        DamiXHunters.getMvAPI().getSafetyTeleporter().to(spawnLocation.clone().add(0, 1.5, 0))
                .checkSafety(true)
                .teleport(getOnlineParticipants().stream().toList());

        for (Player player : getOnlineParticipants()) {
            managePlayer(player);

            if (isHunter(player.getUniqueId())) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 300000, 4, false, false, false));
                cm.getLangConfig().send(player, "starting-phase2");
            }
        }

        startCountdown();
    }

    private void createBossBar() {
        if (bossBar == null) {
            bossBar = Bukkit.createBossBar(colorize("Time: 00:00:00"), BarColor.GREEN, BarStyle.SOLID);
            bossBar.setVisible(true);
        }
    }

    private void scheduleCounter() {
        totalSeconds = cm.getDataYaml().getInt("speedrun-time", 0);
        getTasksId().add(new BukkitRunnable() {
            @Override
            public void run() {
                totalSeconds++;
                assert bossBar != null;
                bossBar.setTitle(formatTime(totalSeconds));
            }
        }.runTaskTimer(DamiXHunters.getInstance(), 20L, 20L).getTaskId());
    }

    public String formatTime(long totalSeconds) {
        String formatString = cm.getMainConfig().getString("time-format");

        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        return colorize(formatString
                .replace("%H%", String.format("%02d", hours))
                .replace("%M%", String.format("%02d", minutes))
                .replace("%S%", String.format("%02d", seconds))
        );
    }

    public void startCountdown() {
        countdownSeconds = cm.getMainConfig().getInt("starting-countdown", 20);
        for (Player player : getOnlineParticipants()) {
            cm.getLangConfig().send(player, "starting-phase3", Map.of("seconds", String.valueOf(countdownSeconds)));
        }

        tasksId.add(new BukkitRunnable() {
            @Override
            public void run() {
                if (countdownSeconds <= 0) {
                    onCountdownFinished();
                    cancel();
                    return;
                }

                if (countdownSeconds <= 5 || countdownSeconds % 5 == 0) {
                    for (Player player : getOnlineParticipants()) {
                        cm.getLangConfig().send(player, "starting-countdown", Map.of("seconds", String.valueOf(countdownSeconds)));
                        DamiUtils.sendSerializedSound(player, cm.getMainConfig().getString("starting-hunters-sound"), "starting-hunters-sound");
                    }
                }
                countdownSeconds--;
            }
        }.runTaskTimer(DamiXHunters.getInstance(), 0L, 20L).getTaskId());
    }

    private void onCountdownFinished() {
        sendTitleToGroup(getOnlineHunters(), "start-hunters-title", "start-hunters-subtitle", "start-hunters-title-times");

        toRemoveBlindness.addAll(hunters);

        for (Player onlineHunter : getOnlineHunters()) {
            onlineHunter.removePotionEffect(PotionEffectType.BLINDNESS);
            toRemoveBlindness.remove(onlineHunter.getUniqueId());
        }

        for (Player player : getOnlineParticipants()) {
            cm.getLangConfig().send(player, "game-started");
        }
    }

    private void sendTitleToGroup(Set<Player> players, String titleKey, String subtitleKey, String timesKey) {
        String title = cm.getLangConfig().getString(titleKey);
        String subtitle = cm.getLangConfig().getString(subtitleKey);
        String[] times = cm.getLangConfig().getString(timesKey).split(" ");

        try {
            int in = Integer.parseInt(times[0]);
            int stay = Integer.parseInt(times[1]);
            int out = Integer.parseInt(times[2]);

            for (Player player : players) {
                DamiUtils.sendTitle(player, title, subtitle, in, stay, out);
            }
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            DamiXHunters.getInstance().getLogger().warning(timesKey + " has invalid integers or format.");
        }
    }

    public void endGame(boolean shutdown) {
        setState(GameState.UNSTARTED);
        saveGameState();
        cleanUpWorlds();

        DamiXHunters.getInstance().getLogger().info("The game has ended. Cleaning up worlds and tasks.");

        for (Integer taskId : tasksId) {
            Bukkit.getScheduler().cancelTask(taskId);
        }

        tasksId.clear();

        if (bossBar != null) {
            bossBar.removeAll();
            bossBar = null;
        }

        totalSeconds = 0;

        DamiXHunters.getCompassManager().stopTrackingTask();
        for (Player onlineParticipant : getOnlineParticipants()) {
            onlineParticipant.getInventory().clear();
            onlineParticipant.setHealth(onlineParticipant.getMaxHealth());
            onlineParticipant.setFoodLevel(20);
        }

        if(shutdown) Bukkit.shutdown();
    }

    public void checkLives() {
        if (getOnlineRunners().stream().filter(player -> DamiXHunters.getPlayerStateManager().getLives(player) > 0).findFirst().isEmpty()) {
            setState(GameState.ENDING);
            sendTitleToGroup(getOnlineHunters(), "hunters-win-title", "win-subtitle", "hunters-win-title-times");
            sendTitleToGroup(getOnlineRunners(), "hunters-win-title", "lose-subtitle", "hunters-win-title-times");

            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                cm.getLangConfig().send(onlinePlayer, "game-hunters-win");
                DamiUtils.sendSerializedSound(onlinePlayer, cm.getMainConfig().getString("hunters-win-sound"), "hunters-win-sound");
            }

            new BukkitRunnable() {
                @Override
                public void run() {
                    endGame(true);
                }
            }.runTaskLater(DamiXHunters.getInstance(), 20L * 5L);
        }
    }

    public void handleRunnersWin() {
        setState(GameState.ENDING);
        sendTitleToGroup(getOnlineHunters(), "runners-win-title", "win-subtitle", "runners-win-title-times");
        sendTitleToGroup(getOnlineRunners(), "runners-win-title", "lose-subtitle", "runners-win-title-times");

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            cm.getLangConfig().send(onlinePlayer, "game-runners-win");
            cm.getLangConfig().send(onlinePlayer, "game-runners-duration", Map.of("duration", formatTime(totalSeconds)));
            DamiUtils.sendSerializedSound(onlinePlayer, cm.getMainConfig().getString("runners-win-sound"), "runners-win-sound");

        }

        new BukkitRunnable() {
            @Override
            public void run() {
                endGame(true);
            }
        }.runTaskLater(DamiXHunters.getInstance(), 20L * 5L);
    }
}
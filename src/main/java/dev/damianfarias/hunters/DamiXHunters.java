package dev.damianfarias.hunters;

import dev.damianfarias.hunters.commands.MainCommand;
import dev.damianfarias.hunters.listeners.GroupChatListener;
import dev.damianfarias.hunters.listeners.OtherListener;
import dev.damianfarias.hunters.managers.CompassManager;
import dev.damianfarias.hunters.managers.ConfigurationManager;
import dev.damianfarias.hunters.managers.GameManager;
import dev.damianfarias.hunters.managers.PlayerStateManager;
import dev.damianfarias.hunters.model.stats.StatsSavingMethod;
import dev.damianfarias.hunters.model.stats.YamlSavingMethod;
import dev.damianfarias.hunters.utils.ItemSerialization;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.mvplugins.multiverse.core.MultiverseCoreApi;
import org.mvplugins.multiverse.netherportals.MultiverseNetherPortals;

import java.util.Objects;

public final class DamiXHunters extends JavaPlugin {

    @Getter
    private static DamiXHunters instance;
    @Getter
    private static ConfigurationManager configurationManager;
    @Getter
    private static GameManager gameManager;
    @Getter
    private static PlayerStateManager playerStateManager;
    @Getter
    private static CompassManager compassManager;
    @Getter
    private static MultiverseCoreApi mvAPI;
    @Getter
    private static MultiverseNetherPortals mvNP;
    @Getter
    private static StatsSavingMethod statsSavingMethod;

    @Override
    public void onEnable() {
        instance = this;
        mvAPI = MultiverseCoreApi.get();
        mvNP = (MultiverseNetherPortals) Bukkit.getPluginManager().getPlugin("Multiverse-NetherPortals");
        configurationManager = new ConfigurationManager(this);


        if(configurationManager.getMainConfig().getString("stats-saving-method").equalsIgnoreCase("YAML")){
            statsSavingMethod = new YamlSavingMethod(configurationManager.getStatsYaml(), this);
        }else{
            statsSavingMethod = new YamlSavingMethod(configurationManager.getStatsYaml(), this);
        }

        gameManager = new GameManager();
        ItemStack compassItem = ItemSerialization.deserializeItemStack(configurationManager.getMainConfig().getSection("compass-item"));
        compassManager = new CompassManager(gameManager, compassItem);
        playerStateManager = new PlayerStateManager();

        gameManager.loadGameState();

        Objects.requireNonNull(getCommand("hunters")).setExecutor(new MainCommand());
        Objects.requireNonNull(getCommand("hunters")).setTabCompleter(new MainCommand());

        Bukkit.getPluginManager().registerEvents(new GroupChatListener(), this);
        Bukkit.getPluginManager().registerEvents(new OtherListener(), this);
        Bukkit.getPluginManager().registerEvents(compassManager, this);

        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null){
            getLogger().warning("PlaceholderAPI is not installed, is recommended to install plugin expansion.");
        }else{
            new HuntersExpansion().register();
        }
    }

    @Override
    public void onDisable() {
        for (Integer taskId : gameManager.getTasksId()) {
            Bukkit.getScheduler().cancelTask(taskId);
        }

        gameManager.getTasksId().clear();
        gameManager.saveGameState();

        if (gameManager.getBossBar() != null) {
            gameManager.getBossBar().removeAll();
        }

        DamiXHunters.getCompassManager().stopTrackingTask();
        gameManager.cleanUpWorlds();
        instance = null;
    }
}
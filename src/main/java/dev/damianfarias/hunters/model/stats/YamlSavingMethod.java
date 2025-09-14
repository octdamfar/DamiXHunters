package dev.damianfarias.hunters.model.stats;

import dev.damianfarias.hunters.model.YamlConfig;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

public class YamlSavingMethod implements StatsSavingMethod{

    private final YamlConfig fileConfig;

    public YamlSavingMethod(YamlConfig fileConfig) {
        this.fileConfig = fileConfig;
    }

    @Override
    public UserStats getStats(OfflinePlayer player) {
        UUID uuid = player.getUniqueId();
        if(fileConfig.getConfig().isConfigurationSection(uuid.toString())){
            UserStats us = new UserStats(
                    uuid, player.getName()
            );

            us.setHunterWins(fileConfig.getInt(uuid+".hunterWins"));
            us.setRunnerWins(fileConfig.getInt(uuid+".runnerWins"));
            us.setHunterKills(fileConfig.getInt(uuid+".hunterKills"));
            us.setRunnerKills(fileConfig.getInt(uuid+".runnerKills"));
            us.setHunterPlayed(fileConfig.getInt(uuid+".hunterPlayed"));
            us.setRunnerPlayed(fileConfig.getInt(uuid+".runnerPlayed"));
            us.setSteak(fileConfig.getInt(uuid+".steak"));
            us.setMaxSteak(fileConfig.getInt(uuid+".maxSteak"));
            return us;
        }
        return null;
    }

    @Override
    public void save(UserStats user) {
        fileConfig.set(user.getId().toString()+".hunterWins", user.getHunterWins());
        fileConfig.set(user.getId().toString()+".runnersWins", user.getRunnerWins());
        fileConfig.set(user.getId().toString()+".hunterKills", user.getHunterKills());
        fileConfig.set(user.getId().toString()+".runnerKills", user.getRunnerKills());
        fileConfig.set(user.getId().toString()+".hunterPlayed", user.getHunterPlayed());
        fileConfig.set(user.getId().toString()+".runnerPlayed", user.getRunnerPlayed());
        fileConfig.set(user.getId().toString()+".steak", user.getSteak());
        fileConfig.set(user.getId().toString()+".maxSteak", user.getMaxSteak());
        fileConfig.save();
    }

    @Override
    public void create(UserStats user) {
        save(user);
    }
}

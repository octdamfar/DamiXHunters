package dev.damianfarias.hunters.model.stats;

import org.bukkit.OfflinePlayer;

import java.util.UUID;

public interface StatsSavingMethod {
    UserStats getStats(OfflinePlayer uuid);
    void save(UserStats user);
    void create(UserStats user);
}

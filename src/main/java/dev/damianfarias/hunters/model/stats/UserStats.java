package dev.damianfarias.hunters.model.stats;

import dev.damianfarias.hunters.DamiXHunters;
import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
public class UserStats {
    private static final Map<String, UserStats> cache = new HashMap<>();
    private final UUID id;
    private final String name;

    private int runnerWins = 0, hunterWins = 0, runnerKills = 0, hunterKills = 0, runnerPlayed = 0, hunterPlayed = 0, steak = 0, maxSteak = 0;

    public UserStats(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    public OfflinePlayer getAsOffline(){
        return Bukkit.getOfflinePlayer(id);
    }

    @Nullable
    public Player getAsOnline(){
        return Bukkit.getPlayer(id);
    }

    @Nullable
    public static UserStats get(OfflinePlayer offlinePlayer){
        if(!offlinePlayer.hasPlayedBefore()) return null;
        if(cache.containsKey(offlinePlayer.getUniqueId().toString())){
            return cache.get(offlinePlayer.getUniqueId().toString());
        }else{
            UserStats us = DamiXHunters.getStatsSavingMethod().getStats(offlinePlayer);
            cache.put(offlinePlayer.getUniqueId().toString(), us);
            return us;
        }
    }
}

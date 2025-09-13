package dev.damianfarias.hunters;

import dev.damianfarias.hunters.managers.GameManager;
import dev.damianfarias.hunters.model.GameState;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HuntersExpansion extends PlaceholderExpansion {
    @Override
    public @NotNull String getIdentifier() {
        return "damixhunters";
    }

    @Override
    public @NotNull String getAuthor() {
        return "DamianFarias";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        GameManager gm = DamiXHunters.getGameManager();
        String cmd = params.split("_")[0];

        // GAME

        if(cmd.equalsIgnoreCase("state")){
            return gm.getState().name();
        } else if(cmd.equalsIgnoreCase("speedrun_time_seconds")){
            return gm.getState() == GameState.UNSTARTED ? "NOT STARTED" : String.valueOf(gm.getTotalSeconds());
        } else if(cmd.equalsIgnoreCase("speedrun_time_formatted")){
            return gm.getState() == GameState.UNSTARTED ? "NOT STARTED" : gm.formatTime(gm.getTotalSeconds());

        // HUNTERS

        } else if(cmd.equalsIgnoreCase("total_hunters_count")){
            return String.valueOf(gm.getHunters().size());
        } else if(cmd.equalsIgnoreCase("total_hunters_names")){
            return String.join(", ", gm.getHunters().stream().map(Bukkit::getOfflinePlayer).map(OfflinePlayer::getName).toList());
        } else if(cmd.equalsIgnoreCase("online_hunters_count")){
            return String.valueOf(gm.getOnlineHunters().size());
        } else if(cmd.equalsIgnoreCase("online_hunters_names")){
            return String.join(", ", gm.getOnlineHunters().stream().map(Player::getName).toList());

        // RUNNERS

        }  else if(cmd.equalsIgnoreCase("total_runners_count")){
            return String.valueOf(gm.getRunners().size());
        } else if(cmd.equalsIgnoreCase("total_runners_names")){
            return String.join(", ", gm.getRunners().stream().map(Bukkit::getOfflinePlayer).map(OfflinePlayer::getName).toList());
        } else if(cmd.equalsIgnoreCase("online_runners_count")){
            return String.valueOf(gm.getOnlineRunners().size());
        } else if(cmd.equalsIgnoreCase("online_runners_names")){
            return String.join(", ", gm.getOnlineRunners().stream().map(Player::getName).toList());
        }

        // LIVES
        else if(cmd.equalsIgnoreCase("runner_lives")){
            try {
                String name = params.split("_")[1];
                if(gm.getState() == GameState.UNSTARTED) return "NOT STARTED";

                if(DamiXHunters.getPlayerStateManager().getLives().containsKey(Bukkit.getOfflinePlayer(name).getUniqueId())){
                    return String.valueOf(DamiXHunters.getPlayerStateManager().getLives().get(Bukkit.getOfflinePlayer(name).getUniqueId()));
                }
                return "NOT RUNNER FOUND";
            }catch (IndexOutOfBoundsException e){
                return "PLAYER NAME?";
            }
        }

        return "%damixhunters_"+params+"%";
    }
}

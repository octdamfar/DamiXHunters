package dev.damianfarias.hunters;

import dev.damianfarias.hunters.managers.GameManager;
import dev.damianfarias.hunters.model.GameState;
import dev.damianfarias.hunters.model.stats.UserStats;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

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
        String[] args = params.split("_");
        GameManager gm = DamiXHunters.getGameManager();

        // GAME
        if (params.startsWith("state")) {
            return gm.getState().name();
        } else if (params.startsWith("speedrun_time_seconds")) {
            return gm.getState() == GameState.UNSTARTED ? "NOT STARTED" : String.valueOf(gm.getTotalSeconds());
        } else if (params.startsWith("speedrun_time_formatted")) {
            return gm.getState() == GameState.UNSTARTED ? "NOT STARTED" : gm.formatTime(gm.getTotalSeconds());

            // HUNTERS
        } else if (params.startsWith("total_hunters_count")) {
            return String.valueOf(gm.getHunters().size());
        } else if (params.startsWith("total_hunters_names")) {
            return gm.getHunters().stream()
                    .map(Bukkit::getOfflinePlayer)
                    .map(OfflinePlayer::getName)
                    .collect(Collectors.joining(", "));
        } else if (params.startsWith("online_hunters_count")) {
            return String.valueOf(gm.getOnlineHunters().size());
        } else if (params.startsWith("online_hunters_names")) {
            return gm.getOnlineHunters().stream()
                    .map(Player::getName)
                    .collect(Collectors.joining(", "));

            // RUNNERS
        } else if (params.startsWith("total_runners_count")) {
            return String.valueOf(gm.getRunners().size());
        } else if (params.startsWith("total_runners_names")) {
            return gm.getRunners().stream()
                    .map(Bukkit::getOfflinePlayer)
                    .map(OfflinePlayer::getName)
                    .collect(Collectors.joining(", "));
        } else if (params.startsWith("online_runners_count")) {
            return String.valueOf(gm.getOnlineRunners().size());
        } else if (params.startsWith("online_runners_names")) {
            return gm.getOnlineRunners().stream()
                    .map(Player::getName)
                    .collect(Collectors.joining(", "));

            // LIVES
        } else if (params.startsWith("runner_lives")) {
            if (gm.getState() != GameState.PLAYING) {
                return "NOT STARTED";
            }
            OfflinePlayer targetPlayer = args.length > 2 ? Bukkit.getOfflinePlayer(args[2]) : player;
            UUID targetId = targetPlayer.getUniqueId();

            if (DamiXHunters.getPlayerStateManager().getLives().containsKey(targetId)) {
                return String.valueOf(DamiXHunters.getPlayerStateManager().getLives().get(targetId));
            }
            return "NOT RUNNER FOUND";
            // STATS
        } else if (params.startsWith("stats_")) {
            if (args.length < 2) return null;

            String playerName = null;
            String statType;

            if (args.length >= 3) {
                playerName = args[args.length - 1];
                statType = String.join("_", Arrays.copyOfRange(args, 1, args.length - 1));
            } else {
                statType = args[1];
            }

            OfflinePlayer targetPlayer = (playerName != null) ? Bukkit.getOfflinePlayer(playerName) : player;

            UserStats us = UserStats.get(targetPlayer);
            if (us == null) {
                return "NOT PLAYER DATA";
            }

            switch (statType.toLowerCase()) {
                case "hunter_kills":
                    return String.valueOf(us.getHunterKills());
                case "hunter_wins":
                    return String.valueOf(us.getHunterWins());
                case "hunter_played":
                    return String.valueOf(us.getHunterPlayed());
                case "runner_kills":
                    return String.valueOf(us.getRunnerKills());
                case "runner_wins":
                    return String.valueOf(us.getRunnerWins());
                case "runner_played":
                    return String.valueOf(us.getRunnerPlayed());
                case "steak":
                    return String.valueOf(us.getSteak());
                case "max_steak":
                    return String.valueOf(us.getMaxSteak());
            }
        }

        return null;
    }
}
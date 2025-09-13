package dev.damianfarias.hunters.commands.subcommands;

import dev.damianfarias.hunters.DamiXHunters;
import dev.damianfarias.hunters.commands.SubCommand;
import dev.damianfarias.hunters.managers.GameManager;
import dev.damianfarias.hunters.model.GameState;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class RemoveRunner implements SubCommand {
    @Override
    public String getName() {
        return "removerunner";
    }

    @Override
    public boolean requierePermission() {
        return true;
    }

    @Override
    public void execute(CommandSender s, String[] args, String cmdName) {
        if(args.length == 0){
            m().send(s, "removerunner-usage", Map.of("label", cmdName));
            return;
        }

        GameManager gm = DamiXHunters.getGameManager();

        if(gm.getState() != GameState.UNSTARTED) {
            m().send(s, "actually-playing");
            return;
        }

        OfflinePlayer p = Bukkit.getOfflinePlayer(args[0]);

        if(!p.hasPlayedBefore()){
            m().send(s, "unknown-player");
            return;
        }

        if(!gm.isRunner(p.getUniqueId())){
            m().send(s, "removerunner-not-runner", Map.of("player", args[0]));
            return;
        }

        DamiXHunters.getPlayerStateManager().removeRunner(p.getUniqueId());
        m().send(s, "removerunner-successfully", Map.of("player", args[0]));
    }

    @Override
    public List<String> tabComplete(CommandSender s, String[] args) {
        return args.length == 1 ? Arrays.stream(Bukkit.getOfflinePlayers())
                .filter(player -> DamiXHunters.getGameManager().getRunners().contains(player.getUniqueId()))
                .map(OfflinePlayer::getName).toList() : List.of();
    }
}

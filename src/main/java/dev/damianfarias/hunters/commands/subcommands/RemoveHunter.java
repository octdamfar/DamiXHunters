package dev.damianfarias.hunters.commands.subcommands;

import dev.damianfarias.hunters.DamiXHunters;
import dev.damianfarias.hunters.commands.SubCommand;
import dev.damianfarias.hunters.managers.GameManager;
import dev.damianfarias.hunters.model.GameState;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class RemoveHunter implements SubCommand {

    @Override
    public String getName() {
        return "removehunter";
    }

    @Override
    public boolean requierePermission() {
        return true;
    }

    @Override
    public void execute(CommandSender s, String[] args, String cmdName) {
        if(args.length == 0){
            m().send(s, "removehunter-usage", Map.of("label", cmdName));
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

        if(!gm.isHunter(p.getUniqueId())){
            m().send(s, "removehunter-not-hunter", Map.of("player", args[0]));
            return;
        }

        gm.removeHunter(p.getUniqueId());
        m().send(s, "removehunter-successfully", Map.of("player", args[0]));
    }

    @Override
    public List<String> tabComplete(CommandSender s, String[] args) {
        return args.length == 1 ? Arrays.stream(Bukkit.getOfflinePlayers())
                .filter(player -> DamiXHunters.getGameManager().getHunters().contains(player.getUniqueId()))
                .map(OfflinePlayer::getName).toList() : List.of();
    }
}

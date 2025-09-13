package dev.damianfarias.hunters.commands.subcommands;

import dev.damianfarias.hunters.DamiXHunters;
import dev.damianfarias.hunters.commands.SubCommand;
import dev.damianfarias.hunters.managers.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class SetLives implements SubCommand {
    @Override
    public String getName() {
        return "setlives";
    }

    @Override
    public boolean requierePermission() {
        return true;
    }

    @Override
    public void execute(CommandSender s, String[] args, String cmdName) {
        if(args.length < 2){
            m().send(s, "setlives-usage", Map.of("label", cmdName));
            return;
        }

        GameManager gm = DamiXHunters.getGameManager();

        OfflinePlayer p = Bukkit.getOfflinePlayer(args[0]);

        if(!p.hasPlayedBefore()){
            m().send(s, "unknown-player");
            return;
        }

        if(!gm.isRunner(p.getUniqueId())){
            m().send(s, "setlives-not-runner", Map.of("player", args[0]));
            return;
        }

        int value;
        try{
            value = Integer.parseInt(args[1]);
        }catch (NumberFormatException e){
            m().send(s, "setlives-invalid", Map.of("input", args[1]));
            return;
        }
        if(value < 1){
            m().send(s, "setlives-invalid", Map.of("input", args[1]));
            return;
        }

        DamiXHunters.getPlayerStateManager().setLives(p.getUniqueId(), value);
        m().send(s, "setlives-successfully", Map.of("player", args[0], "lives", value+""));
    }

    @Override
    public List<String> tabComplete(CommandSender s, String[] args) {
        return args.length == 1 ? Arrays.stream(Bukkit.getOfflinePlayers())
                .filter(player -> DamiXHunters.getGameManager().getRunners().contains(player.getUniqueId()))
                .map(OfflinePlayer::getName).toList() : (args.length == 2 ? List.of("1", "2", "3", "4", "5") : List.of());
    }
}

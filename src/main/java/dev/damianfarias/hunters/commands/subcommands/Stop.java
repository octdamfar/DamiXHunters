package dev.damianfarias.hunters.commands.subcommands;

import dev.damianfarias.hunters.DamiXHunters;
import dev.damianfarias.hunters.commands.SubCommand;
import dev.damianfarias.hunters.managers.GameManager;
import dev.damianfarias.hunters.model.GameState;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

import static dev.damianfarias.hunters.utils.DamiUtils.colorize;

public class Stop implements SubCommand {
    @Override
    public String getName() {
        return "stop";
    }

    @Override
    public boolean requierePermission() {
        return true;
    }

    @Override
    public void execute(CommandSender s, String[] args, String cmdName) {
        GameManager gm = DamiXHunters.getGameManager();

        if(gm.getState() != GameState.PLAYING){
            m().send(s, "stop-not-started");
            return;
        }

        gm.endGame(false);
        m().send(s, "stop-successfully");
        Bukkit.broadcastMessage(colorize(m().getString("stop-broadcast", Map.of("player", s.getName()))));
    }

    @Override
    public List<String> tabComplete(CommandSender s, String[] args) {
        return List.of();
    }
}

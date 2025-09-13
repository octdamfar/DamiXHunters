package dev.damianfarias.hunters.commands;

import dev.damianfarias.hunters.DamiXHunters;
import dev.damianfarias.hunters.configurations.MessagesConfig;
import org.bukkit.command.CommandSender;

import java.util.List;

public interface SubCommand {
    String getName();
    boolean requierePermission();
    void execute(CommandSender s, String[] args, String cmdName);
    List<String> tabComplete(CommandSender s, String[] args);
    default MessagesConfig m(){
        return DamiXHunters.getConfigurationManager().getLangConfig();
    }
}

package dev.damianfarias.hunters.commands;

import dev.damianfarias.hunters.DamiXHunters;
import dev.damianfarias.hunters.commands.subcommands.*;
import dev.damianfarias.hunters.commands.subcommands.*;
import dev.damianfarias.hunters.configurations.MessagesConfig;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static dev.damianfarias.hunters.utils.DamiUtils.filterSuggestions;

public class MainCommand implements TabExecutor {

    private final Map<String, SubCommand> commands = new HashMap<>();
    private final MessagesConfig m = DamiXHunters.getConfigurationManager().getLangConfig();

    public MainCommand() {
        registerCommand(new AddHunter());
        registerCommand(new AddRunner());
        registerCommand(new RemoveHunter());
        registerCommand(new RemoveRunner());
        registerCommand(new SetLives());
        registerCommand(new Start());
        registerCommand(new Stop());
    }

    public void registerCommand(SubCommand subCommand) {
        commands.put(subCommand.getName().toLowerCase(), subCommand);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
        if(args.length == 0){
            m.sendList(sender, "command-help", Map.of("label", s.split(" ")[0]));
            return false;
        }

        String sub = args[0].toLowerCase();
        SubCommand subCommand = commands.get(sub);

        if(subCommand == null) {
            m.sendList(sender, "command-help", Map.of("label", s.split(" ")[0]));
            return false;
        }

        if(subCommand.requierePermission() && !sender.hasPermission("damixhunters.command." + subCommand.getName().toLowerCase())){
            m.send(sender, "no-permission");
            return false;
        }

        subCommand.execute(sender, Arrays.copyOfRange(args, 1, args.length), s.split(" ")[0]);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
        if(args.length == 1){
            List<String> validCommands = commands.values().stream()
                    .filter(subCommand -> !subCommand.requierePermission() || sender.hasPermission("damixhunters.command." + subCommand.getName().toLowerCase()))
                    .map(SubCommand::getName)
                    .collect(Collectors.toList());

            return filterSuggestions(validCommands, args[0]);

        } else if(args.length > 1){
            String sub = args[0].toLowerCase();
            SubCommand subCommand = commands.get(sub);

            if(subCommand == null){
                return List.of();
            }

            if(subCommand.requierePermission() && !sender.hasPermission("damixhunters.command." + subCommand.getName().toLowerCase())){
                return List.of();
            }

            return filterSuggestions(subCommand.tabComplete(sender, Arrays.copyOfRange(args, 1, args.length)), args[args.length - 1]);
        }

        return List.of();
    }
}
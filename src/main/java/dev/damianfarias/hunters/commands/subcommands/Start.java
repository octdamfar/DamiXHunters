package dev.damianfarias.hunters.commands.subcommands;

import dev.damianfarias.hunters.DamiXHunters;
import dev.damianfarias.hunters.commands.SubCommand;
import dev.damianfarias.hunters.managers.GameManager;
import dev.damianfarias.hunters.model.GameState;
import org.bukkit.command.CommandSender;

import java.util.List;

public class Start implements SubCommand {
    @Override
    public String getName() {
        return "start";
    }

    @Override
    public boolean requierePermission() {
        return true;
    }

    @Override
    public void execute(CommandSender s, String[] args, String cmdName) {
        GameManager gm = DamiXHunters.getGameManager();
        if(gm.getState() != GameState.UNSTARTED){
            m().send(s, "start-already-started");
            return;
        }

        if(gm.getHunters().isEmpty()){
            m().send(s, "start-no-hunters");
            return;
        }

        if(gm.getRunners().isEmpty()){
            m().send(s, "start-no-runners");
            return;
        }

        Long seed = null;
        if(args.length > 0){
            try{
                seed = Long.parseLong(args[0]);
            }catch (NumberFormatException e){
                m().send(s, "start-invalid-seed");
                return;
            }

        }

        gm.startNewGame(seed);
        gm.setState(GameState.STARTING);
    }

    @Override
    public List<String> tabComplete(CommandSender s, String[] args) {
        return List.of();
    }
}

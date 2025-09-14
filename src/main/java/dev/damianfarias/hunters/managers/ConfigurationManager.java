package dev.damianfarias.hunters.managers;

import dev.damianfarias.hunters.DamiXHunters;
import dev.damianfarias.hunters.configurations.MainConfig;
import dev.damianfarias.hunters.configurations.MessagesConfig;
import dev.damianfarias.hunters.model.YamlConfig;
import lombok.Getter;

@Getter
public class ConfigurationManager {

    private final YamlConfig mainYaml;
    private final YamlConfig langYaml;
    private final YamlConfig dataYaml;
    private final YamlConfig guiYaml;
    private final YamlConfig statsYaml;

    private final MainConfig mainConfig;
    private final MessagesConfig langConfig;

    public ConfigurationManager(DamiXHunters plugin) {
        this.mainYaml = new YamlConfig(plugin, "config.yml");
        this.langYaml = new YamlConfig(plugin, "lang.yml");
        this.dataYaml = new YamlConfig(plugin, "data.yml");
        this.guiYaml = new YamlConfig(plugin, "gui.yml");
        this.statsYaml = new YamlConfig(plugin, "stats.yml");

        mainYaml.register();
        langYaml.register();
        dataYaml.register();
        guiYaml.register();

        langConfig = new MessagesConfig(langYaml);
        mainConfig = new MainConfig(mainYaml);
    }
}
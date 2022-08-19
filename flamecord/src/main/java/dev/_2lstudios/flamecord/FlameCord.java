package dev._2lstudios.flamecord;

import java.util.Collection;
import java.util.logging.Logger;

import dev._2lstudios.antibot.AddressDataManager;
import dev._2lstudios.antibot.CheckManager;
import dev._2lstudios.flamecord.configuration.FlameCordConfiguration;
import dev._2lstudios.flamecord.configuration.MessagesConfiguration;
import dev._2lstudios.flamecord.configuration.ModulesConfiguration;
import lombok.Getter;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class FlameCord {
    @Getter
    private static FlameCord instance;

    public static void reload(final Logger logger, final Collection<String> whitelistedAddresses) {
        if (FlameCord.instance != null) {
            instance.reload(logger);
        } else {
            FlameCord.instance = new FlameCord(logger, whitelistedAddresses);
        }
    }

    @Getter
    private FlameCordConfiguration flameCordConfiguration;
    @Getter
    private AddressDataManager addressDataManager;
    @Getter
    private CheckManager checkManager;
    @Getter
    private ModulesConfiguration modulesConfiguration;
    @Getter
    private MessagesConfiguration messagesConfiguration;

    private void reload(final Logger logger) {
        final ConfigurationProvider configurationProvider = ConfigurationProvider.getProvider(YamlConfiguration.class);
        
        if (checkManager != null) checkManager.unload();

        this.flameCordConfiguration = new FlameCordConfiguration(configurationProvider);
        this.modulesConfiguration = new ModulesConfiguration(configurationProvider);
        this.messagesConfiguration = new MessagesConfiguration(logger, configurationProvider);
        this.addressDataManager = new AddressDataManager();
        this.checkManager = new CheckManager(addressDataManager, flameCordConfiguration);
    }

    private FlameCord(final Logger logger, final Collection<String> whitelistedAddresses) {
        reload(logger);
    }
}
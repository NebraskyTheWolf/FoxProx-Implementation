package dev._2lstudios.flamecord.configuration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;

public class FlameCordConfiguration extends FlameConfig {
    // FlameCord - Allow Invalid Names
    @Getter
    private boolean allowInvalidNames = false;

    // FlameCord start - Antibot System
    @Getter
    private boolean antibotAccountsEnabled = true;
    @Getter
    private boolean antibotAccountsFirewall = true;
    @Getter
    private int antibotAccountsLimit = 3;
    @Getter
    private boolean antibotAccountsLog = true;
    @Getter
    private boolean antibotCountryEnabled = true;
    @Getter
    private boolean antibotCountryFirewall = true;
    @Getter
    private Collection<String> antibotCountryBlacklist = Arrays.asList("CN", "HK", "RU", "IN", "TH", "ID", "DZ", "VN", "IR", "PK");
    @Getter
    private Collection<String> antibotFirewalledExceptions = Arrays.asList("BadPacketException", "QuietException", "IllegalStateConfig", "FastException");
    @Getter
    private boolean antibotCountryLog = true;
    @Getter
    private boolean antibotFastChatEnabled = true;
    @Getter
    private boolean antibotFastChatFirewall = true;
    @Getter
    private int antibotFastChatTime = 1000;
    @Getter
    private boolean antibotFastChatLog = true;
    @Getter
    private boolean antibotFirewallEnabled = true;
    @Getter
    private boolean antibotFirewallIpset = true;
    @Getter
    private int antibotFirewallExpire = 60;
    @Getter
    private boolean antibotFirewallLog = true;
    @Getter
    private boolean antibotNicknameEnabled = true;
    @Getter
    private boolean antibotNicknameFirewall = true;
    @Getter
    private Collection<String> antibotNicknameBlacklist = Arrays.asList("mcstorm", "mcdown", "mcbot", "theresa_bot", "dropbot", "kingbot");
    @Getter
    private boolean antibotNicknameLog = true;
    @Getter
    private boolean antibotPasswordEnabled = true;
    @Getter
    private boolean antibotPasswordFirewall = true;
    @Getter
    private int antibotPasswordLimit = 3;
    @Getter
    private boolean antibotPasswordLog = true;
    @Getter
    private boolean antibotRatelimitEnabled = true;
    @Getter
    private boolean antibotRatelimitFirewall = true;
    @Getter
    private int antibotRatelimitConnectionsPerSecond = 3;
    @Getter
    private int antibotRatelimitPingsPerSecond = 8;
    @Getter
    private boolean antibotRatelimitLog = true;
    @Getter
    private boolean antibotReconnectEnabled = true;
    @Getter
    private int antibotReconnectAttempts = 2;
    @Getter
    private int antibotReconnectPings = 0;
    @Getter
    private int antibotReconnectMaxTime = 10000;
    @Getter
    private int antibotReconnectConnectionThreshold = 1;
    @Getter
    private int antibotReconnectConnectionThresholdLimit = 8000;
    @Getter
    private boolean antibotReconnectLog = true;

    public void loadAntibot(final Configuration config) {
        this.antibotAccountsEnabled = setIfUnexistant("antibot.accounts.enabled", this.antibotAccountsEnabled, config);
        this.antibotAccountsFirewall = setIfUnexistant("antibot.accounts.firewall", this.antibotAccountsFirewall, config);
        this.antibotAccountsLimit = setIfUnexistant("antibot.accounts.limit", this.antibotAccountsLimit, config);
        this.antibotAccountsLog = setIfUnexistant("antibot.accounts.log", this.antibotAccountsLog, config);
        this.antibotCountryEnabled = setIfUnexistant("antibot.country.enabled", this.antibotCountryEnabled, config);
        this.antibotCountryFirewall = setIfUnexistant("antibot.country.firewall", this.antibotCountryFirewall, config);
        this.antibotCountryBlacklist = setIfUnexistant("antibot.country.blacklist", this.antibotCountryBlacklist, config);
        this.antibotCountryLog = setIfUnexistant("antibot.country.log", this.antibotCountryLog, config);
        this.antibotFastChatEnabled = setIfUnexistant("antibot.fastchat.enabled", this.antibotFastChatEnabled, config);
        this.antibotFastChatFirewall = setIfUnexistant("antibot.fastchat.firewall", this.antibotFastChatFirewall, config);
        this.antibotFastChatTime = setIfUnexistant("antibot.fastchat.time", this.antibotFastChatTime, config);
        this.antibotFastChatLog = setIfUnexistant("antibot.fastchat.log", this.antibotFastChatLog, config);
        this.antibotFirewallEnabled = setIfUnexistant("antibot.firewall.enabled", this.antibotFirewallEnabled, config);
        this.antibotFirewalledExceptions = setIfUnexistant("antibot.firewall.exceptions", this.antibotFirewalledExceptions, config);
        this.antibotFirewallIpset = setIfUnexistant("antibot.firewall.ipset", this.antibotFirewallIpset, config);
        this.antibotFirewallExpire = setIfUnexistant("antibot.firewall.time", this.antibotFirewallExpire, config);
        this.antibotFirewallLog = setIfUnexistant("antibot.firewall.log", this.antibotFirewallLog, config);
        this.antibotNicknameEnabled = setIfUnexistant("antibot.nickname.enabled", this.antibotNicknameEnabled, config);
        this.antibotNicknameFirewall = setIfUnexistant("antibot.nickname.firewall", this.antibotNicknameFirewall, config);
        this.antibotNicknameBlacklist = setIfUnexistant("antibot.nickname.blacklist", this.antibotNicknameBlacklist, config);
        this.antibotNicknameLog = setIfUnexistant("antibot.nickname.log", this.antibotNicknameLog, config);
        this.antibotPasswordEnabled = setIfUnexistant("antibot.password.enabled", this.antibotPasswordEnabled, config);
        this.antibotPasswordFirewall = setIfUnexistant("antibot.password.firewall", this.antibotPasswordFirewall, config);
        this.antibotPasswordLimit = setIfUnexistant("antibot.password.limit", this.antibotPasswordLimit, config);
        this.antibotPasswordLog = setIfUnexistant("antibot.password.log", this.antibotPasswordLog, config);
        this.antibotRatelimitEnabled = setIfUnexistant("antibot.ratelimit.enabled", this.antibotRatelimitEnabled, config);
        this.antibotRatelimitFirewall = setIfUnexistant("antibot.ratelimit.firewall", this.antibotRatelimitFirewall, config);
        this.antibotRatelimitConnectionsPerSecond = setIfUnexistant("antibot.ratelimit.connections-per-second", this.antibotRatelimitConnectionsPerSecond, config);
        this.antibotRatelimitPingsPerSecond = setIfUnexistant("antibot.ratelimit.pings-per-second", this.antibotRatelimitPingsPerSecond, config);
        this.antibotRatelimitLog = setIfUnexistant("antibot.ratelimit.log", this.antibotRatelimitLog, config);
        this.antibotReconnectEnabled = setIfUnexistant("antibot.reconnect.enabled", this.antibotReconnectEnabled, config);
        this.antibotReconnectAttempts = setIfUnexistant("antibot.reconnect.attempts", this.antibotReconnectAttempts, config);
        this.antibotReconnectPings = setIfUnexistant("antibot.reconnect.pings", this.antibotReconnectPings, config);
        this.antibotReconnectMaxTime = setIfUnexistant("antibot.reconnect.max-time", this.antibotReconnectMaxTime, config);
        this.antibotReconnectConnectionThreshold = setIfUnexistant("antibot.reconnect.connection-threshold", this.antibotReconnectConnectionThreshold, config);
        this.antibotReconnectConnectionThresholdLimit = setIfUnexistant("antibot.reconnect.connection-threshold-limit", this.antibotReconnectConnectionThresholdLimit, config);
        this.antibotReconnectLog = setIfUnexistant("antibot.reconnect.log", this.antibotReconnectLog, config);
    }
    // FlameCord end - Antibot System

    // FlameCord - TCP Fast Open
    @Getter
    private int tcpFastOpen = 3;

    private List<String> colors(final List<String> strings) {
        for (int i = 0; i < strings.size(); i++) {
            strings.set(i, ChatColor.translateAlternateColorCodes('&', strings.get(i)));
        }

        return strings;
    }

    public String getMOTD(final int maxPlayers, final int onlinePlayers) {
        final String motd = motds.get(new Random().nextInt(motds.size()));

        return motd.replace("%maxplayers%", String.valueOf(maxPlayers)).replace("%onlineplayers%", String.valueOf(onlinePlayers));
    }

    public String[] getSample(final int maxPlayers, final int onlinePlayers) {
        final String sample = samples.get(new Random().nextInt(samples.size()));

        return sample.replace("%maxplayers%", String.valueOf(maxPlayers)).replace("%onlineplayers%", String.valueOf(onlinePlayers)).split("\n");
    }

    public int getFakePlayersAmount(final int players) {
        switch (fakePlayersMode) {
            case "STATIC":
                return fakePlayersAmount;
            case "RANDOM":
                return (int) (Math.floor(Math.random() * fakePlayersAmount) + 1);
            case "DIVISION":
                return players / fakePlayersAmount;
            default:
                return 0;
        }
    }

    @Getter
    private boolean motdEnabled = false;
    @Getter
    private List<String> motds = Collections.singletonList("&eDefault &cFlameCord&e custom motd!\n&eChange me in &cflamecord.yml&e file!");

    @Getter
    private boolean sampleEnabled = false;
    @Getter
    private List<String> samples = Collections.singletonList("&eDefault &cFlameCord&e sample!\n&eChange me in &cflamecord.yml&e file!");

    @Getter
    private boolean protocolEnabled = false;
    @Getter
    private String protocolName = "FlameCord 1.7-1.18";

    @Getter
    private boolean maxPlayersEnabled = false;
    @Getter
    private int maxPlayersAmount = 1000;
    @Getter
    private boolean maxPlayersOneMore = false;

    @Getter
    private boolean fakePlayersEnabled = false;
    @Getter
    private int fakePlayersAmount = 3;
    private String fakePlayersMode = "DIVISION";

    @Getter
    private boolean loggerInitialhandler = false;
    @Getter
    private boolean loggerExceptions = false;
    @Getter
    private boolean loggerDump = false;
    @Getter
    private boolean loggerHaProxy = false;
    @Getter
    private boolean loggerDetailedConnection = false;

    public FlameCordConfiguration(final ConfigurationProvider configurationProvider) {
        try {
            final String fileName = "./flamecord.yml";
            final File configurationFile = new File(fileName);
            final Configuration configuration;
            final boolean configurationExists = configurationFile.exists();

            if (!configurationExists) {
                configuration = new Configuration();
            } else {
                configuration = configurationProvider.load(configurationFile);
            }

            this.motdEnabled = setIfUnexistant("custom-motd.motd.enabled", this.motdEnabled, configuration);
            this.motds = colors(new ArrayList<>(setIfUnexistant("custom-motd.motd.motds", this.motds, configuration)));
            this.sampleEnabled = setIfUnexistant("custom-motd.sample.enabled", this.sampleEnabled, configuration);
            this.samples = colors(new ArrayList<>(setIfUnexistant("custom-motd.sample.samples", this.samples, configuration)));
            this.protocolEnabled = setIfUnexistant("custom-motd.protocol.enabled", this.protocolEnabled, configuration);
            this.protocolName = setIfUnexistant("custom-motd.protocol.name", this.protocolName, configuration);
            this.maxPlayersEnabled = setIfUnexistant("custom-motd.maxplayers.enabled", this.maxPlayersEnabled, configuration);
            this.maxPlayersAmount = setIfUnexistant("custom-motd.maxplayers.amount", this.maxPlayersAmount, configuration);
            this.maxPlayersOneMore = setIfUnexistant("custom-motd.maxplayers.justonemore", this.maxPlayersOneMore, configuration);
            this.fakePlayersEnabled = setIfUnexistant("custom-motd.fakeplayers.enabled", this.fakePlayersEnabled, configuration);
            this.fakePlayersAmount = setIfUnexistant("custom-motd.fakeplayers.amount", this.fakePlayersAmount, configuration);
            this.fakePlayersMode = setIfUnexistant("custom-motd.fakeplayers.mode", this.fakePlayersMode, configuration);
            this.allowInvalidNames = setIfUnexistant("allow-invalid-names", this.allowInvalidNames, configuration);

            loadAntibot(configuration);

            this.tcpFastOpen = setIfUnexistant("tcp-fast-open", this.tcpFastOpen, configuration);

            this.loggerInitialhandler = setIfUnexistant("logger.initialhandler", this.loggerInitialhandler, configuration);
            this.loggerExceptions = setIfUnexistant("logger.exceptions", this.loggerExceptions, configuration);
            this.loggerDump = setIfUnexistant("logger.dump", this.loggerDump, configuration);
            this.loggerHaProxy = setIfUnexistant("logger.haproxy", this.loggerHaProxy, configuration);
            this.loggerDetailedConnection = setIfUnexistant("logger.detailed-connect-errors", this.loggerDetailedConnection, configuration);

            configurationProvider.save(configuration, configurationFile);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }
}

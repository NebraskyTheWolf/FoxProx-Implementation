package dev._2lstudios.flamecord.configuration;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;

public class MessagesConfiguration extends FlameConfig {
	private final Logger logger;
	private final Map<String, String> messages = new HashMap<>();

	public MessagesConfiguration(final Logger logger, final ConfigurationProvider configurationProvider) {
		this.logger = logger;

		try {
			final String fileName = "./messages.yml";
			final File configurationFile = new File(fileName);
			final Configuration configuration;
			final boolean configurationExists = configurationFile.exists();

			if (!configurationExists) {
				configuration = new Configuration();
			} else {
				configuration = configurationProvider.load(configurationFile);
			}

			// BungeeCord
			setIfUnexistant("alert", "&8[&4Alert&8]&r ", configuration);
			setIfUnexistant("already_connected", "&cYou are already connected to this server!", configuration);
			setIfUnexistant("already_connected_proxy", "&cYou are already connected to this proxy!", configuration);
			setIfUnexistant("already_connecting", "&cAlready connecting to this server!", configuration);
			setIfUnexistant("command_list_format", "&aServers:&r", configuration);
			setIfUnexistant("command_list", "&a[{0}] &e({1}): &r{2}", configuration);
			setIfUnexistant("connect_kick", "&cKicked whilst connecting to {0}: {1}", configuration);
			setIfUnexistant("current_server", "&6You are currently connected to {0}.", configuration);
			setIfUnexistant("fallback_kick",
					"&cCould not connect to a default or fallback server, please try again later: {0}", configuration);
			setIfUnexistant("fallback_lobby",
					"&cCould not connect to target server, you have been moved to a fallback server.", configuration);
			setIfUnexistant("lost_connection", "[Proxy] Lost connection to server.", configuration);
			setIfUnexistant("mojang_fail", "Error occurred while contacting login servers, are they down?",
					configuration);
			setIfUnexistant("no_permission", "&cYou do not have permission to execute this command!", configuration);
			setIfUnexistant("no_server", "&cThe specified server does not exist.", configuration);
			setIfUnexistant("no_server_permission", "&cYou don't have permission to access this server.",
					configuration);
			setIfUnexistant("outdated_client", "Outdated client! Please use {0}", configuration);
			setIfUnexistant("outdated_server", "Outdated server! I'm still on {0}", configuration);
			setIfUnexistant("proxy_full", "Server is full!", configuration);
			setIfUnexistant("restart", "[Proxy] Proxy restarting.", configuration);
			setIfUnexistant("server_list", "&6You may connect to the following servers at this time: ", configuration);
			setIfUnexistant("server_went_down",
					"&cThe server you were previously on went down, you have been connected to a fallback server",
					configuration);
			setIfUnexistant("total_players", "Total players online: {0}", configuration);
			setIfUnexistant("name_invalid", "Username contains invalid characters.", configuration);
			setIfUnexistant("ping_cannot_connect", "&c[Bungee] Can't connect to server.", configuration);
			setIfUnexistant("offline_mode_player", "Not authenticated with Minecraft.net", configuration);
			setIfUnexistant("secure_profile_required", "A secure profile is required to join this server.", configuration);
			setIfUnexistant("secure_profile_expired", "Secure profile expired.", configuration);
			setIfUnexistant("secure_profile_invalid", "Secure profile invalid.", configuration);
			setIfUnexistant("message_needed", "&cYou must supply a message.", configuration);
			setIfUnexistant("error_occurred_player",
					"&cAn error occurred while parsing your message. (Hover for details)", configuration);
			setIfUnexistant("error_occurred_console", "&cAn error occurred while parsing your message: {0}",
					configuration);
			setIfUnexistant("click_to_connect", "Click to connect to the server", configuration);
			setIfUnexistant("username_needed", "&cPlease follow this command by a user name.", configuration);
			setIfUnexistant("user_not_online", "&cThat user is not online.", configuration);
			setIfUnexistant("user_online_at", "&a{0} &ris online at {1}", configuration);
			setIfUnexistant("send_cmd_usage",
					"&cNot enough arguments, usage: /send <server|player|all|current> <target>", configuration);
			setIfUnexistant("player_only", "&cOnly in game players can use this command", configuration);
			setIfUnexistant("you_got_summoned", "&6Summoned to {0} by {1}", configuration);
			setIfUnexistant("command_perms_groups", "&6You have the following groups: {0}", configuration);
			setIfUnexistant("command_perms_permission", "&9- {0}", configuration);
			setIfUnexistant("command_ip", "&9IP of {0} is {1}", configuration);
			setIfUnexistant("illegal_chat_characters", "&cIllegal characters in chat ({0})", configuration);

			// FlameCord start - Antibot System
			setIfUnexistant("antibot_accounts", "&c&lFlameCord\n\n&cYou have too many accounts! ({0})\n\n&cError? Contact us on discord.gg/gF36AT3", configuration);
			setIfUnexistant("antibot_fastchat", "&c&lFlameCord\n\n&cYou are chatting too fast!\n\n&cError? Contact us on discord.gg/gF36AT3", configuration);
			setIfUnexistant("antibot_firewall", "&c&lFlameCord\n\n&cYou are blocked from this server!\n\n&cError? Contact us on discord.gg/gF36AT3", configuration);
			setIfUnexistant("antibot_nickname", "&c&lFlameCord\n\n&cYour nickname was detected as bot! ({0})\n\n&cError? Contact us on discord.gg/gF36AT3", configuration);
			setIfUnexistant("antibot_password", "&c&lFlameCord\n\n&cYour password is used by other players! ({0})\n\n&cError? Contact us on discord.gg/gF36AT3", configuration);
			setIfUnexistant("antibot_ratelimit", "&c&lFlameCord\n\n&cYou are connecting too fast! ({0})\n\n&cError? Contact us on discord.gg/gF36AT3", configuration);
			setIfUnexistant("antibot_reconnect", "&c&lFlameCord\n\n&cReconnect {0} more times to enter!\n\n&cError? Contact us on discord.gg/gF36AT3", configuration);
			setIfUnexistant("antibot_country", "&c&lFlameCord\n\n&cYour country {0} is blacklisted!\n\n&cError? Contact us on discord.gg/gF36AT3", configuration);
			// FlameCord end - Antibot System

			// FlameCord
			setIfUnexistant("flamecord_reload", "&aAll files had been successfully reloaded!", configuration);
			setIfUnexistant("flamecord_help",
					"&aFlameCord&b {0}&a by&b LinsaFTW&a &&b Sammwy&r\n&e /flamecord reload&7 >&b Reloads FlameCord files!\n&e /flamecord help&7 >&b Shows this message!",
					configuration);
			setIfUnexistant("flamecord_nopermission", "&cYou don't have permission to do this!", configuration);

			configurationProvider.save(configuration, configurationFile);

			for (final String key : configuration.getKeys()) {
				final Object value = configuration.get(key);

				if (value instanceof String) {
					this.messages.put(key, ChatColor.translateAlternateColorCodes('&', (String) value));
				}
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	public String getTranslation(final String name, final Object... args) {
		if (!messages.containsKey(name)) {
			logger.warning("[FlameCord] Tried to get translation '" + name
					+ "' from messages.yml file but wasn't found. Please try resetting this file or report to a developer.");
		}

		return MessageFormat.format(messages.getOrDefault(name, "<translation '" + name + "' missing>"), args);
	}
}
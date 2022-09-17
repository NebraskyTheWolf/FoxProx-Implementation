package net.md_5.bungee.listeners;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PubSubMessage;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.util.ModerationUtils;
import net.samagames.api.channels.ModChannel;

public class BanListeners implements Listener {
    private static final String TAG = ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "[" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "Samaritan" + ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "] ";

    @EventHandler
    public void onBan(PubSubMessage message) {
        if (message.getChannel().equals("cheat")) {
            String[] args = message.getMessage().split("#####");

            ModerationUtils.sendMessage(ModChannel.REPORT, TAG +  "The player " + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + args[0] + ChatColor.DARK_PURPLE + ChatColor.BOLD + " has been banned for the reason " + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + args[1] + ChatColor.DARK_PURPLE + ChatColor.BOLD + ".");
        }
    }
}

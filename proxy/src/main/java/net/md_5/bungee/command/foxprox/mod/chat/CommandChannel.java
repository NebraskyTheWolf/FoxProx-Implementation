package net.md_5.bungee.command.foxprox.mod.chat;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.command.AbstractCommand;
import redis.clients.jedis.Jedis;

public class CommandChannel extends AbstractCommand {
    public CommandChannel() {
        super(ProxyServer.getInstance(), "channel");
    }

    @Override
    protected void onCommand(CommandSender sender, String[] args) {
        if (!hasPermission(sender, "mod.channel")) {
            this.sendError(sender, "You don't have the permission to execute this command.");
            return;
        }

        ProxiedPlayer player = (ProxiedPlayer) sender;

        Jedis redis = ProxyServer.getInstance().getRedisConnection();
        String key = "currentchannel:" + player.getUniqueId().toString();

        String currentChannel = redis.get(key);
        if (currentChannel == null) {
            redis.set(key, "moderator");
            this.sendSuccess(sender, ChatColor.DARK_BLUE + "You switched on the " + ChatColor.RED + "moderator" + ChatColor.DARK_BLUE + " chat.");
        } else {
            if (currentChannel.equals("moderator")) {
                redis.set(key, "public");
                this.sendSuccess(sender, ChatColor.DARK_BLUE + "You switched on the " + ChatColor.GREEN + "public" + ChatColor.DARK_BLUE + " chat.");
            } else if (currentChannel.equals("public")) {
                redis.set(key, "moderator");
                this.sendSuccess(sender, ChatColor.DARK_BLUE + "You switched on the " + ChatColor.RED + "moderator" + ChatColor.DARK_BLUE + " chat.");
            }
        }
        redis.close();
    }
}

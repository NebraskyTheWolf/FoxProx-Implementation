package net.md_5.bungee.command.foxprox.player;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.command.AbstractCommand;
import net.md_5.bungee.util.ModerationUtils;
import net.samagames.api.channels.ModChannel;

public class CommandReport extends AbstractCommand {
    public CommandReport() {
        super(ProxyServer.getInstance(), "report");
    }

    @Override
    protected void onCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            this.sendError(sender, "Usage: /report <playerName> <reason>");
        } else {
            String player = args[0];
            String reason = args[1];

            ProxiedPlayer target = ProxyServer.getInstance().getPlayer(player);

            if (target != null && target.isConnected()) {
                if (reason.length() < 1) {
                    this.sendError(sender, "You need to put a reason.");
                } else {
                    String currentServer = ProxyServer.getInstance()
                            .getRedisConnection()
                            .get("currentserver:" + target.getUniqueId().toString());

                    this.sendSuccess(sender, "Player " + ChatColor.GOLD + target.getName() + ChatColor.YELLOW + " reported to the moderators.");

                    ModerationUtils.sendMessage(ModChannel.REPORT, ChatColor.YELLOW + "Player " + ChatColor.GOLD + target.getName() + ChatColor.YELLOW + " report on the server " + ChatColor.GOLD + currentServer + ChatColor.YELLOW + " for " + ChatColor.GOLD + reason);
                }
            } else {
                this.sendError(sender, "The specified player is not connected.");
            }
        }
    }
}

package net.md_5.bungee.command.foxprox.mod.sanction;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.command.AbstractCommand;
import net.md_5.bungee.util.ModerationUtils;
import net.md_5.bungee.util.StringUtils;
import net.samagames.api.channels.ModChannel;
import net.samagames.persistanceapi.beans.players.SanctionBean;
import java.sql.Timestamp;

public class CommandKick extends AbstractCommand {
    public CommandKick() {
        super(ProxyServer.getInstance(), "kick");
    }

    @Override
    protected void onCommand(CommandSender sender, String[] args) throws Exception {
        if (!hasPermission(sender, "mod.kick")) {
            this.sendError(sender, "You don't have the permission to execute this command.");
            return;
        }

        if (args.length < 2) {
            this.sendError(sender, "Usage: /kick <playerName> <reasons>");
            return;
        }

        String player = args[0];
        String reason = StringUtils.join(args, " ", 1, args.length);

        ProxiedPlayer target = ProxyServer.getInstance().getPlayer(player);
        if (target == null) {
            this.sendError(sender, "Player not found.");
            return;
        }
        if (reason.length() < 1) {
            this.sendError(sender, "You have to set a reasons.");
            return;
        }
        ProxiedPlayer moderator = (ProxiedPlayer)sender;

        SanctionBean documents = new SanctionBean(
                target.getUniqueId(),
                SanctionBean.KICK,
                reason,
                moderator.getUniqueId(),
                new Timestamp(System.currentTimeMillis() + 380000),
                false
        );

        if (target.isConnected()) {
            ProxyServer.getInstance()
                    .getGameServiceManager()
                    .applySanction(documents.getTypeId(), documents);

            this.sendSuccess(sender, "You kicked " + ChatColor.GOLD + target.getName() + ChatColor.YELLOW + " for " + ChatColor.GOLD + reason);
            this.kickTarget(target, "Kicked for " + ChatColor.GOLD + reason);

            ModerationUtils.sendMessage(ModChannel.SANCTION, ChatColor.GOLD + "" + moderator.getName() + ChatColor.YELLOW + " Kicked " + ChatColor.GOLD + target.getName() + ChatColor.YELLOW + " for " + ChatColor.GOLD + reason + ChatColor.YELLOW + ".");
        } else {
            this.sendError(sender, "Player " + ChatColor.GOLD + target + ChatColor.YELLOW + " specified is disconnected from the network.");
        }
    }
}

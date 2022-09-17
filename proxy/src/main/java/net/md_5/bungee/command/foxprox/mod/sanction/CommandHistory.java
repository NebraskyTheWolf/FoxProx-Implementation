package net.md_5.bungee.command.foxprox.mod.sanction;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.command.AbstractCommand;
import net.samagames.persistanceapi.beans.players.SanctionBean;

import java.util.List;

public class CommandHistory extends AbstractCommand {
    public CommandHistory() {
        super(ProxyServer.getInstance(), "history");
    }

    @Override
    protected void onCommand(CommandSender sender, String[] args) throws Exception {
        if (!hasPermission(sender, "mod.kick")) {
            this.sendError(sender, "You don't have the permission to execute this command.");
            return;
        }

        if (args.length < 1) {
            this.sendError(sender, "You need to specify a player.");
            return;
        }

        StringBuilder bansBuilder = new StringBuilder(BungeeCord.PROXY_TAG + ChatColor.YELLOW + " Bans History:\n");
        StringBuilder muteBuilder = new StringBuilder(BungeeCord.PROXY_TAG + ChatColor.YELLOW + " Mutes History:\n");
        StringBuilder warnBuilder = new StringBuilder(BungeeCord.PROXY_TAG + ChatColor.YELLOW + " Warns History:\n");

        String player = args[0];
        ProxiedPlayer target = ProxyServer.getInstance().getPlayer(player);

        if (target != null && target.isConnected()) {
            List<SanctionBean> bans = ProxyServer.getInstance().getGameServiceManager().getAllActiveSanctions(target.getUniqueId(), SanctionBean.BAN);
            List<SanctionBean> mute = ProxyServer.getInstance().getGameServiceManager().getAllActiveSanctions(target.getUniqueId(), SanctionBean.MUTE);
            List<SanctionBean> warns = ProxyServer.getInstance().getGameServiceManager().getAllActiveSanctions(target.getUniqueId(), SanctionBean.AVERTISSEMENT);

            if (bans.isEmpty()) {
                bansBuilder.append(ChatColor.YELLOW).append("✖ No ban history detected.\n");
            } else {
                bans.forEach((ban -> bansBuilder.append(ChatColor.GOLD + "├" +  ChatColor.YELLOW + "  [+] Reason -> " + ChatColor.GOLD + ban.getReason() + ChatColor.YELLOW + " At -> " + ChatColor.GOLD + ban.getCreationDate().toLocalDateTime() + ChatColor.YELLOW + " Type -> " + ChatColor.RED + " PERMANENT \n")));
            }

            if (mute.isEmpty()) {
                muteBuilder.append(ChatColor.YELLOW).append("✖ No mute history detected.\n");
            } else {
                mute.forEach((ban -> muteBuilder.append(ChatColor.GOLD + "├" +  ChatColor.YELLOW + "  [+] Reason -> " + ChatColor.GOLD + ban.getReason() + ChatColor.YELLOW + " At -> " + ChatColor.GOLD + ban.getCreationDate().toLocalDateTime() + "\n")));
            }

            if (warns.isEmpty()) {
                warnBuilder.append(ChatColor.YELLOW).append("✖ No warn history detected.\n");
            } else {
                warns.forEach((ban -> warnBuilder.append(ChatColor.GOLD + "├" +  ChatColor.YELLOW + "  [+] Reason -> " + ChatColor.GOLD + ban.getReason() + ChatColor.YELLOW + " At -> " + ChatColor.GOLD + ban.getCreationDate().toLocalDateTime() + "\n")));
            }

            StringBuilder finalBuilder = new StringBuilder()
                    .append(bansBuilder)
                    .append("\n")
                    .append(muteBuilder)
                    .append("\n")
                    .append(warnBuilder)
                    .append("\n");

            int total = bans.size() + mute.size() + warns.size();
            if (total < 1) {
                finalBuilder.append(ChatColor.YELLOW + "⇨ This player have a clean slate.\n");
            } else {
                finalBuilder.append(ChatColor.YELLOW + "⇨ Total of active sanctions: " + ChatColor.GOLD + total + "\n");
            }

            if (total > 5) {
                finalBuilder.append(ChatColor.RED + "⚠" + ChatColor.BOLD +  " Permanent ban recommended." + ChatColor.RED + " This player got too many sanctions. \n");
            }

            sender.sendMessage(new TextComponent(finalBuilder.toString()));
        }
    }
}

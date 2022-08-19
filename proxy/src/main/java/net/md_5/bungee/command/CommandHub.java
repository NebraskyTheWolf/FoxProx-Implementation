package net.md_5.bungee.command;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class CommandHub extends AbstractCommand {
    public CommandHub() {
        super(BungeeCord.getFoxProxManager(), "lobby");
    }

    @Override
    protected void onCommand(CommandSender sender, String[] args) {
        ProxiedPlayer player = (ProxiedPlayer)sender;
        ServerInfo lobby = BungeeCord.getInstance().getServerInfo("Hub_1");
        if (lobby.canAccess(sender))
            player.connect(lobby);
        else
            sender.sendMessage(new TextComponent(ChatColor.RED + " Unable to access to the lobby"));
    }
}

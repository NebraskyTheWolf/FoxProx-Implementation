package net.md_5.bungee.command.foxprox.player;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.command.AbstractCommand;

public class CommandHub extends AbstractCommand {
    public CommandHub() {
        super(ProxyServer.getInstance(), "lobby");
    }

    @Override
    protected void onCommand(CommandSender sender, String[] args) {
        ProxiedPlayer player = (ProxiedPlayer)sender;
        ServerInfo lobby = BungeeCord.getInstance().getLobbyManager().fetchRandomHub();
        if (lobby.canAccess(sender))
            player.connect(lobby);
        else
            this.sendError(sender, "Unable to access to the lobby #" + lobby.getName().split("_")[1]);
    }
}

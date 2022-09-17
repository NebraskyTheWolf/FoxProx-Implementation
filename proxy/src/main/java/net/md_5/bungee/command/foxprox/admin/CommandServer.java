package net.md_5.bungee.command.foxprox.admin;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.TabExecutor;
import net.md_5.bungee.command.AbstractCommand;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class CommandServer extends AbstractCommand implements TabExecutor {
    public CommandServer() {
        super(ProxyServer.getInstance(), "server");
    }

    @Override
    protected void onCommand(CommandSender sender, String[] args) {
        ProxiedPlayer player = (ProxiedPlayer) sender;

        if (args.length == 0)
            sender.sendMessage(new TextComponent(BungeeCord.PROXY_TAG + ChatColor.RED + "Missing arguments /server <server_name>"));
        if (getSenderPermissions(sender).getBungeeRedisPermissionsBean().isBungeecordCommandServer()) {
            ServerInfo info = BungeeCord.getInstance().getServerInfo(args[0]);
            if (info.canAccess(sender))
                if (player.isConnected())
                    player.connect(info);
            else player.sendMessage(new TextComponent(BungeeCord.PROXY_TAG + ChatColor.RED + "Unable to connect on " + args[0]));
        } else {
            this.sendError(sender, "You don't have the permission to execute this command.");
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> SERVERS = new CopyOnWriteArrayList<>();
        for (ServerInfo info : BungeeCord.getInstance().getServers().values()) {
            if (!SERVERS.contains(info.getName()))
                SERVERS.add(info.getName());
        }
        return SERVERS;
    }
}

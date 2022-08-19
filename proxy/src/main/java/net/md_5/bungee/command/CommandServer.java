package net.md_5.bungee.command;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class CommandServer extends AbstractCommand implements TabExecutor {
    public CommandServer() {
        super(BungeeCord.getFoxProxManager(), "server");
    }

    @Override
    protected void onCommand(CommandSender sender, String[] args) {
        ProxiedPlayer player = (ProxiedPlayer) sender;

        if (args.length == 0)
            sender.sendMessage(new TextComponent(ChatColor.RED + "Missing arguments /server <server_name>"));
        if (getSenderPermissions(sender).getBungeeRedisPermissionsBean().isBungeecordCommandServer()) {
            ServerInfo info = BungeeCord.getInstance().getServerInfo(args[0]);
            if (info.canAccess(sender))
                if (player.isConnected())
                    player.connect(info);
            else player.sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + "Unable to connect on " + args[0]));
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

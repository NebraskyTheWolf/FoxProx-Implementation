package net.md_5.bungee.module.cmd.find;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.command.PlayerCommand;

public class CommandFind extends PlayerCommand {
    public CommandFind()
    {
        super("find" );
    }

    @Override
    protected void onCommand(CommandSender sender, String[] args) {
        if (!getSenderPermissions(sender).getBungeeRedisPermissionsBean().isBungeecordCommandFind()) {
            sender.sendMessage(new TextComponent(ChatColor.RED + "You don't have the permissions to execute this command."));
            return;
        }

        if ( args.length != 1 )
        {
            sender.sendMessage( new TextComponent( ProxyServer.getInstance().getTranslation( "username_needed" )) );
        } else {
            ProxiedPlayer player = ProxyServer.getInstance().getPlayer( args[0] );
            if ( player == null || player.getServer() == null ) {
                sender.sendMessage( new TextComponent( ProxyServer.getInstance().getTranslation( "user_not_online" )));
            } else {
                sender.sendMessage( new TextComponent(ProxyServer.getInstance().getTranslation( "user_online_at", player.getName(), player.getServer().getInfo().getName() )) );
            }
        }
    }
}

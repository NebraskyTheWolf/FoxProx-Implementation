package net.md_5.bungee.command.foxprox.admin;

import com.google.common.base.Joiner;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.command.AbstractCommand;

/**
 * Command to terminate the proxy instance. May only be used by the console by
 * default.
 */
public class CommandEnd extends AbstractCommand {
    public CommandEnd()
    {
        super(ProxyServer.getInstance(), "end" );
    }

    @Override
    protected void onCommand(CommandSender sender, String[] args) {
        if (getSenderPermissions(sender).getBungeeRedisPermissionsBean().isBungeecordCommandEnd()) {
            if (args.length == 0) {
                BungeeCord.getInstance().stop(BungeeCord.PROXY_TAG + "Shutting down....");
            } else {
                BungeeCord.getInstance().stop(BungeeCord.PROXY_TAG + "Requested by administrator." );
            }
        } else {
            this.sendError(sender, "You don't have the permission to execute this command.");
        }
    }
}

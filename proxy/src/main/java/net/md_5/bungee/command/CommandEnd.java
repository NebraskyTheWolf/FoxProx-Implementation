package net.md_5.bungee.command;

import com.google.common.base.Joiner;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;

/**
 * Command to terminate the proxy instance. May only be used by the console by
 * default.
 */
public class CommandEnd extends AbstractCommand {
    public CommandEnd()
    {
        super(BungeeCord.getFoxProxManager(), "end" );
    }

    @Override
    protected void onCommand(CommandSender sender, String[] args) {
        if (getSenderPermissions(sender).getBungeeRedisPermissionsBean().isBungeecordCommandEnd()) {
            if (args.length == 0) {
                BungeeCord.getInstance().stop();
            } else {
                BungeeCord.getInstance().stop( ChatColor.translateAlternateColorCodes( '&', Joiner.on( ' ' ).join( args ) ) );
            }
        } else {
            sender.sendMessage(new TextComponent(ChatColor.RED + "Permission denied."));
        }
    }
}

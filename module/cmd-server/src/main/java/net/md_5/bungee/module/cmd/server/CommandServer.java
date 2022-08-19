package net.md_5.bungee.module.cmd.server;

import java.util.Collections;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

/**
 * Command to list and switch a player between available servers.
 */
public class CommandServer extends Command implements TabExecutor
{

    public CommandServer()
    {
        super( "oldserver", "bungeecord.command.server" );
    }

    @Override
    public void execute(CommandSender sender, String[] args)
    {
        TextComponent serverTextComponent = new TextComponent( " COMMAND DISABLED PLEASE USE /hserver instead of /server " );
        sender.sendMessage(serverTextComponent);
    }

    @Override
    public Iterable<String> onTabComplete(final CommandSender sender, final String[] args)
    {
        return Collections.EMPTY_LIST;
    }
}

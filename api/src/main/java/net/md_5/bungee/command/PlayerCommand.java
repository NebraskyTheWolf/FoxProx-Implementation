package net.md_5.bungee.command;

import java.util.Locale;
import java.util.stream.Collectors;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.TabExecutor;

/**
 * @deprecated internal use only
 */
@Deprecated
public abstract class PlayerCommand extends AbstractCommand implements TabExecutor
{

    public PlayerCommand(String name)
    {
        super(ProxyServer.getInstance(), name );
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args)
    {
        final String lastArg = ( args.length > 0 ) ? args[args.length - 1].toLowerCase( Locale.ROOT ) : "";
        return ProxyServer.getInstance().getPlayers().stream().filter(player -> player.getName().toLowerCase(Locale.ROOT).startsWith(lastArg)).collect(Collectors.toList()).stream().map(player -> player.getName()).collect(Collectors.toList());
    }
}

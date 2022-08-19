package net.md_5.bungee.command;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Command;

public class CommandBungee extends Command
{

    public CommandBungee()
    {
        super( "foxprox" );
    }

    @Override
    public void execute(CommandSender sender, String[] args)
    {
        sender.sendMessage(ChatColor.RED + "---------------------------------------------");
        sender.sendMessage(ChatColor.RED +"FoxProx v" + BungeeCord.getInstance().getVersion());
        sender.sendMessage(ChatColor.RED +"By Vakea using a fork of Waterfall ( Private Fork )");
        sender.sendMessage(ChatColor.RED +"---------------------------------------------");
    }
}

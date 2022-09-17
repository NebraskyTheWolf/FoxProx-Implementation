package net.md_5.bungee.command.foxprox.mod.ingame;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.command.AbstractCommand;

public class CommandBTP extends AbstractCommand {
    public CommandBTP() {
        super(ProxyServer.getInstance(), "btp");
    }

    @Override
    protected void onCommand(CommandSender sender, String[] args) {
        if (!hasPermission(sender, "mod.kick")) {
            this.sendError(sender, "You don't have the permission to execute this command.");
            return;
        }


    }
}

package net.md_5.bungee.command.foxprox.admin;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.command.AbstractCommand;

public class CommandSEnd extends AbstractCommand {
    public CommandSEnd() {
        super(ProxyServer.getInstance(), "send");
    }

    @Override
    protected void onCommand(CommandSender sender, String[] args) {
        if (getSenderPermissions(sender).getBungeeRedisPermissionsBean().isBungeecordCommandEnd()) {
            if (args.length == 0)
                return;
            ProxyServer.getInstance().getPubSub().send("commands.servers." + args[0], "stop");
        } else {
            this.sendError(sender, "You don't have the permission to execute this command.");
        }
    }
}

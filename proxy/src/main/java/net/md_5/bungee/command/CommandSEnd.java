package net.md_5.bungee.command;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.CommandSender;

public class CommandSEnd extends AbstractCommand{
    public CommandSEnd() {
        super(BungeeCord.getFoxProxManager(), "send");
    }

    @Override
    protected void onCommand(CommandSender sender, String[] args) {
        if (getSenderPermissions(sender).getBungeeRedisPermissionsBean().isBungeecordCommandEnd()) {
            if (args.length == 0)
                return;
            BungeeCord.getFoxProxManager().getPubSub().send("commands.servers." + args[0], "stop");
        }
    }
}

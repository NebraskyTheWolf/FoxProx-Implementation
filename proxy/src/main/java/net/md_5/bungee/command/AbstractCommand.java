package net.md_5.bungee.command;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.samagames.Loader;
import net.samagames.core.helpers.permissions.EntityPermissions;

public abstract class AbstractCommand extends Command {

    protected final Loader plugin;

    public AbstractCommand(Loader plugin, String name) {
        super(name);
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        this.onCommand(sender, args);
    }

    protected abstract void onCommand(CommandSender sender, String[] args);

    protected EntityPermissions getSenderPermissions(CommandSender sender) {
        ProxiedPlayer player = (ProxiedPlayer) sender;
        return this.plugin.getPermissionsManager().getPlayerPermission(player.getUniqueId());
    }
}

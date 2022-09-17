package net.md_5.bungee.command;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.permissions.EntityPermissions;

import java.util.List;

public abstract class AbstractCommand extends Command {

    protected final ProxyServer plugin;

    public static final String TAG = ChatColor.DARK_AQUA + "[" + ChatColor.AQUA + "FoxGuard" + ChatColor.DARK_AQUA + "] " + ChatColor.WHITE + "┊ " + ChatColor.RESET;

    public AbstractCommand(ProxyServer plugin, String name) {
        super(name);
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) throws Exception {
        if (sender instanceof ProxiedPlayer) {
            this.onCommand(sender, args);
        } else {
            this.sendError(sender, "You can't use this command in the console.");
            this.sendError(sender, "The developer console is set on `SAFE` mode.");
            this.sendError(sender, "---");
        }
    }

    protected abstract void onCommand(CommandSender sender, String[] args) throws Exception;

    protected void sendError(CommandSender sender, String message) {
        sender.sendMessage(new TextComponent(TAG + ChatColor.RED + "" + ChatColor.BOLD + "✖" + ChatColor.YELLOW + " " + message));
    }

    protected void sendSuccess(CommandSender sender, String message) {
        sender.sendMessage(new TextComponent(TAG + ChatColor.GREEN + "" + ChatColor.BOLD + "✔" + ChatColor.YELLOW + " " + message));
    }

    protected void sendWarning(CommandSender sender, String message) {
        sender.sendMessage(new TextComponent(TAG + ChatColor.GOLD + "" + ChatColor.BOLD + "⚠" + ChatColor.YELLOW + " " + message));
    }

    protected void kickTarget(ProxiedPlayer sender, String reason) {
        sender.disconnect(new TextComponent(TAG + ChatColor.RED + "" + ChatColor.BOLD + "✖ " + reason));
    }

    protected void sendFormattedList(CommandSender sender, String TAG, List<String> messages) {
        StringBuilder formatted = new StringBuilder(TAG + "\n");
        for (String str : messages)
            formatted.append(ChatColor.GOLD).append("├").append(" ").append(str).append("\n");
        sender.sendMessage(new TextComponent(formatted.toString()));
    }

    protected EntityPermissions getSenderPermissions(CommandSender sender) {
        ProxiedPlayer player = (ProxiedPlayer) sender;
        return player.getPermission();
    }

    protected boolean hasPermission(CommandSender sender, String permissions) {
        ProxiedPlayer player = (ProxiedPlayer) sender;
        return player.getPermission().getListPermissions().get(permissions);
    }
}

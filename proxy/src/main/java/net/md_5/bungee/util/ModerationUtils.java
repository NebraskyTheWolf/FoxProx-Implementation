package net.md_5.bungee.util;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.samagames.api.channels.ModChannel;

import java.util.LinkedHashMap;
import java.util.Map;

public class ModerationUtils {
    private static final String MODERATING_TAG = ChatColor.DARK_AQUA + "[" + ChatColor.AQUA + "%s" + ChatColor.DARK_AQUA + "] " + ChatColor.WHITE + "┊ " + ChatColor.RESET;
    public static final String TAG = ChatColor.DARK_AQUA + "[" + ChatColor.AQUA + "Moderation" + ChatColor.DARK_AQUA + "] " + ChatColor.WHITE + "┊ " + ChatColor.RESET;
    public static final String REPORT_TAG = ChatColor.DARK_AQUA + "[" + ChatColor.AQUA + "Report" + ChatColor.DARK_AQUA + "] " + ChatColor.WHITE + "┊ " + ChatColor.RESET;

    public static void sendMessage(ModChannel channel, String message) {
        // Formatting the message before sending it to the moderators.
        TextComponent component = (TextComponent) new TextComponent()
                .setText(String.format(MODERATING_TAG + "%s", channel.getName(), message))
                .setColor(channel.getColor());
        // Get online moderators and then sending the message.
        getPlayerFor(channel).forEach((player, modChannel) -> {
            if (modChannel.equals(channel))
                if (player.isConnected())
                    player.sendMessage(ChatMessageType.SYSTEM, component);
        });
    }
    // Moderation channels permissions sorter
    private static Map<ProxiedPlayer, ModChannel> getPlayerFor(ModChannel channel) {
        Map<ProxiedPlayer, ModChannel> PLAYERS = new LinkedHashMap<>();
        for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
            switch (channel) {
                case SANCTION:
                    if (player.getPermission().getModerationPermissionsBean().isModBan()
                            || player.getPermission().getModerationPermissionsBean().isModMute()
                            || player.getPermission().getModerationPermissionsBean().isModKick()
                            || player.getPermission().getModerationPermissionsBean().isModMuteLongtime()
                            || player.getPermission().getModerationPermissionsBean().isModPardon())
                        PLAYERS.put(player, ModChannel.SANCTION);
                    break;
                case REPORT:
                    if (player.getPermission().getModerationPermissionsBean().isModChannelReport())
                        PLAYERS.put(player, ModChannel.REPORT);
                    break;
                case DISCUSSION:
                    if (player.getPermission().getModerationPermissionsBean().isModChannel())
                        PLAYERS.put(player, ModChannel.DISCUSSION);
                    break;
                case INFORMATION:
                    if (player.getPermission().getModerationPermissionsBean().isModChannel())
                        PLAYERS.put(player, ModChannel.INFORMATION);
                    break;
            }
        }
        return PLAYERS;
    }
}

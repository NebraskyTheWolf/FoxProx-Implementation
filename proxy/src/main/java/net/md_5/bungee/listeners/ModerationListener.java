package net.md_5.bungee.listeners;

import com.google.gson.Gson;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PubSubMessage;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.util.ModerationUtils;
import net.samagames.api.channels.JsonModMessage;
import net.samagames.api.channels.ModChannel;
import redis.clients.jedis.Jedis;

public class ModerationListener implements Listener {

    public static final String PROXY_TAG = ChatColor.DARK_AQUA + "[" + ChatColor.DARK_PURPLE + "Staff Chat" + ChatColor.DARK_AQUA + "] " + ChatColor.WHITE + "┊ " + ChatColor.RESET;

    @EventHandler
    public void onMessage(PubSubMessage message) {
        if (message.getChannel().equals("moderationchan")) {
            JsonModMessage mod = new Gson().fromJson(message.getMessage(), JsonModMessage.class);

            ModerationUtils.sendMessage(mod.getModChannel(), mod.getMessage());
        }
    }

    @EventHandler
    public void onChatMessage(ChatEvent event) {
        ProxiedPlayer player = (ProxiedPlayer) event.getSender();
        String key = "currentchannel:" + player.getUniqueId().toString();
        Jedis redis = ProxyServer.getInstance().getRedisConnection();

        if (redis.exists(key)) {
            if ("moderator".equals(redis.get(key))) {
                if (!event.isProxyCommand() && !event.isCommand()) {
                    ModerationUtils.sendMessage(ModChannel.DISCUSSION, PROXY_TAG + ChatColor.GOLD + "" + ChatColor.BOLD + player.getName() + ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + " ≫ " + ChatColor.YELLOW + event.getMessage());
                    event.setCancelled(true);
                }
            }
        }
    }

}

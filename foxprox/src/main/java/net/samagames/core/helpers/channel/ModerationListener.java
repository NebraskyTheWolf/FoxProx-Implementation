package net.samagames.core.helpers.channel;

import com.google.gson.Gson;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PubSubMessage;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.samagames.Loader;
import net.samagames.core.helpers.channel.api.JsonModMessage;
import net.samagames.core.helpers.permissions.EntityPermissions;

public class ModerationListener implements Listener {
    @EventHandler
    public void onMessage(PubSubMessage message) {
        if (message.getChannel().equals("moderationchan")) {
            JsonModMessage mod = new Gson().fromJson(message.getMessage(), JsonModMessage.class);
            for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
                EntityPermissions permissions = Loader.getInstance().getPermissionsManager().getPlayerPermission(player.getUniqueId());
                if (permissions.getModerationPermissionsBean().isModChannel()) {
                    player.sendMessage(ChatMessageType.CHAT, new TextComponent(mod.getModChannel().getColor() + String.format("[%s] %s", mod.getModChannel().getName(), mod.getMessage())));
                }
            }
        }
    }
}

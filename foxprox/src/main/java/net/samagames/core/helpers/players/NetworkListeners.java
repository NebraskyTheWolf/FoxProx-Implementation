package net.samagames.core.helpers.players;

import com.google.gson.Gson;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PubSubMessage;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.samagames.Loader;

import java.util.UUID;

public class NetworkListeners implements Listener {
    @EventHandler
    public void onRequest(PubSubMessage message) {
        if (message.getChannel().contains("apiexec")) {
            String[] args = message.getMessage().split(" ");
            ProxiedPlayer player = Loader.getInstance().getProxy().getPlayer(UUID.fromString(args[0]));
            switch (message.getChannel()) {
                case "apiexec.connect": {
                    ServerInfo server = Loader.getInstance().getProxy().getServerInfo(args[1]);
                    player.connect(server);
                }
                break;
                case "apiexec.send": {
                    TextComponent component = new Gson().fromJson(args[1], TextComponent.class);
                    if (component != null)
                        player.sendMessage(component);
                }
                break;
                case "apiexec.kick": {
                    TextComponent component = new Gson().fromJson(args[1], TextComponent.class);
                    player.disconnect(component);
                }
                break;
            }
        }
    }
}

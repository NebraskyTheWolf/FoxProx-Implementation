package net.md_5.bungee.listeners;

import com.google.gson.Gson;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PubSubMessage;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import java.util.UUID;

public class NetworkListeners implements Listener {
    @EventHandler
    public void onRequest(PubSubMessage message) {
        if (message.getChannel().contains("apiexec")) {
            String[] args = message.getMessage().split(" ");
            ProxiedPlayer player = ProxyServer.getInstance().getPlayer(UUID.fromString(args[0]));
            switch (message.getChannel()) {
                case "apiexec.connect": {
                    ServerInfo server = ProxyServer.getInstance().getServerInfo(args[1]);

                    if (server.getName().equals("bleppy_server")) {
                        if (!player.isForgeUser()) {
                            player.sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(BungeeCord.PROXY_TAG + ChatColor.YELLOW + "You can't connect on this server! You need to have ForgeModLoader to join."));
                            return;
                        }
                    }

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

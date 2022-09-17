package net.md_5.bungee.listeners;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.FetchLobby;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.samagames.api.channels.JsonModMessage;
import net.samagames.api.channels.ModChannel;
import net.samagames.persistanceapi.beans.players.PlayerBean;

import java.io.IOException;

public class PlayerJoinListener implements Listener {
    @EventHandler
    public void onJoin(ServerConnectEvent event) throws Exception {
        PlayerBean player = event.getPlayer().handledPlayer();
        if (player != null) {
            event.getPlayer().updateHandledPlayer();

            if (event.getPlayer().isBanned())
                event.getPlayer().disconnect(new TextComponent(BungeeCord.PROXY_TAG + ChatColor.YELLOW + "You are " + ChatColor.RED + "PERMANENTLY " + ChatColor.YELLOW + "banned for " + ChatColor.RED + "Cheat/Bypass"));

            if (!event.getPlayer().connectToLobby(0))
                event.getPlayer().disconnect(new TextComponent(BungeeCord.PROXY_TAG + ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "PROXY RESTARTING..."));
        } else {
           event.getPlayer().createHandledPlayer();
        }

        if (ProxyServer.getInstance().getLobbyManager().isServerAvailable()) {
            FetchLobby lobby = ProxyServer.getInstance().getLobbyManager();
            event.getPlayer().connect(lobby.fetchRandomHub());
        } else {
            event.getPlayer().disconnect(new TextComponent(BungeeCord.PROXY_TAG + ChatColor.RED + "Impossible to connect on the server. The proxy is restarting..."));
        }
    }

    @EventHandler
    public void onChangeServer(ServerConnectEvent event) throws IOException {
        event.getHandles().set("lastserver:" + event.getPlayer().getUniqueId().toString(), event.getTarget().getName());
        event.getHandles().set("currentserver:" + event.getPlayer().getUniqueId().toString(), event.getTarget().getName());

        if (!event.getTarget().isHydroManaged()) {
            ProxyServer.getInstance().sendModerationMessage(ModChannel.INFORMATION, new JsonModMessage()
                    .setSender("FoxProx")
                    .setMessage(String.format("%s connected on a non-managed server.", event.getPlayer().getName()))
            );
        }
    }

    @EventHandler
    public void onJoins(PostLoginEvent event) {
        if (event.getPlayer().isForgeUser()) {
            event.getPlayer()
                    .getModList()
                    .forEach((modId, version) -> event.getHandles().sadd(
                            String.format("forge:%s/mods", event.getPlayer().getUniqueId().toString()),
                            String.format("%s:%s", modId, version)
                    ));
            event.getHandles().close();
        }
    }
}

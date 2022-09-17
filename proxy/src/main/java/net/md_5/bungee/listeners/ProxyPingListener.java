package net.md_5.bungee.listeners;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ProxyPingListener implements Listener {
    @EventHandler
    public void onPing(ProxyPingEvent event) {
        ServerPing ping = event.getResponse();
        int online = ping.getPlayers().getOnline();

        ping.setPlayers(new ServerPing.Players(ProxyServer.getInstance().getBungeeConfig().getMaxPlayers(), online, null));
        ping.setDescription(ProxyServer.getInstance().getBungeeConfig().getMotd());

        event.setResponse(ping);
    }
}

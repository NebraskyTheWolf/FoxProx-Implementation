package net.samagames.core.helpers.players;

import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.samagames.Loader;

public class ProxyPingListener implements Listener {
    @EventHandler
    public void onPing(ProxyPingEvent event) {
        ServerPing ping = event.getResponse();
        int online = ping.getPlayers().getOnline();

        ping.setVersion(new ServerPing.Protocol("FoxProx - 1.7 <--> 1.19.X", ping.getVersion().getProtocol()));
        ping.setPlayers(new ServerPing.Players(Loader.getInstance().getBungeeConfig().getMaxPlayers(), online, null));
        ping.setDescription(Loader.getInstance().getBungeeConfig().getMotd());

        event.setResponse(ping);
    }
}

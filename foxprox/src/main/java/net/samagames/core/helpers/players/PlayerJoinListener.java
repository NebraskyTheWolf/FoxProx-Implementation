package net.samagames.core.helpers.players;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.samagames.Loader;
import net.samagames.persistanceapi.beans.players.PlayerBean;
import net.samagames.persistanceapi.beans.players.SanctionBean;
import java.sql.Timestamp;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onPreLog(PreLoginEvent event) {
        if (ProxyServer.getInstance().getServers().size() < 1)
            event.getConnection().disconnect(new TextComponent(ChatColor.RED + "The network restarting please try again later."));
    }

    @EventHandler
    public void onJoin(ServerConnectEvent event) throws Exception {
        PlayerBean player = Loader.getInstance().getGameServiceManager().getPlayer(event.getPlayer().getUniqueId());
        if (player != null) {
            Loader.getInstance().getGameServiceManager().updatePlayer(new PlayerBean(
                    player.getUuid(),
                    event.getPlayer().getName(),
                    null,
                    player.getCoins(),
                    player.getStars(),
                    player.getPowders(),
                    new Timestamp(System.currentTimeMillis()),
                    player.getFirstLogin(),
                    event.getPlayer().getPendingConnection().getListener().getSocketAddress().toString(),
                    player.getTopTpKey(),
                    player.getGroupId()
            ));

            SanctionBean banned = Loader.getInstance().getGameServiceManager().getPlayerBanned(player);
            if (banned != null && !banned.isDeleted())
                event.getPlayer().disconnect(new TextComponent(ChatColor.RED + "You are banned for " + banned.getReason()));
        } else {
            Loader.getInstance().getGameServiceManager().createPlayer(new PlayerBean(
                    event.getPlayer().getUniqueId(),
                    event.getPlayer().getName(),
                    null,
                    500,
                    0,
                    0,
                    new Timestamp(System.currentTimeMillis()),
                    new Timestamp(System.currentTimeMillis()),
                    event.getPlayer().getPendingConnection().getListener().getSocketAddress().toString(),
                    null,
                    1
            ));
        }
    }
}

package net.samagames.core.helpers.players;

import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.samagames.Loader;
import net.samagames.core.helpers.servers.FetchLobby;
import net.samagames.persistanceapi.beans.players.PlayerBean;
import net.samagames.persistanceapi.beans.players.SanctionBean;
import redis.clients.jedis.Jedis;
import java.sql.Timestamp;

public class PlayerJoinListener implements Listener {

    private final FetchLobby fetchLobby = Loader.getInstance().getFetchLobby();

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

    @EventHandler
    public void onJoins(ServerConnectEvent event) {
        if (this.fetchLobby.isServerAvailable())
            event.getPlayer().disconnect(new TextComponent(ChatColor.RED + "The network restarting please try again later."));

        ServerInfo serverInfo = this.fetchLobby.fetchRandomHub();
        event.getPlayer().connect(serverInfo, new Callback<Boolean>() {
            @Override
            public void done(Boolean result, Throwable error) {
                if (!result) {
                    ServerInfo serverInfo = fetchLobby.fetchRandomHub();
                    event.getPlayer().connect(serverInfo);
                }
            }
        });
    }

    @EventHandler
    public void onJoins(PostLoginEvent event) {
        if (event.getPlayer().isForgeUser()) {
            Jedis redis = Loader.getInstance().getDatabaseConnector().getBungeeResource();
            event.getPlayer()
                    .getModList()
                    .forEach((modId, version) -> redis.sadd(
                            String.format("forge:%s/mods", event.getPlayer().getUniqueId().toString()),
                            String.format("%s:%s", modId, version)
                    ));
            redis.close();
        }
    }

    @EventHandler
    public void onCheckClient(PostLoginEvent event) {
        ProxiedPlayer player = event.getPlayer();
        // BRAND NAME SECURITY
        if (player.ClientBrandName().isEmpty()
                || player.ClientBrandName().length() >= 32
                || player.ClientBrandName().length() <= 1)
            player.disconnect(new TextComponent(ChatColor.RED + "Illegal ClientBrand."));

        // CUSTOM CLIENT HANDSHAKE
        if (player.ClientBrandName().equals("flooffclient")) {
            Jedis redis = Loader.getInstance().getDatabaseConnector().getBungeeResource();
            redis.set("client:" + player.getUniqueId().toString(), player.ClientBrandName());
            redis.set("ability:" + player.getUniqueId().toString(), "true");
            redis.close();

            // ACCEPT PACKET
            // 0x1E -> 0x4F -> 0x2F -> 0x3A
            player.sendData("FLOOFF|HANDSHAKE", new byte[] { 0x1E, 0x4F, 0x2F, 0x3A });
            player.sendData("FLOOFF|ABILITIES", new byte[] { 0x1F });
            player.sendData("FLOOFF|PINGS", new byte[] { 0x1F });
        } else {
            // SEND CLIENT BRAND TO THE REDIS STORAGE.
            Jedis redis = Loader.getInstance().getDatabaseConnector().getBungeeResource();
            redis.set("client:" + player.getUniqueId().toString(), player.ClientBrandName());
            redis.close();
        }
    }

    @EventHandler
    public void onLeave(PlayerDisconnectEvent event) {
        ProxiedPlayer player = event.getPlayer();

        Jedis redis = Loader.getInstance().getDatabaseConnector().getBungeeResource();
        redis.del("client:" + player.getUniqueId().toString());
        redis.close();
    }
}

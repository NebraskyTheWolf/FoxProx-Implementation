package net.md_5.bungee.listeners;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.permissions.DefaultPermission;
import net.md_5.bungee.api.permissions.EntityPermissions;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.samagames.persistanceapi.beans.permissions.PlayerPermissionsBean;
import net.samagames.persistanceapi.beans.players.PlayerBean;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class PermissionsManager implements Listener {
    @EventHandler
    public void onJoin(ServerConnectedEvent event) throws Exception {
        PlayerPermissionsBean permissions = ProxyServer
                .getInstance()
                .getGameServiceManager()
                .getAllPlayerPermissions(event
                        .getPlayer()
                        .handledPlayer()
                );
        event.getPlayer().setPermissions(new EntityPermissions(
                event.getPlayer().getUniqueId(),
                permissions.getApiPermissions(),
                permissions.getBukkitPermissions(),
                permissions.getBungeeRedisPermissions(),
                permissions.getModerationPermissions(),
                permissions.getProxiesPermissions(),
                permissions.getStaffPermissions()
        ));
    }

    @EventHandler
    public void onQuit(PlayerDisconnectEvent event) {
        event.getPlayer().resetPermissions();
    }

    public EntityPermissions getPlayerPermission(UUID player) {
        return ProxyServer.getInstance().getPlayer(player).getPermission();
    }
}

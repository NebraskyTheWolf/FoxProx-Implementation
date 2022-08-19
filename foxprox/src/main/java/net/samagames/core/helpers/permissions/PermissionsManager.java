package net.samagames.core.helpers.permissions;

import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.samagames.Loader;
import net.samagames.persistanceapi.beans.permissions.PlayerPermissionsBean;
import net.samagames.persistanceapi.beans.players.PlayerBean;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class PermissionsManager implements Listener {

    private final Map<UUID, EntityPermissions> PERMISSIONS_LIST = new LinkedHashMap<>();

    @EventHandler
    public void onJoin(ServerConnectedEvent event) throws Exception {
        PlayerBean player = Loader.getInstance().getGameServiceManager().getPlayer(event.getPlayer().getUniqueId());
        if (!PERMISSIONS_LIST.containsKey(player.getUuid())) {
            PlayerPermissionsBean permissions = Loader.getInstance().getGameServiceManager().getAllPlayerPermissions(player);
            this.PERMISSIONS_LIST.put(player.getUuid(), new EntityPermissions(
                    player.getUuid(),
                    permissions.getApiPermissions(),
                    permissions.getBukkitPermissions(),
                    permissions.getBungeeRedisPermissions(),
                    permissions.getModerationPermissions(),
                    permissions.getProxiesPermissions(),
                    permissions.getStaffPermissions()
            ));
        }
    }

    @EventHandler
    public void onQuit(PlayerDisconnectEvent event) {
        if (this.PERMISSIONS_LIST.containsKey(event.getPlayer().getUniqueId()))
            this.PERMISSIONS_LIST.remove(event.getPlayer().getUniqueId());
    }

    public EntityPermissions getPlayerPermission(UUID player) {
        return this.PERMISSIONS_LIST.getOrDefault(player, new DefaultPermission(player));
    }
}

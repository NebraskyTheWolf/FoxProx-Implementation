package net.samagames.core.helpers.permissions;

import net.samagames.persistanceapi.beans.permissions.*;

import java.util.UUID;

public class EntityPermissions {
    public final UUID Player;

    protected final APIPermissionsBean apiPermissionsBean;
    protected final BukkitPermissionsBean bukkitPermissionsBean;
    protected final BungeeRedisPermissionsBean bungeeRedisPermissionsBean;
    protected final ModerationPermissionsBean moderationPermissionsBean;
    protected final ProxiesPermissionsBean proxiesPermissionsBean;
    protected final StaffPermissionsBean staffPermissionsBean;

    public EntityPermissions(UUID Player,
                             APIPermissionsBean apiPermissionsBean,
                             BukkitPermissionsBean bukkitPermissionsBean,
                             BungeeRedisPermissionsBean bungeeRedisPermissionsBean,
                             ModerationPermissionsBean moderationPermissionsBean,
                             ProxiesPermissionsBean proxiesPermissionsBean,
                             StaffPermissionsBean staffPermissionsBean) {
        this.Player = Player;
        this.apiPermissionsBean = apiPermissionsBean;
        this.bukkitPermissionsBean = bukkitPermissionsBean;
        this.bungeeRedisPermissionsBean = bungeeRedisPermissionsBean;
        this.moderationPermissionsBean = moderationPermissionsBean;
        this.proxiesPermissionsBean = proxiesPermissionsBean;
        this.staffPermissionsBean = staffPermissionsBean;
    }

    public UUID getPlayer() {
        return Player;
    }

    public APIPermissionsBean getApiPermissionsBean() {
        return apiPermissionsBean;
    }

    public BungeeRedisPermissionsBean getBungeeRedisPermissionsBean() {
        return bungeeRedisPermissionsBean;
    }

    public ModerationPermissionsBean getModerationPermissionsBean() {
        return moderationPermissionsBean;
    }

    public ProxiesPermissionsBean getProxiesPermissionsBean() {
        return proxiesPermissionsBean;
    }

    public StaffPermissionsBean getStaffPermissionsBean() {
        return staffPermissionsBean;
    }
}

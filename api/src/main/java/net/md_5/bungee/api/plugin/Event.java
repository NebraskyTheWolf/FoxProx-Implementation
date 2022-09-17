package net.md_5.bungee.api.plugin;

import net.md_5.bungee.api.ProxyServer;
import net.samagames.databases.impl.PubSubAPI;
import net.samagames.persistanceapi.GameServiceManager;
import net.samagames.persistanceapi.datamanager.PermissionsManager;
import redis.clients.jedis.Jedis;

/**
 * Dummy class which all callable events must extend.
 */
public abstract class Event
{

    /**
     * Method called after this event has been dispatched to all handlers.
     */
    public void postCall()
    {
    }
    // Waterfall start - Add callEvent() shortcut, borrowed from PaperMC/Paper
    /**
     * Calls the event and tests if cancelled.
     *
     * @return false if event was cancelled, if cancellable. otherwise true.
     */
    public final boolean callEvent() {
        net.md_5.bungee.api.ProxyServer.getInstance().getPluginManager().callEvent(this);
        if (this instanceof Cancellable) {
            return !((Cancellable) this).isCancelled();
        }
        return true;
    }

    public Jedis getHandles() {
        return ProxyServer.getInstance().getDatabaseConnector().getBungeeResource();
    }

    public void killHandles(Jedis handle) {
        handle.close();
    }

    public PubSubAPI getPubSub() {
        return ProxyServer.getInstance().getPubSub();
    }

    public GameServiceManager getDatabase() {
        return ProxyServer.getInstance().getGameServiceManager();
    }

    // Waterfall end
}

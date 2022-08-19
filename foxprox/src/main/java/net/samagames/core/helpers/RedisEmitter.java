package net.samagames.core.helpers;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.event.PubSubMessage;
import net.md_5.bungee.api.plugin.PluginManager;
import net.samagames.Loader;
import net.samagames.core.databases.api.IPacketsReceiver;
import net.samagames.core.databases.api.IPatternReceiver;

public class RedisEmitter implements IPacketsReceiver {

    public final PluginManager manager = ProxyServer.getInstance().getPluginManager();

    @Override
    public void receive(String channel, String packet) {
        ProxyServer.getInstance().getPluginManager().eventBus.post(new PubSubMessage(
                        "",
                        channel,
                        packet),
                manager::handleEventException
        );
    }

    public static class PatternEmitter implements IPatternReceiver {
        @Override
        public void receive(String pattern, String channel, String packet) {
            ProxyServer.getInstance().getPluginManager().eventBus.post(new PubSubMessage(
                            pattern,
                            channel,
                            packet),
                    Loader.getInstance().getProxy().getPluginManager()::handleEventException
            );
        }
    }
}

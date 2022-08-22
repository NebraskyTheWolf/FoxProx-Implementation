package net.samagames.core.helpers.servers;

import net.md_5.bungee.api.config.ServerInfo;
import net.samagames.Loader;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;

public class FetchLobby {

    private final Loader plugin;

    public FetchLobby(Loader plugin) {
        this.plugin = plugin;
    }

    public List<ServerInfo> fetchAllLobby() {
        List<ServerInfo> HUBS = new CopyOnWriteArrayList<>();
        this.plugin.getProxy().getServers().forEach(new BiConsumer<String, ServerInfo>() {
            @Override
            public void accept(String name, ServerInfo data) {
                if (name.contains("Hub_"))
                    if (!(data.getPlayers().size() >= 20))
                        HUBS.add(data);
            }
        });
        return HUBS;
    }

    public ServerInfo fetchRandomHub() {
        return this.fetchAllLobby().get(new Random().nextInt(this.fetchAllLobby().size() - 1));
    }

    public boolean isServerAvailable() {
        return this.fetchAllLobby().size() < 1;
    }
}

package net.md_5.bungee.api;

import net.md_5.bungee.api.config.ServerInfo;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;

public class FetchLobby {

    public List<ServerInfo> fetchAllLobby() {
        List<ServerInfo> lobby = new CopyOnWriteArrayList<>();
        ProxyServer.getInstance().getServers().forEach(new BiConsumer<String, ServerInfo>() {
            @Override
            public void accept(String name, ServerInfo data) {
                if (name.contains("Hub_"))
                    lobby.add(data);
            }
        });
        return lobby;
    }

    public ServerInfo fetchRandomHub() {
        return this.fetchAllLobby().get(new Random().nextInt(this.fetchAllLobby().size() - 1));
    }

    public boolean isServerAvailable() {
        return false;
    }
}

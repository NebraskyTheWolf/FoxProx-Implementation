package net.samagames.core.helpers.servers;

import net.samagames.Loader;
import java.net.InetSocketAddress;

public class ServerUtils {
    public void createServer(String name, String ip, String port) {
        if (!Loader.getInstance()
                .getProxy()
                .getServers()
                .containsKey(name)) {
            Loader.getInstance()
                    .getProxy()
                    .getServers()
                    .put(name, Loader.getInstance().getProxy().constructServerInfo(name, new InetSocketAddress(ip, Integer.parseInt(port)), "Please connect with mc.floofflight.net.", false));
            Loader.getInstance().log(String.format("Adding %s to the server list.", name));
        }
    }

    public void stopServer(String name) {
        Loader.getInstance()
                .getProxy()
                .getServers()
                .remove(name);
        Loader.getInstance().log(String.format("Deleting %s from the network.", name));
    }

    public void startServer(String name) {
        if (Loader.getInstance().getProxy().getServers().containsKey(name))
            Loader.getInstance().log(String.format("%s Successfully started!", name));
        else
            Loader.getInstance().log(String.format("Unable to determine if %s is online. Please check the redis server lists.", name));
    }
}

package net.md_5.bungee.listeners;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.PubSubMessage;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.samagames.api.channels.JsonModMessage;
import net.samagames.api.channels.ModChannel;

import java.net.InetSocketAddress;
import java.util.Map;

public class ServersListeners implements Listener {
    @EventHandler
    public void onPubSub(PubSubMessage message) {
        if (message.getChannel().equals("servers")) {
            String[] arguments = message.getMessage().split(" ");

            Map<String, ServerInfo> servers = ProxyServer.getInstance().getServers();

            switch (arguments[0]) {
                case "heartbeat": {
                    String name = arguments[1];
                    String ip   = arguments[2];
                    String port = arguments[3];

                    if (!servers.containsKey(name)) {
                        servers.put(name, ProxyServer.getInstance().constructServerInfo(name, new InetSocketAddress(ip, Integer.parseInt(port)), "Please connect with mc.floofflight.net.", false));
                        ProxyServer.getInstance().getLogger().info(String.format("Adding %s to the server list. with IP: %s, PORT: %s", name, ip, port));
                    }
                }
                break;
                case "stop": {
                    String name = arguments[1];
                    if (servers.containsKey(name)) {
                        servers.remove(name);
                        ProxyServer.getInstance().getLogger().info(String.format("Deleting %s from the network.", name));
                    }
                }
                break;
                case "starts": {
                    if (ProxyServer.getInstance().getConfig().isLogCommands()) {
                        ProxyServer.getInstance().sendModerationMessage(ModChannel.INFORMATION,
                                new JsonModMessage()
                                        .setSender("Hydroangeas")
                                        .setMessage(arguments[1] + " Successfully started.")
                        );
                    }
                }
                break;
                default:
                    ProxyServer.getInstance().getLogger().info(String.format("Unable to process %s, Invalid arguments", arguments[0]));
                break;
            }
        }
    }
}

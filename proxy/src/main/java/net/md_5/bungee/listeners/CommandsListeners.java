package net.md_5.bungee.listeners;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.event.PubSubMessage;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.samagames.api.pubsub.PendingMessage;

public class CommandsListeners implements Listener {

    @EventHandler
    public void onCommand(PubSubMessage event) {
        if (event.getChannel().equals("command.servers.all")) {
            if (event.getMessage().split(" ")[1].equals("stop")) {
                event.getPubSub().send(new PendingMessage("response.samacli", "server FoxProx SHUTDOWN", new Runnable() {
                    @Override
                    public void run() {
                        ProxyServer.getInstance().stop("STOPPED BY CLI ADMINISTRATOR REQUEST ID: " + event.getMessage().split(" ")[2]);
                    }
                }));
            }
        }
    }
}

package net.samagames.core.helpers.servers;

import net.md_5.bungee.api.event.PubSubMessage;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.samagames.Loader;

public class ServersListeners implements Listener {
    @EventHandler
    public void onPubSub(PubSubMessage message) {
        if (message.getChannel().equals("servers")) {
            String[] arguments = message.getMessage().split(" ");
            switch (arguments[0]) {
                case "heartbeat": {
                    Loader.getInstance().getServerUtils().createServer(arguments[1], arguments[2], arguments[3]);
                }
                break;
                case "stop": {
                    Loader.getInstance().getServerUtils().stopServer(arguments[1]);
                }
                break;
                case "start": {
                    Loader.getInstance().getServerUtils().startServer(arguments[1]);
                }
                break;
                default:
                    Loader.getInstance().log(String.format("Unable to process %s, Invalid arguments", arguments[0]));
                break;
            }
        }
    }
}

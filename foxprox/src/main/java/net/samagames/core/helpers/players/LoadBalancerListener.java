package net.samagames.core.helpers.players;

import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class LoadBalancerListener implements Listener {

    @EventHandler
    public void onPostLogin(PostLoginEvent event) {}
}

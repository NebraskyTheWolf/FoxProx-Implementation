package net.md_5.bungee.listeners;

import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import java.util.LinkedHashMap;
import java.util.Map;

public class LoadBalancerListener implements Listener {

    private final Map<String, String> IP_LIST = new LinkedHashMap<>();

    @EventHandler
    public void onPostLogin(PostLoginEvent event) {}
}

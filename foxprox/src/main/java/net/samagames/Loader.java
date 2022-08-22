package net.samagames;

import net.md_5.bungee.api.ProxyServer;
import net.samagames.core.databases.DatabaseConnector;
import net.samagames.core.databases.RedisServer;
import net.samagames.core.databases.impl.PubSubAPI;
import net.samagames.core.helpers.RedisEmitter;
import net.samagames.core.helpers.channel.ModerationListener;
import net.samagames.core.helpers.permissions.PermissionsManager;
import net.samagames.core.helpers.players.LoadBalancerListener;
import net.samagames.core.helpers.players.NetworkListeners;
import net.samagames.core.helpers.players.PlayerJoinListener;
import net.samagames.core.helpers.players.ProxyPingListener;
import net.samagames.core.helpers.servers.FetchLobby;
import net.samagames.core.helpers.servers.ServerUtils;
import net.samagames.core.helpers.servers.ServersListeners;
import net.samagames.persistanceapi.GameServiceManager;
import net.samagames.persistanceapi.beans.utils.BungeeConfigBean;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Loader {
    private static Loader instance;
    private final GameServiceManager manager;
    private final PermissionsManager permissionsManager;
    private final ServerUtils serverUtils;
    private final ServersListeners serversListeners;
    private final ModerationListener moderationListener;

    private final PlayerJoinListener playerJoinListener;

    private final ProxyPingListener proxyPingListener;

    private final NetworkListeners networkListeners;

    private final LoadBalancerListener loadBalancerListener;

    private final ScheduledExecutorService executor;
    private final DatabaseConnector databaseConnector;
    private final PubSubAPI pubSub;

    private final String url;
    private final String username;
    private final String password;

    private final BungeeConfigBean bungeeConfig;

    private final ProxyServer server;

    private final FetchLobby fetchLobby;

    public Loader(String url, String username, String password, RedisServer bungee, ProxyServer server) throws Exception {
        instance = this;
        this.url = url;
        this.username = username;
        this.password = password;
        this.server = server;

        this.executor = Executors.newScheduledThreadPool(32);

        this.manager = new GameServiceManager(this.url, this.username, this.password, 1, 10);
        this.bungeeConfig = this.getGameServiceManager().getBungeeConfig();
        this.serverUtils = new ServerUtils();

        this.permissionsManager = new PermissionsManager();
        this.serversListeners = new ServersListeners();
        this.moderationListener = new ModerationListener();
        this.playerJoinListener = new PlayerJoinListener();
        this.proxyPingListener = new ProxyPingListener();
        this.networkListeners = new NetworkListeners();
        this.loadBalancerListener = new LoadBalancerListener();

        this.fetchLobby = new FetchLobby(this);

        this.databaseConnector = new DatabaseConnector(this, bungee);
        this.pubSub = new PubSubAPI(this);

        this.pubSub.subscribe("*", new RedisEmitter());
        this.pubSub.subscribe("*", new RedisEmitter.PatternEmitter());

        this.server
                .getPluginManager()
                .registerListener(this.permissionsManager);
        this.server
                .getPluginManager()
                .registerListener(this.serversListeners);
        this.server
                .getPluginManager()
                .registerListener(this.moderationListener);
        this.server
                .getPluginManager()
                .registerListener(this.playerJoinListener);
        this.server
                .getPluginManager()
                .registerListener(this.proxyPingListener);
        this.server
                .getPluginManager()
                .registerListener(this.networkListeners);
        this.server
                .getPluginManager()
                .registerListener(this.loadBalancerListener);
    }

    public void onShutdown() {
        this.databaseConnector.killConnection();
        this.pubSub.disable();
    }

    public GameServiceManager getGameServiceManager() {
        return manager;
    }
    public static Loader getInstance() {
        return instance;
    }
    public ProxyServer getProxy() {
        return this.server;
    }
    public PermissionsManager getPermissionsManager() {
        return permissionsManager;
    }
    public ServerUtils getServerUtils() {
        return serverUtils;
    }
    public void log(String message) {
        ProxyServer.getInstance().getLogger().warning(message);
    }
    public ScheduledExecutorService getExecutor() {
        return executor;
    }
    public PubSubAPI getPubSub() {
        return pubSub;
    }
    public DatabaseConnector getDatabaseConnector() {
        return databaseConnector;
    }
    public BungeeConfigBean getBungeeConfig() {
        return bungeeConfig;
    }
    public FetchLobby getFetchLobby() {
        return fetchLobby;
    }
}

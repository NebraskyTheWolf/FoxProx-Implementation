package net.md_5.bungee;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import dev._2lstudios.flamecord.FlameCord;
import dev._2lstudios.flamecord.commands.FlameCordCommand;
import dev._2lstudios.flamecord.configuration.ModulesConfiguration;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.github.waterfallmc.waterfall.conf.WaterfallConfiguration;
import io.github.waterfallmc.waterfall.event.ProxyExceptionEvent;
import io.github.waterfallmc.waterfall.exception.ProxyPluginEnableDisableException;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.util.ResourceLeakDetector;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;
import lombok.Synchronized;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.Favicon;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ReconnectHandler;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.Title;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.KeybindComponent;
import net.md_5.bungee.api.chat.ScoreComponent;
import net.md_5.bungee.api.chat.SelectorComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import net.md_5.bungee.api.config.ConfigurationAdapter;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.chat.ComponentSerializer;
import net.md_5.bungee.chat.KeybindComponentSerializer;
import net.md_5.bungee.chat.ScoreComponentSerializer;
import net.md_5.bungee.chat.SelectorComponentSerializer;
import net.md_5.bungee.chat.TextComponentSerializer;
import net.md_5.bungee.chat.TranslatableComponentSerializer;
import net.md_5.bungee.command.*;
import net.md_5.bungee.compress.CompressFactory;
import net.md_5.bungee.conf.Configuration;
import net.md_5.bungee.conf.YamlConfig;
import net.md_5.bungee.forge.ForgeConstants;
import net.md_5.bungee.module.ModuleManager;
import net.md_5.bungee.module.cmd.alert.CommandAlert;
import net.md_5.bungee.module.cmd.alert.CommandAlertRaw;
import net.md_5.bungee.module.cmd.find.CommandFind;
import net.md_5.bungee.module.cmd.list.CommandList;
import net.md_5.bungee.module.cmd.send.CommandSend;
import net.md_5.bungee.module.reconnect.yaml.YamlReconnectHandler;
import net.md_5.bungee.netty.PipelineUtils;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.ProtocolConstants;
import net.md_5.bungee.protocol.packet.PluginMessage;
import net.md_5.bungee.query.RemoteQuery;
import net.md_5.bungee.scheduler.BungeeScheduler;
import net.md_5.bungee.util.CaseInsensitiveMap;
import net.samagames.Loader;
import net.samagames.core.databases.RedisServer;
import redis.clients.jedis.Jedis;

/**
 * Main BungeeCord proxy class.
 */
public class BungeeCord extends ProxyServer
{

    private static Loader FoxProxManager;

    /**
     * Current operation state.
     */
    public volatile boolean isRunning;
    /**
     * Configuration.
     */
    @Getter
    public final Configuration config = new WaterfallConfiguration();
    /**
     * Localization bundle.
     */
    private ResourceBundle baseBundle;
    private ResourceBundle customBundle;
    public EventLoopGroup bossEventLoopGroup, workerEventLoopGroup;
    /**
     * locations.yml save thread.
     */
    private final Timer saveThread = new Timer( "Reconnect Saver" );
    // private final Timer metricsThread = new Timer( "Metrics Thread" ); // Waterfall: Disable Metrics
    /**
     * Server socket listener.
     */
    private final Collection<Channel> listeners = new HashSet<>();
    /**
     * Fully qualified connections.
     */
    private final Map<String, UserConnection> connections = new CaseInsensitiveMap<>();
    // Used to help with packet rewriting
    private final Map<UUID, UserConnection> connectionsByOfflineUUID = new HashMap<>();
    private final Map<UUID, UserConnection> connectionsByUUID = new HashMap<>();
    private final ReadWriteLock connectionLock = new ReentrantReadWriteLock();
    /**
     * Lock to protect the shutdown process from being triggered simultaneously
     * from multiple sources.
     */
    private final ReentrantLock shutdownLock = new ReentrantLock();
    /**
     * Plugin manager.
     */
    @Getter
    public final PluginManager pluginManager;
    @Getter
    @Setter
    private ReconnectHandler reconnectHandler;
    @Getter
    @Setter
    private ConfigurationAdapter configurationAdapter = new YamlConfig();
    private final Collection<String> pluginChannels = new HashSet<>();
    @Getter
    private final File pluginsFolder = new File( "plugins" );
    @Getter
    private final BungeeScheduler scheduler = new BungeeScheduler();
    // Waterfall start - Remove ConsoleReader for JLine 3 update
    /*
    @Getter
    private final ConsoleReader consoleReader;
    */
    // Waterfall end
    @Getter
    private final Logger logger;
    public final Gson gson = new GsonBuilder()
            .registerTypeAdapter( BaseComponent.class, new ComponentSerializer() )
            .registerTypeAdapter( TextComponent.class, new TextComponentSerializer() )
            .registerTypeAdapter( TranslatableComponent.class, new TranslatableComponentSerializer() )
            .registerTypeAdapter( KeybindComponent.class, new KeybindComponentSerializer() )
            .registerTypeAdapter( ScoreComponent.class, new ScoreComponentSerializer() )
            .registerTypeAdapter( SelectorComponent.class, new SelectorComponentSerializer() )
            .registerTypeAdapter( ServerPing.PlayerInfo.class, new PlayerInfoSerializer() )
            .registerTypeAdapter( Favicon.class, Favicon.getFaviconTypeAdapter() ).create();
    // FlameCord start - 1.7.x support
    public final Gson gsonLegacy = new GsonBuilder()
            .registerTypeAdapter( BaseComponent.class, new ComponentSerializer() )
            .registerTypeAdapter( TextComponent.class, new TextComponentSerializer() )
            .registerTypeAdapter( TranslatableComponent.class, new TranslatableComponentSerializer() )
            .registerTypeAdapter( ServerPing.PlayerInfo.class, new PlayerInfoSerializer( ProtocolConstants.MINECRAFT_1_7_2 ) )
            .registerTypeAdapter( Favicon.class, Favicon.getFaviconTypeAdapter() ).create();
    // FlameCord end - 1.7.x support
    @Getter
    private ConnectionThrottle connectionThrottle;
    private final ModuleManager moduleManager = new ModuleManager();

    {
        // TODO: Proper fallback when we interface the manager
        registerChannel( "BungeeCord" );
    }

    public static BungeeCord getInstance()
    {
        return (BungeeCord) ProxyServer.getInstance();
    }

    @SuppressFBWarnings("DM_DEFAULT_ENCODING")
    public BungeeCord() throws IOException
    {
        Preconditions.checkState( new File( "." ).getAbsolutePath().indexOf( '!' ) == -1, "Cannot use FlameCord in directory with ! in path." );

        try
        {
            baseBundle = ResourceBundle.getBundle( "messages" );
        } catch ( MissingResourceException ex )
        {
            baseBundle = ResourceBundle.getBundle( "messages", Locale.ENGLISH );
        }
        reloadMessages();

        System.setProperty( "library.jansi.version", "BungeeCord" );

        logger = io.github.waterfallmc.waterfall.log4j.WaterfallLogger.create();
        // Waterfall end

        pluginManager = new PluginManager( this );
        // FlameCord - We register commands in our new method

        if ( !Boolean.getBoolean( "net.md_5.bungee.native.disable" ) )
        {
            if ( EncryptionUtil.nativeFactory.load() )
            {
                logger.info( "Using mbed TLS based native cipher." );
            } else
            {
                logger.info( "Using standard Java JCE cipher." );
            }
            if ( CompressFactory.zlib.load() )
            {
                logger.info( "Using zlib based native compressor." );
            } else
            {
                logger.info( "Using standard Java compressor." );
            }
        }
    }

    /**
     * Start this proxy instance by loading the configuration, plugins and
     * starting the connect thread.
     *
     * @throws Exception any critical errors encountered
     */
    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_BAD_PRACTICE")
    public void start() throws Exception
    {
        System.setProperty( "io.netty.selectorAutoRebuildThreshold", "0" ); // Seems to cause Bungee to stop accepting connections
        if ( System.getProperty( "io.netty.leakDetectionLevel" ) == null && System.getProperty( "io.netty.leakDetection.level" ) == null )
        {
            ResourceLeakDetector.setLevel( ResourceLeakDetector.Level.DISABLED ); // Eats performance
        }

        bossEventLoopGroup = PipelineUtils.newEventLoopGroup( 0, new ThreadFactoryBuilder().setNameFormat( "Netty Boss IO Thread #%1$d" ).build() );
        workerEventLoopGroup = PipelineUtils.newEventLoopGroup( 0, new ThreadFactoryBuilder().setNameFormat( "Netty Worker IO Thread #%1$d" ).build() );

        pluginsFolder.mkdir();
        pluginManager.detectPlugins( pluginsFolder );

        pluginManager.loadPlugins();
        config.load();



        // FlameCord - Renew and register modules
        final Collection<String> whitelistedAddresses = new HashSet<>();

        for (final ServerInfo serverInfo : getServers().values()) {
            whitelistedAddresses.add(serverInfo.getSocketAddress().toString());
        }

        FlameCord.reload(logger, whitelistedAddresses);

        if (config.isForgeSupport()) {
            registerChannel( ForgeConstants.FML_TAG );
            registerChannel( ForgeConstants.FML_HANDSHAKE_TAG );
            registerChannel( ForgeConstants.FORGE_REGISTER );
        }

        isRunning = true;

        pluginManager.enablePlugins();

        if ( config.getThrottle() > 0 )
        {
            connectionThrottle = new ConnectionThrottle( config.getThrottle(), config.getThrottleLimit() );
        }
        startListeners();
        loadModules();

        saveThread.scheduleAtFixedRate( new TimerTask()
        {
            @Override
            public void run()
            {
                if ( getReconnectHandler() != null )
                {
                    getReconnectHandler().save();
                }
            }
        }, 0, TimeUnit.MINUTES.toMillis( 5 ) );

        Runtime.getRuntime().addShutdownHook( new Thread()
        {
            @Override
            public void run()
            {
                independentThreadStop( getTranslation( "restart" ), false );
            }
        } );

        if (config.getSqlUrl().length() < 16) {
            logger.severe("/!\\ Database not configured.");
            logger.severe("/!\\ Database not configured.");
            logger.severe("/!\\ Database not configured.");
            logger.severe("/!\\ Database not configured.");
            System.exit(1);
        }

        FoxProxManager = new Loader(config.getSqlUrl(), config.getSqlUser(), config.getSqlPass(), new RedisServer(
                "127.0.0.1",
                6379,
               "Mohammedia1970@"
        ), this);
    }

    public void startListeners()
    {
        for ( final ListenerInfo info : config.getListeners() )
        {
            if ( info.isProxyProtocol() )
            {
                getLogger().log( Level.WARNING, "Using PROXY protocol for listener {0}, please ensure this listener is adequately firewalled.", info.getSocketAddress() );

                if ( connectionThrottle != null )
                {
                    connectionThrottle = null;
                    getLogger().log( Level.WARNING, "Since PROXY protocol is in use, internal connection throttle has been disabled." );
                }
            }

            ChannelFutureListener listener = new ChannelFutureListener()
            {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception
                {
                    if ( future.isSuccess() )
                    {
                        listeners.add( future.channel() );
                        getLogger().log( Level.INFO, "Listening on {0}", info.getSocketAddress() );
                    } else
                    {
                        getLogger().log( Level.WARNING, "Could not bind to host " + info.getSocketAddress(), future.cause() );
                    }
                }
            };
            new ServerBootstrap()
                    .channel( PipelineUtils.getServerChannel( info.getSocketAddress() ) )
                    .option( ChannelOption.SO_REUSEADDR, true ) // TODO: Move this elsewhere!
                    .childAttr( PipelineUtils.LISTENER, info )
                    .childHandler( PipelineUtils.SERVER_CHILD )
                    .group( bossEventLoopGroup, workerEventLoopGroup )
                    .localAddress( info.getSocketAddress() )
                    .bind().addListener( listener );

            if ( info.isQueryEnabled() )
            {
                Preconditions.checkArgument( info.getSocketAddress() instanceof InetSocketAddress, "Can only create query listener on UDP address" );

                ChannelFutureListener bindListener = new ChannelFutureListener()
                {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception
                    {
                        if ( future.isSuccess() )
                        {
                            listeners.add( future.channel() );
                            getLogger().log( Level.INFO, "Started query on {0}", future.channel().localAddress() );
                        } else
                        {
                            getLogger().log( Level.WARNING, "Could not bind to host " + info.getSocketAddress(), future.cause() );
                        }
                    }
                };
                new RemoteQuery( this, info ).start( PipelineUtils.getDatagramChannel(), new InetSocketAddress( info.getHost().getAddress(), info.getQueryPort() ), workerEventLoopGroup, bindListener );
            }
        }
    }

    public void stopListeners()
    {
        for ( Channel listener : listeners )
        {
            getLogger().log( Level.INFO, "Closing listener {0}", listener );
            try
            {
                listener.close().syncUninterruptibly();
            } catch ( ChannelException ex )
            {
                getLogger().severe( "Could not close listen thread" );
            }
        }
        listeners.clear();
    }

    @Override
    public void stop()
    {
        stop( getTranslation( "restart" ) );
    }

    @Override
    public void stop(final String reason)
    {
        new Thread( "Shutdown Thread" )
        {
            @Override
            public void run()
            {
                independentThreadStop( reason, true );
            }
        }.start();
    }

    // This must be run on a separate thread to avoid deadlock!
    @SuppressFBWarnings("DM_EXIT")
    @SuppressWarnings("TooBroadCatch")
    private void independentThreadStop(final String reason, boolean callSystemExit)
    {
        // Acquire the shutdown lock
        // This needs to actually block here, otherwise running 'end' and then ctrl+c will cause the thread to terminate prematurely
        shutdownLock.lock();

        // Acquired the shutdown lock
        if ( !isRunning )
        {
            // Server is already shutting down - nothing to do
            shutdownLock.unlock();
            return;
        }
        isRunning = false;

        stopListeners();
        getLogger().info( "Closing pending connections" );

        connectionLock.readLock().lock();
        try
        {
            getLogger().log( Level.INFO, "Disconnecting {0} connections", connections.size() );
            for ( UserConnection user : connections.values() )
            {
                user.disconnect( reason );
            }
        } finally
        {
            connectionLock.readLock().unlock();
        }

        try
        {
            Thread.sleep( 500 );
        } catch ( InterruptedException ex )
        {
        }

        if ( reconnectHandler != null )
        {
            getLogger().info( "Saving reconnect locations" );
            reconnectHandler.save();
            reconnectHandler.close();
        }
        saveThread.cancel();
        //metricsThread.cancel(); // Waterfall: Disable Metrics

        getLogger().info( "Disabling plugins" );
        for ( Plugin plugin : Lists.reverse( new ArrayList<>( pluginManager.getPlugins() ) ) )
        {
            try
            {
                plugin.onDisable();
                for ( Handler handler : plugin.getLogger().getHandlers() )
                {
                    handler.close();
                }
            } catch ( Throwable t )
            {
                // Waterfall start - throw exception event
                String msg = "Exception disabling plugin " + plugin.getDescription().getName();
                getLogger().log( Level.SEVERE, msg, t );
                pluginManager.callEvent( new ProxyExceptionEvent( new ProxyPluginEnableDisableException( msg, t, plugin) ) );
                // Waterfall end
            }
            getScheduler().cancel( plugin );
            plugin.getExecutorService().shutdownNow();
        }

        getLogger().info( "Closing IO threads" );
                bossEventLoopGroup.shutdownGracefully();
                workerEventLoopGroup.shutdownGracefully();
                while (true) {
                    try {
                        bossEventLoopGroup.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
                        workerEventLoopGroup.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
                        break;
                    } catch (InterruptedException ignored) {}
        }

        getLogger().info( "Thank you and goodbye" );
        // Need to close loggers after last message!
        org.apache.logging.log4j.LogManager.shutdown(); // Waterfall

        // Unlock the thread before optionally calling system exit, which might invoke this function again.
        // If that happens, the system will obtain the lock, and then see that isRunning == false and return without doing anything.
        shutdownLock.unlock();

        if ( callSystemExit )
        {
            System.exit( 0 );
        }
    }

    /**
     * Broadcasts a packet to all clients that is connected to this instance.
     *
     * @param packet the packet to send
     */
    public void broadcast(DefinedPacket packet)
    {
        connectionLock.readLock().lock();
        try
        {
            for ( UserConnection con : connections.values() )
            {
                con.unsafe().sendPacket( packet );
            }
        } finally
        {
            connectionLock.readLock().unlock();
        }
    }

    @Override
    public String getName()
    {
        return "FoxProx";
    }

    @Override
    public String getVersion()
    {
        return "1.0.0";
    }

    public void reloadMessages()
    {
        File file = new File( "messages.properties" );
        if ( file.isFile() )
        {
            try ( FileReader rd = new FileReader( file ) )
            {
                customBundle = new PropertyResourceBundle( rd );
            } catch ( IOException ex )
            {
                getLogger().log( Level.SEVERE, "Could not load custom messages.properties", ex );
            }
        }
    }

    @Override
    public String getTranslation(String name, Object... args)
    {
        // FlameCord - Use own translation system
        return FlameCord.getInstance().getMessagesConfiguration().getTranslation(name, args);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<ProxiedPlayer> getPlayers()
    {
        connectionLock.readLock().lock();
        try
        {
            return Collections.unmodifiableCollection( new HashSet( connections.values() ) );
        } finally
        {
            connectionLock.readLock().unlock();
        }
    }

    @Override
    public int getOnlineCount()
    {
        return connections.size();
    }

    @Override
    public ProxiedPlayer getPlayer(String name)
    {
        connectionLock.readLock().lock();
        try
        {
            return connections.get( name );
        } finally
        {
            connectionLock.readLock().unlock();
        }
    }

    public UserConnection getPlayerByOfflineUUID(UUID name)
    {
        // Waterfall start - Skip lock if we get a v4 UUID, we're not gonna be in here
        if (name != null && name.version() == 4) {
            return null;
        }
        // Waterfall end
        connectionLock.readLock().lock();
        try
        {
            return connectionsByOfflineUUID.get( name );
        } finally
        {
            connectionLock.readLock().unlock();
        }
    }

    @Override
    public ProxiedPlayer getPlayer(UUID uuid)
    {
        connectionLock.readLock().lock();
        try
        {
            return connectionsByUUID.get( uuid );
        } finally
        {
            connectionLock.readLock().unlock();
        }
    }

    @Override
    public Map<String, ServerInfo> getServers()
    {
        return config.getServers();
    }

    // Waterfall start
    @Override
    public Map<String, ServerInfo> getServersCopy()
    {
        return config.getServersCopy();
    }
    // Waterfall end

    @Override
    public ServerInfo getServerInfo(String name)
    {
        return config.getServerInfo( name ); // Waterfall
    }

    @Override
    @Synchronized("pluginChannels")
    public void registerChannel(String channel)
    {
        pluginChannels.add( channel );
    }

    @Override
    @Synchronized("pluginChannels")
    public void unregisterChannel(String channel)
    {
        pluginChannels.remove( channel );
    }

    @Override
    @Synchronized("pluginChannels")
    public Collection<String> getChannels()
    {
        return Collections.unmodifiableCollection( pluginChannels );
    }

    public PluginMessage registerChannels(int protocolVersion)
    {
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_13 )
        {
            return new PluginMessage( "minecraft:register", Util.format( Iterables.transform( pluginChannels, PluginMessage.MODERNISE ), "\00" ).getBytes( Charsets.UTF_8 ), false );
        }

        return new PluginMessage( "REGISTER", Util.format( pluginChannels, "\00" ).getBytes( Charsets.UTF_8 ), false );
    }

    @Override
    public int getProtocolVersion()
    {
        return ProtocolConstants.SUPPORTED_VERSION_IDS.get( ProtocolConstants.SUPPORTED_VERSION_IDS.size() - 1 );
    }

    @Override
    public String getGameVersion()
    {
        return getConfig().getGameVersion(); // Waterfall
    }

    @Override
    public ServerInfo constructServerInfo(String name, InetSocketAddress address, String motd, boolean restricted)
    {
        return constructServerInfo( name, (SocketAddress) address, motd, restricted );
    }

    @Override
    public ServerInfo constructServerInfo(String name, SocketAddress address, String motd, boolean restricted)
    {
        return new BungeeServerInfo( name, address, motd, restricted );
    }

    @Override
    public CommandSender getConsole()
    {
        return ConsoleCommandSender.getInstance();
    }

    @Override
    public void broadcast(String message)
    {
        broadcast( TextComponent.fromLegacyText( message ) );
    }

    @Override
    public void broadcast(BaseComponent... message)
    {
        getConsole().sendMessage( BaseComponent.toLegacyText( message ) );
        for ( ProxiedPlayer player : getPlayers() )
        {
            player.sendMessage( message );
        }
    }

    @Override
    public void broadcast(BaseComponent message)
    {
        getConsole().sendMessage( message.toLegacyText() );
        for ( ProxiedPlayer player : getPlayers() )
        {
            player.sendMessage( message );
        }
    }

    public void addConnection(UserConnection con)
    {
        connectionLock.writeLock().lock();
        try
        {
            connections.put( con.getName(), con );
            connectionsByUUID.put( con.getUniqueId(), con );
            connectionsByOfflineUUID.put( con.getPendingConnection().getOfflineId(), con );
        } finally
        {
            connectionLock.writeLock().unlock();
        }
    }

    public void removeConnection(UserConnection con)
    {
        connectionLock.writeLock().lock();
        try
        {
            // TODO See #1218
            if ( connections.get( con.getName() ) == con )
            {
                connections.remove( con.getName() );
                connectionsByUUID.remove( con.getUniqueId() );
                connectionsByOfflineUUID.remove( con.getPendingConnection().getOfflineId() );
            }
        } finally
        {
            connectionLock.writeLock().unlock();
        }
    }

    @Override
    public Collection<String> getDisabledCommands()
    {
        return config.getDisabledCommands();
    }

    @Override
    public Collection<ProxiedPlayer> matchPlayer(final String partialName)
    {
        Preconditions.checkNotNull( partialName, "partialName" );

        ProxiedPlayer exactMatch = getPlayer( partialName );
        if ( exactMatch != null )
        {
            return Collections.singleton( exactMatch );
        }

        return Sets.newHashSet(getPlayers().stream().filter(input -> input != null && input.getName().toLowerCase(Locale.ROOT).startsWith(partialName.toLowerCase(Locale.ROOT))).collect(Collectors.toList()));
    }

    @Override
    public Title createTitle()
    {
        return new BungeeTitle();
    }

    @Override
    public Jedis getRedisConnection() {
        Preconditions.checkNotNull(FoxProxManager, "FoxProx is not initialized.");
        return FoxProxManager.getDatabaseConnector().getBungeeResource();
    }

    @Override
    public void publish(String channel, String message) {
        Preconditions.checkNotNull(FoxProxManager, "FoxProx is not initialized.");
        FoxProxManager.getPubSub().send(channel, message);
    }

    // FlameCord - Method to simplify module registering
    public void loadModules() {
        pluginManager.registerCommand(null, new CommandEnd());
        pluginManager.registerCommand(null, new CommandHub());
        pluginManager.registerCommand(null, new CommandSEnd());
        pluginManager.registerCommand(null, new CommandBungee());
        pluginManager.registerCommand(null, new CommandServer());

        pluginManager.loadPlugins();
    }

    public static Loader getFoxProxManager() {
        return FoxProxManager;
    }
}

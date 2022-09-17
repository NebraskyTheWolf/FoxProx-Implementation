package net.samagames.databases;

import net.md_5.bungee.api.ProxyServer;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import java.util.concurrent.TimeUnit;

public class DatabaseConnector {
    private final ProxyServer plugin;
    private JedisPool cachePool;
    private RedisServer bungee;

    public DatabaseConnector(ProxyServer plugin)
    {
        this.plugin = plugin;
    }

    public DatabaseConnector(ProxyServer plugin, RedisServer bungee)
    {
        this.plugin = plugin;
        this.bungee = bungee;

        initiateConnection();
    }

    public Jedis getBungeeResource()
    {
        return cachePool.getResource();
    }

    public void killConnection()
    {
        cachePool.close();
        cachePool.destroy();
    }

    private void initiateConnection()
    {
        connect();

        this.plugin.getExecutor().scheduleAtFixedRate(() ->
        {
            try
            {
                cachePool.getResource().close();
            } catch (Exception e)
            {
                e.printStackTrace();
                plugin.getLogger().severe("Error redis connection, Try to reconnect!");
                connect();
            }
        }, 0, 10, TimeUnit.SECONDS);
    }

    private void connect()
    {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(-1);
        config.setJmxEnabled(false);

        try
        {
            this.cachePool = new JedisPool(config, this.bungee.getIp(), this.bungee.getPort(), 0, this.bungee.getPassword());
            this.cachePool.getResource().close();

            ProxyServer.getInstance().getLogger().info("Connected to database.");
        }
        catch (Exception e)
        {
            plugin.getLogger().severe("Can't connect to the database!");
            plugin.stop("Can't connect to database");
        }
    }
}

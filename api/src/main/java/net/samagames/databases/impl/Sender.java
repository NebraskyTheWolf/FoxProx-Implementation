package net.samagames.databases.impl;

import net.md_5.bungee.api.ProxyServer;
import net.samagames.api.pubsub.ISender;
import net.samagames.api.pubsub.PendingMessage;
import redis.clients.jedis.Jedis;

import java.util.concurrent.LinkedBlockingQueue;

class Sender implements Runnable, ISender
{

    private final LinkedBlockingQueue<PendingMessage> pendingMessages = new LinkedBlockingQueue<>();
    private final ProxyServer connector;
    private Jedis jedis;

    public Sender(ProxyServer connector)
    {
        this.connector = connector;
    }

    public void publish(PendingMessage message)
    {
        pendingMessages.add(message);
    }

    @Override
    public void run()
    {
        fixDatabase();
        while (true)
        {
            PendingMessage message;
            try
            {
                message = pendingMessages.take();
            } catch (InterruptedException e)
            {
                e.printStackTrace();
                jedis.close();
                return;
            }

            boolean published = false;
            while (!published)
            {
                try
                {
                    jedis.publish(message.getChannel(), message.getMessage());
                    message.runAfter();
                    published = true;
                } catch (Exception e)
                {
                    fixDatabase();
                }
            }
        }
    }

    private void fixDatabase()
    {
        try
        {
            jedis = connector.getRedisConnection();
        } catch (Exception e)
        {
            ProxyServer.getInstance().getLogger().warning("[Publisher] Cannot connect to redis server : " + e.getMessage() + ". Retrying in 5 seconds.");
            try
            {
                Thread.sleep(5000);
                fixDatabase();
            } catch (InterruptedException e1)
            {
                e1.printStackTrace();
            }
        }
    }
}
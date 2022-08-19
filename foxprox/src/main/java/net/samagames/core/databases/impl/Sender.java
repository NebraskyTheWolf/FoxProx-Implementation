package net.samagames.core.databases.impl;

import net.samagames.Loader;
import net.samagames.core.databases.api.ISender;
import net.samagames.core.databases.api.PendingMessage;
import redis.clients.jedis.Jedis;

import java.util.concurrent.LinkedBlockingQueue;

class Sender implements Runnable, ISender
{

    private final LinkedBlockingQueue<PendingMessage> pendingMessages = new LinkedBlockingQueue<>();
    private final Loader connector;
    private Jedis jedis;

    public Sender(Loader connector)
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
            jedis = connector.getDatabaseConnector().getBungeeResource();
        } catch (Exception e)
        {
            Loader.getInstance().log("[Publisher] Cannot connect to redis server : " + e.getMessage() + ". Retrying in 5 seconds.");
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
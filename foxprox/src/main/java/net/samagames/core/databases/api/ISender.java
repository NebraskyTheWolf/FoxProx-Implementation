package net.samagames.core.databases.api;

public interface ISender
{
    /**
     * Publish a given message
     *
     * @param message Message
     */
    void publish(PendingMessage message);
}
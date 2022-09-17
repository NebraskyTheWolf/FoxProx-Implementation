package net.samagames.api.pubsub;

public interface ISender
{
    /**
     * Publish a given message
     *
     * @param message Message
     */
    void publish(PendingMessage message);
}
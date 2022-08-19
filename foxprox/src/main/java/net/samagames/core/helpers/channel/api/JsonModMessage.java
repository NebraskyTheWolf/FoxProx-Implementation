package net.samagames.core.helpers.channel.api;

import net.md_5.bungee.api.ChatColor;

public class JsonModMessage {
    protected String sender;
    protected ModChannel modChannel;
    protected String message;

    /**
     * Set the sender of the message
     *
     * @param sender Message's sender
     */
    public void setSender(String sender)
    {
        this.sender = sender;
    }

    /**
     * Set the channel of the moderator message
     *
     * @param modChannel Message's channel
     */
    public void setModChannel(ModChannel modChannel)
    {
        this.modChannel = modChannel;
    }

    /**
     * Set the prefix color of the sender
     *
     * @param senderPrefix Prefix's color
     */

    /**
     * Set the content of the message
     *
     * @param message Message's content
     */
    public void setMessage(String message)
    {
        this.message = message;
    }

    /**
     * Get the sender of the message
     *
     * @return Message's sender
     */
    public String getSender()
    {
        return this.sender;
    }

    /**
     * Get the channel of the moderator message
     *
     * @return Message's channel
     */
    public ModChannel getModChannel()
    {
        return this.modChannel;
    }

    /**
     * Get the prefix color of the sender
     *
     * @return Prefix's color
     */
    /**
     * Get the content of the message
     *
     * @return Message's content
     */
    public String getMessage()
    {
        return this.message;
    }
}

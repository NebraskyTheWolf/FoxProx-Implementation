package net.md_5.bungee.api.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.md_5.bungee.api.plugin.Event;

@Data
@ToString(callSuper = false)
@EqualsAndHashCode(callSuper = false)
public class PubSubMessage extends Event {
    private final String pattern;
    private final String channel;
    private final String message;

    public PubSubMessage(String pattern, String channel, String message)
    {
        this.pattern = pattern;
        this.channel = channel;
        this.message = message;
    }
}

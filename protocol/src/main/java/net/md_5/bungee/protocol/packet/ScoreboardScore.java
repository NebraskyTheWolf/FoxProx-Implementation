package net.md_5.bungee.protocol.packet;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.ProtocolConstants;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ScoreboardScore extends DefinedPacket
{

    private String itemName;
    /**
     * 0 = create / update, 1 = remove.
     */
    private byte action;
    private String scoreName;
    private int value;

    @Override
    public void read(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        // FlameCord start - 1.7.x support
        if ( ProtocolConstants.isBeforeOrEq( protocolVersion, ProtocolConstants.MINECRAFT_1_7_6 ) )
        {
            itemName = readString( buf );
            action = buf.readByte();
            if ( action != 1 )
            {
                scoreName = readString( buf );
                value = buf.readInt();
            }
            return;
        }
        // FlameCord end - 1.7.x support

        itemName = readString( buf );
        action = buf.readByte();
        scoreName = readString( buf );
        if ( action != 1 )
        {
            value = readVarInt( buf );
        }
    }

    @Override
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        // FlameCord start - 1.7.x support
        if ( ProtocolConstants.isBeforeOrEq( protocolVersion, ProtocolConstants.MINECRAFT_1_7_6 ) )
        {
            writeString( itemName, buf );
            buf.writeByte( action );
            if ( action != 1 )
            {
                writeString( scoreName, buf );
                buf.writeInt( value );
            }
            return;
        }
        // FlameCord end - 1.7.x support

        writeString( itemName, buf );
        buf.writeByte( action );
        writeString( scoreName, buf );
        if ( action != 1 )
        {
            writeVarInt( value, buf );
        }
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}

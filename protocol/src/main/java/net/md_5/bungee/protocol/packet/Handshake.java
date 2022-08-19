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
public class Handshake extends DefinedPacket
{

    private int protocolVersion;
    private String host;
    private int port;
    private int requestedProtocol;

    @Override
    public void read(ByteBuf buf)
    {
        protocolVersion = readVarInt( buf );
        host = readString( buf, 255 );
        port = buf.readUnsignedShort();
        requestedProtocol = readVarInt( buf );
    }

    @Override
    public void write(ByteBuf buf)
    {
        writeVarInt( protocolVersion, buf );
        writeString( host, buf );
        buf.writeShort( port );
        writeVarInt( requestedProtocol, buf );
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }

    @Override
    public int expectedMaxLength(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion) {
        return 1036 + 256; // FlameCord - Apply packet limits
    }

    @Override
    public int expectedMinLength(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion) {
        return 5; // FlameCord - Apply packet limits
    }
}

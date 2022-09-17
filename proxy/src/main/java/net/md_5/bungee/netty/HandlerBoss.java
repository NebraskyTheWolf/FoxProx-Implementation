package net.md_5.bungee.netty;

import com.google.common.base.Preconditions;

import dev._2lstudios.flamecord.FlameCord;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.haproxy.HAProxyMessage;
import io.netty.handler.timeout.ReadTimeoutException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.logging.Level;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.connection.CancelSendSignal;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.connection.PingHandler;
import net.md_5.bungee.protocol.BadPacketException;
import net.md_5.bungee.protocol.OverflowPacketException;
import net.md_5.bungee.protocol.PacketWrapper;
import net.md_5.bungee.util.QuietException;

/**
 * This class is a primitive wrapper for {@link PacketHandler} instances tied to
 * channels to maintain simple states, and only call the required, adapted
 * methods when the channel is connected.
 */
public class HandlerBoss extends ChannelInboundHandlerAdapter
{

    private ChannelWrapper channel;
    private PacketHandler handler;

    public void setHandler(PacketHandler handler)
    {
        Preconditions.checkArgument( handler != null, "handler" );
        this.handler = handler;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception
    {
        if ( handler != null )
        {
            channel = new ChannelWrapper( ctx );
            handler.connected( channel );

            // FlameCord - Option to log initialhandler
            if ( !( handler instanceof InitialHandler || handler instanceof PingHandler ) && FlameCord.getInstance().getFlameCordConfiguration().isLoggerInitialhandler() )
            {
                ProxyServer.getInstance().getLogger().log( Level.INFO, "{0} has connected", handler );
            }
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception
    {
        if ( handler != null )
        {
            channel.markClosed();
            handler.disconnected( channel );

            // FlameCord - Option to log initialhandler
            if ( !( handler instanceof InitialHandler || handler instanceof PingHandler ) && FlameCord.getInstance().getFlameCordConfiguration().isLoggerInitialhandler() )
            {
                ProxyServer.getInstance().getLogger().log( Level.INFO, "{0} has disconnected", handler );
            }
        }
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception
    {
        if ( handler != null )
        {
            handler.writabilityChanged( channel );
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception
    {
        // FlameCord - Return if channel isn't active
        if (!ctx.channel().isActive()) {
            if (msg instanceof PacketWrapper) {
                ((PacketWrapper) msg).trySingleRelease();
            }

            return;
        }

        if ( msg instanceof HAProxyMessage )
        {
            HAProxyMessage proxy = (HAProxyMessage) msg;
            try
            {
                if ( proxy.sourceAddress() != null )
                {
                    InetSocketAddress newAddress = new InetSocketAddress( proxy.sourceAddress(), proxy.sourcePort() );

                    // FlameCord - Option to log haproxy
                    if ( FlameCord.getInstance().getFlameCordConfiguration().isLoggerHaProxy() )
                        ProxyServer.getInstance().getLogger().log( Level.FINE, "Set remote address via PROXY {0} -> {1}", new Object[]
                        {
                            channel.getRemoteAddress(), newAddress
                        } );

                    channel.setRemoteAddress( newAddress );
                }
            } finally
            {
                proxy.release();
            }
            return;
        }

        PacketWrapper packet = (PacketWrapper) msg;


        try
        {
            if ( handler != null )
            {
                boolean sendPacket = handler.shouldHandle( packet );
                if ( sendPacket && packet.packet != null )
                {
                    try
                    {
                        packet.packet.handle( handler );
                    } catch ( CancelSendSignal ex )
                    {
                        sendPacket = false;
                    }
                }
                if ( sendPacket )
                {
                    handler.handle( packet );
                }
            }
        } finally
        {
            packet.trySingleRelease();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
    {
        // Flamecord start - Antibot System
        if (FlameCord.getInstance().getFlameCordConfiguration().getAntibotFirewalledExceptions().contains(cause.getClass().getSimpleName()))
        {
            FlameCord.getInstance().getAddressDataManager().getAddressData(ctx.channel().remoteAddress()).firewall();
            BungeeCord.getInstance().getLogger().log( Level.INFO, "[FlameCord] [{0}] was firewalled because of " + cause.getClass().getSimpleName(), ctx.channel().remoteAddress() );
        }
        // Flamecord end - Antibot System

        if ( ctx.channel().isActive() )
        {
            boolean logExceptions = !( handler instanceof PingHandler );

            // FlameCord - Option to log exceptions
            logExceptions = FlameCord.getInstance().getFlameCordConfiguration().isLoggerExceptions() && logExceptions;
            
            if ( logExceptions )
            {
                if ( cause instanceof ReadTimeoutException )
                {
                    ProxyServer.getInstance().getLogger().log( Level.WARNING, "{0} - read timed out", handler );
                } else if ( cause instanceof DecoderException )
                {
                    // Waterfall start
                    if (net.md_5.bungee.protocol.MinecraftDecoder.DEBUG) {
                        java.util.logging.LogRecord logRecord = new java.util.logging.LogRecord(Level.WARNING, "{0} - A decoder exception has been thrown:");
                        logRecord.setParameters(new Object[]{handler});
                        logRecord.setThrown(cause);
                        ProxyServer.getInstance().getLogger().log(logRecord);
                    } else
                    // Waterfall end
                    if ( cause instanceof CorruptedFrameException )
                    {
                        ProxyServer.getInstance().getLogger().log( Level.WARNING, "{0} - corrupted frame: {1}", new Object[]
                        {
                            handler, cause.getMessage()
                        } );
                    } else if ( cause.getCause() instanceof BadPacketException )
                    {
                        ProxyServer.getInstance().getLogger().log( Level.WARNING, "{0} - bad packet, are mods in use!? {1}", new Object[]
                        {
                            handler, cause.getCause().getMessage()
                        } );
                    } else if ( cause.getCause() instanceof OverflowPacketException )
                    {
                        ProxyServer.getInstance().getLogger().log( Level.WARNING, "{0} - overflow in packet detected! {1}", new Object[]
                        {
                            handler, cause.getCause().getMessage()
                        } );
                    } else
                    {
                        ProxyServer.getInstance().getLogger().log( Level.WARNING, "{0} - could not decode packet! {1}", new Object[]
                        {
                            handler, cause.getCause() != null ? cause.getCause() : cause
                        } );
                    }
                } else if ( cause instanceof IOException || ( cause instanceof IllegalStateException && handler instanceof InitialHandler ) )
                {
                    ProxyServer.getInstance().getLogger().log( Level.WARNING, "{0} - {1}: {2}", new Object[]
                    {
                        handler, cause.getClass().getSimpleName(), cause.getMessage()
                    } );
                } else if ( cause instanceof QuietException )
                {
                    ProxyServer.getInstance().getLogger().log( Level.SEVERE, "{0} - encountered exception: {1}", new Object[]
                    {
                        handler, cause
                    } );
                } else
                {
                    ProxyServer.getInstance().getLogger().log( Level.SEVERE, handler + " - encountered exception", cause );
                }
            }

            if ( handler != null )
            {
                try
                {
                    handler.exception( cause );
                } catch ( Exception ex )
                {
                    ProxyServer.getInstance().getLogger().log( Level.SEVERE, handler + " - exception processing exception", ex );
                }
            }

            ctx.close();
        }
    }
}

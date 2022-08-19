package net.md_5.bungee.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.LastHttpContent;
import java.nio.charset.Charset;
import java.util.logging.Level;

import dev._2lstudios.flamecord.FlameCord;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.Callback;

@RequiredArgsConstructor
public class HttpHandler extends SimpleChannelInboundHandler<HttpObject>
{

    private final Callback<String> callback;
    private final StringBuilder buffer = new StringBuilder(640);

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

        try
        {
            callback.done( null, cause );
        } finally
        {
            ctx.channel().pipeline().remove( this );
            ctx.channel().close();
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception
    {
        if ( msg instanceof HttpResponse )
        {
            HttpResponse response = (HttpResponse) msg;
            int responseCode = response.status().code();

            if ( responseCode == HttpResponseStatus.NO_CONTENT.code() )
            {
                done( ctx );
                return;
            }

            if ( responseCode != HttpResponseStatus.OK.code() )
            {
                throw new IllegalStateException( "Expected HTTP response 200 OK, got " + response.status() );
            }
        }
        if ( msg instanceof HttpContent )
        {
            HttpContent content = (HttpContent) msg;
            buffer.append( content.content().toString( Charset.forName( "UTF-8" ) ) );

            if ( msg instanceof LastHttpContent )
            {
                done( ctx );
            }
        }
    }

    private void done(ChannelHandlerContext ctx)
    {
        try
        {
            callback.done( buffer.toString(), null );
        } finally
        {
            ctx.channel().pipeline().remove( this );
            ctx.channel().close();
        }
    }
}
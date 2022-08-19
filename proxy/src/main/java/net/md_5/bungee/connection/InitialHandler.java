package net.md_5.bungee.connection;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import dev._2lstudios.antibot.AddressData;
import dev._2lstudios.antibot.CheckManager;
import dev._2lstudios.flamecord.FlameCord;

import dev._2lstudios.flamecord.configuration.FlameConfig;
import dev._2lstudios.flamecord.configuration.FlameCordConfiguration;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.BungeeServerInfo;
import net.md_5.bungee.EncryptionUtil;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.Util;
import net.md_5.bungee.api.AbstractReconnectHandler;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerHandshakeEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.md_5.bungee.http.HttpClient;
import net.md_5.bungee.jni.cipher.BungeeCipher;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.netty.HandlerBoss;
import net.md_5.bungee.netty.PacketHandler;
import net.md_5.bungee.netty.PipelineUtils;
import net.md_5.bungee.netty.cipher.CipherDecoder;
import net.md_5.bungee.netty.cipher.CipherEncoder;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.PacketWrapper;
import net.md_5.bungee.protocol.PlayerPublicKey;
import net.md_5.bungee.protocol.Protocol;
import net.md_5.bungee.protocol.ProtocolConstants;
import net.md_5.bungee.protocol.packet.EncryptionRequest;
import net.md_5.bungee.protocol.packet.EncryptionResponse;
import net.md_5.bungee.protocol.packet.Handshake;
import net.md_5.bungee.protocol.packet.Kick;
import net.md_5.bungee.protocol.packet.LegacyHandshake;
import net.md_5.bungee.protocol.packet.LegacyPing;
import net.md_5.bungee.protocol.packet.LoginPayloadResponse;
import net.md_5.bungee.protocol.packet.LoginRequest;
import net.md_5.bungee.protocol.packet.LoginSuccess;
import net.md_5.bungee.protocol.packet.PingPacket;
import net.md_5.bungee.protocol.packet.PluginMessage;
import net.md_5.bungee.protocol.packet.StatusRequest;
import net.md_5.bungee.protocol.packet.StatusResponse;
import net.md_5.bungee.util.AllowedCharacters;
import net.md_5.bungee.util.BufUtil;
import net.md_5.bungee.util.QuietException;

@RequiredArgsConstructor
public class InitialHandler extends PacketHandler implements PendingConnection
{

    private static final String MOJANG_AUTH_URL = System.getProperty("waterfall.auth.url", "https://sessionserver.mojang.com/session/minecraft/hasJoined?username=%s&serverId=%s%s");

    private final BungeeCord bungee;
    private ChannelWrapper ch;
    @Getter
    private final ListenerInfo listener;
    @Getter
    private Handshake handshake;
    @Getter
    private LoginRequest loginRequest;
    private EncryptionRequest request;
    @Getter
    private PluginMessage brandMessage;
    @Getter
    private final Set<String> registeredChannels = new HashSet<>();
    private State thisState = State.HANDSHAKE;
    private final Unsafe unsafe = new Unsafe()
    {
        @Override
        public void sendPacket(DefinedPacket packet)
        {
            ch.write( packet );
        }
    };
    @Getter
    private boolean onlineMode = BungeeCord.getInstance().config.isOnlineMode();
    @Getter
    private InetSocketAddress virtualHost;
    private String name;
    @Getter
    private UUID uniqueId;
    @Getter
    private UUID offlineId;
    @Getter
    private LoginResult loginProfile;
    @Getter
    private boolean legacy;
    @Getter
    private String extraDataInHandshake = "";

    @Override
    public boolean shouldHandle(PacketWrapper packet) throws Exception
    {
        return !ch.isClosing();
    }

    private enum State
    {

        PROCESSING, PROCESSING_USERNAME, HANDSHAKE, STATUS, PING, USERNAME, ENCRYPT, FINISHING;
    }

    private boolean canSendKickMessage()
    {
        return thisState == State.PROCESSING_USERNAME || thisState == State.USERNAME || thisState == State.ENCRYPT || thisState == State.FINISHING;
    }

    @Override
    public void connected(ChannelWrapper channel) throws Exception
    {
        this.ch = channel;
    }

    @Override
    public void exception(Throwable t) throws Exception
    {
        if ( canSendKickMessage() )
        {
            disconnect( ChatColor.RED + Util.exception( t ) );
        } else
        {
            ch.close();
        }
    }

    @Override
    public void handle(PacketWrapper packet) throws Exception
    {
        if ( packet.packet == null )
        {
            throw new QuietException( "Unexpected packet received during server login process!\n" + BufUtil.dump(packet.buf, 16) );
        }
    }

    @Override
    public void handle(PluginMessage pluginMessage) throws Exception
    {
        this.relayMessage( pluginMessage );
    }

    @Override
    public void handle(LegacyHandshake legacyHandshake) throws Exception
    {
        this.legacy = true;
        ch.close( bungee.getTranslation( "outdated_client", bungee.getGameVersion() ) );
    }

    @Override
    public void handle(LegacyPing ping) throws Exception
    {
        this.legacy = true;
        final boolean v1_5 = ping.isV1_5();

        ServerInfo forced = AbstractReconnectHandler.getForcedHost( this );
        final String motd = ( forced != null ) ? forced.getMotd() : listener.getMotd();
        final int protocol = bungee.getProtocolVersion();

        Callback<ServerPing> pingBack = new Callback<ServerPing>()
        {
            @Override
            public void done(ServerPing result, Throwable error)
            {
                if ( error != null )
                {
                    result = getPingInfo( bungee.getTranslation( "ping_cannot_connect" ), protocol );
                    bungee.getLogger().log( Level.WARNING, "Error pinging remote server", error );
                }

                Callback<ProxyPingEvent> callback = new Callback<ProxyPingEvent>()
                {
                    @Override
                    public void done(ProxyPingEvent result, Throwable error)
                    {
                        if ( ch.isClosed() )
                        {
                            return;
                        }

                        ServerPing legacy = result.getResponse();

                // FlameCord - Close and return if legacy == null
                if (legacy == null) {
                    ch.close();
                    return;
                }

                        String kickMessage;

                        if ( v1_5 )
                        {
                            kickMessage = ChatColor.DARK_BLUE
                                    + "\00" + 127
                                    + '\00' + legacy.getVersion().getName()
                                    + '\00' + getFirstLine( legacy.getDescription() )
                                    + '\00' + ( ( legacy.getPlayers() != null ) ? legacy.getPlayers().getOnline() : "-1" )
                                    + '\00' + ( ( legacy.getPlayers() != null ) ? legacy.getPlayers().getMax() : "-1" );
                        } else
                        {
                            // Clients <= 1.3 don't support colored motds because the color char is used as delimiter
                            kickMessage = ChatColor.stripColor( getFirstLine( legacy.getDescription() ) )
                                    + '\u00a7' + ( ( legacy.getPlayers() != null ) ? legacy.getPlayers().getOnline() : "-1" )
                                    + '\u00a7' + ( ( legacy.getPlayers() != null ) ? legacy.getPlayers().getMax() : "-1" );
                        }

                        ch.close( kickMessage );
                    }
                };

                bungee.getPluginManager().callEvent( new ProxyPingEvent( InitialHandler.this, result, callback ) );
            }
        };

        if ( forced != null && listener.isPingPassthrough() )
        {
            ( (BungeeServerInfo) forced ).ping( pingBack, bungee.getProtocolVersion() );
        } else
        {
            pingBack.done( getPingInfo( motd, protocol ), null );
        }
    }

    private static String getFirstLine(String str)
    {
        int pos = str.indexOf( '\n' );
        return pos == -1 ? str : str.substring( 0, pos );
    }

    private ServerPing getPingInfo(String motd, int protocol)
    {
        return new ServerPing(
                new ServerPing.Protocol( bungee.getName() + " " + bungee.getGameVersion(), protocol ),
                new ServerPing.Players( listener.getMaxPlayers(), bungee.getOnlineCount(), null ),
                motd, BungeeCord.getInstance().config.getFaviconObject()
        );
    }

    @Override
    public void handle(StatusRequest statusRequest) throws Exception
    {
        Preconditions.checkState( thisState == State.STATUS, "Not expecting STATUS" );
        thisState = State.PROCESSING;

        ServerInfo forced = AbstractReconnectHandler.getForcedHost( this );
        final int protocol = ( ProtocolConstants.SUPPORTED_VERSION_IDS.contains( handshake.getProtocolVersion() ) ) ? handshake.getProtocolVersion() : bungee.getProtocolVersion();

        Callback<ServerPing> pingBack = new Callback<ServerPing>()
        {
            @Override
            public void done(ServerPing result, Throwable error)
            {
                if ( error != null )
                {
                    result = getPingInfo( bungee.getTranslation( "ping_cannot_connect" ), protocol );
                    bungee.getLogger().log( Level.WARNING, "Error pinging remote server", error );
                }

                Callback<ProxyPingEvent> callback = new Callback<ProxyPingEvent>()
                {
                    @Override
                    public void done(ProxyPingEvent pingResult, Throwable error)
                    {
                        // FlameCord - Close if response is null
                        if (pingResult.getResponse() == null) {
                            ch.close();
                            return;
                        }
                        
                        // FlameCord - Return if connection is closed
                        if (ch.isClosed()) {
                            return;
                        }

                        // FlameCord start - 1.7.x support
                        Gson gson = handshake.getProtocolVersion() == ProtocolConstants.MINECRAFT_1_7_2 ? BungeeCord.getInstance().gsonLegacy : BungeeCord.getInstance().gson;
                        if ( ProtocolConstants.isBeforeOrEq( handshake.getProtocolVersion() , ProtocolConstants.MINECRAFT_1_8 ) )
                        {
                            // Minecraft < 1.9 doesn't send string server descriptions as chat components. Older 1.7+                            // clients even crash when encountering a chat component instead of a string. To be on the
                            // safe side, always send legacy descriptions for < 1.9 clients.
                            JsonElement element = gson.toJsonTree(pingResult.getResponse());
                            Preconditions.checkArgument(element.isJsonObject(), "Response is not a JSON object");
                            JsonObject object = element.getAsJsonObject();
                            object.addProperty("description", pingResult.getResponse().getDescription());

                            unsafe.sendPacket(new StatusResponse(gson.toJson(element)));
                        } else
                        {
                            unsafe.sendPacket( new StatusResponse( gson.toJson( pingResult.getResponse() ) ) );
                        }
                        // FlameCord end - 1.7.x support
                        if ( bungee.getConnectionThrottle() != null )
                        {
                            bungee.getConnectionThrottle().unthrottle( getSocketAddress() );
                        }
                    }
                };

                bungee.getPluginManager().callEvent( new ProxyPingEvent( InitialHandler.this, result, callback ) );
            }
        };

        if ( forced != null && listener.isPingPassthrough() )
        {
            ( (BungeeServerInfo) forced ).ping( pingBack, handshake.getProtocolVersion() );
        } else
        {
            // FlameCord - Custom MOTD
            final FlameCordConfiguration config = FlameCord.getInstance().getFlameCordConfiguration();

            String motd;
            String protocolName;

            ServerPing.PlayerInfo[] sample = null;

            int maxPlayers = listener.getMaxPlayers();
            int onlinePlayers = bungee.getOnlineCount();

            if (config.isFakePlayersEnabled()) {
                onlinePlayers += config.getFakePlayersAmount(onlinePlayers);
            }

            if (config.isMaxPlayersEnabled()) {
                maxPlayers = config.isMaxPlayersOneMore() ? onlinePlayers + 1 : config.getMaxPlayersAmount();
            }

            if (config.isMotdEnabled()) {
                motd = config.getMOTD(maxPlayers, onlinePlayers);
            } else {
                motd = ( forced != null ) ? forced.getMotd() : listener.getMotd();
            }

            if (config.isProtocolEnabled()) {
                protocolName = config.getProtocolName();
            } else {
                protocolName = bungee.getName() + " " + bungee.getGameVersion();
            }

            if (config.isSampleEnabled()) {
                final UUID fakeUuid = new UUID(0, 0);
                final String[] sampleString = config.getSample(maxPlayers, onlinePlayers);

                sample = new ServerPing.PlayerInfo[sampleString.length];

                for (int i = 0; i < sampleString.length; i++) {
                    sample[i] = new ServerPing.PlayerInfo(sampleString[i], fakeUuid);
                }
            }

            pingBack.done( new ServerPing(
                    new ServerPing.Protocol( protocolName, protocol ),
                    new ServerPing.Players( maxPlayers, onlinePlayers, sample ),
                    motd, BungeeCord.getInstance().config.getFaviconObject() ), null );
        }

        thisState = State.PING;
    }

    private static final boolean ACCEPT_INVALID_PACKETS = Boolean.parseBoolean(System.getProperty("waterfall.acceptInvalidPackets", "false"));

    @Override
    public void handle(PingPacket ping) throws Exception
    {
        // FlameCord - Never accept invalid packets
        Preconditions.checkState( thisState == State.PING, "Not expecting PING" );
        thisState = State.PROCESSING;

        unsafe.sendPacket( ping );

        // FlameCord - Close instead of disconnect
        ch.close();
    }

    @Override
    public void handle(Handshake handshake) throws Exception
    {
        Preconditions.checkState( thisState == State.HANDSHAKE, "Not expecting HANDSHAKE" );
        thisState = State.PROCESSING;
        this.handshake = handshake;
        ch.setVersion( handshake.getProtocolVersion() );

        // Starting with FML 1.8, a "\0FML\0" token is appended to the handshake. This interferes
        // with Bungee's IP forwarding, so we detect it, and remove it from the host string, for now.
        // We know FML appends \00FML\00. However, we need to also consider that other systems might
        // add their own data to the end of the string. So, we just take everything from the \0 character
        // and save it for later.
        if ( handshake.getHost().contains( "\0" ) )
        {
            String[] split = handshake.getHost().split( "\0", 2 );
            handshake.setHost( split[0] );
            extraDataInHandshake = "\0" + split[1];
        }

        // SRV records can end with a . depending on DNS / client.
        if ( handshake.getHost().endsWith( "." ) )
        {
            handshake.setHost( handshake.getHost().substring( 0, handshake.getHost().length() - 1 ) );
        }

        this.virtualHost = InetSocketAddress.createUnresolved( handshake.getHost(), handshake.getPort() );

        // FlameCord - Make PlayerHandshakeEvent cancellable
        if (bungee.getPluginManager().callEvent(new PlayerHandshakeEvent(InitialHandler.this, handshake)).isCancelled()) {
            ch.close();
            return;
        }

        // FlameCord start - Antibot System
        AddressData addressData = FlameCord.getInstance().getAddressDataManager().getAddressData( ch.getRemoteAddress() );
        CheckManager checkManager = FlameCord.getInstance().getCheckManager();
        // FlameCord end - Antibot System

        switch ( handshake.getRequestedProtocol() )
        {
            case 1:
                // Ping
                // FlameCord - Option to log initialhandler
                if ( bungee.getConfig().isLogPings() && FlameCord.getInstance().getFlameCordConfiguration().isLoggerInitialhandler() )
                {
                    bungee.getLogger().log( Level.INFO, "{0} has pinged", this );
                }
                thisState = State.STATUS;
                ch.setProtocol( Protocol.STATUS );

                // FlameCord start - Antibot System
                addressData.addPing();

                if ( checkManager.getRatelimitCheck().check( ch.getRemoteAddress() ) )
                {
                    if ( FlameCord.getInstance().getFlameCordConfiguration().isAntibotRatelimitLog() )
                    {
                        bungee.getLogger().log( Level.INFO, "[FlameCord] [{0}] is pinging too fast", ch.getRemoteAddress() );
                    }

                    disconnect( bungee.getTranslation( "antibot_ratelimit", addressData.getPingsSecond() ) );
                    return;
                }
                // FlameCord end - Antibot System

                break;
            case 2:
                // Login
                // FlameCord - Option to log initialhandler
                if (BungeeCord.getInstance().getConfig().isLogInitialHandlerConnections() && FlameCord.getInstance().getFlameCordConfiguration().isLoggerInitialhandler() ) // Waterfall
                {
                    bungee.getLogger().log( Level.INFO, "{0} has connected", this );
                }
                thisState = State.USERNAME;
                ch.setProtocol( Protocol.LOGIN );

                // FlameCord start - Antibot System
                addressData.addConnection();

                if ( checkManager.getRatelimitCheck().check( ch.getRemoteAddress() ) )
                {
                    if ( FlameCord.getInstance().getFlameCordConfiguration().isAntibotRatelimitLog() )
                    {
                        bungee.getLogger().log( Level.INFO, "[FlameCord] [{0}] is connecting too fast", ch.getRemoteAddress() );
                    }

                    disconnect( bungee.getTranslation( "antibot_ratelimit", addressData.getConnectionsSecond() ) );
                    return;
                }
                // FlameCord end - Antibot System

                if ( !ProtocolConstants.SUPPORTED_VERSION_IDS.contains( handshake.getProtocolVersion() ) )
                {
                    if ( handshake.getProtocolVersion() > bungee.getProtocolVersion() )
                    {
                        disconnect( bungee.getTranslation( "outdated_server", bungee.getGameVersion() ) );
                    } else
                    {
                        disconnect( bungee.getTranslation( "outdated_client", bungee.getGameVersion() ) );
                    }
                    return;
                }
                break;
            default:
                throw new QuietException( "Cannot request protocol " + handshake.getRequestedProtocol() );
        }
    }

    @Override
    public void handle(LoginRequest loginRequest) throws Exception
    {
        Preconditions.checkState( thisState == State.USERNAME, "Not expecting USERNAME" );
        thisState = State.PROCESSING_USERNAME;

        if ( !FlameCord.getInstance().getFlameCordConfiguration().isAllowInvalidNames() && !AllowedCharacters.isValidName( loginRequest.getData(), onlineMode ) )
        {
            disconnect( bungee.getTranslation( "name_invalid" ) );
            return;
        }

        if ( BungeeCord.getInstance().config.isEnforceSecureProfile() )
        {
            if ( handshake.getProtocolVersion() < ProtocolConstants.MINECRAFT_1_19 ) {disconnect(bungee.getTranslation("secure_profile_unsupported"));} // Waterfall - Tell old clients to update if secure profiles are required
            PlayerPublicKey publicKey = loginRequest.getPublicKey();
            if ( publicKey == null )
            {
                disconnect( bungee.getTranslation( "secure_profile_required" ) );
                return;
            }

            if ( Instant.ofEpochMilli( publicKey.getExpiry() ).isBefore( Instant.now() ) )
            {
                disconnect( bungee.getTranslation( "secure_profile_expired" ) );
                return;
            }

            if ( getVersion() < ProtocolConstants.MINECRAFT_1_19_1 )
            {
                if ( !EncryptionUtil.check( publicKey, null ) )
                {
                    disconnect( bungee.getTranslation( "secure_profile_invalid" ) );
                    return;
                }
            }
        }

        this.loginRequest = loginRequest;

        int limit = BungeeCord.getInstance().config.getPlayerLimit();
        if ( limit > 0 && bungee.getOnlineCount() >= limit )
        {
            disconnect( bungee.getTranslation( "proxy_full" ) );
            return;
        }

        // FlameCord start - Antibot System
        CheckManager checkManager = FlameCord.getInstance().getCheckManager();
        AddressData addressData = FlameCord.getInstance().getAddressDataManager().getAddressData( ch.getRemoteAddress() );
        String nickname = loginRequest.getData();

        addressData.addNickname( nickname );

        if ( checkManager.getAccountsCheck().check( ch.getRemoteAddress(), nickname ) )
        {
            if ( FlameCord.getInstance().getFlameCordConfiguration().isAntibotAccountsLog() )
            {
                bungee.getLogger().log( Level.INFO, "[FlameCord] [{0}] has too many accounts", ch.getRemoteAddress() );
            }

            disconnect( bungee.getTranslation( "antibot_accounts", addressData.getNicknames().size() ) );
            return;
        }

        if ( checkManager.getNicknameCheck().check( ch.getRemoteAddress() ) )
        {
            if ( FlameCord.getInstance().getFlameCordConfiguration().isAntibotNicknameLog() )
            {
                bungee.getLogger().log( Level.INFO, "[FlameCord] [{0}] has a blacklisted nickname", ch.getRemoteAddress() );
            }

            disconnect( bungee.getTranslation( "antibot_nickname", loginRequest.getData() ) );
            return;
        }

        if ( checkManager.getReconnectCheck().check( ch.getRemoteAddress() ) )
        {
            if ( FlameCord.getInstance().getFlameCordConfiguration().isAntibotReconnectLog() )
            {
                bungee.getLogger().log( Level.INFO, "[FlameCord] [{0}] has to reconnect to join", ch.getRemoteAddress() );
            }

            disconnect( bungee.getTranslation( "antibot_reconnect", FlameCord.getInstance().getFlameCordConfiguration().getAntibotReconnectAttempts() - addressData.getTotalConnections() ) );
            return;
        }

        if ( checkManager.getCountryCheck().check( ch.getRemoteAddress() ) )
        {
            if ( FlameCord.getInstance().getFlameCordConfiguration().isAntibotCountryLog() )
            {
                bungee.getLogger().log( Level.INFO, "[FlameCord] [{0}] has his country blocked from the server", ch.getRemoteAddress() );
            }

            disconnect( bungee.getTranslation( "antibot_country", addressData.getCountry() ) );
            return;
        }
        // FlameCord end - Antibot System

        // If offline mode and they are already on, don't allow connect
        // We can just check by UUID here as names are based on UUID
        if ( !isOnlineMode() && bungee.getPlayer( getUniqueId() ) != null )
        {
            disconnect( bungee.getTranslation( "already_connected_proxy" ) );
            return;
        }

        Callback<PreLoginEvent> callback = new Callback<PreLoginEvent>()
        {

            @Override
            public void done(PreLoginEvent result, Throwable error)
            {
                if ( result.isCancelled() )
                {
                    BaseComponent[] reason = result.getCancelReasonComponents();
                    disconnect( ( reason != null ) ? reason : TextComponent.fromLegacyText( bungee.getTranslation( "kick_message" ) ) );
                    return;
                }
                if ( ch.isClosed() )
                {
                    return;
                }
                if ( onlineMode )
                {
                    thisState = State.ENCRYPT;
                    unsafe().sendPacket( request = EncryptionUtil.encryptRequest() );
                } else
                {
                    thisState = State.FINISHING;
                    finish();
                }
            }
        };

        // fire pre login event
        bungee.getPluginManager().callEvent( new PreLoginEvent( InitialHandler.this, callback ) );
    }

    @Override
    public void handle(final EncryptionResponse encryptResponse) throws Exception
    {
        Preconditions.checkState( thisState == State.ENCRYPT, "Not expecting ENCRYPT" );
        Preconditions.checkState( EncryptionUtil.check( loginRequest.getPublicKey(), encryptResponse, request ), "Invalid verification" );
        thisState = State.FINISHING; // Waterfall - move earlier - There is no verification of this later (and this is not API)

        SecretKey sharedKey = EncryptionUtil.getSecret( encryptResponse, request );
        // Waterfall start
        if (sharedKey instanceof SecretKeySpec) {
            if (sharedKey.getEncoded().length != 16) {
             this.ch.close();
             return;
            }
        }
        // Waterfall end
        BungeeCipher decrypt = EncryptionUtil.getCipher( false, sharedKey );
        ch.addBefore( PipelineUtils.FRAME_DECODER, PipelineUtils.DECRYPT_HANDLER, new CipherDecoder( decrypt ) );
        BungeeCipher encrypt = EncryptionUtil.getCipher( true, sharedKey );
        ch.addBefore( PipelineUtils.FRAME_PREPENDER, PipelineUtils.ENCRYPT_HANDLER, new CipherEncoder( encrypt ) );

        String encName = URLEncoder.encode( InitialHandler.this.getName(), "UTF-8" );

        MessageDigest sha = MessageDigest.getInstance( "SHA-1" );
        for ( byte[] bit : new byte[][]
        {
            request.getServerId().getBytes( "ISO_8859_1" ), sharedKey.getEncoded(), EncryptionUtil.keys.getPublic().getEncoded()
        } )
        {
            sha.update( bit );
        }
        String encodedHash = URLEncoder.encode( new BigInteger( sha.digest() ).toString( 16 ), "UTF-8" );

        String preventProxy = ( BungeeCord.getInstance().config.isPreventProxyConnections() && getSocketAddress() instanceof InetSocketAddress ) ? "&ip=" + URLEncoder.encode( getAddress().getAddress().getHostAddress(), "UTF-8" ) : "";
        String authURL = String.format( MOJANG_AUTH_URL, encName, encodedHash, preventProxy );

        Callback<String> handler = new Callback<String>()
        {
            @Override
            public void done(String result, Throwable error)
            {
                if ( error == null )
                {
                    LoginResult obj = BungeeCord.getInstance().gson.fromJson( result, LoginResult.class );
                    if ( obj != null && obj.getId() != null )
                    {
                        loginProfile = obj;
                        name = obj.getName();
                        // FlameCord - Don't declare uuid unless it's null
                        if (uniqueId == null) {
                            uniqueId = Util.getUUID(obj.getId());
                        }
                        finish();
                        return;
                    }
                    disconnect( bungee.getTranslation( "offline_mode_player" ) );
                } else
                {
                    disconnect( bungee.getTranslation( "mojang_fail" ) );
                    bungee.getLogger().log( Level.SEVERE, "Error authenticating " + getName() + " with minecraft.net", error );
                }
            }
        };
        //thisState = State.FINISHING; // Waterfall - move earlier
        HttpClient.get( authURL, ch.getHandle().eventLoop(), handler );
    }

    private void finish()
    {
        offlineId = UUID.nameUUIDFromBytes( ( "OfflinePlayer:" + getName() ).getBytes( Charsets.UTF_8 ) );
        if ( uniqueId == null )
        {
            uniqueId = offlineId;
        }

        if ( BungeeCord.getInstance().config.isEnforceSecureProfile() )
        {
            if ( getVersion() >= ProtocolConstants.MINECRAFT_1_19_1 )
            {
                boolean secure = false;
                try
                {
                    secure = EncryptionUtil.check( loginRequest.getPublicKey(), uniqueId );
                } catch ( GeneralSecurityException ex )
                {
                }

                if ( !secure )
                {
                    disconnect( bungee.getTranslation( "secure_profile_invalid" ) );
                    return;
                }
            }
        }

        if ( isOnlineMode() )
        {
            // Check for multiple connections
            // We have to check for the old name first
            ProxiedPlayer oldName = bungee.getPlayer( getName() );
            if ( oldName != null )
            {
                // TODO See #1218
                oldName.disconnect( bungee.getTranslation( "already_connected_proxy" ) );
            }
            // And then also for their old UUID
            ProxiedPlayer oldID = bungee.getPlayer( getUniqueId() );
            if ( oldID != null )
            {
                // TODO See #1218
                oldID.disconnect( bungee.getTranslation( "already_connected_proxy" ) );
            }
        } else
        {
            // In offline mode the existing user stays and we kick the new one
            ProxiedPlayer oldName = bungee.getPlayer( getName() );
            if ( oldName != null )
            {
                // TODO See #1218
                disconnect( bungee.getTranslation( "already_connected_proxy" ) );
                return;
            }

        }

        Callback<LoginEvent> complete = new Callback<LoginEvent>()
        {
            @Override
            public void done(LoginEvent result, Throwable error)
            {
                if ( result.isCancelled() )
                {
                    BaseComponent[] reason = result.getCancelReasonComponents();
                    disconnect( ( reason != null ) ? reason : TextComponent.fromLegacyText( bungee.getTranslation( "kick_message" ) ) );
                    return;
                }
                if ( ch.isClosed() )
                {
                    return;
                }

                ch.getHandle().eventLoop().execute( new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if ( !ch.isClosing() )
                        {
                            UserConnection userCon = new UserConnection( bungee, ch, getName(), InitialHandler.this );
                            userCon.setCompressionThreshold( BungeeCord.getInstance().config.getCompressionThreshold() );
                            userCon.init();

                            unsafe.sendPacket( new LoginSuccess( getUniqueId(), getName(), ( loginProfile == null ) ? null : loginProfile.getProperties() ) );
                            ch.setProtocol( Protocol.GAME );

                            ch.getHandle().pipeline().get( HandlerBoss.class ).setHandler( new UpstreamBridge( bungee, userCon ) );
                            bungee.getPluginManager().callEvent( new PostLoginEvent( userCon ) );
                            ServerInfo server;
                            if ( bungee.getReconnectHandler() != null )
                            {
                                server = bungee.getReconnectHandler().getServer( userCon );
                            } else
                            {
                                server = AbstractReconnectHandler.getForcedHost( InitialHandler.this );
                            }
                            if ( server == null )
                            {
                                server = bungee.getServerInfo( listener.getDefaultServer() );
                            }

                            userCon.connect( server, null, true, ServerConnectEvent.Reason.JOIN_PROXY );
                        }
                    }
                } );
            }
        };

        // fire login event
        bungee.getPluginManager().callEvent( new LoginEvent( InitialHandler.this, complete, this.getLoginProfile() ) ); // Waterfall: Parse LoginResult object to new constructor of LoginEvent
    }

    @Override
    public void handle(LoginPayloadResponse response) throws Exception
    {
        disconnect( "Unexpected custom LoginPayloadResponse" );
    }

    @Override
    public void disconnect(String reason)
    {
        if ( canSendKickMessage() )
        {
            disconnect( TextComponent.fromLegacyText( reason ) );
        } else
        {
            ch.close();
        }
    }

    @Override
    public void disconnect(final BaseComponent... reason)
    {
        if ( canSendKickMessage() )
        {
            // FlameCord - Changed delayedClose to close
            ch.close( new Kick( ComponentSerializer.toString( reason ) ) );
        } else
        {
            ch.close();
        }
    }

    @Override
    public void disconnect(BaseComponent reason)
    {
        disconnect( new BaseComponent[]
        {
            reason
        } );
    }

    @Override
    public String getName()
    {
        return ( name != null ) ? name : ( loginRequest == null ) ? null : loginRequest.getData();
    }

    @Override
    public int getVersion()
    {
        return ( handshake == null ) ? -1 : handshake.getProtocolVersion();
    }

    @Override
    public InetSocketAddress getAddress()
    {
        return (InetSocketAddress) getSocketAddress();
    }

    @Override
    public SocketAddress getSocketAddress()
    {
        return ch.getRemoteAddress();
    }

    @Override
    public Unsafe unsafe()
    {
        return unsafe;
    }

    @Override
    public void setOnlineMode(boolean onlineMode)
    {
        Preconditions.checkState( thisState == State.USERNAME || thisState == State.PROCESSING_USERNAME, "Can only set online mode status whilst state is username" );
        this.onlineMode = onlineMode;
    }

    @Override
    public void setUniqueId(UUID uuid)
    {
        Preconditions.checkState( thisState == State.USERNAME || thisState == State.PROCESSING_USERNAME, "Can only set uuid while state is username" );
        // FlameCord - Allow custom uuids even if onlineMode is true
        this.uniqueId = uuid;
    }

    @Override
    public String getUUID()
    {
        return io.github.waterfallmc.waterfall.utils.UUIDUtils.undash( uniqueId.toString() ); // Waterfall
    }

    @Override
    public String toString()
    {
        return "[" + getSocketAddress() + ( getName() != null ? "|" + getName() : "" ) + "] <-> InitialHandler";
    }

    @Override
    public boolean isConnected()
    {
        return !ch.isClosed();
    }

    public void relayMessage(PluginMessage input) throws Exception
    {
        if ( input.getTag().equals( "REGISTER" ) || input.getTag().equals( "minecraft:register" ) )
        {
            String content = new String( input.getData(), StandardCharsets.UTF_8 );

            for ( String id : content.split( "\0" ) )
            {
                // Waterfall start: Add configurable limits for plugin messaging
                Preconditions.checkState( !(registeredChannels.size() > bungee.getConfig().getPluginChannelLimit()), "Too many registered channels. This limit can be configured in the waterfall.yml" );
                Preconditions.checkArgument( !(id.length() > bungee.getConfig().getPluginChannelNameLimit()), "Channel name too long. This limit can be configured in the waterfall.yml" );
                // Waterfall end
                registeredChannels.add( id );
            }
        } else if ( input.getTag().equals( "UNREGISTER" ) || input.getTag().equals( "minecraft:unregister" ) )
        {
            String content = new String( input.getData(), StandardCharsets.UTF_8 );

            for ( String id : content.split( "\0" ) )
            {
                registeredChannels.remove( id );
            }
        } else if ( input.getTag().equals( "MC|Brand" ) || input.getTag().equals( "minecraft:brand" ) )
        {
            brandMessage = input;
        }
    }
}

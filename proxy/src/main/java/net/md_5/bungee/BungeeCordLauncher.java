package net.md_5.bungee;

import java.security.Security;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.command.ConsoleCommandSender;

public class BungeeCordLauncher
{

    public static void main(String[] args) throws Exception
    {
        Security.setProperty( "networkaddress.cache.ttl", "30" );
        Security.setProperty( "networkaddress.cache.negative.ttl", "10" );
        // For JDK9+ we force-enable multi-release jar file support #3087
        if ( System.getProperty( "jdk.util.jar.enableMultiRelease" ) == null )
        {
            System.setProperty( "jdk.util.jar.enableMultiRelease", "force" );
        }

        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();

        OptionSet options = parser.parse( args );

        BungeeCord bungee = new BungeeCord();
        ProxyServer.setInstance( bungee );
        // FlameCord - Use bungee name
        bungee.getLogger().info( "Enabled " + bungee.getName() + " version " + bungee.getVersion() );
        bungee.start();

        if ( !options.has( "noconsole" ) )
        {
            new io.github.waterfallmc.waterfall.console.WaterfallConsole().start();
        }
    }
}

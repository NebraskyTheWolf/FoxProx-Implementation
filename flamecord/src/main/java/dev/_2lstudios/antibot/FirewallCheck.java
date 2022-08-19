package dev._2lstudios.antibot;

import java.io.IOException;
import java.net.SocketAddress;

import dev._2lstudios.flamecord.configuration.FlameCordConfiguration;

public class FirewallCheck {
    private final AddressDataManager addressDataManager;
    private final FlameCordConfiguration flameCordConfiguration;

    public FirewallCheck(final AddressDataManager addressDataManager, final FlameCordConfiguration flameCordConfiguration) {
        this.addressDataManager = addressDataManager;
        this.flameCordConfiguration = flameCordConfiguration;
    }

    public void load() {
        if (flameCordConfiguration.isAntibotFirewallIpset()) {
            Runtime runtime = Runtime.getRuntime();

            try {
                runtime.exec("iptables -D INPUT -p tcp -m set --match-set flamecord_blacklist src -j DROP");
                runtime.exec("ipset flush flamecord_blacklist");
                runtime.exec("ipset destroy flamecord_blacklist");
                runtime.exec("ipset create flamecord_blacklist hash:ip timeout " + flameCordConfiguration.getAntibotFirewallExpire());
                runtime.exec("iptables -I INPUT -p tcp -m set --match-set flamecord_blacklist src -j DROP");
            } catch (IOException exception) {
                // Ignored
            }
        }
    }

    public boolean check(final SocketAddress socketAddress) {
        if (flameCordConfiguration.isAntibotFirewallEnabled()) {
            final AddressData addressData = addressDataManager.getAddressData(socketAddress);

            return addressData.isFirewalled();
        }

        return false;
    }
}

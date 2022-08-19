package dev._2lstudios.antibot;

import java.net.SocketAddress;

import dev._2lstudios.flamecord.FlameCord;
import dev._2lstudios.flamecord.configuration.FlameCordConfiguration;

public class RatelimitCheck {
    private final AddressDataManager addressDataManager;

    public RatelimitCheck(final AddressDataManager addressDataManager) {
        this.addressDataManager = addressDataManager;
    }

    public boolean check(final SocketAddress socketAddress) {
        final FlameCordConfiguration config = FlameCord.getInstance().getFlameCordConfiguration();

        if (config.isAntibotRatelimitEnabled()) {
            final AddressData addressData = addressDataManager.getAddressData(socketAddress);

            if (addressData.getConnectionsSecond() >= config.getAntibotRatelimitConnectionsPerSecond()
                    || addressData.getPingsSecond() >= config.getAntibotRatelimitPingsPerSecond()) {
                if (config.isAntibotRatelimitFirewall()) {
                    addressData.firewall();
                }

                return true;
            }
        }

        return false;
    }
}

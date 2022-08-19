package dev._2lstudios.antibot;

import java.net.SocketAddress;

import dev._2lstudios.flamecord.FlameCord;
import dev._2lstudios.flamecord.configuration.FlameCordConfiguration;

public class FastChatCheck {
    private final AddressDataManager addressDataManager;

    public FastChatCheck(final AddressDataManager addressDataManager) {
        this.addressDataManager = addressDataManager;
    }

    public boolean check(final SocketAddress socketAddress) {
        final FlameCordConfiguration config = FlameCord.getInstance().getFlameCordConfiguration();

        if (config.isAntibotFastChatEnabled()) {
            final AddressData addressData = addressDataManager.getAddressData(socketAddress);

            if (addressData.getTimeSinceLastConnection() <= config.getAntibotFastChatTime()) {
                if (config.isAntibotFastChatFirewall()) {
                    addressData.firewall();
                }

                return true;
            }
        }

        return false;
    }
}

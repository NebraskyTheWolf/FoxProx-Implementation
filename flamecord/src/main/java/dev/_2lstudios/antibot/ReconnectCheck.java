package dev._2lstudios.antibot;

import java.net.SocketAddress;

import dev._2lstudios.flamecord.FlameCord;
import dev._2lstudios.flamecord.configuration.FlameCordConfiguration;

public class ReconnectCheck {
    private final AddressDataManager addressDataManager;
    private int connections = 0;
    private long lastConnection = 0;

    public ReconnectCheck(final AddressDataManager addressDataManager) {
        this.addressDataManager = addressDataManager;
    }

    public boolean check(final SocketAddress socketAddress) {
        final FlameCordConfiguration config = FlameCord.getInstance().getFlameCordConfiguration();

        if (config.isAntibotReconnectEnabled()) {
            final long currentTime = System.currentTimeMillis();

            if (currentTime - lastConnection > config.getAntibotReconnectConnectionThresholdLimit()) {
                lastConnection = currentTime;
                connections = 0;
            }

            if (++connections > config.getAntibotReconnectConnectionThreshold()) {
                final AddressData addressData = addressDataManager.getAddressData(socketAddress);
                final boolean needsAttempts = addressData.getTotalConnections() < config.getAntibotReconnectAttempts()
                        || addressData.getTotalPings() < config.getAntibotReconnectPings();
                final boolean tooSlow = addressData.getTimeSincePenultimateConnection() > config
                        .getAntibotReconnectMaxTime();

                if (tooSlow) {
                    addressData.setTotalConnections(0);
                    return true;
                } else {
                    return needsAttempts;
                }
            }
        }

        return false;
    }
}

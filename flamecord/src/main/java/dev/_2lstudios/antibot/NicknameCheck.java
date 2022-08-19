package dev._2lstudios.antibot;

import java.net.SocketAddress;

import dev._2lstudios.flamecord.FlameCord;
import dev._2lstudios.flamecord.configuration.FlameCordConfiguration;

public class NicknameCheck {
    private AddressDataManager addressDataManager;

    public NicknameCheck(final AddressDataManager addressDataManager) {
        this.addressDataManager = addressDataManager;
    }

    private boolean isBlacklisted(final FlameCordConfiguration config, final String nickname) {
        for (final String blacklisted : config.getAntibotNicknameBlacklist()) {
            if (nickname.toLowerCase().contains(blacklisted)) {
                return true;
            }
        }

        return false;
    }

    public boolean check(final SocketAddress socketAddress) {
        final FlameCordConfiguration config = FlameCord.getInstance().getFlameCordConfiguration();

        if (config.isAntibotNicknameEnabled()) {
            final AddressData addressData = addressDataManager.getAddressData(socketAddress);
            final String nickname = addressData.getLastNickname();

            if (isBlacklisted(config, nickname)) {
                if (config.isAntibotNicknameFirewall()) {
                    addressData.firewall();
                }

                return true;
            }
        }

        return false;
    }
}

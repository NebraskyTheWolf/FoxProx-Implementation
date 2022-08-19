package dev._2lstudios.antibot;

import java.net.SocketAddress;
import java.util.Collection;

import dev._2lstudios.flamecord.FlameCord;
import dev._2lstudios.flamecord.configuration.FlameCordConfiguration;

public class AccountsCheck {
    private final AddressDataManager addressDataManager;

    public AccountsCheck(final AddressDataManager addressDataManager) {
        this.addressDataManager = addressDataManager;
    }

    public boolean check(final SocketAddress socketAddress, final String nickname) {
        final FlameCordConfiguration config = FlameCord.getInstance().getFlameCordConfiguration();

        if (config.isAntibotAccountsEnabled()) {
            final AddressData addressData = addressDataManager.getAddressData(socketAddress);
            final Collection<String> nicknames = addressData.getNicknames();

            if (nicknames.size() > config.getAntibotAccountsLimit()) {
                nicknames.remove(nickname);

                if (config.isAntibotAccountsFirewall()) {
                    addressData.firewall();
                }

                return true;
            }
        }

        return false;
    }
}

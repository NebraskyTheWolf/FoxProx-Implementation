package dev._2lstudios.antibot;

import java.net.SocketAddress;

import dev._2lstudios.flamecord.FlameCord;
import dev._2lstudios.flamecord.configuration.FlameCordConfiguration;

public class PasswordCheck {
    private AddressDataManager addressDataManager;
    private String lastNickname = "";
    private String lastPassword = "";
    private int repeatCount = 0;

    public PasswordCheck(final AddressDataManager addressDataManager) {
        this.addressDataManager = addressDataManager;
    }

    private void updatePassword(final FlameCordConfiguration config, final String nickname, final String password) {
        if (!nickname.equals(lastNickname)) {
            if (password.equals(lastPassword)) {
                if (repeatCount < config.getAntibotPasswordLimit()) {
                    repeatCount++;
                }
            } else if (repeatCount > 0) {
                repeatCount--;
            }
        }

        lastNickname = nickname;
        lastPassword = password;
    }

    public boolean check(final SocketAddress socketAddress, final String passwordMessage) {
        final FlameCordConfiguration config = FlameCord.getInstance().getFlameCordConfiguration();

        if (config.isAntibotPasswordEnabled()) {
            if (passwordMessage.contains("/login ") || passwordMessage.contains("/l ")
                    || passwordMessage.contains("/register ")
                    || passwordMessage.contains("/reg ")) {
                final AddressData addressData = addressDataManager.getAddressData(socketAddress);
                final String nickname = addressData.getLastNickname();
                final String password = passwordMessage.split(" ")[1];

                updatePassword(config, nickname, password);

                if (repeatCount >= config.getAntibotPasswordLimit()) {
                    if (config.isAntibotPasswordFirewall()) {
                        addressData.firewall();
                    }

                    return true;
                }
            }
        }

        return false;
    }

    public int getRepeatCount() {
        return repeatCount;
    }
}

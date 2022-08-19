package dev._2lstudios.antibot;

import dev._2lstudios.flamecord.configuration.FlameCordConfiguration;
import lombok.Getter;

public class CheckManager {
    @Getter
    private final AccountsCheck accountsCheck;
    @Getter
    private final CountryCheck countryCheck;
    @Getter
    private final FastChatCheck fastChatCheck;
    @Getter
    private final FirewallCheck firewallCheck;
    @Getter
    private final NicknameCheck nicknameCheck;
    @Getter
    private final PasswordCheck passwordCheck;
    @Getter
    private final RatelimitCheck ratelimitCheck;
    @Getter
    private final ReconnectCheck reconnectCheck;

    public CheckManager(final AddressDataManager addressDataManager, final FlameCordConfiguration flameCordConfiguration) {
        this.accountsCheck = new AccountsCheck(addressDataManager);
        this.countryCheck = new CountryCheck(addressDataManager);
        this.fastChatCheck = new FastChatCheck(addressDataManager);
        this.firewallCheck = new FirewallCheck(addressDataManager, flameCordConfiguration);
        this.nicknameCheck = new NicknameCheck(addressDataManager);
        this.passwordCheck = new PasswordCheck(addressDataManager);
        this.ratelimitCheck = new RatelimitCheck(addressDataManager);
        this.reconnectCheck = new ReconnectCheck(addressDataManager);

        this.countryCheck.load();
        this.firewallCheck.load();
    }

    public void unload() {
        this.countryCheck.unload();
    }
}

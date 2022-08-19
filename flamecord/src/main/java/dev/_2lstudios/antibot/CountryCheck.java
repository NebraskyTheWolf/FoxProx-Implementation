package dev._2lstudios.antibot;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URL;
import java.nio.file.Files;

import com.maxmind.db.CHMCache;
import com.maxmind.db.MaxMindDbConstructor;
import com.maxmind.db.MaxMindDbParameter;
import com.maxmind.db.Reader;

import dev._2lstudios.flamecord.FlameCord;
import dev._2lstudios.flamecord.configuration.FlameCordConfiguration;

public class CountryCheck {
    private final AddressDataManager addressDataManager;
    private Reader maxMindReader;

    public CountryCheck(final AddressDataManager addressDataManager) {
        this.addressDataManager = addressDataManager;
    }

    public void download(final URL url, final File file) throws Exception {
        try (InputStream in = url.openStream()) {
            Files.copy(in, file.toPath());
        }
    }

    public void load() {
        final File file = new File("GeoLite2-Country.mmdb");

        try {
            if (!file.exists()) {
                System.out.println("Starting download of MaxMindDB (This will take some seconds...)");
                download(new URL("https://git.io/GeoLite2-Country.mmdb"), file);
            }

            this.maxMindReader = new Reader(file, new CHMCache());
        } catch (final Exception exception) {
            // Ignored
        }
    }

    public void unload() {
        try {
            if (this.maxMindReader != null) {
                this.maxMindReader.close();
            }
        } catch (final IOException ex) {
            // Ignored
        }
    }

    private boolean isBlacklisted(final FlameCordConfiguration config, final String isoCode) {
        for (final String blacklisted : config.getAntibotCountryBlacklist()) {
            if (isoCode.contains(blacklisted)) {
                return true;
            }
        }

        return false;
    }

    public static class LookupResult {
        private final Country country;

        @MaxMindDbConstructor
        public LookupResult(@MaxMindDbParameter(name = "country") final Country country) {
            this.country = country;
        }

        public Country getCountry() {
            return this.country;
        }
    }

    public static class Country {
        private final String isoCode;

        @MaxMindDbConstructor
        public Country(@MaxMindDbParameter(name = "iso_code") final String isoCode) {
            this.isoCode = isoCode;
        }

        public String getIsoCode() {
            return this.isoCode;
        }
    }

    public String getIsoCode(final InetAddress address) {
        try {
            final LookupResult lookupResult = maxMindReader.get(address, LookupResult.class);

            if (lookupResult == null) {
                return "LOCAL";
            } else {
                final Country country = lookupResult.getCountry();
                final String isoCode = country.getIsoCode();

                return isoCode;
            }
        } catch (final IOException exception) {
            // Ignored
        }

        return null;
    }

    public boolean check(final SocketAddress socketAddress) {
        final FlameCordConfiguration config = FlameCord.getInstance().getFlameCordConfiguration();

        if (config.isAntibotCountryEnabled()) {
            final AddressData addressData = addressDataManager.getAddressData(socketAddress);
            final String addressCountry = addressData.getCountry();
            final String country;

            if (addressCountry != null) {
                country = addressCountry;
            } else {
                country = getIsoCode(((InetSocketAddress) socketAddress).getAddress());
                addressData.setCountry(country);
            }

            if (country != null && isBlacklisted(config, country)) {
                if (config.isAntibotCountryFirewall()) {
                    addressData.firewall();
                }

                return true;
            }
        }

        return false;
    }
}

package dev._2lstudios.antibot;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

import dev._2lstudios.flamecord.FlameCord;

public class AddressData {
    private final Collection<String> nicknames = new HashSet<>();
    private final String hostString;
    private String lastNickname = "";
    private String country = null;
    private long lastPing = 0;
    private long penultimateConnection = 0;
    private long lastConnection = 0;
    private long lastFirewall = 0;
    private int pingsSecond = 0;
    private int totalPings = 0;
    private int connectionsSecond = 0;
    private int totalConnections = 0;

    public AddressData(final String hostString) {
        this.hostString = hostString;
    }

    public Collection<String> getNicknames() {
        return nicknames;
    }

    public String getLastNickname() {
        return lastNickname;
    }

    public void addNickname(final String nickname) {
        if (!lastNickname.equals(nickname)) {
            this.lastNickname = nickname;
            this.totalConnections = 1;
        }

        if (!this.nicknames.contains(nickname)) {
            this.nicknames.add(nickname);
        }
    }

    public long getPenultimateConnection() {
        return penultimateConnection;
    }

    public long getTimeSincePenultimateConnection() {
        return System.currentTimeMillis() - penultimateConnection;
    }

    public long getLastConnection() {
        return lastConnection;
    }

    public long getTimeSinceLastConnection() {
        return System.currentTimeMillis() - lastConnection;
    }

    private void updatePingsSecond() {
        if (System.currentTimeMillis() - lastPing >= 1000) {
            pingsSecond = 0;
        }
    }

    public int getPingsSecond() {
        updatePingsSecond();
        return pingsSecond;
    }

    public void addPing() {
        updatePingsSecond();
        lastPing = System.currentTimeMillis();
        pingsSecond++;
        totalPings++;
    }

    public int getTotalPings() {
        return totalPings;
    }

    private void updateConnectionsSecond() {
        if (System.currentTimeMillis() - lastConnection >= 1000) {
            connectionsSecond = 0;
        }
    }

    public int getConnectionsSecond() {
        updateConnectionsSecond();
        return connectionsSecond;
    }

    public void addConnection() {
        final long currentTime = System.currentTimeMillis();

        updateConnectionsSecond();
        penultimateConnection = lastConnection == 0 ? currentTime : lastConnection;
        lastConnection = currentTime;
        connectionsSecond++;
        totalConnections++;
    }

    public int getTotalConnections() {
        return totalConnections;
    }

    public boolean isFirewalled() {
        return System.currentTimeMillis() - lastFirewall < FlameCord.getInstance().getFlameCordConfiguration()
                .getAntibotFirewallExpire() * 1000;
    }

    public String getHostString() {
        return hostString;
    }

    public void firewall() {
        if (!hostString.equals("127.0.0.1")) {
            if (FlameCord.getInstance().getFlameCordConfiguration().isAntibotFirewallIpset()) {
                Runtime runtime = Runtime.getRuntime();

                try {
                    runtime.exec("ipset add flamecord_blacklist " + hostString);
                } catch (IOException exception) {
                    // Ignored
                }
            }

            this.lastFirewall = System.currentTimeMillis();
        }
    }

    public void setTotalConnections(final int totalConnections) {
        this.totalConnections = totalConnections;
    }

    public String setCountry(final String country) {
        return this.country = country;
    }

    public String getCountry() {
        return country;
    }

    public boolean hasNickname(final String nickname) {
        return nicknames.contains(nickname);
    }
}

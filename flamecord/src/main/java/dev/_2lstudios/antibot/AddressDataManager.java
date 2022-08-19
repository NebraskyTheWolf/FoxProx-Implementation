package dev._2lstudios.antibot;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;

public class AddressDataManager {
    private final Map<String, AddressData> addressData = new HashMap<>();

    public AddressData getAddressData(final SocketAddress address) {
        final InetSocketAddress iNetSocketAddress = (InetSocketAddress) address;
        final String addressString = iNetSocketAddress.getHostString();
        
        if (addressData.containsKey(addressString)) {
            return addressData.get(addressString);
        } else {
            AddressData data = new AddressData(addressString);

            addressData.put(addressString, data);
    
            return data;
        }
    }
}

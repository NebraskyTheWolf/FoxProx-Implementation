package dev._2lstudios.flamecord.configuration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import net.md_5.bungee.config.Configuration;

public class FlameConfig {
    int setIfUnexistant(final String arg1, final int arg2, final Configuration configuration) {
        return (int) setIfUnexistant(arg1, (Object) arg2, configuration);
    }

    String setIfUnexistant(final String arg1, final String arg2, final Configuration configuration) {
        return (String) setIfUnexistant(arg1, (Object) arg2, configuration);
    }

    boolean setIfUnexistant(final String arg1, final boolean arg2, final Configuration configuration) {
        return (boolean) setIfUnexistant(arg1, (Object) arg2, configuration);
    }

    Object setIfUnexistant(final String arg1, final Object arg2, final Configuration configuration) {
        if (!configuration.contains(arg1)) {
            configuration.set(arg1, arg2);

            return arg2;
        } else {
            return configuration.get(arg1);
        }
    }

    Collection<String> setIfUnexistant(final String arg1, final Collection<String> arg2,
            final Configuration configuration) {
        if (!configuration.contains(arg1)) {
            configuration.set(arg1, new ArrayList<>(arg2));

            return arg2;
        } else {
            return new HashSet<>(configuration.getStringList(arg1));
        }
    }
}

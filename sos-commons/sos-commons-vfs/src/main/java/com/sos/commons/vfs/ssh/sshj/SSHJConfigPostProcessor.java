package com.sos.commons.vfs.ssh.sshj;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.sos.commons.util.SOSString;

import net.schmizz.sshj.Config;
import net.schmizz.sshj.DefaultConfig;
import net.schmizz.sshj.common.Factory;

/** Applies SSHJ algorithm configuration overrides to a {@link Config} ({@link DefaultConfig}) instance.
 * <p>
 * Supported settings include cipher, key exchange (KEX), public key, and MAC algorithm preferences.<br/>
 * Configuration values are resolved from application properties and used to replace the corresponding SSHJ default algorithm lists.
 * </p>
 * <p>
 * Invalid or unsupported algorithm names are ignored and logged.<br/>
 * If no valid algorithm remains after processing a configured list, the original SSHJ defaults are preserved.
 * </p>
 * <p>
 * For supported algorithm names, refer to the SSHJ sources:
 * </p>
 * <ul>
 * <li>Key exchange (KEX) algorithms: https://github.com/hierynomus/sshj/tree/master/src/main/java/com/hierynomus/sshj/transport/kex</li>
 * <li>Public key algorithms: https://github.com/hierynomus/sshj/blob/master/src/main/java/com/hierynomus/sshj/key/KeyAlgorithms.java</li>
 * <li>Ciphers: https://github.com/hierynomus/sshj/tree/master/src/main/java/com/hierynomus/sshj/transport/cipher</li>
 * <li>MAC (message authentication code) algorithms: https://github.com/hierynomus/sshj/tree/master/src/main/java/com/hierynomus/sshj/transport/mac</li>
 * </ul>
 */
public class SSHJConfigPostProcessor {

    /** Property containing a semicolon-separated list of allowed SSH key exchange (KEX) algorithms.<br/>
     * The property value defines the ordered preference for SSH key exchange negotiation. */
    private static final String PROPERTY_NAME_KEX = "sos.sshj.kex";
    /** Property containing a semicolon-separated list of allowed SSH public key algorithms.<br/>
     * The property value defines which authentication key types are permitted and their order of preference. */
    private static final String PROPERTY_NAME_KEYALG = "sos.sshj.keyalg";
    /** Property containing a semicolon-separated list of allowed SSH cipher algorithms.<br/>
     * The property value itself encodes an ordered list used for SSH negotiation preference. */
    private static final String PROPERTY_NAME_CIPHERS = "sos.sshj.ciphers";
    /** Property containing a semicolon-separated list of allowed SSH MAC (message authentication code) algorithms.<br/>
     * The property value defines integrity algorithms and their negotiation preference order. */
    private static final String PROPERTY_NAME_MACS = "sos.sshj.macs";

    private static final String VALUE_DELIMITER = ";";
    private static final String VALUE_JOIN_DELIMITER = VALUE_DELIMITER + " ";

    private SSHJConfigPostProcessor() {
    }

    public static void apply(SSHJProvider provider, Config config) {
        Properties props = provider.loadConfiguration();
        if (props == null || props.isEmpty()) {
            return;
        }

        overrideKex(provider, config, props);
        overrideKeyAlgorithms(provider, config, props);
        overrideCiphers(provider, config, props);
        overrideMacs(provider, config, props);
    }

    private static void overrideKex(SSHJProvider provider, Config config, Properties props) {
        overrideAlgorithmList(provider, config, props, PROPERTY_NAME_KEX, Config::getKeyExchangeFactories, Config::setKeyExchangeFactories, "kex");
    }

    private static void overrideKeyAlgorithms(SSHJProvider provider, Config config, Properties props) {
        overrideAlgorithmList(provider, config, props, PROPERTY_NAME_KEYALG, Config::getKeyAlgorithms, Config::setKeyAlgorithms, "keyalg");
    }

    private static void overrideCiphers(SSHJProvider provider, Config config, Properties props) {
        overrideAlgorithmList(provider, config, props, PROPERTY_NAME_CIPHERS, Config::getCipherFactories, Config::setCipherFactories, "cipher");
    }

    private static void overrideMacs(SSHJProvider provider, Config config, Properties props) {
        overrideAlgorithmList(provider, config, props, PROPERTY_NAME_MACS, Config::getMACFactories, Config::setMACFactories, "mac");
    }

    private static <T> void overrideAlgorithmList(SSHJProvider provider, Config config, Properties props, String propertyName,
            Function<Config, Collection<Factory.Named<T>>> availableSupplier, BiConsumer<Config, List<Factory.Named<T>>> setter, String logLabel) {

        List<String> requested = toList(props, propertyName);
        if (requested.isEmpty()) {
            return;
        }

        Map<String, Factory.Named<T>> available = availableSupplier.apply(config).stream().collect(Collectors.toMap(f -> normalize(f.getName()),
                Function.identity(), (a, b) -> a, LinkedHashMap::new));

        List<Factory.Named<T>> selected = new ArrayList<>();
        List<String> unknown = new ArrayList<>();

        for (String raw : requested) {
            String key = normalize(raw);
            Factory.Named<T> item = available.get(key);

            if (item == null) {
                unknown.add(raw);
            } else {
                selected.add(item);
            }
        }

        if (!unknown.isEmpty()) {
            provider.getLogger().warn("[%s][unknown %s requested=%s]available=%s", propertyName, logLabel, SOSString.join(unknown,
                    VALUE_JOIN_DELIMITER), SOSString.join(available.keySet(), VALUE_JOIN_DELIMITER));
        }

        if (selected.isEmpty()) {
            provider.getLogger().warn("[%s][no valid %s found]keeping SSHJ defaults", propertyName, logLabel);
            return;
        }

        // set factory
        setter.accept(config, selected);

        if (provider.getLogger().isDebugEnabled()) {
            provider.getLogger().debug("[%s][applied]%s", propertyName, SOSString.join(selected.stream().map(Factory.Named::getName).toList(),
                    VALUE_JOIN_DELIMITER));
        }
    }

    private static List<String> toList(Properties p, String key) {
        String v = p.getProperty(key);
        if (SOSString.isEmpty(v)) {
            return List.of();
        }
        return Arrays.stream(v.split(VALUE_DELIMITER)).map(String::trim).filter(s -> !s.isEmpty()).toList();
    }

    private static String normalize(String s) {
        if (s == null) {
            return null;
        }
        return s.trim().toLowerCase(Locale.ROOT);
    }

}

package org.betonquest.betonquest.modules.config.transformers;

import org.betonquest.betonquest.api.config.patcher.PatchException;
import org.betonquest.betonquest.api.config.patcher.PatchTransformer;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Locale;
import java.util.Map;

/**
 * Changes the data type of an existing value.
 */
public class TypeTransformer implements PatchTransformer {

    /**
     * Default constructor.
     */
    public TypeTransformer() {
    }

    @Override
    public void transform(final Map<String, String> options, final ConfigurationSection config) throws PatchException {
        final String key = options.get("key");
        final Object value = config.get(key);
        if (value == null) {
            throw new PatchException("Value is not set, skipping transformation.");
        }
        final String type = options.get("newType");

        final String valueString = value.toString();
        switch (type.toLowerCase(Locale.ROOT)) {
            case "boolean" -> config.set(key, Boolean.valueOf(valueString));
            case "integer" -> config.set(key, Integer.valueOf(valueString));
            case "double" -> config.set(key, Double.valueOf(valueString));
            case "float" -> config.set(key, Float.valueOf(valueString));
            case "string" -> config.set(key, valueString);
            default -> throw new PatchException("Unknown type '" + type + "', skipping transformation.");
        }
    }
}

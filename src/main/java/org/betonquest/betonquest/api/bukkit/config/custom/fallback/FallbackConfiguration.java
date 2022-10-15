package org.betonquest.betonquest.api.bukkit.config.custom.fallback;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationOptions;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * This class hides an original and a fallback {@link ConfigurationSection} and exposes it,
 * as if it were a {@link Configuration} based on the  original {@link ConfigurationSection},
 * except for missing keys, then the fallback is used.
 */
public class FallbackConfiguration extends FallbackConfigurationSection implements Configuration {
    /**
     * Holds the defaults of this {@link FallbackConfiguration}.
     */
    private FallbackConfiguration defaults;

    /**
     * Creates a new decorator instance.
     *
     * @param original The original {@link ConfigurationSection} that should be decorated.
     * @param fallback The fallback {@link ConfigurationSection} that should be used
     *                 when there is no value in the  original {@link ConfigurationSection}.
     */
    public FallbackConfiguration(@NotNull final ConfigurationSection original, @Nullable final ConfigurationSection fallback) {
        super(original, fallback);
    }

    @Override
    public void addDefaults(@NotNull final Map<String, Object> defaults) {
        final Configuration originalRoot = manager.getOriginal().getRoot();
        if (originalRoot == null) {
            throw new IllegalStateException("Cannot add defaults, because the original configuration does not have a root");
        }
        originalRoot.addDefaults(defaults);
    }

    @Override
    public void addDefaults(@NotNull final Configuration defaults) {
        final Configuration originalRoot = manager.getOriginal().getRoot();
        if (originalRoot == null) {
            throw new IllegalStateException("Cannot add defaults, because the original configuration does not have a root");
        }
        originalRoot.addDefaults(defaults);
    }

    @Override
    public @Nullable Configuration getDefaults() {
        createDefaults(false);
        return defaults;
    }

    @Override
    public void setDefaults(@NotNull final Configuration defaults) {
        final Configuration originalRoot = manager.getOriginal().getRoot();
        if (originalRoot == null) {
            throw new IllegalStateException("Cannot add defaults, because the original configuration does not have a root");
        }
        originalRoot.setDefaults(defaults);
        if (!createDefaults(true)) {
            this.defaults.manager.setOriginal(defaults);
        }
    }

    @Override
    public @NotNull ConfigurationOptions options() {
        final Configuration originalRoot = manager.getOriginal().getRoot();
        final ConfigurationSection fallback = manager.getFallback();
        final Configuration fallbackRoot = fallback == null ? null : fallback.getRoot();
        if (originalRoot == null || fallback != null && fallbackRoot == null) {
            throw new IllegalStateException("Cannot get options when the root of original or fallback is null");
        }
        final ConfigurationOptions fallbackOptions = fallbackRoot == null ? null : fallbackRoot.options();
        return new FallbackConfigurationOptions(this, originalRoot.options(), fallbackOptions);
    }

    private boolean createDefaults(final boolean force) {
        if (defaults != null) {
            return false;
        }
        final Configuration originalRoot = manager.getOriginal().getRoot();
        if (originalRoot == null) {
            throw new IllegalStateException("Cannot create defaults instance when the root of original is null");
        }
        final ConfigurationSection fallback = manager.getFallback();
        final Configuration fallbackRoot = fallback == null ? null : fallback.getRoot();
        Configuration originalDefault = originalRoot.getDefaults();
        final Configuration fallbackDefault = fallbackRoot == null ? null : fallbackRoot.getDefaults();
        if (originalDefault == null) {
            if (fallbackDefault == null && !force) {
                return false;
            }
            originalDefault = new MemoryConfiguration();
            originalRoot.setDefaults(originalDefault);
        }
        defaults = new FallbackConfiguration(originalDefault, fallbackDefault);
        return true;
    }
}

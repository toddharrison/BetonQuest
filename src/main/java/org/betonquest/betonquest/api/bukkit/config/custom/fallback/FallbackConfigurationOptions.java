package org.betonquest.betonquest.api.bukkit.config.custom.fallback;

import org.betonquest.betonquest.api.bukkit.config.custom.handle.HandleModificationConfiguration;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationOptions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This {@link ConfigurationOptions} implementation guarantees that it is not possible to break out of the
 * {@link HandleModificationConfiguration} instance.
 */
public class FallbackConfigurationOptions extends ConfigurationOptions {

    /**
     * The original {@link ConfigurationOptions} hidden behind this decorator.
     */
    private final ConfigurationOptions original;
    /**
     * The fallback {@link ConfigurationOptions} hidden behind this decorator.
     */
    private final ConfigurationOptions fallback;

    /**
     * Creates a new {@link ConfigurationOptions} instance, that maps to the original one.
     *
     * @param configuration The {@link Configuration} instance that should be returned by the configuration method
     * @param original      The original {@link Configuration}, to apply the options to
     * @param fallback      The fallback {@link Configuration}, to apply the options to
     */
    protected FallbackConfigurationOptions(@NotNull final Configuration configuration, @NotNull final ConfigurationOptions original, @Nullable final ConfigurationOptions fallback) {
        super(configuration);
        this.original = original;
        this.fallback = fallback;
    }

    @Override
    public char pathSeparator() {
        return original.pathSeparator();
    }

    @Override
    @NotNull
    public ConfigurationOptions pathSeparator(final char value) {
        original.pathSeparator(value);
        if (fallback != null) {
            fallback.pathSeparator(value);
        }
        return this;
    }

    @Override
    public boolean copyDefaults() {
        return original.copyDefaults();
    }

    @Override
    @NotNull
    public ConfigurationOptions copyDefaults(final boolean value) {
        original.copyDefaults(value);
        if (fallback != null) {
            fallback.copyDefaults(value);
        }
        return this;
    }
}

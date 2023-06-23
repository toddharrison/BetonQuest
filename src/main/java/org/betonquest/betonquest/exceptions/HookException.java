package org.betonquest.betonquest.exceptions;

import org.bukkit.plugin.Plugin;

import java.io.Serial;

/**
 * Thrown if BetonQuest tries to hook a plugin
 */
public class HookException extends Exception {
    @Serial
    private static final long serialVersionUID = 7965553395053833302L;

    /**
     * The plugin
     */
    private final Plugin plugin;

    /**
     * Constructs a new exception related to a plugin
     * {@link Exception#Exception(String)}
     *
     * @param plugin  The plugin
     * @param message The Message.
     */
    public HookException(final Plugin plugin, final String message) {
        super(message);
        this.plugin = plugin;
    }

    /**
     * Constructs a new exception related to a plugin
     * {@link Exception#Exception(String, Throwable)}
     *
     * @param plugin  The plugin
     * @param message The message
     * @param cause   The Throwable
     */
    public HookException(final Plugin plugin, final String message, final Throwable cause) {
        super(message, cause);
        this.plugin = plugin;
    }

    /**
     * Constructs a new exception related to a plugin
     * {@link Exception#Exception(Throwable)}
     *
     * @param plugin The plugin
     * @param cause  the exceptions cause.
     */
    public HookException(final Plugin plugin, final Throwable cause) {
        super(cause);
        this.plugin = plugin;
    }

    /**
     * @return Get the {@link Plugin}
     */
    public Plugin getPlugin() {
        return plugin;
    }

    /**
     * @return Get the name of the {@link Plugin}
     */
    public String getPluginName() {
        return plugin.getName();
    }

    /**
     * @return Get the version of the {@link Plugin}
     */
    public String getPluginVersion() {
        return getPlugin().getDescription().getVersion();
    }
}

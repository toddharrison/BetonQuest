package org.betonquest.betonquest.compatibility.fakeblock;

import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.compatibility.Integrator;
import org.betonquest.betonquest.exceptions.HookException;
import org.betonquest.betonquest.exceptions.UnsupportedVersionException;
import org.betonquest.betonquest.modules.versioning.UpdateStrategy;
import org.betonquest.betonquest.modules.versioning.Version;
import org.betonquest.betonquest.modules.versioning.VersionComparator;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;

/**
 * Integrates with FakeBlock
 */
public class FakeBlockIntegrator implements Integrator {
    /**
     * The minimum required version of FakeBlock.
     */
    public static final String REQUIRED_VERSION = "2.0.1";

    /**
     * The instance of {@link BetonQuest}.
     */
    private final BetonQuest plugin;

    /**
     * Create the FakeBlock integration.
     */
    public FakeBlockIntegrator() {
        this.plugin = BetonQuest.getInstance();
    }

    @Override
    public void hook() throws HookException {
        checkRequiredVersion();
        final Server server = plugin.getServer();
        plugin.registerNonStaticEvent("fakeblock", new FakeBlockEventFactory(server, server.getScheduler(), plugin));
    }

    private void checkRequiredVersion() throws UnsupportedVersionException {
        final Plugin fakeBlockPlugin = Bukkit.getPluginManager().getPlugin("fake-block");
        if (fakeBlockPlugin != null) {
            final Version version = new Version(fakeBlockPlugin.getDescription().getVersion());
            final VersionComparator comparator = new VersionComparator(UpdateStrategy.MAJOR);
            if (comparator.isOtherNewerThanCurrent(version, new Version(REQUIRED_VERSION))) {
                throw new UnsupportedVersionException(plugin, REQUIRED_VERSION);
            }
        }
    }

    @Override
    public void reload() {
        // Empty
    }

    @Override
    public void close() {
        // Empty
    }
}

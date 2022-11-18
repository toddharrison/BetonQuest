package org.betonquest.betonquest.compatibility.worldguard;

import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.Instruction;
import org.betonquest.betonquest.api.Objective;
import org.betonquest.betonquest.api.profiles.OnlineProfile;
import org.betonquest.betonquest.api.profiles.Profile;
import org.betonquest.betonquest.exceptions.InstructionParseException;
import org.betonquest.betonquest.utils.PlayerConverter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Player has to enter the WorldGuard region
 */
@SuppressWarnings("PMD.CommentRequired")
public class RegionObjective extends Objective implements Listener {

    private final String name;
    private final boolean entry;
    private final boolean exit;
    private final Map<UUID, Boolean> playersInsideRegion;

    public RegionObjective(final Instruction instruction) throws InstructionParseException {
        super(instruction);
        template = ObjectiveData.class;
        name = instruction.next();
        entry = instruction.hasArgument("entry");
        exit = instruction.hasArgument("exit");
        playersInsideRegion = new HashMap<>();
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        final OnlineProfile onlineProfile = PlayerConverter.getID(event.getPlayer());
        if (containsPlayer(onlineProfile)) {
            final boolean inside = WorldGuardIntegrator.isInsideRegion(onlineProfile.getPlayer().getLocation(), name);
            if (!entry && !exit && inside && checkConditions(onlineProfile)) {
                completeObjective(onlineProfile);
            } else {
                playersInsideRegion.put(onlineProfile.getProfileUUID(), inside);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuit(final PlayerQuitEvent event) {
        playersInsideRegion.remove(PlayerConverter.getID(event.getPlayer()).getProfileUUID());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDeath(final PlayerDeathEvent event) {
        checkLocation(event.getEntity(), event.getEntity().getLocation());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerRespawn(final PlayerRespawnEvent event) {
        checkLocation(event.getPlayer(), event.getRespawnLocation());
    }

    @EventHandler(ignoreCancelled = true)
    public void onTeleport(final PlayerTeleportEvent event) {
        onMove(event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(final PlayerMoveEvent event) {
        checkLocation(event.getPlayer(), event.getTo());
    }

    @EventHandler(ignoreCancelled = true)
    public void onRide(final VehicleMoveEvent event) {
        qreHandler.handle(() -> {
            final List<Entity> passengers = event.getVehicle().getPassengers();
            for (final Entity passenger : passengers) {
                if (passenger instanceof Player player) {
                    checkLocation(player, event.getTo());
                }
            }
        });
    }

    @SuppressWarnings("PMD.CyclomaticComplexity")
    private void checkLocation(final Player player, final Location location) {
        final OnlineProfile onlineProfile = PlayerConverter.getID(player);
        if (!containsPlayer(onlineProfile)) {
            return;
        }

        final boolean inside = WorldGuardIntegrator.isInsideRegion(location, name);

        if (!entry && !exit) {
            if (inside && checkConditions(onlineProfile)) {
                completeObjective(onlineProfile);
            }
            return;
        }
        if (!playersInsideRegion.containsKey(onlineProfile.getProfileUUID())) {
            playersInsideRegion.put(onlineProfile.getProfileUUID(), WorldGuardIntegrator.isInsideRegion(player.getLocation(), name));
        }
        final boolean fromInside = playersInsideRegion.get(onlineProfile.getProfileUUID());
        playersInsideRegion.put(onlineProfile.getProfileUUID(), inside);

        if ((entry && inside && !fromInside || exit && fromInside && !inside) && checkConditions(onlineProfile)) {
            completeObjective(onlineProfile);
            playersInsideRegion.remove(onlineProfile.getProfileUUID());
        }
    }

    @Override
    public void start() {
        Bukkit.getPluginManager().registerEvents(this, BetonQuest.getInstance());
    }

    @Override
    public void stop() {
        HandlerList.unregisterAll(this);
    }

    @Override
    public String getDefaultDataInstruction() {
        return "";
    }

    @Override
    public String getProperty(final String name, final Profile profile) {
        return "";
    }

}

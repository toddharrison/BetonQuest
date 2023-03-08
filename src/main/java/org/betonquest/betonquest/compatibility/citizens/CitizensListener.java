package org.betonquest.betonquest.compatibility.citizens;

import lombok.CustomLog;
import net.citizensnpcs.api.event.NPCClickEvent;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.api.profiles.OnlineProfile;
import org.betonquest.betonquest.config.Config;
import org.betonquest.betonquest.conversation.CombatTagger;
import org.betonquest.betonquest.exceptions.QuestRuntimeException;
import org.betonquest.betonquest.utils.PlayerConverter;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Starts new conversations with NPCs
 */
@SuppressWarnings("PMD.CommentRequired")
@CustomLog
public class CitizensListener implements Listener {

    private final Map<UUID, Long> npcInteractionLimiter = new HashMap<>();
    private RightClickListener rightClick;
    private LeftClickListener leftClick;
    private int interactionLimit;

    /**
     * Initializes the listener
     */
    public CitizensListener() {
        reload();
    }

    public final void reload() {
        if (rightClick != null) {
            HandlerList.unregisterAll(rightClick);
        }
        if (leftClick != null) {
            HandlerList.unregisterAll(leftClick);
        }


        final BetonQuest plugin = BetonQuest.getInstance();

        rightClick = new RightClickListener();
        Bukkit.getPluginManager().registerEvents(rightClick, plugin);

        if (plugin.getPluginConfig().getBoolean("acceptNPCLeftClick")) {
            leftClick = new LeftClickListener();
            Bukkit.getPluginManager().registerEvents(leftClick, plugin);
        }
        interactionLimit = plugin.getPluginConfig().getInt("npcInteractionLimit", 500);
    }

    @SuppressWarnings({"PMD.AvoidLiteralsInIfCondition", "PMD.CyclomaticComplexity", "PMD.NPathComplexity"})
    public void interactLogic(final NPCClickEvent event) {
        if (!event.getClicker().hasPermission("betonquest.conversation")) {
            return;
        }
        final Long lastClick = npcInteractionLimiter.get(event.getClicker().getUniqueId());
        final long currentClick = new Date().getTime();
        if (lastClick != null && lastClick + interactionLimit >= currentClick) {
            return;
        }
        npcInteractionLimiter.put(event.getClicker().getUniqueId(), currentClick);
        if (NPCMoveEvent.blocksTalking(event.getNPC())) {
            return;
        }
        final OnlineProfile onlineProfile = PlayerConverter.getID(event.getClicker());
        if (CombatTagger.isTagged(onlineProfile)) {
            try {
                Config.sendNotify(null, onlineProfile, "busy", "busy,error");
            } catch (final QuestRuntimeException e) {
                LOG.warn("The notify system was unable to play a sound for the 'busy' category. Error was: '" + e.getMessage() + "'", e);
            }
            return;
        }
        final String npcId = String.valueOf(event.getNPC().getId());
        String assignment = Config.getNpc(npcId);
        if ("true".equalsIgnoreCase(Config.getString("config.citizens_npcs_by_name")) && assignment == null) {
            assignment = Config.getNpc(event.getNPC().getName());
        }
        if (assignment != null) {
            event.setCancelled(true);
            new CitizensConversation(onlineProfile, assignment, event.getNPC().getEntity().getLocation(),
                    event.getNPC());
        }
    }

    private class RightClickListener implements Listener {

        public RightClickListener() {
        }

        @EventHandler(ignoreCancelled = true)
        public void onNPCClick(final NPCRightClickEvent event) {
            interactLogic(event);
        }
    }

    private class LeftClickListener implements Listener {

        public LeftClickListener() {
        }

        @EventHandler(ignoreCancelled = true)
        public void onNPCClick(final NPCLeftClickEvent event) {
            interactLogic(event);
        }
    }
}

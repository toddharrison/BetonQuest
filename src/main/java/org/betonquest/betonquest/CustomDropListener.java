package org.betonquest.betonquest;

import org.betonquest.betonquest.api.BetonQuestLogger;
import org.betonquest.betonquest.exceptions.InstructionParseException;
import org.betonquest.betonquest.exceptions.ObjectNotFoundException;
import org.betonquest.betonquest.id.ItemID;
import org.betonquest.betonquest.item.QuestItem;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.metadata.MetadataValue;

import java.util.List;

@SuppressWarnings("PMD.CommentRequired")
public class CustomDropListener implements Listener {
    /**
     * Custom {@link BetonQuestLogger} instance for this class.
     */
    private static final BetonQuestLogger LOG = BetonQuestLogger.create();

    public CustomDropListener() {
        Bukkit.getPluginManager().registerEvents(this, BetonQuest.getInstance());
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDeath(final EntityDeathEvent event) {
        int dropIndex = 0;
        while (event.getEntity().hasMetadata("betonquest-drops-" + dropIndex)) {
            try {
                final List<MetadataValue> metadata = event.getEntity().getMetadata("betonquest-drops-" + dropIndex);
                for (final MetadataValue m : metadata) {
                    final String item = m.asString();
                    final int separatorIndex = item.indexOf(':');
                    event.getDrops().add(new QuestItem(new ItemID(null, item.substring(0, separatorIndex)))
                            .generate(Integer.parseInt(item.substring(separatorIndex + 1))));
                }
            } catch (final InstructionParseException | ObjectNotFoundException e) {
                LOG.warn("Error when dropping custom item from entity: " + e.getMessage(), e);
            }
            dropIndex++;
        }
    }

}

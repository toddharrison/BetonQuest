package org.betonquest.betonquest.compatibility.mmogroup.mmoitems;

import net.Indyuce.mmoitems.api.event.AbilityUseEvent;
import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.Instruction;
import org.betonquest.betonquest.api.Objective;
import org.betonquest.betonquest.api.profiles.OnlineProfile;
import org.betonquest.betonquest.api.profiles.Profile;
import org.betonquest.betonquest.exceptions.InstructionParseException;
import org.betonquest.betonquest.utils.PlayerConverter;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

@SuppressWarnings("PMD.CommentRequired")
public class MMOItemsCastAbilityObjective extends Objective implements Listener {

    private final String abilityID;

    public MMOItemsCastAbilityObjective(final Instruction instruction) throws InstructionParseException {
        super(instruction);

        abilityID = instruction.next();
        template = ObjectiveData.class;
    }

    @EventHandler(ignoreCancelled = true)
    public void onAbilityCast(final AbilityUseEvent event) {
        if (!event.getAbility().getAbility().getName().equalsIgnoreCase(abilityID)) {
            return;
        }
        final OnlineProfile onlineProfile = PlayerConverter.getID(event.getPlayer());

        if (!containsPlayer(onlineProfile) || !checkConditions(onlineProfile)) {
            return;
        }
        completeObjective(onlineProfile);
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

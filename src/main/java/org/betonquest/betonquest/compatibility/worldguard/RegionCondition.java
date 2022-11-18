package org.betonquest.betonquest.compatibility.worldguard;

import org.betonquest.betonquest.Instruction;
import org.betonquest.betonquest.api.Condition;
import org.betonquest.betonquest.api.profiles.Profile;
import org.betonquest.betonquest.exceptions.InstructionParseException;

/**
 * Checks if the player is in specified region
 */
@SuppressWarnings("PMD.CommentRequired")
public class RegionCondition extends Condition {

    private final String name;

    public RegionCondition(final Instruction instruction) throws InstructionParseException {
        super(instruction, true);
        name = instruction.next();
    }

    @Override
    protected Boolean execute(final Profile profile) {
        return WorldGuardIntegrator.isInsideRegion(profile.getOnlineProfile().get().getPlayer().getLocation(), name);
    }

}

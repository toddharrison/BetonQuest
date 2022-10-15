package org.betonquest.betonquest.events;

import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.Instruction;
import org.betonquest.betonquest.Point;
import org.betonquest.betonquest.api.profiles.Profile;
import org.betonquest.betonquest.database.GlobalData;
import org.betonquest.betonquest.exceptions.InstructionParseException;
import org.betonquest.betonquest.exceptions.QuestRuntimeException;

/**
 * Modifies global Points
 */
@SuppressWarnings("PMD.CommentRequired")
public class GlobalPointEvent extends PointEvent {

    public GlobalPointEvent(final Instruction instruction) throws InstructionParseException {
        super(instruction);
        staticness = true;
        persistent = true;
    }

    @Override
    protected Void execute(final Profile profile) throws QuestRuntimeException {
        final GlobalData globalData = BetonQuest.getInstance().getGlobalData();
        addPoints(profile, globalData);
        return null;
    }

    private void addPoints(final Profile profile, final GlobalData globalData) throws QuestRuntimeException {
        if (multi) {
            for (final Point p : globalData.getPoints()) {
                if (p.getCategory().equalsIgnoreCase(category)) {
                    globalData.modifyPoints(category,
                            (int) Math.floor(p.getCount() * count.getDouble(profile) - p.getCount()));
                }
            }
        } else {
            globalData.modifyPoints(category, count.getInt(profile));
        }
    }
}

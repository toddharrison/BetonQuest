package org.betonquest.betonquest.compatibility.mmogroup.mmocore;

import net.Indyuce.mmocore.api.player.PlayerData;
import org.betonquest.betonquest.Instruction;
import org.betonquest.betonquest.VariableNumber;
import org.betonquest.betonquest.api.Condition;
import org.betonquest.betonquest.api.profiles.Profile;
import org.betonquest.betonquest.exceptions.InstructionParseException;
import org.betonquest.betonquest.exceptions.QuestRuntimeException;

@SuppressWarnings("PMD.CommentRequired")
public class MMOCoreProfessionLevelCondition extends Condition {
    private final String professionName;
    private final VariableNumber targetLevelVar;
    private final boolean mustBeEqual;

    public MMOCoreProfessionLevelCondition(final Instruction instruction) throws InstructionParseException {
        super(instruction, true);

        professionName = instruction.next();
        targetLevelVar = instruction.getVarNum();
        mustBeEqual = instruction.hasArgument("equal");
    }

    @Override
    protected Boolean execute(final Profile profile) throws QuestRuntimeException {
        final PlayerData data = PlayerData.get(profile.getOfflinePlayer().getUniqueId());
        final int actualLevel = data.getCollectionSkills().getLevel(professionName);
        final int targetLevel = targetLevelVar.getInt(profile);

        return mustBeEqual ? actualLevel == targetLevel : actualLevel >= targetLevel;
    }
}

package org.betonquest.betonquest.compatibility.mmogroup.mmocore;

import net.Indyuce.mmocore.api.player.PlayerData;
import org.betonquest.betonquest.Instruction;
import org.betonquest.betonquest.VariableNumber;
import org.betonquest.betonquest.api.QuestEvent;
import org.betonquest.betonquest.api.profiles.Profile;
import org.betonquest.betonquest.exceptions.InstructionParseException;
import org.betonquest.betonquest.exceptions.QuestRuntimeException;

@SuppressWarnings("PMD.CommentRequired")
public class MMOCoreClassPointsEvent extends QuestEvent {

    private final VariableNumber amountVar;

    public MMOCoreClassPointsEvent(final Instruction instruction) throws InstructionParseException {
        super(instruction, true);
        amountVar = instruction.getVarNum();
    }

    @Override
    protected Void execute(final Profile profile) throws QuestRuntimeException {
        final PlayerData data = PlayerData.get(profile.getPlayerUUID());
        final int amount = amountVar.getInt(profile);
        data.giveClassPoints(amount);
        return null;
    }
}

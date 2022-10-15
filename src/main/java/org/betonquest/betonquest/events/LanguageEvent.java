package org.betonquest.betonquest.events;

import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.Instruction;
import org.betonquest.betonquest.api.QuestEvent;
import org.betonquest.betonquest.api.profiles.Profile;
import org.betonquest.betonquest.config.Config;
import org.betonquest.betonquest.exceptions.InstructionParseException;

/**
 * Changes player's language.
 */
@SuppressWarnings("PMD.CommentRequired")
public class LanguageEvent extends QuestEvent {

    private final String lang;

    public LanguageEvent(final Instruction instruction) throws InstructionParseException {
        super(instruction, false);
        lang = instruction.next();
        if (!Config.getLanguages().contains(lang)) {
            throw new InstructionParseException("Language " + lang + " does not exists");
        }
    }

    @Override
    protected Void execute(final Profile profile) {
        BetonQuest.getInstance().getPlayerData(profile).setLanguage(lang);
        return null;
    }

}

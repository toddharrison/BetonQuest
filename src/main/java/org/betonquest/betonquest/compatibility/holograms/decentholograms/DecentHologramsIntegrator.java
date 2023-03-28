package org.betonquest.betonquest.compatibility.holograms.decentholograms;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import lombok.CustomLog;
import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.Instruction;
import org.betonquest.betonquest.api.Variable;
import org.betonquest.betonquest.api.config.quest.QuestPackage;
import org.betonquest.betonquest.compatibility.holograms.BetonHologram;
import org.betonquest.betonquest.compatibility.holograms.HologramIntegrator;
import org.betonquest.betonquest.compatibility.holograms.HologramProvider;
import org.betonquest.betonquest.exceptions.HookException;
import org.betonquest.betonquest.exceptions.InstructionParseException;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.UUID;
import java.util.regex.Matcher;

/**
 * Integrates with DecentHolograms
 */
@CustomLog
public class DecentHologramsIntegrator extends HologramIntegrator {

    /**
     * Creates a new DecentHologramsIntegrator for DecentHolograms
     */
    public DecentHologramsIntegrator() {
        super("DecentHolograms", "2.7.5");
    }

    @Override
    public BetonHologram createHologram(final Location location) {
        final Hologram hologram = DHAPI.createHologram(UUID.randomUUID().toString(), location);
        hologram.enable();
        return new DecentHologramsHologram(hologram);
    }

    @Override
    public void hook() throws HookException {
        super.hook();
        if (!Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            LOG.warn("Holograms from DecentHolograms will not be able to use BetonQuest variables in text lines " +
                    "without PlaceholderAPI plugin! Install it to use holograms with variables!");
        }
    }

    @Override
    public String parseVariable(final QuestPackage pack, final String text) {
        /* We must convert a normal BetonQuest variable such as "%pack.objective.kills.left%" to
           "%betonquest_pack:objective.kills.left%" which is parsed by DecentHolograms as a PlaceholderAPI placeholder. */
        final Matcher matcher = HologramProvider.VARIABLE_VALIDATOR.matcher(text);
        return matcher.replaceAll(match -> {
            final String group = match.group();
            try {
                final Variable variable = BetonQuest.createVariable(pack, group);
                if (variable != null) {
                    final Instruction instruction = variable.getInstruction();
                    return "%betonquest_" + instruction.getPackage().getQuestPath() + ":" + instruction.getInstruction() + "%";
                }
            } catch (final InstructionParseException exception) {
                LOG.warn("Could not create variable '" + group + "' variable: " + exception.getMessage(), exception);
            }
            return group;
        });
    }
}

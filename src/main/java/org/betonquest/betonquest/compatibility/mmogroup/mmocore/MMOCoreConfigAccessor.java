package org.betonquest.betonquest.compatibility.mmogroup.mmocore;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.attribute.PlayerAttribute;
import net.Indyuce.mmocore.api.player.attribute.PlayerAttributes;
import org.betonquest.betonquest.exceptions.InstructionParseException;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.UUID;

@SuppressWarnings("PMD.CommentRequired")
public final class MMOCoreConfigAccessor {

    private final YamlConfiguration attributeConfig;

    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    public MMOCoreConfigAccessor() {
        attributeConfig = YamlConfiguration.loadConfiguration(new File(Bukkit.getPluginManager().getPlugin("MMOCore").getDataFolder(), "attributes.yml"));
    }

    public int getMMOCoreAttribute(final UUID uuid, final String attribute) {
        final PlayerAttributes attributes = PlayerData.get(uuid).getAttributes();
        return attributes.getAttribute(new PlayerAttribute(attributeConfig.getConfigurationSection(attribute)));
    }

    public void isMMOConfigValidForAttribute(final String attributeName) throws InstructionParseException {
        if (!attributeConfig.contains(attributeName)) {
            throw new InstructionParseException("Couldn't find the attribute \"" + attributeName + "\" in the MMOCore attribute config!");
        }
    }
}

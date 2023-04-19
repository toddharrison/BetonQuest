package org.betonquest.betonquest.modules.config.quest;

import org.betonquest.betonquest.api.bukkit.config.custom.multi.MultiConfiguration;
import org.betonquest.betonquest.api.config.ConfigAccessor;
import org.betonquest.betonquest.api.config.quest.QuestPackage;
import org.betonquest.betonquest.utils.Utils;
import org.betonquest.betonquest.variables.GlobalVariableResolver;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

/**
 * This {@link QuestPackageImpl} represents all functionality based on a {@link Quest}.
 */
public class QuestPackageImpl extends QuestTemplate implements QuestPackage {
    /**
     * Creates a new {@link QuestPackage}.  For more information see {@link Quest}.
     *
     * @param questPath the path that addresses this {@link QuestPackage}
     * @param root      the root file of this {@link QuestPackage}
     * @param files     all files contained by this {@link QuestPackage}
     * @throws InvalidConfigurationException thrown if a {@link QuestPackage} could not be created
     *                                       or an exception occurred while creating the {@link MultiConfiguration}
     * @throws FileNotFoundException         thrown if a file could not be found during the creation
     *                                       of a {@link ConfigAccessor}
     */
    public QuestPackageImpl(final String questPath, final File root, final List<File> files) throws InvalidConfigurationException, FileNotFoundException {
        super(questPath, root, files);
    }

    @Override
    public boolean hasTemplate(final String templatePath) {
        return getTemplates().contains(templatePath);
    }

    @Override
    @SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
    public String getRawString(final String address) {
        final String[] parts = address.split("\\.");
        if (parts.length < 2) {
            return null;
        }
        final String path = parts[0];
        int startPath = 1;
        ConfigurationSection section = getConfig().getConfigurationSection(path);
        if (section != null && path.equals("conversations")) {
            if (parts.length < 3) {
                return null;
            }
            section = section.getConfigurationSection(parts[1]);
            startPath = 2;
        }
        if (section == null) {
            return null;
        }
        final StringBuilder restPath = new StringBuilder();
        for (int i = startPath; i < parts.length; i++) {
            restPath.append(parts[i]);
            if (i < parts.length - 1) {
                restPath.append('.');
            }
        }
        return section.getString(restPath.toString(), null);
    }

    @Override
    public String subst(final String input) {
        return GlobalVariableResolver.resolve(this, input);
    }

    @Override
    public String getString(final String address) {
        return getString(address, null);
    }

    @Override
    public String getString(final String address, final String def) {
        final String value = getRawString(address);
        if (value == null) {
            return def;
        }
        if (!value.contains("$")) {
            return value;
        }

        return GlobalVariableResolver.resolve(this, value);
    }

    @Override
    public String getFormattedString(final String address) {
        return Utils.format(getString(address));
    }

}

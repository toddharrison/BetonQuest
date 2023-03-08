package org.betonquest.betonquest.id;

import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.Instruction;
import org.betonquest.betonquest.api.bukkit.config.custom.multi.MultiConfiguration;
import org.betonquest.betonquest.api.config.quest.QuestPackage;
import org.betonquest.betonquest.config.Config;
import org.betonquest.betonquest.exceptions.ObjectNotFoundException;

import java.util.List;
import java.util.Objects;

@SuppressWarnings({"PMD.ShortClassName", "PMD.AbstractClassWithoutAbstractMethod", "PMD.CommentRequired"})
public abstract class ID {

    public static final String UP_STR = "_"; // string used as "up the hierarchy" package

    public static final List<String> PATHS = List.of("events", "conditions", "objectives", "variables");

    protected String identifier;
    protected QuestPackage pack;
    protected Instruction instruction;
    protected String rawInstruction;

    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPathComplexity", "PMD.CognitiveComplexity",
            "PMD.AvoidLiteralsInIfCondition", "PMD.NcssCount"})
    protected ID(final QuestPackage pack, final String identifier) throws ObjectNotFoundException {
        // id must be specified
        if (identifier == null || identifier.length() == 0) {
            throw new ObjectNotFoundException("ID is null");
        }
        // resolve package name
        if (identifier.contains(".")) {
            // id has specified a package, get it!
            int dotIndex = identifier.indexOf('.');
            final String packName = identifier.substring(0, dotIndex);
            if (pack != null && packName.startsWith(UP_STR + "-")) {
                // resolve relative name if we have a supplied package
                final String[] root = pack.getQuestPath().split("-");
                final String[] path = packName.split("-");
                // count how many packages up we need to go
                int stepsUp = 0;
                while (stepsUp < path.length && UP_STR.equals(path[stepsUp])) {
                    stepsUp++;
                }
                // can't go out of BetonQuest folder of course
                if (stepsUp > root.length) {
                    throw new ObjectNotFoundException("Relative path goes out of package scope! Consider removing a few '"
                            + UP_STR + "'s in ID " + identifier);
                }
                // construct the final absolute path
                final StringBuilder builder = new StringBuilder();
                for (int i = 0; i < root.length - stepsUp; i++) {
                    builder.append(root[i]).append('-');
                }
                for (int i = stepsUp; i < path.length; i++) {
                    builder.append(path[i]).append('-');
                }
                final String absolute = builder.substring(0, builder.length() - 1);
                this.pack = Config.getPackages().get(absolute);
                // throw error earlier so it can have more information than default one at the bottom
                if (this.pack == null) {
                    throw new ObjectNotFoundException("Relative path in ID '" + identifier + "' resolved to '" + absolute +
                            "', but this package does not exist!");
                }
                // We want to go down
            } else if (pack != null && packName.startsWith("-")) {
                final String currentPath = pack.getQuestPath();
                final String fullPath = currentPath + packName;

                this.pack = Config.getPackages().get(fullPath);
                // throw error earlier so it can have more information than default one at the bottom
                if (this.pack == null) {
                    throw new ObjectNotFoundException("Relative path in ID '" + identifier + "' resolved to '" + fullPath +
                            "', but this package does not exist!");
                }
            } else {
                // if no relative path is available, check if packName is a package or if it is an ID
                // split at ':' first as to only consider the identifier before the ':' in the case of the math variable
                final String[] parts = identifier.split(":")[0].split("\\.");
                final QuestPackage potentialPack = Config.getPackages().get(packName);
                if (potentialPack == null) {
                    this.pack = pack;
                    dotIndex = -1;
                } else {
                    if (BetonQuest.isVariableType(packName)) {
                        // if first term shares the same name as a variable type
                        if (parts.length == 2 && isIdFromPack(potentialPack, parts[1])) {
                            this.pack = potentialPack;
                        } else if (parts.length > 2) {
                            if (BetonQuest.isVariableType(parts[1]) && isIdFromPack(potentialPack, parts[2])) {
                                // if second term is a variable type and third term is an ID
                                // we can assume that the ID is in the form pack.variable.id.args
                                this.pack = potentialPack;
                            } else if (isIdFromPack(potentialPack, parts[1])) {
                                // if second term is not a variable type, check if it's an ID. If it is an ID
                                // we can assume that the ID is in the form variable.id.args
                                this.pack = pack;
                                dotIndex = -1;
                            } else {
                                this.pack = potentialPack;
                            }
                        } else {
                            this.pack = pack;
                            dotIndex = -1;
                        }
                    } else {
                        this.pack = potentialPack;
                    }
                }
            }
            if (identifier.length() == dotIndex + 1) {
                throw new ObjectNotFoundException("ID of the pack '" + this.pack + "' is null");
            }
            this.identifier = identifier.substring(dotIndex + 1);
        } else {
            if (pack == null) {
                throw new ObjectNotFoundException("No package specified for id '" + identifier + "'!");
            }
            this.pack = pack;
            this.identifier = identifier;
        }

        // no package yet? this is an error
        if (this.pack == null) {
            throw new ObjectNotFoundException("Package in ID '" + identifier + "' does not exist");
        }
    }

    /**
     * Checks if an ID belongs to a provided QuestPackage. This checks all events, conditions, objectives and variables
     * for any ID matching the provided string
     *
     * @param pack       The quest package to search
     * @param identifier The id
     * @return true if the id exists in the quest package
     */
    private boolean isIdFromPack(final QuestPackage pack, final String identifier) {
        final MultiConfiguration config = pack.getConfig();
        for (final String path : PATHS) {
            if (config.getString(path + "." + identifier, null) != null) {
                return true;
            }
        }
        return false;
    }

    public QuestPackage getPackage() {
        return pack;
    }

    public String getBaseID() {
        return identifier;
    }

    public String getFullID() {
        return pack.getQuestPath() + "." + getBaseID();
    }

    @Override
    public String toString() {
        return getFullID();
    }

    @Override
    public boolean equals(final Object other) {
        if (other instanceof ID identifier) {
            return identifier.identifier.equals(this.identifier) &&
                    identifier.pack.getQuestPath().equals(this.pack.getQuestPath());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier, pack.getQuestPath());
    }

    public Instruction generateInstruction() {
        if (rawInstruction == null) {
            return null;
        }
        if (instruction == null) {
            instruction = new Instruction(pack, this, rawInstruction);
        }
        return instruction;
    }

}

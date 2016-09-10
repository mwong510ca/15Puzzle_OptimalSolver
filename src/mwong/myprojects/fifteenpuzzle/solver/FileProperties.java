package mwong.myprojects.fifteenpuzzle.solver;

import mwong.myprojects.fifteenpuzzle.PropertiesCache;
import mwong.myprojects.fifteenpuzzle.solver.components.PatternOptions;

/**
 * FilePreperties contains all data files setting of 15 puzzle.
 *
 * @author Meisze Wong
 *         www.linkedin.com/pub/macy-wong/46/550/37b/
 */
public class FileProperties {
    private static final String SEPERATOR = System.getProperty("file.separator");
    private static String directory = "database";
    private static String walkingDist = "walking_distance";
    private static String pdbPrefix =  "pattern_";
    private static String pdbElement =  "element_";
    private static String pdbDefault =  "_default";
    private static String pdbOption =  "_option";
    private static String referenceCollection =  "reference_accumulator";
    private static String extension = "db";

    static {
        directory = "database";
        extension = "db";
        walkingDist = "walking_distance";
        pdbPrefix =  "pattern_";
        pdbElement =  "element_";
        pdbDefault =  "_default";
        pdbOption =  "_option";
        referenceCollection =  "reference_accumulator";

        if (PropertiesCache.getInstance().containsKey("directory")) {
            directory = PropertiesCache.getInstance().getProperty("directory");
        }

        if (PropertiesCache.getInstance().containsKey("fileExtension")) {
            extension = PropertiesCache.getInstance().getProperty("fileExtension");
        }

        if (PropertiesCache.getInstance().containsKey("fileWd")) {
            walkingDist = PropertiesCache.getInstance().getProperty("fileWd");
        }

        if (PropertiesCache.getInstance().containsKey("filePdbPrefix")) {
            pdbPrefix = PropertiesCache.getInstance().getProperty("filePdbPrefix");
        }

        if (PropertiesCache.getInstance().containsKey("filePdbElement")) {
            pdbElement = PropertiesCache.getInstance().getProperty("filePdbElement");
        }

        if (PropertiesCache.getInstance().containsKey("filePdbDefault")) {
            pdbDefault = PropertiesCache.getInstance().getProperty("filePdbDefault");
        }

        if (PropertiesCache.getInstance().containsKey("filePdbOption")) {
            pdbOption = PropertiesCache.getInstance().getProperty("filePdbOption");
        }

        if (PropertiesCache.getInstance().containsKey("fileRefCollection")) {
            referenceCollection = PropertiesCache.getInstance().getProperty("fileRefCollection");
        }
    }

    public static final String getDirectory() {
        return directory;
    }

    public static final String getFilepathWD() {
        return directory + SEPERATOR + walkingDist + "." +  extension;
    }

    public static final String getFilepathPDElement(int group) {
        return directory + SEPERATOR + pdbPrefix + pdbElement + group + "." +  extension;
    }

    /**
     * Returns the String of file path for preset pattern database.
     *
     * @param type the given PatternOptions type
     * @param choice the index of preset pattern option
     * @return String of file path for preset pattern database
     */
    public static final String getFilepathPD(PatternOptions type, int choice) {
        if (type == PatternOptions.Pattern_Custom) {
            throw new UnsupportedOperationException("Custom pattern will not store a local copy.");
        }
        String filepath = directory + SEPERATOR + pdbPrefix + type.getType();
        if (choice == 0) {
            return filepath + pdbDefault + "." +  extension;
        }
        return filepath + pdbOption  + choice + "." + extension;
    }

    public static final String getFilepathReference() {
        return directory + SEPERATOR + referenceCollection + "." +  extension;
    }
}


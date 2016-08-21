package mwong.myprojects.fifteenpuzzle.solver.components;

/**
 * FilePrepertiess contains all data files setting of 15 puzzle.
 *
 * @author   Meisze Wong
 *           www.linkedin.com/pub/macy-wong/46/550/37b/
 */
public class FileProperties {
    private static final String DIRECTORY = "database";
    private static final String SEPERATOR = System.getProperty("file.separator");
    private static final String FILE_WD = "walking_distance";
    private static final String FILE_PD_PREFIX =  "pattern_";
    private static final String FILE_PD_ELEMENT =  "element_";
    private static final String FILE_PD_DEFAULT =  "_default";
    private static final String FILE_PD_OPTION =  "_option";
    private static final String FILE_REFERENCE =  "reference_accumulator";
    private static final String FILE_EXTENSION = ".db";

    public static final String getDirectory() {
        return DIRECTORY;
    }

    static final String getFilepathWD() {
        return DIRECTORY + SEPERATOR + FILE_WD + FILE_EXTENSION;
    }

    static final String getFilepathPDElement(int group) {
        return DIRECTORY + SEPERATOR + FILE_PD_PREFIX + FILE_PD_ELEMENT + group + FILE_EXTENSION;
    }

    static final String getFilepathPD(PatternOptions type, int choice) {
        if (type == PatternOptions.Pattern_Custom) {
            throw new UnsupportedOperationException("Custom pattern will not store a local copy.");
        }
        String filepath = DIRECTORY + SEPERATOR + FILE_PD_PREFIX + type.getType();
        if (choice == 0) {
            return filepath + FILE_PD_DEFAULT + FILE_EXTENSION;
        }
        return filepath + FILE_PD_OPTION + FILE_EXTENSION;
    }

    public static final String getFilepathReference() {
        return DIRECTORY + SEPERATOR + FILE_REFERENCE + FILE_EXTENSION;
    }
}


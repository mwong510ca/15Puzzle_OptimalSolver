package mwong.myprojects.fifteenpuzzle;

import mwong.myprojects.fifteenpuzzle.puzzle.PatternOptions;

/**
 * FilePreperties contains all data files setting of 15 puzzle data storage.
 * If config.properties file not found or no custom setting, it will use default
 * setting.
 *
 * <p>Dependencies : PatternOptions.java, PropertiesCache.java
 *
 * @author <a href="http://www.linkedin.com/pub/macy-wong/46/550/37b/"
 *            target="_blank">Meisze Wong (linkedin)</a>
 * @see <a href="http://www.github.com/mwong510ca/15PuzzleOptimalSolver/"
 *         target="_blank">GitHub (full project)</a>
 */
public final class FileProperties {
  /** The separator of system. */
  private static final String SEPARATOR = System.getProperty("file.separator");
  /** The preset direction name. */
  private static String directory = "database";
  /** The walking distance filename. */
  private static String walkingDist = "walking_distance";
  /** The pattern database file prefix. */
  private static String pdbPrefix = "pattern_";
  /** The partial filename of element group. */
  private static String pdbElement = "element_";
  /** The partial filename of default pattern. */
  private static String pdbDefault = "_default";
  /** The partial filename of pattern preset option. */
  private static String pdbOption = "_option";
  /** The reference collection file name. */
  private static String referenceCollection = "reference";
  /** The file extension. */
  private static String extension = "db";

  static {
    if (PropertiesCache.getInstance().containsKey("directory")) {
      directory = PropertiesCache.getInstance().getProperty("directory");
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

    if (PropertiesCache.getInstance().containsKey("fileExtension")) {
      extension = PropertiesCache.getInstance().getProperty("fileExtension");
    }
  }

  /** Private constructor, no instance. */
  private FileProperties() {
    // Not called
  }

  /**
   * Returns the file path for remote property.
   *
   * @return String of file path for remote property
   */
  public static String getRemotePropertyFile() {
    return PropertiesCache.getRemotePropertyFile();
  }

  /**
   * Returns the directory folder name of data storage.
   *
   * @return String of directory folder name
   */
  public static String getDirectory() {
    return directory;
  }

  /**
   * Returns the file path for walking distance data set.
   *
   * @return String of file path for walking distance data set
   */
  public static String getFilepathWd() {
    return directory + SEPARATOR + walkingDist + "." + extension;
  }

  /**
   * Returns the file path for pattern element data set.
   *
   * @param group the given element group size
   * @return String of file path for pattern element data set
   */
  public static String getFilepathPdElement(final int group) {
    return directory + SEPARATOR + pdbPrefix + pdbElement + group + "." + extension;
  }

  /**
   * Returns the file path for preset pattern database.
   *
   * @param type the given preset pattern type
   * @param choice the index of preset pattern option
   * @return String of file path for preset pattern database
   */
  public static String getFilepathPdb(final PatternOptions type, final int choice) {
    if (type == PatternOptions.Pattern_Custom) {
      throw new UnsupportedOperationException("Custom pattern will not store a local copy.");
    }
    String filepath = directory + SEPARATOR + pdbPrefix + type.getType();
    if (choice == 0) {
      return filepath + pdbDefault + "." + extension;
    }
    return filepath + pdbOption + choice + "." + extension;
  }

  /**
   * Returns the file path for reference data storage.
   *
   * @return String of file path for reference data storage
   */
  public static String getFilepathReference() {
    return directory + SEPARATOR + referenceCollection + "." + extension;
  }

  /**
   * Returns the directory path of all files.
   *
   * @return String of directory path of all files
   */
  public static String getFilepathBase() {
    return directory + SEPARATOR;
  }
}


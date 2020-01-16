package mwong.myprojects.fifteenpuzzle.puzzle;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;

import mwong.myprojects.fifteenpuzzle.FileProperties;
import mwong.myprojects.fifteenpuzzle.solution.Solver.ApplicationMode;
import mwong.myprojects.fifteenpuzzle.solution.Stopwatch;

/**
 * PatternElement provides universal pattern keys and formats with links.  It takes a boolean array
 * represent the pattern groups and a PatternElementMode for usage. It either load from storage
 * or generate a new set if local file not exists.
 *
 * <p>Dependencies : FileProperties.java, PuzzleConstants.java, PatternConstants.java,
 *                   SolverBuilder.java, Stopwatch.java
 *
 * @author <a href="http://www.linkedin.com/pub/macy-wong/46/550/37b/"
 *            target="_blank">Meisze Wong (linkedin)</a>
 * @see <a href="http://www.github.com/mwong510ca/15PuzzleOptimalSolver/"
 *         target="_blank">GitHub (full project)</a>
 */
public final class PatternElement {
  /** Puzzle size.
   *  @see PuzzleConstants#SIZE */
  private static final int PUZZLE_SIZE = PuzzleConstants.getSize();
  /** Puzzle row size.
   *  @see PuzzleConstants#ROW_SIZE */
  private static final int ROW_SIZE = PuzzleConstants.getRowSize();
  /** Pattern key size array of group size.
   *  @see PatternConstants#KEY_SIZE */
  private static final int[] KEY_SIZE = PatternConstants.getKeySize();
  /** Pattern format size array of group size.
   *  @see PatternConstants#FORMAT_SIZE */
  private static final int[] FORMAT_SIZE = PatternConstants.getFormatSize();
  /** Maximum pattern size allowed.
   *  @see PatternConstants#MAX_GROUP_SIZE */
  private static final int MAX_GROUP_SIZE = PatternConstants.getMaxGroupSize();
  /** Maximum key shift per group.
   *  @see PatternConstants#MAX_SHIFT */
  private static final int[] MAX_SHIFT = PatternConstants.getMaxShift();
  /** The array of single format bit of 16 tiles.
   *  @see PatternConstants#FORMAT_BIT_16 */
  private static final int[] FORMAT_BIT_16 = PatternConstants.getFormatBit16();
  /** The number of possible moves or directions.
   *  @see PuzzleConstants#DIRECTION_SIZE */
  private static final int NUM_DIR = PuzzleConstants.getDirectionSize();
  /** The total format move size.
   *  @see PatternConstants#FORMAT_MOVE_SIZE */
  private static final int FORMAT_MOVE_SIZE = PatternConstants.getFormatMoveSize();
  /** The pattern key bit size.
   *  @see PuzzleConstants#TILE_BIT_SIZE */
  private static final int KEY_BIT_SIZE = PatternConstants.getKeyBitSize();
  /** The pattern key bits.
   *  @see PuzzleConstants#TILE_BITS */
  private static final int KEY_BITS = PatternConstants.getKeyBits();

  /** The HashMap of key bits to key index. */
  private HashMap<Integer, Integer> keys;
  /** The HashMap of format bits to format index. */
  private HashMap<Integer, Integer> formats;
  /** The double integer array of keys. */
  private int[][] keys2combo;
  /** The double integer array of formats. */
  private int[][] formats2combo;
  /** The double array of key change by number of rotation.
   *  for each group : key size x number of keys x 6 shift max (3 left, 3 right)
   *  odd:  tile DOWN, tile 1 shift to right 012 =&gt; 002
   *                                         000    010
   *  even: tile UP,   tile 2 shift to left  000 =&gt; 002
   *                                         012    010 */
  private int[][] rotateKeyByPos;
  /** The triple integer array of format size to move size per group, use by pattern database
   *  generator. It store compress two values per single format bit, number of key rotation
   *  and next format index.
   *  For each group : format size x number of keys x 4 direction of moves */
  private int[][][] linkFormatCombo;
  /** The double integer array of format moves per group, use by the solver.
   *  Unlike the linkFormatCombo, it compress in 2 dimension arrays.
   *  It carry next format index after the single bit of format take a direction change. *
   *   for each group : format size x (number of keys x 4 direction of moves) */
  private int[][] linkFormatMove;

  /**
   * Initializes the PatternElement with standard groups in generator mode.
   */
  public PatternElement() {
    this(PatternConstants.getStandardGroups(), ElementRole.GENERATOR, ApplicationMode.CONSOLE);
  }

  /**
   * Initializes the PatternElement with given pattern groups and generator mode.
   *
   * @param patternGroups boolean array of pattern groups in use
   * @param action the PatternElementMode of Generator or PuzzleSolver
   */
  public PatternElement(final boolean[] patternGroups, final ElementRole action) {
    this(patternGroups, action, ApplicationMode.CONSOLE);
  }

  /**
   * Initializes the PatternElement with given pattern groups, generator mode and application mode.
   *
   * @param patternGroups boolean array of pattern groups in use
   * @param action the PatternElementMode of Generator or PuzzleSolver
   * @param appMode the given applicationMode for GUI or CONSOLE
   */
  public PatternElement(final boolean[] patternGroups, final ElementRole action,
      final ApplicationMode appMode) {
    if (patternGroups.length != PatternConstants.getMaxGroupSize() + 1) {
      System.err.println("Invalid input - require boolean array of size 9 (0 to 8 group)");
      throw new IllegalArgumentException();
    }
    loadData(patternGroups, action, appMode);
  }

  /**
   * Load the database pattern components from file.
   *
   * @param patternGroups the boolean array represents the pattern group in use
   * @param action the given ElementMode for puzzle solver or generator
   * @param appMode the given ApplicationMode, GUI mode load from data file only
   */
  private void loadData(final boolean[] patternGroups, final ElementRole action,
      final ApplicationMode appMode) {
    keys = new HashMap<Integer, Integer>();
    formats = new HashMap<Integer, Integer>();
    linkFormatCombo = new int[MAX_GROUP_SIZE + 1][0][0];
    linkFormatMove = new int[MAX_GROUP_SIZE + 1][0];
    rotateKeyByPos = new int[MAX_GROUP_SIZE + 1][0];
    keys2combo = new int[MAX_GROUP_SIZE + 1][0];
    formats2combo = new int[MAX_GROUP_SIZE + 1][0];

    boolean printMsg = true;
    if (action == ElementRole.PUZZLE_SOLVER) {
      printMsg = false;
    }
    Stopwatch stopwatch = new Stopwatch();

    for (int group = 2; group <= MAX_GROUP_SIZE; group++) {
      if (patternGroups[group]) {
        String filepath = FileProperties.getFilepathPdElement(group);
        try (FileInputStream fin = new FileInputStream(filepath);
            FileChannel inChannel = fin.getChannel();) {
          ByteBuffer buffer =
              inChannel.map(FileChannel.MapMode.READ_ONLY, 0, inChannel.size());

          keys2combo[group] = new int[KEY_SIZE[group]];
          for (int i = 0; i < KEY_SIZE[group]; i++) {
            keys2combo[group][i] = buffer.getInt();
            keys.put(keys2combo[group][i], i);
          }

          rotateKeyByPos[group] = new int[KEY_SIZE[group] * group * MAX_SHIFT[group] * 2];
          for (int i = 0; i < rotateKeyByPos[group].length; i++) {
            rotateKeyByPos[group][i] = buffer.getInt();
          }

          formats2combo[group] = new int[FORMAT_SIZE[group]];
          for (int i = 0; i < FORMAT_SIZE[group]; i++) {
            formats2combo[group][i] = buffer.getInt();
            formats.put(formats2combo[group][i], i);
          }

          if (action == ElementRole.PUZZLE_SOLVER) {
            linkFormatMove[group] = new int[FORMAT_SIZE[group] * FORMAT_MOVE_SIZE];
            for (int i = 0; i < linkFormatMove[group].length; i++) {
              linkFormatMove[group][i] = buffer.getInt();
            }
            // skip remaining linkFormatCombo set for puzzle solver
          } else {
            // skip following linkFormatMove set for generator
            for (int i = 0; i < FORMAT_SIZE[group] * FORMAT_MOVE_SIZE; i++) {
              buffer.getInt();
            }

            linkFormatCombo[group] = new int[FORMAT_SIZE[group]][group * NUM_DIR];
            for (int f = 0; f < FORMAT_SIZE[group]; f++) {
              for (int i = 0; i < group * NUM_DIR; i++) {
                linkFormatCombo[group][f][i] = buffer.getInt();
              }
            }
          }
        } catch (BufferUnderflowException | IOException ex) {
          if (appMode == ApplicationMode.GUI) {
            System.err.println("\n\t*** Data files missing, please download from cloud drive."
                + " ***\n\n\thttps://my.pcloud.com/publink/show?"
                + "code=kZSoaLZgNeLhO2eu0RQcu9D2aXeOFgtioUV\n");
            throw new UnsupportedOperationException();
          }
          build();
          saveData(patternGroups, printMsg);
          wrapup(patternGroups, action);
          return;
        }
      }
    }
    if (printMsg) {
      System.out.println("PatternElement - load data from file succeeded : "
          + stopwatch.currentTime() + "s");
    }
  }

  /**
   * Save the database pattern components in file.
   *
   * @param patternGroups boolean array represents the pattern group in use
   * @param printMsg the boolean flag to print message on terminal
   */
  private void saveData(final boolean[] patternGroups, final boolean printMsg)  {
    String directory = FileProperties.getDirectory();
    if (!(new File(directory)).exists()) {
      (new File(directory)).mkdir();
    }

    Stopwatch stopwatch = new Stopwatch();
    // store key components from group 2 to 8 in data file
    for (int group = 2; group <= MAX_GROUP_SIZE; group++) {
      if (patternGroups[group]) {
        final String filepath = FileProperties.getFilepathPdElement(group);
        if (new File(filepath).exists()) {
          (new File(filepath)).delete();
        }

        final int integerByteSize = 4;
        try (FileOutputStream fout = new FileOutputStream(filepath);
            FileChannel outChannel = fout.getChannel();) {
          ByteBuffer buffer = ByteBuffer.allocateDirect(KEY_SIZE[group] * integerByteSize);
          for (int combo : keys2combo[group]) {
            buffer.putInt(combo);
          }
          buffer.flip();
          outChannel.write(buffer);

          buffer = ByteBuffer.allocateDirect(rotateKeyByPos[group].length * integerByteSize);
          for (int i = 0; i < rotateKeyByPos[group].length; i++) {
            buffer.putInt(rotateKeyByPos[group][i]);
          }
          buffer.flip();
          outChannel.write(buffer);

          buffer = ByteBuffer.allocateDirect(FORMAT_SIZE[group] * integerByteSize);
          for (int combo : formats2combo[group]) {
            buffer.putInt(combo);
          }
          buffer.flip();
          outChannel.write(buffer);

          buffer = ByteBuffer.allocateDirect(FORMAT_SIZE[group] * FORMAT_MOVE_SIZE
              * integerByteSize);
          for (int combo : linkFormatMove[group]) {
            buffer.putInt(combo);
          }
          buffer.flip();
          outChannel.write(buffer);

          buffer = ByteBuffer.allocateDirect(FORMAT_SIZE[group] * group * NUM_DIR
              * integerByteSize);
          for (int f = 0; f < FORMAT_SIZE[group]; f++) {
            for (int combo : linkFormatCombo[group][f]) {
              buffer.putInt(combo);
            }
          }
          buffer.flip();
          outChannel.write(buffer);
        } catch (BufferUnderflowException | IOException ex) {
          if (printMsg) {
            System.out.println("PatternElement - save data in file failed.");
          }
          if ((new File(filepath)).exists()) {
            (new File(filepath)).delete();
          }
          return;
        }
      }
    }
    if (printMsg) {
      System.out.println("PatternElement - save data in file succeeded : "
          + stopwatch.currentTime() + "s");
    }
  }

  /**
   * Clear all unused components.
   *
   * @param groups the boolean array represents the pattern group in use
   * @param mode the represent the application for puzzle or generator
   */
  private void wrapup(final boolean[] groups, final ElementRole mode) {
    for (int group = 1; group < groups.length; group++) {
      if (!groups[group]) {
        if (group > 1) {
          for (int key : keys2combo[group]) {
            keys.remove(key);
          }
        }
        for (int format : formats2combo[group]) {
          formats.remove(format);
        }
        keys2combo[group] = null;
        formats2combo[group] = null;
        linkFormatCombo[group] = null;
        rotateKeyByPos[group] = null;
        linkFormatMove[group] = null;
      }
    }

    if (mode == ElementRole.GENERATOR) {
      linkFormatMove = null;
    } else if (mode == ElementRole.PUZZLE_SOLVER) {
      keys2combo = null;
      formats2combo = null;
      linkFormatCombo = null;
    }
  }

  /**
   * Initializes all storages then generate keys and format components.
   */
  private void build() {
    keys = new HashMap<Integer, Integer>();
    formats = new HashMap<Integer, Integer>();
    keys2combo = new int[MAX_GROUP_SIZE + 1][];
    formats2combo = new int[MAX_GROUP_SIZE + 1][];
    linkFormatCombo = new int[MAX_GROUP_SIZE + 1][][];
    linkFormatMove = new int[MAX_GROUP_SIZE + 1][];
    rotateKeyByPos = new int[MAX_GROUP_SIZE + 1][];
    Stopwatch stopwatch = new Stopwatch();
    genKeys();
    genFormats();
    System.out.println("PatternElement - generate data set completed : "
        + stopwatch.currentTime() + "s");
  }

  /**
   * Generate the key components from group 2 to 8.
   */
  private void genKeys() {
    int[] initKeys = new int[MAX_GROUP_SIZE + 1];
    int basedGroup = 1;
    int counter = 0;
    HashSet<int[]> set = new HashSet<int[]>();
    int[] basedKey = {0};
    set.add(basedKey);

    int[] partialBits = new int[MAX_GROUP_SIZE];
    for (int i = 1; i < MAX_GROUP_SIZE; i++) {
      int val = 0;
      for (int j = 0; j < i; j++) {
        val = (val << KEY_BIT_SIZE) | KEY_BITS;
      }
      partialBits[i] = val;
    }

    // expand the based group (1 - 7) for key set of size 2 - 8
    while (basedGroup < MAX_GROUP_SIZE) {
      keys2combo[basedGroup + 1] = new int[KEY_SIZE[basedGroup + 1]];

      HashSet<int[]> expend = new HashSet<int[]>();
      counter = 0;
      for (int[] previousKey : set) {
        for (int pos = 0; pos < basedGroup; pos++) {
          basedKey = new int[basedGroup + 1];
          System.arraycopy(previousKey, 0, basedKey, 0, pos);
          basedKey[pos] = basedGroup;
          System.arraycopy(previousKey, pos, basedKey, pos + 1, basedGroup - pos);
          expend.add(basedKey);
        }

        basedKey = new int[basedGroup + 1];
        System.arraycopy(previousKey, 0, basedKey, 0, basedGroup);
        basedKey[basedGroup] = basedGroup;
        expend.add(basedKey);
      }

      basedGroup++;

      // keep in sorted order, easy for track. (unnecessary)
      TreeSet<Integer> sorted = new TreeSet<Integer>();
      for (int[] combo : expend) {
        int compressKey = 0;
        for (int key : combo) {
          compressKey = compressKey << KEY_BIT_SIZE | key;
        }
        sorted.add(compressKey);
      }
      for (int compressKey : sorted) {
        keys2combo[basedGroup][counter] = compressKey;
        keys.put(compressKey, counter++);
      }

      set = expend;
      initKeys[basedGroup] = sorted.first();
      genRotateKeys(basedGroup, initKeys, partialBits);
    }
  }

  /**
   * Generate the key links of move UP or DOWN which impact the changes of the order set of keys.
   * <p>odd:  tile DOWN, tile 1 shift to right 012 =&gt; 002
   *                                         000    010
   * even: tile UP,   tile 2 shift to left  000 =&gt; 002
   *                                        012    010 </p>
   *
   * @param group the given pattern group size
   * @param initKeys the integer array of keys of given group size
   * @param partialBits the preset bits for extraction from pattern keys
   */
  private void genRotateKeys(final int group, final int[] initKeys, final int[] partialBits) {
    if (group < 2) {
      return;
    }
    int shiftCount = MAX_SHIFT[group];

    int[][][] temp = new int[KEY_SIZE[group]][][];
    HashSet<Integer> set = new HashSet<Integer>();
    HashSet<Integer> visited;
    set.add(initKeys[group]);

    while (!set.isEmpty()) {
      visited = set;
      set = new HashSet<Integer>();
      for (int val : visited) {
        int keyIdx = keys.get(val);
        temp[keyIdx] = new int[group][shiftCount * 2];

        for (int pos = 0; pos < group; pos++) {
          int shiftBits = (group - pos - 1) * KEY_BIT_SIZE;
          int self = (val & (partialBits[1] << shiftBits)) >> shiftBits;

          //right shift use odd numbers 1, 3, 5 for 1 to 3 shifts
          int base = val >> (shiftBits + KEY_BIT_SIZE);
          for (int shift = 1; shift <= shiftCount; ++shift) {
            if (pos + shift < group) {
              int rightShift = shiftBits - (shift * KEY_BIT_SIZE);
              int portion = (val & (partialBits[shift] << rightShift)) >> rightShift;
              int unshift = val & partialBits[group - pos - shift - 1];
              int val2 = ((((base << (shift * KEY_BIT_SIZE)) | portion) << KEY_BIT_SIZE) | self)
                  << ((group - pos - shift - 1) * KEY_BIT_SIZE) | unshift;
              temp[keyIdx][pos][shift * 2 - 1] = keys.get(val2);
              if (temp[keys.get(val2)] == null) {
                set.add(val2);
              }
            } else {
              break;
            }
          }

          //left shift use even numbers 0, 2, 4 for 1 to 3 shifts
          base = val & partialBits[group - pos - 1];
          for (int shift = 1; shift <= shiftCount; ++shift) {
            if (pos - shift >= 0) {
              int unshift = val >> ((group - pos + shift) * KEY_BIT_SIZE);
              int leftShift = shiftBits + KEY_BIT_SIZE;
              int portion = (val & (partialBits[shift] << leftShift)) >> leftShift;
              int val2 = ((((unshift << KEY_BIT_SIZE) | self) << (shift * KEY_BIT_SIZE) | portion)
                  << (KEY_BIT_SIZE * (group - pos - 1))) | base;
              temp[keyIdx][pos][(shift - 1) * 2] = keys.get(val2);
              if (temp[keys.get(val2)] == null) {
                set.add(val2);
              }
            } else {
              break;
            }
          }
        }
      }
    }

    rotateKeyByPos[group] = new int[KEY_SIZE[group] * group * (shiftCount * 2)];
    int idx = 0;
    for (int k = 0; k < KEY_SIZE[group]; k++) {
      for (int o = 0; o < group; o++) {
        for (int s = 0; s < shiftCount * 2; s++) {
          rotateKeyByPos[group][idx++] = temp[k][o][s];
        }
      }
    }
  }

  /**
   * Generate the format components from group 1 to 8.
   */
  private void genFormats() {
    int[] initFormat = new int[MAX_GROUP_SIZE + 1];
    int basedGroup = 0;
    int counter = 0;
    HashSet<int[]> set = new HashSet<int[]>();
    int[] basedFormat = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    set.add(basedFormat);

    // expand the based group (1 - 7) for format set with key size 2 - 8
    while (basedGroup < MAX_GROUP_SIZE) {
      formats2combo[basedGroup + 1] = new int[FORMAT_SIZE[basedGroup + 1]];
      HashSet<int[]> expend = new HashSet<int[]>();
      counter = 0;

      // keep in sorted order, easy to track (unnecessary)
      TreeSet<Integer> sorted = new TreeSet<Integer>();
      for (int[] previousFormat : set) {
        for (int pos = 0; pos < PUZZLE_SIZE; pos++) {
          if (previousFormat[pos] == 0) {
            basedFormat = new int[PUZZLE_SIZE];
            System.arraycopy(previousFormat, 0, basedFormat, 0, PUZZLE_SIZE);
            basedFormat[pos] = 1;

            int compressFormat = 0;
            for (int key : basedFormat) {
              compressFormat = compressFormat << 1 | key;
            }

            if (!sorted.contains(compressFormat)) {
              sorted.add(compressFormat);
              expend.add(basedFormat);
            }
          }
        }
      }

      basedGroup++;
      for (int compressFormat : sorted) {
        formats2combo[basedGroup][counter] = compressFormat;
        formats.put(compressFormat, counter++);
      }

      set = expend;
      initFormat[basedGroup] = sorted.first();
      if (basedGroup > 1) {
        genLinkFormats(basedGroup, initFormat);
      }
    }
  }

  /**
   * Generate the format link of 4 direction of moves of each bit represents a tile location.
   * and the key shift reference code
   *
   * @param group the given pattern group size
   * @param initFormat the integer array of formats of given group size
   */
  private void genLinkFormats(final int group, final int[] initFormat) {
    linkFormatCombo[group] = new int[FORMAT_SIZE[group]][0];
    linkFormatMove[group] = new int[FORMAT_SIZE[group] * FORMAT_MOVE_SIZE];
    HashSet<Integer> set = new HashSet<Integer>();
    HashSet<Integer> visited;
    set.add(initFormat[group]);
    while (!set.isEmpty()) {
      visited = set;
      set = new HashSet<Integer>();
      for (int fmt : visited) {
        int fmtIdx = formats.get(fmt);
        int key = 0;
        linkFormatCombo[group][fmtIdx] = new int[group * NUM_DIR];

        for (int pos = 0; pos < PUZZLE_SIZE; pos++) {
          if ((fmt & FORMAT_BIT_16[pos]) > 0) {
            int[] next = {-1, -1, -1, -1};
            int[] shift = {0, 0, 0, 0};
            int base = fmt ^ FORMAT_BIT_16[pos];

            // space right, tile left
            if (pos % ROW_SIZE > 0) {
              if ((fmt & FORMAT_BIT_16[pos - 1]) == 0) {
                next[Board.Move.RIGHT.getValue()] = base | FORMAT_BIT_16[pos - 1];
              }
            }

            // space down, tile up
            if (pos / ROW_SIZE > 0) {
              if ((fmt & FORMAT_BIT_16[pos - ROW_SIZE]) == 0) {
                next[Board.Move.DOWN.getValue()] = base | FORMAT_BIT_16[pos - ROW_SIZE];
                int numShift = 0;
                for (int keyShift = 1; keyShift < ROW_SIZE; keyShift++) {
                  if ((fmt & FORMAT_BIT_16[pos - keyShift]) > 0) {
                    numShift++;
                  }
                }
                if (numShift > 0) {
                  shift[Board.Move.DOWN.getValue()] = numShift * 2 - 1;
                }
              }
            }

            // space left, tile right
            if (pos % ROW_SIZE < ROW_SIZE - 1) {
              if ((fmt & FORMAT_BIT_16[pos + 1]) == 0) {
                next[Board.Move.LEFT.getValue()] = base | FORMAT_BIT_16[pos + 1];
              }
            }

            // space up, tile down
            if (pos / ROW_SIZE < ROW_SIZE - 1) {
              if ((fmt & FORMAT_BIT_16[pos + ROW_SIZE]) == 0) {
                next[Board.Move.UP.getValue()] = base | FORMAT_BIT_16[pos + ROW_SIZE];
                int numShift = 0;
                for (int keyShift = 1; keyShift < ROW_SIZE; keyShift++) {
                  if ((fmt & FORMAT_BIT_16[pos + keyShift]) > 0) {
                    numShift++;
                  }
                }
                if (numShift > 0) {
                  shift[Board.Move.UP.getValue()] = numShift * 2;
                }
              }
            }

            for (int move = 0; move < NUM_DIR; move++) {
              if (next[move] > -1) {
                int zeroPos = pos;
                if (move == Board.Move.RIGHT.getValue()) {
                  zeroPos--;
                } else if (move == Board.Move.DOWN.getValue()) {
                  zeroPos -= ROW_SIZE;
                } else if (move == Board.Move.LEFT.getValue()) {
                  zeroPos++;
                } else if (move == Board.Move.UP.getValue()) {
                  zeroPos += ROW_SIZE;
                }
                linkFormatCombo[group][fmtIdx][key * NUM_DIR + move]
                    = shift[move] | (next[move] << KEY_BIT_SIZE);
                linkFormatMove[group][fmtIdx * FORMAT_MOVE_SIZE + zeroPos * NUM_DIR + move]
                    = (formats.get(next[move]) << (KEY_BIT_SIZE * 2))
                    | (key << KEY_BIT_SIZE) | shift[move];
                if (linkFormatCombo[group][formats.get(next[move])].length == 0) {
                  set.add(next[move]);
                }
              }
            }
            key++;
          }
        }
      }
    }
  }

  /**
   * Returns HashMap of compress key combo to key index.
   *
   * @return HashMap of compress key combo to key index
   */
  public HashMap<Integer, Integer> getKeys() {
    return keys;
  }

  /**
   * Returns HashMap of 16 bits format pattern to format index.
   *
   * @return HashMap of 16 bits format pattern to format index
   */
  public HashMap<Integer, Integer> getFormats() {
    return formats;
  }

  /**
   * Returns integer array of 16 bits format pattern with the given group.
   *
   * @param group the given group size
   * @return integer array of 16 bits format pattern with the given group
   */
  public int[] getFormatCombo(final int group) {
    return formats2combo[group];
  }

  /**
   * Returns integer array of 32 bits keys combo with the given group.
   *
   * @param group the given group size
   * @return integer array of 32 bits keys combo with the given group
   */
  public int[] getKeyCombo(final int group) {
    return keys2combo[group];
  }

  /**
   * Returns integer array of key shifting set with the given group.
   *
   * @param group the given group size
   * @return integer array of key shifting set with the given group
   */
  public int[] getKeyShiftSet(final int group) {
    return rotateKeyByPos[group];
  }

  /**
   * Returns integer array of format change set with the given group.
   *
   * @param group the given group size
   * @return integer array of format change set with the given group
   */
  int[][] getLinkFormatComboSet(final int group) {
    return linkFormatCombo[group];
  }

  /**
   * Returns integer array of key shift count from format change set with the given group.
   *
   * @param group the given group size
   * @return integer array of key shift count from format change set with the given group
   */
  public int[] getLinkFormatMoveSet(final int group) {
    return linkFormatMove[group];
  }

  /**
   * ElementMode is enum type that determine the purpose of the object.
   *
   * @author <a href="http://www.linkedin.com/pub/macy-wong/46/550/37b/"
   *            target="_blank">Meisze Wong (linkedin)</a>
   */
  public enum ElementRole {
    /**
     * Pattern element mode for generator.
     */
    GENERATOR,

    /**
     * Pattern element mode for puzzle solver.
     */
    PUZZLE_SOLVER;
  }

  /**
   * Unit Test.
   *
   * @param args standard argument main function
   */
  public static void main(final String[] args) {
    PatternElement pe = new PatternElement();
    pe.build();
    pe.saveData(PatternConstants.getStandardGroups(), true);
  }
}

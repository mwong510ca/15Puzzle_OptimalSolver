package mwong.myprojects.fifteenpuzzle.puzzle;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;

import mwong.myprojects.fifteenpuzzle.FileProperties;
import mwong.myprojects.fifteenpuzzle.solution.Solver.ApplicationMode;
import mwong.myprojects.fifteenpuzzle.solution.Stopwatch;

/**
 * PatternDatabase provides a set of pattern database values and conversion keys.
 * It takes preset pattern of PatternOptions or a byte array of custom pattern.
 * It either load from storage if preset pattern exists. Otherwise, it will generate
 * a new set. Custom pattern is not allow to generate the group of 8 pattern.
 *
 * <p>Dependencies : FileProperties.java, PuzzleConstants.java, PatternOptions.java,
 *                   PatternConstants.java, PatternElement.java, SolverBuilder.java, Stopwatch.java
 *
 * <p>Remarks: group size of 8 takes 2.5-3 hours and require at least 2 GB -Xms2g
 *
 * @author <a href="http://www.linkedin.com/pub/macy-wong/46/550/37b/"
 *            target="_blank">Meisze Wong (linkedin)</a>
 * @see <a href="http://www.github.com/mwong510ca/15PuzzleOptimalSolver/"
 *         target="_blank">GitHub (full project)</a>
 */
public final class PatternDatabase {
  /** Puzzle size.
   *  @see PuzzleConstants#SIZE */
  private static final int PUZZLE_SIZE = PuzzleConstants.getSize();
  /** Row size.
   *  @see PuzzleConstants#ROW_SIZE */
  private static final int ROW_SIZE = PuzzleConstants.getRowSize();
  /** Number of moves or directions.
   *  @see PuzzleConstants#DIRECTION_SIZE */
  private static final int NUM_DIR = PuzzleConstants.getDirectionSize();
  /** Maximum pattern group size allowed 8.
   *  @see PatternConstants#MAX_GROUP_SIZE */
  private static final int MAX_GROUP_SIZE = PatternConstants.getMaxGroupSize();
  /** The key bit size is 4 for 0-15.
   *  @see PuzzleConstants#TILE_BIT_SIZE */
  private static final int KEY_BIT_SIZE = PatternConstants.getKeyBitSize(); // 4
  /** The key bits in binary is 00001111 =&gt; 0x0F.
   *  @see PuzzleConstants#TILE_BITS */
  private static final int KEY_BITS = PatternConstants.getKeyBits();        // 0x0F
  /** The array of single format bit of 16 tiles.
   *  @see PatternConstants#FORMAT_BIT_16 */
  private static final int[] FORMAT_BIT_16 = PatternConstants.getFormatBit16();
  /** For pattern 7-8 only. The zero position of 8 spaces. */
  private static final byte[] FORMAT_ZERO_8_ORDER;

  /** The byte array of each pattern group size. */
  private byte[] patternGroups;
  /** The double byte array of patterns.
   *  For each pattern group : key size x format size */
  private byte[][] patterns;
  /** The byte array of tile value convert to pattern key. */
  private byte[] val2ptnKey;
  /** The byte array of tile value convert to pattern group order. */
  private byte[] val2ptnOrder;
  /** The double byte array to restore the pattern key to tile value. */
  private byte[][] ptnKey2val;
  /** The boolean flag to enable or disable pattern group size 8 generation. */
  private final boolean size8disabled;

  // Generate 1<<7, 1<<6, ... 1<<1, 1
  static {
    final int size = 8;
    byte[] order8 = new byte[size];
    int shift = size - 1;
    for (int i = 0; i < size; i++) {
      order8[i] = (byte) (1 << shift--);
    }
    FORMAT_ZERO_8_ORDER = order8;
  }

  /**
   * Initializes the PatternDatabase object using default pattern.
   *
   * @param type the given PatternOptions type
   */
  public PatternDatabase(final PatternOptions type) {
    // default option is 0
    this(type, 0);
  }

  /**
   * Initializes the PatternDatabase object using the given preset pattern.
   *
   * @param type the given PatternOptions type
   * @param choice the integer of pattern option in PatternOptions
   */
  public PatternDatabase(final PatternOptions type, final int choice) {
    this(type, choice, ApplicationMode.CONSOLE);
  }

  /**
   * Initializes the PatternDatabase object using the given preset pattern and application mode.
   *
   * @param type the given PatternOptions type
   * @param choice the integer of pattern option in PatternOptions
   * @param appMode the given applicationMode
   */
  public PatternDatabase(final PatternOptions type, final int choice,
      final ApplicationMode appMode) {
    if (appMode == ApplicationMode.CONSOLE) {
      size8disabled = false;
    } else {
      size8disabled = true;
    }

    if (!type.isValidPattern(choice)) {
      throw new IllegalArgumentException("Pattern option out of range (0 to "
          + (type.getPatternSize() - 1) + "): " + type + " " + choice);
    } else {
      loadData(type, choice, appMode);
    }
  }

  /**
   * Initializes the PatternDatabase object using the given custom pattern.
   * It allow group 2 to 7 only and it will not save in data file.
   *
   * @param pattern the byte array of user defined pattern
   */
  public PatternDatabase(final byte[] pattern) {
    size8disabled = true;
    createPattern(pattern, null);
  }

  /**
   * Load the pattern database from file if exists.  Otherwise, create a new set and save in file.
   *
   * @param type the given PatternOptions
   * @param choice the given patterns index
   * @param appMode the given ApplicationMode
   */
  private void loadData(final PatternOptions type, final int choice,
      final ApplicationMode appMode) {
    String filepath = FileProperties.getFilepathPdb(type, choice);
    try (FileInputStream fin = new FileInputStream(filepath);
        FileChannel inChannel = fin.getChannel();) {
      ByteBuffer buf = inChannel.map(FileChannel.MapMode.READ_ONLY, 0, inChannel.size());
      int numPatterns = buf.get();
      patternGroups = new byte[numPatterns];
      val2ptnKey = new byte[PUZZLE_SIZE];
      val2ptnOrder = new byte[PUZZLE_SIZE];

      buf.get(patternGroups);
      buf.get(val2ptnKey);
      buf.get(val2ptnOrder);

      patterns = new byte[numPatterns][];
      for (int i = 0; i < numPatterns; i++) {
        int sizeKeys = PatternConstants.getKeySize(patternGroups[i]);
        int sizeFmts = PatternConstants.getFormatSize(patternGroups[i]);

        patterns[i] = new byte[sizeKeys * sizeFmts];
        buf.get(patterns[i]);
      }
    } catch (BufferUnderflowException | IOException ex) {
      if (appMode != ApplicationMode.CONSOLE) {
        System.err.println("\n\t*** Data files missing, please download from cloud drive. ***\n");
        System.err.println("\n\thttps://my.pcloud.com/publink/show?"
            + "code=kZSoaLZgNeLhO2eu0RQcu9D2aXeOFgtioUV\n");
        throw new UnsupportedOperationException();
      }
      if (type == PatternOptions.Pattern_78) {
        System.out.println("Warning: Please make sure increase minimum memory to -Xms2g");
        System.out.println("     and it takes ~ 2.5-3 hours to generate 78 pattern.");
      }
      createPattern(type.getPattern(choice), type.getElements());
      saveData(filepath);
    }
  }

  /**
   * Save the pattern database in file.
   *
   * @param filepath the given file path
   */
  private void saveData(final String filepath) {
    System.out.println("Saving a local copy ...");
    String directory = FileProperties.getDirectory();
    if (!(new File(directory)).exists()) {
      (new File(directory)).mkdir();
    }
    if (!(new File(filepath)).exists()) {
      (new File(filepath)).delete();
    }

    try (FileOutputStream fout = new FileOutputStream(filepath);
        FileChannel outChannel = fout.getChannel();) {
      int numPatternGroups = patternGroups.length;
      final int initPtnSize = 33; // see below for details
      ByteBuffer buffer = ByteBuffer.allocateDirect(numPatternGroups + initPtnSize);
      buffer.put((byte) patternGroups.length);  // 1 byte - number of Pattern Groups
      buffer.put(patternGroups);         // Pattern Groups array
      buffer.put(val2ptnKey);            // 16 bytes - keys array
      buffer.put(val2ptnOrder);          // 16 bytes - orders array
      buffer.flip();
      outChannel.write(buffer);

      for (int i = 0; i < numPatternGroups; i++) {
        int sizeKeys = PatternConstants.getKeySize(patternGroups[i]);
        int sizeFmts = PatternConstants.getFormatSize(patternGroups[i]);

        buffer = ByteBuffer.allocateDirect(sizeKeys * sizeFmts);
        buffer.put(patterns[i]);
        buffer.flip();
        outChannel.write(buffer);
      }

      System.out.println("PatternDatabase - save data set in file succeeded.");
    } catch (BufferUnderflowException | IOException ex) {
      System.out.println("PatternDatabase - save data set in file failed");
      if ((new File(filepath)).exists()) {
        (new File(filepath)).delete();
      }
    }
  }

  /**
   * validate the pattern format, and generate the additive pattern database.
   *
   * @param pattern the byte array of pattern
   * @param initElementGroups the boolean array of group size associate with pattern array
   */
  private void createPattern(final byte[] pattern, final boolean[] initElementGroups) {
    // validate the pattern format
    if (pattern.length != PUZZLE_SIZE) {
      System.err.println("Invalid pattern - size != 16");
      throw new IllegalArgumentException();
    }
    if (pattern[PUZZLE_SIZE - 1] != 0) {
      System.err.println("Invalid pattern - pattern[15] != 0");
      throw new IllegalArgumentException();
    }
    if (pattern[PUZZLE_SIZE - 2] < 2 || pattern[PUZZLE_SIZE - 2] > MAX_GROUP_SIZE) {
      System.err.println("Invalid pattern - pattern[14] num of groups");
      throw new IllegalArgumentException();
    }

    // analysis the pattern format, store the relations of original tile value,
    // element components and pattern order in local storage
    int numOfPatterns = pattern[PUZZLE_SIZE - 2];
    patternGroups = new byte[numOfPatterns];
    int[] ptnFormat = new int[numOfPatterns];

    for (int i = 0; i < PUZZLE_SIZE - 1; i++) {
      if (pattern[i] < 1) {
        System.err.println("Invalid pattern - " + pattern[i] + " < 1");
        throw new IllegalArgumentException();
      }
      if (pattern[i] > numOfPatterns) {
        System.err.println("Invalid pattern - " + pattern[i] + " > " + pattern[PUZZLE_SIZE - 2]);
        throw new IllegalArgumentException();
      }
      int group = pattern[i] - 1;
      patternGroups[group]++;

      for (int j = 0; j < numOfPatterns; j++) {
        ptnFormat[j] = ptnFormat[j] << 1;
        if (j == group) {
          ptnFormat[j] |= 1;
        }
      }
    }
    for (int i = 0; i < numOfPatterns; i++) {
      if (patternGroups[i] == 0) {
        System.err.println("Invalid pattern - group " + (i + 1) + " is empty ");
        throw new IllegalArgumentException();
      }
    }

    patterns = new byte[numOfPatterns][];
    val2ptnKey = new byte[PUZZLE_SIZE];
    val2ptnOrder = new byte[PUZZLE_SIZE];
    ptnKey2val = new byte[pattern[PUZZLE_SIZE - 2]][];
    val2ptnKey[0] = -1;
    val2ptnOrder[0] = -1;
    int[] ctGroup = new int[numOfPatterns];
    for (int i = 0; i < PUZZLE_SIZE - 1; i++) {
      val2ptnOrder[i + 1] = (byte) (pattern[i] - 1);
      ctGroup[pattern[i] - 1]++;
    }
    for (int i = 0; i < numOfPatterns; i++) {
      ptnKey2val[i] = new byte[ctGroup[i]];

      // check runtime memory for pattern 8, stop if less than 1.6 GB.
      if (ctGroup[i] == MAX_GROUP_SIZE) {
        if (size8disabled) {
          throw new UnsupportedOperationException("Pattern group of 8 is not supported.");
        }
        final int mb = 1024 * 1024;
        final int reqMemory = 1600;
        if (Runtime.getRuntime().maxMemory() / mb < reqMemory) {
          final double thousand = 1000.0;
          System.out.println("Not enough estimate memory : "
              + (Runtime.getRuntime().maxMemory() / mb / thousand)
              + "GB < 1.6GB for pattern of 8");
          System.out.println("Please increase runtime memory (java -d64 -Xms2g)"
              + " and try again!  System exit.");
          System.exit(0);
        }
      }
    }

    for (int i = 0; i < numOfPatterns; i++) {
      int count2 = 0;
      for (byte j = 1; j < PUZZLE_SIZE; j++) {
        if (val2ptnOrder[j] == i) {
          ptnKey2val[i][count2] = j;
          val2ptnKey[j] = (byte) count2++;
        }
      }
    }

    // create PatternElement object if using in additive pattern
    boolean[] elementGroups = new boolean[MAX_GROUP_SIZE + 1];
    if (initElementGroups == null) {
      elementGroups = new boolean[MAX_GROUP_SIZE + 1];
      for (byte group : patternGroups) {
        elementGroups[group] = true;
      }
    } else {
      elementGroups = initElementGroups.clone();
    }
    PatternElement element = new PatternElement(elementGroups,
        PatternElement.ElementRole.GENERATOR);

    // create each additive pattern
    for (int i = 0; i < patternGroups.length; i++) {
      // shift 1 bit to left for zero at position 15, lower right corner
      ptnFormat[i] <<= 1;
      if (patternGroups[i] == MAX_GROUP_SIZE) {
        genPatternByte(i, ptnFormat[i], element);
      } else {
        genPatternShort(i, patternGroups[i], ptnFormat[i], element);
      }
    }
    System.out.println("PatternDatabase - generate additive pattern database completed");
  }

  /**
   * Use by additive pattern with 8 tiles (8 spaces for zeroes), collect actual zeroes
   * and pass in as byte value, move all zero spaces freely until it reach the tile
   * return 16 bits short represents a set of final moves that stop by a tile only.
   *
   * @param zeroOrder the byte value of combined zeroes space for pattern group 8 only
   * @param fmt the integer represent the given format combo
   * @return short value represent 16 bits format of zero reached the tile
   */
  private short freeMoveByte(final byte zeroOrder, final int fmt) {
    short initMoves = 0;
    boolean[] next = new boolean[PUZZLE_SIZE];
    int order = 0;
    for (int zeroPos = 0; zeroPos < PUZZLE_SIZE; zeroPos++) {
      if ((fmt & FORMAT_BIT_16[zeroPos]) > 0) {
        continue;
      }

      if ((zeroOrder & FORMAT_ZERO_8_ORDER[order]) != 0) {
        initMoves |= (short) FORMAT_BIT_16[zeroPos];
        next[zeroPos] = true;
      }
      order++;
    }
    return freeMove(initMoves, fmt, next);
  }

  /**
   * Use by additive pattern with 2 - 7 tiles (9 - 14 spaces for zeroes), collect
   * actual zeroes and pass in as integer value, move all zero spaces freely until
   * it reach the tile return 16 bits short represents a set of final moves that
   * stop by a tile only.
   *
   * @param zeroPos the short value of combined zeroes associate with the format
   * @param fmt the integer represent the given format combo
   * @return short value represent 16 bits format of zero reached the tile
   */
  private short freeMoveShort(final short zeroPos, final int fmt) {
    short initMoves = 0;
    boolean[] next = new boolean[PUZZLE_SIZE];
    for (int i = 0; i < PUZZLE_SIZE; i++) {
      if ((fmt & FORMAT_BIT_16[i]) > 0) {
        continue;
      }

      if ((zeroPos & FORMAT_BIT_16[i]) != 0) {
        initMoves |= (short) FORMAT_BIT_16[i];
        next[i] = true;
      }
    }
    return freeMove(initMoves, fmt, next);
  }

  /**
   * Return 16 bits short represents a set of final moves that stop by a tile only.
   * Expand the next moves until no more moves.
   *
   * @param initMoves the short value represent the initial moves
   * @param fmt the integer represent the given format combo
   * @param next the boolean array represent the next available moves
   * @return short value represent 16 bits format of zero reached the tile
   */
  private short freeMove(final short initMoves, final int fmt, final boolean[] next) {
    boolean flag = true;
    short validMoves = initMoves;
    while (flag) {
      flag = false;
      for (int i = 0; i < PUZZLE_SIZE; i++) {
        if (next[i]) {
          if (i % ROW_SIZE < ROW_SIZE - 1 && (fmt & FORMAT_BIT_16[i + 1]) == 0
              && (validMoves & FORMAT_BIT_16[i + 1]) == 0) {
            validMoves |= (short) FORMAT_BIT_16[i + 1];
            next[i + 1] = true;
            flag = true;
          }

          if (i / ROW_SIZE < ROW_SIZE - 1 && (fmt & FORMAT_BIT_16[i + ROW_SIZE]) == 0
              && (validMoves & FORMAT_BIT_16[i + ROW_SIZE]) == 0) {
            validMoves |= (short) FORMAT_BIT_16[i + ROW_SIZE];
            next[i + ROW_SIZE] = true;
            flag = true;
          }

          if (i % ROW_SIZE > 0 && (fmt & FORMAT_BIT_16[i - 1]) == 0
              && (validMoves & FORMAT_BIT_16[i - 1]) == 0) {
            validMoves |= (short) FORMAT_BIT_16[i - 1];
            next[i - 1] = true;
            flag = true;
          }

          if (i / ROW_SIZE > 0 && (fmt & FORMAT_BIT_16[i - ROW_SIZE]) == 0
              && (validMoves & FORMAT_BIT_16[i - ROW_SIZE]) == 0) {
            validMoves |= (short) FORMAT_BIT_16[i - ROW_SIZE];
            next[i - ROW_SIZE] = true;
            flag = true;
          }
          next[i] = false;
        }
      }
    }
    return validMoves;
  }

  /**
   * Return the position of empty slot from left to right in the format base on
   * the given index of zero.
   * eg. format: 0000111000011110, zero index: 7, return 4 (the 5th zero in format
   *          ^
   *     from left to right)
   *
   * @param zeroIdx the original index of zero space
   * @param fmt the integer value represent the format combo
   * @return integer value of zero index ignore the tiles
   */
  private int zeroIdx2Pos(final int zeroIdx, final int fmt) {
    int pos = 0;
    for (int i = 0; i < zeroIdx; i++) {
      if ((fmt & FORMAT_BIT_16[i]) == 0) {
        pos++;
      }
    }
    return pos;
  }

  /**
   * Generate the additive pattern of 8 tiles, use 8 bits byte (1 zero space
   * plus 7 tile locations) to record each move during the expansion.
   *
   * @param order the pattern order number of group size 8 only
   * @param orgFmt the initial format combo
   * @param element the PatternElement associate with the pattern
   */
  private void genPatternByte(final int order, final int orgFmt, final PatternElement element) {
    final int group = MAX_GROUP_SIZE;
    int sizeKey = PatternConstants.getKeySize(group);
    int sizeFmt = PatternConstants.getFormatSize(group);
    int sizeShift = PatternConstants.getMaxShiftX2(group);

    patterns[order] = new byte[sizeKey * sizeFmt];
    HashMap<Integer, Integer> formats = element.getFormats();
    int[] formats2combo = element.getFormatCombo(group);
    int[][] moveSet = element.getLinkFormatComboSet(group);
    int[] shiftSet = element.getKeyShiftSet(group);

    System.out.print("Screen additive pattern " + (order + 1) + " : (");
    for (int i = 0; i < PUZZLE_SIZE - 1; i++) {
      if ((orgFmt & FORMAT_BIT_16[i]) == 0) {
        System.out.print("x ");
      } else {
        System.out.print((i + 1) + " ");
      }
    }
    int orgKeyIdx = 0;
    int orgFmtIdx = formats.get(orgFmt);
    Stopwatch stopwatch = new Stopwatch();
    System.out.println("0) at " + stopwatch.currentTime() + "s");

    // additive pattern of 8 tiles
    byte[] currMove = new byte[sizeKey * sizeFmt];
    currMove[orgKeyIdx * sizeFmt + orgFmtIdx]
        |= FORMAT_ZERO_8_ORDER[zeroIdx2Pos(PUZZLE_SIZE - 1, orgFmt)];
    patterns[order][orgKeyIdx * sizeFmt + orgFmtIdx] = 1;
    int pending = sizeKey * sizeFmt - 1;
    int remaining = pending;
    int step = 1;
    int counter = 0;

    while (pending > 0) {
      // a 8 bits byte represent 8 order of zero spaces of format combo
      byte[] nextMove = new byte[sizeKey * sizeFmt];
      for (int k = 0; k < sizeKey; k++) {
        for (int f = 0; f < sizeFmt; f++) {
          int fmt = formats2combo[f];
          if (currMove[k * sizeFmt + f] == 0) {
            continue;
          }

          counter++;
          short freeMove = freeMoveByte(currMove[k * sizeFmt + f], fmt);

          for (int zeorPos = 0; zeorPos < PUZZLE_SIZE; zeorPos++) {
            if ((fmt & FORMAT_BIT_16[zeorPos]) > 0) {
              continue;
            }

            if ((freeMove & FORMAT_BIT_16[zeorPos]) > 0) {
              ArrayList<Integer> neighbors = new ArrayList<Integer>();

              if (zeorPos - ROW_SIZE >= 0 && (fmt & FORMAT_BIT_16[zeorPos - ROW_SIZE]) > 0) {
                neighbors.add(zeorPos - ROW_SIZE);
                neighbors.add(Board.Move.UP.getValue());
              }

              if (zeorPos % ROW_SIZE > 0 && (fmt & FORMAT_BIT_16[zeorPos - 1]) > 0) {
                neighbors.add(zeorPos - 1);
                neighbors.add(Board.Move.LEFT.getValue());
              }

              if (zeorPos % ROW_SIZE < ROW_SIZE - 1
                  && (fmt & FORMAT_BIT_16[zeorPos + 1]) > 0) {
                neighbors.add(zeorPos + 1);
                neighbors.add(Board.Move.RIGHT.getValue());
              }

              if (zeorPos + ROW_SIZE < PUZZLE_SIZE
                  && (fmt & FORMAT_BIT_16[zeorPos + ROW_SIZE]) > 0) {
                neighbors.add(zeorPos + ROW_SIZE);
                neighbors.add(Board.Move.DOWN.getValue());
              }

              if (neighbors.isEmpty()) {
                continue;
              }

              int pos = 0;
              int tileOrder = 0;
              for (int i = 0; i < neighbors.size(); i += 2) {
                int tile = neighbors.get(i);
                int dirValue = neighbors.get(i + 1);
                while (pos < tile) {
                  if ((fmt & FORMAT_BIT_16[pos]) > 0) {
                    tileOrder++;
                  }
                  pos++;
                }

                if (tileOrder == group) {
                  break;
                }

                if (moveSet[f][tileOrder * NUM_DIR + dirValue] > 0) {
                  int nextFmt = moveSet[f][tileOrder * NUM_DIR + dirValue]
                      >> KEY_BIT_SIZE;
                  int nextFmtIdx = formats.get(nextFmt);
                  int rotKey = (moveSet[f][tileOrder * NUM_DIR + dirValue]
                      & KEY_BITS);

                  if (rotKey == 0) {
                    if (patterns[order][k * sizeFmt + nextFmtIdx] == 0) {
                      patterns[order][k * sizeFmt + nextFmtIdx] = (byte) step;
                      pending--;
                    }
                    nextMove[k * sizeFmt + nextFmtIdx]
                        |= FORMAT_ZERO_8_ORDER[zeroIdx2Pos(tile, nextFmt)];
                  } else {
                    int nextKey = shiftSet[k * group * sizeShift
                                + tileOrder * sizeShift
                                + rotKey - 1];
                    if (patterns[order][nextKey * sizeFmt + nextFmtIdx] == 0) {
                      patterns[order][nextKey * sizeFmt + nextFmtIdx]
                          = (byte) step;
                      pending--;
                    }
                    nextMove[nextKey * sizeFmt + nextFmtIdx]
                        |= FORMAT_ZERO_8_ORDER[zeroIdx2Pos(tile, nextFmt)];
                  }
                }
              }
            }
          }
        }
      }

      System.out.printf("moves : " + step + "\t count : %-15s scanned : %-15s ended at "
          + stopwatch.currentTime() + "s\n", Integer.toString(remaining - pending),
          Integer.toString(counter));
      step++;
      currMove = nextMove;
      remaining = pending;
      counter = 0;
    }
    System.out.println();
    patterns[order][orgKeyIdx * sizeFmt + orgFmtIdx] = 0;
  }

  /**
   * Generate the additive pattern of 2 to 7 tiles, use 16 bits short (1 zero space
   * plus 8 to 13 tile spaces) to record each move during the expansion.
   *
   * @param order the pattern order number
   * @param group the pattern group size
   * @param orgFmt the initial format combo
   * @param element the PatternElement associate with the pattern
   */
  private void genPatternShort(final int order, final int group, final int orgFmt,
      final PatternElement element) {
    int sizeKey = PatternConstants.getKeySize(group);
    int sizeFmt = PatternConstants.getFormatSize(group);
    int sizeShift = PatternConstants.getMaxShiftX2(group);

    patterns[order] = new byte[sizeKey * sizeFmt];
    HashMap<Integer, Integer> formats = element.getFormats();
    int[] formats2combo = element.getFormatCombo(group);
    int[][] moveSet = element.getLinkFormatComboSet(group);
    int[] shiftSet = element.getKeyShiftSet(group);

    System.out.print("Screen additive pattern " + (order + 1) + " : (");
    for (int i = 0; i < PUZZLE_SIZE - 1; i++) {
      if ((orgFmt & FORMAT_BIT_16[i]) == 0) {
        System.out.print("x ");
      } else {
        System.out.print((i + 1) + " ");
      }
    }
    int orgKeyIdx = 0;
    int orgFmtIdx = formats.get(orgFmt);
    Stopwatch stopwatch = new Stopwatch();
    System.out.println("0) at " + stopwatch.currentTime() + "s");

    // a 16 bits short represent 16 position of the board for zero space
    short[] currMove = new short[sizeKey * sizeFmt];
    currMove[orgKeyIdx * sizeFmt + orgFmtIdx]
        = freeMoveShort((short) (PUZZLE_SIZE - 1), orgFmt);
    patterns[order][orgKeyIdx * sizeFmt + orgFmtIdx] = 1;
    int pending = sizeKey * sizeFmt - 1;
    int remaining = pending;
    int step = 1;
    int counter = 0;

    while (pending > 0) {
      short[] nextMove = new short[sizeKey * sizeFmt];
      for (int k = 0; k < sizeKey; k++) {
        for (int f = 0; f < sizeFmt; f++) {
          int fmt = formats2combo[f];
          if (currMove[k * sizeFmt + f] == 0) {
            continue;
          }

          counter++;
          short freeMove = freeMoveShort(currMove[k * sizeFmt + f], fmt);

          for (int zeorPos = 0; zeorPos < PUZZLE_SIZE; zeorPos++) {
            if ((fmt & FORMAT_BIT_16[zeorPos]) > 0) {
              continue;
            }

            if ((freeMove & FORMAT_BIT_16[zeorPos]) > 0) {
              ArrayList<Integer> neighbors = new ArrayList<Integer>();

              if (zeorPos - ROW_SIZE >= 0 && (fmt & FORMAT_BIT_16[zeorPos - ROW_SIZE]) > 0) {
                neighbors.add(zeorPos - ROW_SIZE);
                neighbors.add(Board.Move.UP.getValue());
              }

              if (zeorPos % ROW_SIZE > 0 && (fmt & FORMAT_BIT_16[zeorPos - 1]) > 0) {
                neighbors.add(zeorPos - 1);
                neighbors.add(Board.Move.LEFT.getValue());
              }

              if (zeorPos % ROW_SIZE < ROW_SIZE - 1
                  && (fmt & FORMAT_BIT_16[zeorPos + 1]) > 0) {
                neighbors.add(zeorPos + 1);
                neighbors.add(Board.Move.RIGHT.getValue());
              }

              if (zeorPos + ROW_SIZE < PUZZLE_SIZE
                  && (fmt & FORMAT_BIT_16[zeorPos + ROW_SIZE]) > 0) {
                neighbors.add(zeorPos + ROW_SIZE);
                neighbors.add(Board.Move.DOWN.getValue());
              }

              if (neighbors.isEmpty()) {
                continue;
              }

              int pos = 0;
              int tileOrder = 0;
              for (int i = 0; i < neighbors.size(); i += 2) {
                int tile = neighbors.get(i);
                int dirValue = neighbors.get(i + 1);
                while (pos < tile) {
                  if ((fmt & FORMAT_BIT_16[pos]) > 0) {
                    tileOrder++;
                  }
                  pos++;
                }

                if (tileOrder == group) {
                  break;
                }

                if (moveSet[f][tileOrder * NUM_DIR + dirValue] > 0) {
                  int nextFmt = moveSet[f][tileOrder * NUM_DIR + dirValue]
                      >> KEY_BIT_SIZE;
                  int nextFmtIdx = formats.get(nextFmt);
                  int rotKey = (moveSet[f][tileOrder * NUM_DIR + dirValue]
                      & KEY_BITS);

                  if (rotKey == 0) {
                    if (patterns[order][k * sizeFmt + nextFmtIdx] == 0) {
                      patterns[order][k * sizeFmt + nextFmtIdx] = (byte) step;
                      pending--;
                    }
                    nextMove[k * sizeFmt + nextFmtIdx] |= FORMAT_BIT_16[tile];
                  } else {
                    int nextKey = shiftSet[k * group * sizeShift
                                + tileOrder * sizeShift
                                + rotKey - 1];
                    if (patterns[order][nextKey * sizeFmt + nextFmtIdx] == 0) {
                      patterns[order][nextKey * sizeFmt + nextFmtIdx]
                          = (byte) step;
                      pending--;
                    }
                    nextMove[nextKey * sizeFmt + nextFmtIdx]
                        |= FORMAT_BIT_16[tile];
                  }
                }
              }
            }
          }
        }
      }

      System.out.printf("moves : " + step + "\t count : %-15s scanned : %-15s ended at "
          + stopwatch.currentTime() + "s\n", Integer.toString(remaining - pending),
          Integer.toString(counter));
      step++;
      currMove = nextMove;
      remaining = pending;
      counter = 0;
    }
    System.out.println();
    patterns[order][orgKeyIdx * sizeFmt + orgFmtIdx] = 0;
  }

  /**
   * Returns the byte array of pattern group size.
   *
   * @return byte array of pattern group size
   */
  public byte[] getPatternGroups() {
    return patternGroups;
  }

  /**
   * Returns the byte array of conversion set of tile values to detached element keys.
   *
   * @return byte array of conversion set of tile values to detached element keys
   */
  public byte[] getVal2ptnKey() {
    return val2ptnKey;
  }

  /**
   * Returns the byte array of conversion set of tile values to pattern order.
   *
   * @return byte array of conversion set of tile values to pattern order
   */
  public byte[] getVal2ptnOrder() {
    return val2ptnOrder;
  }

  /**
   * Returns the 2 dimensional byte array of full pattern value.
   *
   * @return 2 dimensional byte array of full pattern value
   */
  public byte[][] getPatternSet() {
    return patterns;
  }

  /**
   * Unit Test.
   *
   * @param args standard argument main function
   */
  public static void main(final String[] args) {
    PatternDatabase pdb = new PatternDatabase(PatternOptions.Pattern_663);
    PatternOptions type = PatternOptions.Pattern_78;
    int choice = 1;
    pdb.createPattern(type.getPattern(choice), type.getElements());
    pdb.saveData(FileProperties.getFilepathPdb(type, choice));
  }
}

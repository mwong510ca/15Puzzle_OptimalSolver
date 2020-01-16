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
import java.util.Map.Entry;

import mwong.myprojects.fifteenpuzzle.FileProperties;
import mwong.myprojects.fifteenpuzzle.solution.Solver.ApplicationMode;

/**
 * WalkingDistane provides a set of link and a set of heuristic values of Walking Distance.
 * It either load from storage or generate a new set if local file not exists.
 *
 * <p>Dependencies : FileProperties.java, PuzzleConstants.java
 *
 * @author <a href="http://www.linkedin.com/pub/macy-wong/46/550/37b/"
 *            target="_blank">Meisze Wong (linkedin)</a>
 * @see <a href="http://www.github.com/mwong510ca/15PuzzleOptimalSolver/"
 *         target="_blank">GitHub (full project)</a>
 */
public final class WalkingDistance {
  /** Puzzle size.
   *  @see PuzzleConstants#SIZE */
  private static final int ROW_SIZE = PuzzleConstants.getRowSize();
  /** Key size 55, 35 keys for full row and 20 key for zero row. */
  private static final int KEY_SIZE = 55;
  /** Pattern size 24964 total. */
  private static final int PATTERN_SIZE = 24964;
  /** Use 4 bits for zero row index bit shift.  Actual only use 2 bits (0 - 3). */
  private static final int ZERO_ROW_BIT_SHIFT = 4;
  /** Zero row bits in binary 0011 =&gt; hex 0x03. */
  private static final int ZERO_ROW_BIT = 0x03;
  /** Key bit size is 3, key value from 0 to 4, (binary) 0000 to 0100. */
  private static final int KEY_BIT_SIZE = 3;
  /** Key bits in binary 0111 -&gt; 0x07. */
  private static final int KEY_BITS = 0x07;
  /** Key index bit size is 6 for maximum value 55. */
  private static final int KEY_IDX_BIT_SIZE = 6;
  /** Key index bits in binary 00111111 =&gt; hex 0x3F. */
  private static final int KEY_IDX_BITS = 0x3F;
  /** Integer array of partial row keys. 3 bits per key.
   *  Last three keys 000111111111, first and last two keys 111000111111,
   *  first two and last keys 111111000111, first three keys 111111111000 */
  private static final int[] PARTIAL_KEY;
  /** Integer array of partial pattern, 6 bits per row.
   *  Last 2 keys 111111111111, First and last key 111111000000000000111111,
   *  First two keys 11111111111100000000000 */
  private static final int[] PARTIAL_PATTERN;

  /** Row keys to index map. (4 * 3 bits) */
  private HashMap<Integer, Integer> rowIdxMap;
  /** Pattern to index map. (4 * 6 bits + 4 bits of zero row index) */
  private HashMap<Integer, Integer> ptnIdxMap;
  /** The byte array of pattern values. */
  private byte[] pattern;
  /** The integer array of pattern changes links. (PATTERN_SIZE * ROW_SIZE * 2 directions) */
  private int[] ptnLink;

  static {
    int[] temp = new int[ROW_SIZE];
    for (int i = 0; i < ROW_SIZE; i++) {
      int val = 0;
      for (int j = 0; j < ROW_SIZE; j++) {
        val = val << KEY_BIT_SIZE;
        if (i != j) {
          val = val | KEY_BITS;
        }
      }
      temp[i] = val;
    }
    PARTIAL_KEY = temp;

    temp = new int[ROW_SIZE - 1];
    for (int i = 0; i < ROW_SIZE - 1; i++) {
      int val = 0;
      for (int j = 0; j < ROW_SIZE - 1; j++) {
        val = val << KEY_IDX_BIT_SIZE;
        if (i != j) {
          val = val | KEY_IDX_BITS;
        } else {
          val = val << KEY_IDX_BIT_SIZE;
        }
      }
      temp[i] = val;
    }
    PARTIAL_PATTERN = temp;
  }

  /**
   * Initializes the WalkingDistance object.
   */
  private WalkingDistance() {
    this(ApplicationMode.CONSOLE);
  }

  /**
   * Initializes the WalkingDistance object with application mode.
   *
   * @param appMode the given applicationMode for GUI or CONSOLE
   */
  public WalkingDistance(final ApplicationMode appMode) {
    loadData(appMode);
  }

  /**
   * Load the walking distance from data file.
   *
   * @param appMode the given application mode
   */
  private void loadData(final ApplicationMode appMode) {
    rowIdxMap = new HashMap<Integer, Integer>();
    ptnIdxMap = new HashMap<Integer, Integer>();
    pattern = new byte[PATTERN_SIZE];
    ptnLink = new int[PATTERN_SIZE * ROW_SIZE * 2];

    String filepath = FileProperties.getFilepathWd();
    try (FileInputStream fin = new FileInputStream(filepath);
        FileChannel inChannel = fin.getChannel();) {
      ByteBuffer buf = inChannel.map(FileChannel.MapMode.READ_ONLY, 0, inChannel.size());
      buf.get(pattern);

      for (int i = 0; i < KEY_SIZE; i++) {
        rowIdxMap.put(buf.getInt(), buf.getInt());
      }

      for (int i = 0; i < PATTERN_SIZE; i++) {
        ptnIdxMap.put(buf.getInt(), buf.getInt());
      }

      for (int i = 0; i < ptnLink.length; i++) {
        ptnLink[i] = buf.getInt();
      }
    } catch (BufferUnderflowException | IOException ex) {
      if (appMode == ApplicationMode.GUI) {
        System.err.println("\n\t*** Data files missing, please download from cloud drive. ***\n");
        System.err.println("\n\thttps://my.pcloud.com/publink/show?"
            + "code=kZSoaLZgNeLhO2eu0RQcu9D2aXeOFgtioUV\n");
        throw new UnsupportedOperationException();
      }

      int[] keyLink = genKeys();
      genPattern(keyLink);
      saveData(filepath);
    }
  }

  /**
   * Save the walking distance in file.
   *
   * @param filepath the given file path
   */
  private void saveData(final String filepath) {
    String directory = FileProperties.getDirectory();
    if (!(new File(directory)).exists()) {
      (new File(directory)).mkdir();
    }
    if ((new File(filepath)).exists()) {
      (new File(filepath)).delete();
    }

    try (FileOutputStream fout = new FileOutputStream(filepath);
        FileChannel outChannel = fout.getChannel();) {
      ByteBuffer buffer;
      buffer = ByteBuffer.allocateDirect(PATTERN_SIZE);
      buffer.put(pattern);
      buffer.flip();
      outChannel.write(buffer);

      final int integerByteSize = 4;
      buffer = ByteBuffer.allocateDirect(KEY_SIZE * integerByteSize * 2);
      for (Entry<Integer, Integer> entry : rowIdxMap.entrySet()) {
        buffer.putInt(entry.getKey());
        buffer.putInt(entry.getValue());
      }
      buffer.flip();
      outChannel.write(buffer);

      buffer = ByteBuffer.allocateDirect(PATTERN_SIZE * integerByteSize * 2);
      for (Entry<Integer, Integer> entry : ptnIdxMap.entrySet()) {
        buffer.putInt(entry.getKey());
        buffer.putInt(entry.getValue());
      }
      buffer.flip();
      outChannel.write(buffer);

      buffer = ByteBuffer.allocateDirect(ptnLink.length * integerByteSize);
      for (int i = 0; i < ptnLink.length; i++) {
        buffer.putInt(ptnLink[i]);
      }
      buffer.flip();
      outChannel.write(buffer);
    } catch (BufferUnderflowException | IOException ex2) {
      if ((new File(filepath)).exists()) {
        (new File(filepath)).delete();
      }
    }
  }

  /**
   * Generate all keys for the walking distance.
   *
   * @return the byte array of key links
   */
  private int[] genKeys() {
    HashSet<Integer> set = new HashSet<Integer>();
    HashSet<int[]> next = new HashSet<int[]>();
    rowIdxMap = new HashMap<Integer, Integer>();
    int[] rowKeys2combo = new int[KEY_SIZE];

    // 1st set starts with 0004, 0040, 0400, 4000
    int counter = 0;
    int key;
    for (int i = 0; i < ROW_SIZE; i++) {
      int[] temp = new int[ROW_SIZE];
      temp[i] = ROW_SIZE;
      key = rowCombo2Key(temp);
      rowKeys2combo[counter] = key;
      rowIdxMap.put(key, counter++);
      set.add(key);
      next.add(temp);
    }

    while (next.size() > 0) {
      HashSet<int[]> expand = next;
      next = new HashSet<int[]>();
      for (int[] combo : expand) {
        for (int i = 0; i < ROW_SIZE; i++) {
          if (combo[i] > 0) {
            for (int j = 0; j < ROW_SIZE; j++) {
              if (i != j) {
                int[] shift = new int[ROW_SIZE];
                System.arraycopy(combo, 0, shift, 0, ROW_SIZE);
                shift[i] = combo[i] - 1;
                shift[j] = combo[j] + 1;
                key = rowCombo2Key(shift);
                if (!set.contains(key)) {
                  rowKeys2combo[counter] = key;
                  rowIdxMap.put(key, counter++);
                  set.add(key);
                  next.add(shift);
                }
              }
            }
          }
        }
      }
    }

    final int splitIdx = counter;

    // 2nd set starts with 0003, 0030, 0300, 3000
    for (int i = 0; i < ROW_SIZE; i++) {
      int[] temp = new int[ROW_SIZE];
      temp[i] = ROW_SIZE - 1;
      key = rowCombo2Key(temp);
      rowKeys2combo[counter] = key;
      rowIdxMap.put(key, counter++);
      set.add(key);
      next.add(temp);
    }

    while (next.size() > 0) {
      HashSet<int[]> expand = next;
      next = new HashSet<int[]>();
      for (int[] combo : expand) {
        for (int i = 0; i < ROW_SIZE; i++) {
          if (combo[i] > 0) {
            for (int j = 0; j < ROW_SIZE; j++) {
              if (i != j) {
                int[] shift = new int[ROW_SIZE];
                System.arraycopy(combo, 0, shift, 0, ROW_SIZE);
                shift[i] = combo[i] - 1;
                shift[j] = combo[j] + 1;
                key = rowCombo2Key(shift);
                if (!set.contains(key)) {
                  rowKeys2combo[counter] = key;
                  rowIdxMap.put(key, counter++);
                  set.add(key);
                  next.add(shift);
                }
              }
            }
          }
        }
      }
    }
    return genKeyLink(splitIdx, rowKeys2combo);
  }

  /**
   * Generate all key link for the walking distance.
   *
   * @param splitIdx the row index of the first zero row
   * @param rowKeys2combo the integer array of row keys combo
   * @return integer array of row keys links
   */
  private int[] genKeyLink(final int splitIdx, final int[] rowKeys2combo) {
    int[] rowKeyLink = new int[KEY_SIZE * ROW_SIZE];

    // shift out from column, from 4 tiles to 3 tiles
    for (int i = 0; i < splitIdx; i++) {
      int combo = rowKeys2combo[i];
      for (int j = 0; j < ROW_SIZE; j++) {
        int shiftBits = (ROW_SIZE - j - 1) * KEY_BIT_SIZE;
        int self = ((combo >> shiftBits) & KEY_BITS);
        if (self > 0) {
          self = (self - 1) << shiftBits;
          int nextKey = (combo & PARTIAL_KEY[j]) | self;
          rowKeyLink[i * ROW_SIZE + j] = rowIdxMap.get(nextKey);
        } else {
          // invalid link, empty column
          rowKeyLink[i * ROW_SIZE + j] = -1;
        }
      }
    }

    // shift in to column, from 3 tiles 4 tiles
    for (int i = splitIdx; i < KEY_SIZE; i++) {
      for (int j = 0; j < ROW_SIZE; j++) {
        int combo = rowKeys2combo[i];
        int shiftBits = (ROW_SIZE - j - 1) * KEY_BIT_SIZE;
        int nextKey = (combo & PARTIAL_KEY[j]) | ((((combo >> shiftBits) & KEY_BITS) + 1)
            << shiftBits);
        rowKeyLink[i * ROW_SIZE + j] = rowIdxMap.get(nextKey);
      }
    }
    return rowKeyLink;
  }

  /**
   * Generate all patterns for the walking distance.
   *
   * @param rowKeyLink the integer array of row keys links
   */
  private void genPattern(final int[] rowKeyLink) {
    ptnIdxMap = new HashMap<Integer, Integer>();
    pattern = new byte[PATTERN_SIZE];
    ptnLink = new int[PATTERN_SIZE * ROW_SIZE * 2];

    /* starts with 4000  // 6 bits each
                   0400
                   0040
                   0003 | 3 (4 bits for zero row index)
       total 4 x 6 bits + 4 bits = 28 bits for combo */
    int initCombo = 0;
    for (int i = 0; i < ROW_SIZE - 1; i++) {
      int key = ROW_SIZE << ((ROW_SIZE - i - 1) * (ROW_SIZE - 1));
      initCombo = (initCombo << KEY_IDX_BIT_SIZE) | rowIdxMap.get(key);
    }
    initCombo = (initCombo << KEY_IDX_BIT_SIZE) | rowIdxMap.get(ROW_SIZE - 1);
    initCombo = (initCombo << ZERO_ROW_BIT_SHIFT) | (ROW_SIZE - 1);
    int ctPtn = 0;
    byte moves = 0;
    int[] ptnKeys2combo = new int[PATTERN_SIZE];

    ptnKeys2combo[ctPtn] = initCombo;
    ptnIdxMap.put(initCombo, ctPtn);
    pattern[ctPtn++] = moves;
    boolean loop = true;
    int top = 0;
    int top2 = 0;
    int end = 1;
    int end2 = 1;

    while (loop) {
      moves++;
      top = top2;
      end = end2;
      top2 = end2;
      loop = false;

      for (int i = top; i < end; i++) {
        int currPtn = ptnKeys2combo[i];
        int ptnCombo = currPtn >> ZERO_ROW_BIT_SHIFT;
        int zeroRow = currPtn & ZERO_ROW_BIT;
        int zeroIdx = getRowKeyIdx(ptnCombo, zeroRow);

        for (Arrow arrow : Arrow.values()) {
          int linkBase = i * ROW_SIZE * 2;
          if ((arrow == Arrow.FORWARD && zeroRow == ROW_SIZE - 1)
              || (arrow == Arrow.BACKWARD && zeroRow == 0)) {
            linkBase += arrow.getVal();
            for (int j = 0; j < ROW_SIZE; j++) {
              ptnLink[linkBase] = -1;
              linkBase += 2;
            }
          } else {
            final int zeroNext = zeroRow + arrow.getZeroRowChange();
            linkBase += arrow.getVal();

            int nextIdx = getRowKeyIdx(ptnCombo, zeroNext);
            for (int j = 0; j < ROW_SIZE; j++) {
              if (rowKeyLink[nextIdx * ROW_SIZE + j] != -1) {

                int pairKeys = rowKeyLink[zeroIdx * ROW_SIZE + j];
                if (arrow == Arrow.FORWARD) {
                  pairKeys = (pairKeys << KEY_IDX_BIT_SIZE) | rowKeyLink[nextIdx * ROW_SIZE + j];
                } else {
                  pairKeys =  (rowKeyLink[nextIdx * ROW_SIZE + j] << KEY_IDX_BIT_SIZE) | pairKeys;
                }

                final int mergeCode = zeroRow - arrow.getVal();
                int newPtn = (pairKeys << ((2 - mergeCode) * KEY_IDX_BIT_SIZE))
                    | (ptnCombo & PARTIAL_PATTERN[mergeCode]);

                newPtn = (newPtn << ZERO_ROW_BIT_SHIFT) | (zeroNext);
                if (ptnIdxMap.containsKey(newPtn)) {
                  ptnLink[linkBase] = ptnIdxMap.get(newPtn);
                } else {
                  ptnKeys2combo[ctPtn] = newPtn;
                  ptnIdxMap.put(newPtn, ctPtn);
                  pattern[ctPtn] = moves;
                  ptnLink[linkBase] = ctPtn++;
                  loop = true;
                  end2++;
                }
              } else {
                ptnLink[linkBase] = -1;
              }
              linkBase += 2;
            }
          }
        }
      }
    }
  }

  /**
   * Returns HashMap of compress row Key to key index.
   *
   * @return HashMap of compress row Key to key index
   */
  public HashMap<Integer, Integer> getRowIdxMap() {
    return rowIdxMap;
  }

  /**
   * Returns HashMap of compress a set of row key to pattern index.
   *
   * @return HashMap of compress a set of row key to pattern index
   */
  public HashMap<Integer, Integer> getPtnIdxMap() {
    return ptnIdxMap;
  }

  /**
   * Returns byte array of walking distance pattern.
   *
   * @return byte array of walking distance pattern
   */
  public byte[] getPattern() {
    return pattern;
  }

  /**
   * Returns integer array of pattern move link set.
   *
   * @return integer array of pattern move link set
   */
  public int[] getPtnLink() {
    return ptnLink;
  }

  /**
   * Compress the rowKey set to pattern key.
   *
   * @param combo the given integer array of row keys.
   * @return integer value of
   */
  private int rowCombo2Key(final int[] combo) {
    int key = 0;
    for (int i : combo) {
      key = (key << KEY_BIT_SIZE) | i;
    }
    return key;
  }

  /**
   * Extract the row key index from the pattern rows combo of the given row.
   *
   * @param combo the integer value of pattern rows combo
   * @param rowIdx the given row index
   * @return integer value of row key index from the pattern rows combo
   */
  private int getRowKeyIdx(final int combo, final int rowIdx) {
    int key = (combo >> ((ROW_SIZE - rowIdx - 1) * KEY_IDX_BIT_SIZE)) & KEY_IDX_BITS;
    return key;
  }

  /**
   * Return the integer value of key bit size.
   *
   * @return integer value of key bit size
   */
  public static int getKeyBitSize() {
    return KEY_BIT_SIZE;
  }

  /**
   * Return the integer value of key index bit size.
   *
   * @return integer value of key index bit size
   */
  public static int getKeyIdxBitSize() {
    return KEY_IDX_BIT_SIZE;
  }

  /**
   * Return the integer value of bit shift size for zero row.
   *
   * @return integer value of bit shift size for zero row
   */
  public static int getZeroRowBitShift() {
    return ZERO_ROW_BIT_SHIFT;
  }

  /**
   * Arrow is the enum type of moving direction for Walking Distance.
   *
   * @author <a href="http://www.linkedin.com/pub/macy-wong/46/550/37b/"
   *            target="_blank">Meisze Wong (linkedin)</a>
   */
  public enum Arrow {
    /**
     * Forward.
     */
    FORWARD(0, 1),
    /**
     * Backward.
     */
    BACKWARD(1, -1);

    /** The integer value of Arrow. */
    private int val;
    /** The row change factor of Arrow. */
    private int zeroRowChange;

    /**
     * Initial the Arrow.
     *
     * @param val the value of arrow
     * @param zeroRowChange the zero row change factor
     */
    Arrow(final int val, final int zeroRowChange) {
      this.val = val;
      this.zeroRowChange = zeroRowChange;
    }

    /**
     * Returns the integer of value.
     * @return integer of value
     */
    public int getVal() {
      return val;
    }

    /**
     * Returns the integer represent the impact of zero row change.
     * @return integer represent the impact of zero row change
     */
    int getZeroRowChange() {
      return zeroRowChange;
    }
  }

  /**
   * Unit Test.
   * @param args standard argument main function
   */
  public static void main(final String[] args) {
    WalkingDistance wd = new WalkingDistance();
    int[] keyLink = wd.genKeys();
    wd.genPattern(keyLink);
    //wd.saveData(FileProperties.getFilepathWd());
  }
}

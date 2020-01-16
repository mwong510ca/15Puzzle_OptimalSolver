package mwong.myprojects.fifteenpuzzle.solution;

import java.util.HashMap;

import mwong.myprojects.fifteenpuzzle.puzzle.HeuristicOptions;
import mwong.myprojects.fifteenpuzzle.puzzle.PatternDatabase;
import mwong.myprojects.fifteenpuzzle.puzzle.PatternElement;
import mwong.myprojects.fifteenpuzzle.puzzle.WalkingDistance;
import mwong.myprojects.fifteenpuzzle.puzzle.WalkingDistance.Arrow;

/**
 *  SolverSetup extends SolverTemplate. It is the abstract class contains all variables
 *  and functions for Manhattan Distance, Walking Distance, and Patter Database.
 *
 *  <p>Dependencies : HeuristicOptions.java, PatternConstants.java, PatternDatabase.java,
 *                    PatternElement.java, Solver.java, SolverBuilder.java,
 *                    SolverTemplate.java
 *
 * @author <a href="http://www.linkedin.com/pub/macy-wong/46/550/37b/"
 *            target="_blank">Meisze Wong (linkedin)</a>
 * @see <a href="http://www.github.com/mwong510ca/15PuzzleOptimalSolver/"
 *         target="_blank">GitHub (full project)</a>
 */
abstract class SolverSetup extends SolverTemplate {
  /** The pattern database reverse direction offset. */
  static final int PDB_REVERSE_OFFSET = 2;
  /** The walking distance keys size is 4 for the puzzle solver. A pair of horizontal index
   *  and value, and a pair of vertical index and value. */
  static final int WD_KEY_SZIE = 4;
  /** The walking distance horizontal index key for the puzzle solver. */
  static final int WD_KEY_ORDER_H_IDX = 0;
  /** The walking distance vertical index key for the puzzle solver. */
  static final int WD_KEY_ORDER_V_IDX = 1;
  /** The walking distance horizontal value key for the puzzle solver. */
  static final int WD_KEY_ORDER_H_VAL = 2;
  /** The walking distance vertical value key for the puzzle solver. */
  static final int WD_KEY_ORDER_V_VAL = 3;

  /** The walking distance key bit size is 3, key value from 0 to 4.
   *  @see mwong.myprojects.fifteenpuzzle.puzzle.WalkingDistance#KEY_BIT_SIZE */
  private static final int WD_KEY_BIT_SIZE = SolverConstants.getWdKeyBitSize();
  /** The walking distance key index bit size is 6 for maximum value 55.
   *  @see mwong.myprojects.fifteenpuzzle.puzzle.WalkingDistance#KEY_IDX_BIT_SIZE */
  private static final int WD_KEY_IDX_BIT_SIZE = SolverConstants.getWdKeyIdxBitSize();
  /** The walking distance use 4 bits for zero row index bit shift.
   *  @see mwong.myprojects.fifteenpuzzle.puzzle.WalkingDistance#ZERO_ROW_BIT_SHIFT */
  private static final int WD_ZERO_ROW_BIT_SHIFT =
      SolverConstants.getWdZeroRowBitShift();
  /** The pattern database format move size is 16 tile times 4 directions.
   *  @see mwong.myprojects.fifteenpuzzle.puzzle.PatternConstants#FORMAT_MOVE_SIZE*/
  private static final int PDB_FORMAT_MOVE_SIZE = SolverConstants.getPdbFormatMoveSize();
  /** The pattern database key bit size is 4 for 0-15.
   *  @see mwong.myprojects.fifteenpuzzle.puzzle.PuzzleConstants#TILE_BIT_SIZE */
  private static final int PDB_KEY_BIT_SIZE = SolverConstants.getPdbKeyBitSize();
  /** The pattern database key bits in binary is 00001111 =&gt; 0x0F.
   *  @see mwong.myprojects.fifteenpuzzle.puzzle.PuzzleConstants#TILE_BITS */
  private static final int PDB_KEY_BITS = SolverConstants.getPdbKeyBits();
  /** Total 4 direction moves - left, right, up and down.
   *  @see mwong.myprojects.fifteenpuzzle.puzzle.PuzzleConstants#DIRECTION_SIZE */
  private static final int NUM_MOVES = SolverConstants.getDirectionSize();

  //Additive Walking Distance Components
  /** Walking Distance row keys to index map.
   *  @see mwong.myprojects.fifteenpuzzle.puzzle.WalkingDistance */
  private HashMap<Integer, Integer> wdRowIdxMap;
  /** Walking Distance pattern to index map.
   *  @see mwong.myprojects.fifteenpuzzle.puzzle.WalkingDistance */
  private HashMap<Integer, Integer> wdPtnIdxMap;
  /** The byte array of pattern values of Walking Distance.
   *  @see mwong.myprojects.fifteenpuzzle.puzzle.WalkingDistance */
  byte[] wdPattern;
  /** The integer array of pattern changes links of Walking Distance.
   *  @see mwong.myprojects.fifteenpuzzle.puzzle.WalkingDistance */
  private int[] wdPtnLink;
  /** The integer array of initial walking distance combo of the board object. */
  int[] initWdCombo;

  // Additive Pattern Database Components
  /** The byte array of each pattern group size. */
  private byte[] patternGroups;
  /** The integer array of each format size. */
  private int[] patternFormatSize;
  /** The double byte array of patterns.
   *  For each pattern group : key size x format size */
  private byte[][] patternSet;
  /** The byte array of tile value convert to pattern key. */
  private byte[] val2ptnKey;
  /** The byte array of tile value convert to pattern group order. */
  byte[] val2ptnOrder;

  // Detached Pattern Database Keys and Formats Components with links
  /** The HashMap of key bits to key index. */
  private HashMap<Integer, Integer> keysMap;
  /** The HashMap of format bits to format index. */
  private HashMap<Integer, Integer> formatsMap;
  /** The double integer array of format moves per group, use by the solver. */
  private int[][] linkFormatMove;
  /** The double array of key change by number of rotation. */
  private int[][] rotateKeysByPos;
  /** Preset the maximum key shift times two per group. */
  private int[] maxShiftX2;

  /** The number of pattern groups. */
  int groupSize;
  /** The number of pattern groups times two. */
  int groupSizeX2;
  /** The pattern combo size, 2 pairs of keys and values. (groupSize x 4) */
  int pdbComboSize;

  /** The integer array of initial pattern database combo of the board object. */
  int[] initPdbCombo;
  /** The integer array of pattern database combo for searching. */
  int[] pdbCombo;
  /** The integer array of pattern combo index lookup for mirror reflection. */
  int[] mirrorComboLookup;

  /**
   * Initialize the SolverTemplate object.
   *
   * @param option the mandatory HeuristicOption
   */
  SolverSetup(final HeuristicOptions option) {
    super(option);
  }

  /**
   * Load the walking distance components. Generate the data set if data file
   * is not available.
   *
   * @param appMode the mandatory ApplicationMode
   */
  final void loadWdComponents(final ApplicationMode appMode) {
    WalkingDistance wd = new WalkingDistance(appMode);
    wdRowIdxMap = wd.getRowIdxMap();
    wdPtnIdxMap = wd.getPtnIdxMap();
    wdPattern = wd.getPattern();
    wdPtnLink = wd.getPtnLink();
    initWdCombo = new int[WD_KEY_SZIE];
    wd = null;
  }

  /**
   * Load preset additive pattern database from a data file, if file not exists
   * generate a new set. Estimate takes 15s for 555 pattern, 2 minutes for 663 pattern,
   * 2.5 - 3 hours for 78 pattern also require minimum 2gigabytes memory -Xms2g.
   *
   * @param pdb the given PatternDatabase object
   * @param appMode the given ApplicationMode for loading the pattern element
   */
  final void loadPdbComponents(final PatternDatabase pdb, final ApplicationMode appMode) {
    patternGroups = pdb.getPatternGroups();
    setInUsePdbPtn(patternGroups);
    patternFormatSize = new int[patternGroups.length];
    boolean[] elementGroups = new boolean[SolverConstants.getPdbMaxGroupSize() + 1];
    for (int i = 0; i < patternGroups.length; i++) {
      patternFormatSize[i] = SolverConstants.getPdbFormatSize(patternGroups[i]);
      elementGroups[patternGroups[i]] = true;
    }
    patternSet = pdb.getPatternSet();
    val2ptnKey = pdb.getVal2ptnKey();
    val2ptnOrder = pdb.getVal2ptnOrder();
    groupSize = patternGroups.length;
    groupSizeX2 = groupSize * 2;
    pdbComboSize = groupSizeX2 * 2;
    //pdbComboHalfSize = groupSize * 2;

    PatternElement pde = new PatternElement(elementGroups,
        PatternElement.ElementRole.PUZZLE_SOLVER, appMode);
    keysMap = pde.getKeys();
    formatsMap = pde.getFormats();
    linkFormatMove = new int[groupSize][];
    rotateKeysByPos = new int[groupSize][];
    maxShiftX2 = new int[groupSize];
    for (int i = 0; i < groupSize; i++) {
      int group = patternGroups[i];
      linkFormatMove[i] = pde.getLinkFormatMoveSet(group);
      rotateKeysByPos[i] = pde.getKeyShiftSet(group);
      maxShiftX2[i] = SolverConstants.getPdbMaxShiftX2(group);
    }
    pde = null;

    pdbCombo = new int[pdbComboSize];
    mirrorComboLookup = new int[groupSize];
    for (int i = 0; i < groupSize; i++) {
      mirrorComboLookup[i] = i + groupSizeX2;
    }
  }

  /**
   * Load preset additive pattern database from the given solver object.
   *
   * @param inSolver the given solver object
   * @throws IllegalArgumentException if given solver is not using pattern database
   */
  final void loadPdbComponents(final Solver inSolver) {
    SolverSetup copySolver = (SolverSetup) inSolver;
    if (copySolver.patternSet == null) {
      throw new IllegalArgumentException("loadPdbComponents copySolver is not using pattern"
          + " database - " + inSolver.getHeuristic());
    }
    this.patternGroups = copySolver.patternGroups;
    setInUsePdbPtn(copySolver.getInUsePdbPtn());
    this.patternFormatSize = copySolver.patternFormatSize;
    this.patternSet = copySolver.patternSet;
    this.val2ptnKey = copySolver.val2ptnKey;
    this.val2ptnOrder = copySolver.val2ptnOrder;
    this.groupSize = copySolver.groupSize;
    this.groupSizeX2 = copySolver.groupSizeX2;
    this.pdbComboSize = copySolver.pdbComboSize;
    this.keysMap = copySolver.keysMap;
    this.formatsMap = copySolver.formatsMap;
    this.linkFormatMove = copySolver.linkFormatMove;
    this.rotateKeysByPos = copySolver.rotateKeysByPos;
    this.maxShiftX2 = copySolver.maxShiftX2;
    //this.regValLookup = copySolver.regValLookup;
    this.mirrorComboLookup = copySolver.mirrorComboLookup;
    //this.mirrorValLookup = copySolver.mirrorValLookup;
    pdbCombo = new int[pdbComboSize];
  }

  // --- Manhattan distance with linear conflict ---
 /**
   * Returns the integer value of Manhattan distance, with linear conflict option.
   *
   * @param flagLinearConflict the choice of linear conflict option
   * @return integer value of Manhattan distance
   */
  final int mdEstimate(final boolean flagLinearConflict) {
    int base = 0;
    int priority = 0;

    for (int row = 0; row < ROW_SIZE; row++) {
      final int baseRange = base + ROW_SIZE;
      for (int col = 0; col < ROW_SIZE; col++) {
        int value = tiles[base + col];
        if (value > 0) {
          priority += Math.abs((value - 1) % ROW_SIZE - col);
          priority += Math.abs((((value - 1)
              - (value - 1) % ROW_SIZE) / ROW_SIZE) - row);

          // linear conflict horizontal
          if (flagLinearConflict) {
            if (value > base && value <= baseRange) {
              for (int col2 = col + 1; col2 < ROW_SIZE; col2++) {
                int value2 = tiles[base + col2];
                if ((value2 > base) && (value2 < value)) {
                  priority += 2;
                  break;
                }
              }
            }
          }
        }

        // linear conflict vertical
        if (flagLinearConflict && tilesMirror[base + col] > 0) {
          value = tilesMirror[base + col];
          if (value > base && value <= baseRange) {
            for (int col2 = col + 1; col2 < ROW_SIZE; col2++) {
              int value2 = tilesMirror[base + col2];
              if ((value2 > base) && (value2 < value)) {
                priority += 2;
                break;
              }
            }
          }
        }
      }
      base += ROW_SIZE;
    }
    return priority;
  }

  /**
   * Returns the integer value of update horizontal linear conflict when the tile move vertically.
   *
   * @param zeroX the x-coordinate of zero space
   * @param zeroY the y-coordinate of zero space
   * @param rowId the row index of the tile to be shift to zero space
   * @param priority the current priority value
   * @param value the tile value to be shift to zero space
   * @param diff the index difference between zero and tile, can be position or negative
   * @param tilesSet the given copy tiles
   * @return integer value of update horizontal linear conflict
   */
  final int updateLinearConflict(final int zeroX, final int zeroY, final int rowId,
      final int priority, final byte value, final int diff, final byte[] tilesSet) {
    int priorityLc = priority;
    if (rowId == zeroY) {
      int base = rowId * ROW_SIZE;
      int baseRange = base + ROW_SIZE;
      for (int col = base; col < baseRange; col++) {
        int val = tilesSet[col];
        if (val > base && val <= baseRange) {
          for (int col2 = col + 1; col2 < baseRange; col2++) {
            int val2 = tilesSet[col2];
            if (val2 > base && val2 < val) {
              priorityLc -= 2;
              break;
            }
          }
        }
      }
      tilesSet[zeroY * ROW_SIZE + zeroX] = value;
      for (int col = base; col < baseRange; col++) {
        int val = tilesSet[col];
        if (val > base && val <= baseRange) {
          for (int col2 = col + 1; col2 < baseRange; col2++) {
            int val2 = tilesSet[col2];
            if (val2 > base && val2 < val) {
              priorityLc += 2;
              break;
            }
          }
        }
      }
      tilesSet[zeroY * ROW_SIZE + zeroX] = 0;
    } else if (rowId == zeroY + diff) {
      int base = rowId * ROW_SIZE;
      int baseRange = base + ROW_SIZE;
      for (int col = base; col < baseRange; col++) {
        int val = tilesSet[col];
        if (val > base && val <= baseRange) {
          for (int col2 = col + 1; col2 < baseRange; col2++) {
            int val2 = tilesSet[col2];
            if (val2 > base && val2 < val) {
              priorityLc -= 2;
              break;
            }
          }
        }
      }
      tilesSet[(zeroY + diff) * ROW_SIZE + zeroX] = 0;
      for (int col = base; col < baseRange; col++) {
        int val = tilesSet[col];
        if (val > base && val <= baseRange) {
          for (int col2 = col + 1; col2 < baseRange; col2++) {
            int val2 = tilesSet[col2];
            if (val2 > base && val2 < val) {
              priorityLc += 2;
              break;
            }
          }
        }
      }
      tilesSet[(zeroY + diff) * ROW_SIZE + zeroX] = value;
    }
    return priorityLc;
  }

  // --- Walking Distance ---
  /**
   * Convert the tiles into walking distance combo.
   */
  final void transWdCombo() {
    byte[] wdhKeys = new byte[PUZZLE_SIZE];
    byte[] wdvKeys = new byte[PUZZLE_SIZE];

    for (int i = 0; i < PUZZLE_SIZE; i++) {
      int value = tiles[i];
      if (value != 0) {
        int col = (value - 1) / ROW_SIZE;
        wdhKeys[(i / ROW_SIZE) * ROW_SIZE + col]++;

        col = value % ROW_SIZE - 1;
        if (col < 0) {
          col = ROW_SIZE - 1;
        }
        wdvKeys[(i % ROW_SIZE) * ROW_SIZE + col]++;
      }
    }

    initWdCombo[WD_KEY_ORDER_H_IDX] = getWdPtnIdx(wdhKeys, zeroY);
    initWdCombo[WD_KEY_ORDER_V_IDX] = getWdPtnIdx(wdvKeys, zeroX);
    initWdCombo[WD_KEY_ORDER_H_VAL] = wdPattern[initWdCombo[WD_KEY_ORDER_H_IDX]];
    initWdCombo[WD_KEY_ORDER_V_VAL] = wdPattern[initWdCombo[WD_KEY_ORDER_V_IDX]];
  }

  /**
   * Returns the integer value of walking distance pattern index from the given keys.
   *
   * @param wdKeys the given byte array of walking distance keys
   * @param zeroRow the given integer value of zero row index
   * @return integer value of walking distance pattern index
   */
  private int getWdPtnIdx(final byte[] wdKeys, final int zeroRow) {
    int key = 0;
    int count = 0;

    while (count < wdKeys.length) {
      int temp = 0;
      for (int i = 0; i < ROW_SIZE; i++) {
        temp = (temp << WD_KEY_BIT_SIZE) | wdKeys[count++];
      }
      key = (key << WD_KEY_IDX_BIT_SIZE) | wdRowIdxMap.get(temp);
      assert (wdRowIdxMap.get(temp) != -1) : " Invalid index : -1";
    }
    key = (key << WD_ZERO_ROW_BIT_SHIFT) | zeroRow;
    return wdPtnIdxMap.get(key);
  }

  /**
   * Return the key index after the move.  Take a key index, the column index of move
   * and Arrow direction.
   *
   * @param idx the given pattern index
   * @param col the column index of zero space
   * @param arrow the moving direction of walking distance
   * @return integer value of walking distance pattern index after the move
   */
  final int getWdPtnIdx(final int idx, final int col, final Arrow arrow) {
    if (arrow == Arrow.FORWARD) {
      return wdPtnLink[idx * ROW_SIZE * 2 + col * 2];
    } else {
      return wdPtnLink[idx * ROW_SIZE * 2 + col * 2 + 1];
    }
  }

  // --- pattern database ---
  /**
   * Convert the tiles into pattern database combo.
   */
  final void transPdbCombo() {
    int[] orgFmt = new int[groupSizeX2];
    int[] orgKey = new int[groupSizeX2];
    initPdbCombo = new int[pdbComboSize];

    for (int i = 0; i < PUZZLE_SIZE; i++) {
      for (int j = 0; j < groupSize; j++) {
        orgFmt[j] <<= 1;
        orgFmt[j + groupSize] <<= 1;
      }

      int value = tiles[i];
      if (value != 0) {
        int group = val2ptnOrder[value];
        orgFmt[group] |= 1;
        orgKey[group] = (orgKey[group] << PDB_KEY_BIT_SIZE) | val2ptnKey[value];
      }
      value = tilesMirror[i];
      if (value != 0) {
        int group = val2ptnOrder[value];
        orgFmt[group + groupSize] |= 1;
        orgKey[group + groupSize] = (orgKey[group + groupSize] << PDB_KEY_BIT_SIZE)
            | val2ptnKey[value];
      }
    }

    for (int i = 0; i < groupSize; i++) {
      initPdbCombo[i] = (keysMap.get(orgKey[i])) * patternFormatSize[i] + formatsMap.get(orgFmt[i]);
      initPdbCombo[i + groupSize] = getPdbValue(i, initPdbCombo[i]);
      initPdbCombo[mirrorComboLookup[i]] = (keysMap.get(orgKey[i + groupSize]))
          * patternFormatSize[i] + formatsMap.get(orgFmt[i + groupSize]);
      initPdbCombo[mirrorComboLookup[i] + groupSize] = getPdbValue(i,
          initPdbCombo[mirrorComboLookup[i]]);
    }

    // duplicate copy
    System.arraycopy(initPdbCombo, 0, pdbCombo, 0, pdbComboSize);
  }

  /**
   * Return the additive pattern database value with the given pattern order and
   * the pattern index of compressed key and compressed format.
   *
   * @param ptnOrder the given pattern order
   * @param ptnIdx the given pattern index
   * @return integer value of pattern database
   */
  private int getPdbValue(final int ptnOrder, final int ptnIdx) {
    return patternSet[ptnOrder][ptnIdx];
  }

  /**
   * Update the pattern database combo from the given information and changes.
   *
   * @param colShiftZeroPos the zero position of column shift
   * @param colShiftPtnOrder the pattern order of column shift
   * @param colShiftComboIdx the pattern combo index of column shift
   * @param rowShiftZeroPos the zero position of row shift
   * @param rowShiftPtnOrder the pattern order of row shift
   * @param rowShiftComboIdx the pattern combo index of row shift
   * @param offset the direction offset value
   */
  final void pdbShift(final int colShiftZeroPos, final int colShiftPtnOrder,
      final int colShiftComboIdx, final int rowShiftZeroPos, final int rowShiftPtnOrder,
      final int rowShiftComboIdx, final int offset) {
    // LEFT or RIGHT
    int oldFmt = pdbCombo[colShiftComboIdx] % patternFormatSize[colShiftPtnOrder];
    int move = linkFormatMove[colShiftPtnOrder][oldFmt * PDB_FORMAT_MOVE_SIZE
                                           + colShiftZeroPos * NUM_MOVES + offset];
    pdbCombo[colShiftComboIdx] += (move >> PDB_KEY_BIT_SIZE * 2) - oldFmt;

    // UP or DOWN
    oldFmt = pdbCombo[rowShiftComboIdx] % patternFormatSize[rowShiftPtnOrder];
    move = linkFormatMove[rowShiftPtnOrder][oldFmt * PDB_FORMAT_MOVE_SIZE
                                       + rowShiftZeroPos * NUM_MOVES + 1 + offset];
    int shift = move & PDB_KEY_BITS;
    if (shift > 0) {
      pdbCombo[rowShiftComboIdx] = getPdbKeyPtnShift(rowShiftPtnOrder,
          pdbCombo[rowShiftComboIdx] / patternFormatSize[rowShiftPtnOrder],
          (move >> PDB_KEY_BIT_SIZE) & PDB_KEY_BITS, shift - 1)
              * patternFormatSize[rowShiftPtnOrder] + (move >> PDB_KEY_BIT_SIZE * 2);
    } else {
      pdbCombo[rowShiftComboIdx] += (move >> PDB_KEY_BIT_SIZE * 2) - oldFmt;
    }
  }

  /**
   * Returns key index after the space tile shift up or down which impact the key order has changed.
   *
   * @param ptnOrder the given pattern order
   * @param key the given key index
   * @param keyOrder the given key order
   * @param shift the number of key shift
   * @return integer value of key index after the space tile shift up or down
   */
  private int getPdbKeyPtnShift(final int ptnOrder, final int key, final int keyOrder,
      final int shift) {
    int group = patternGroups[ptnOrder];
    return rotateKeysByPos[ptnOrder][(key * group + keyOrder) * maxShiftX2[ptnOrder] + shift];
  }

  /**
   * Update and returns the additive pattern database value with the given information.
   *
   * @param oldValue the current pattern value
   * @param ptnOrder the pattern group order
   * @param ptnComboIdx the pattern combo index
   * @return integer value of the updated pattern value
   */
  final int updatePdbValue(final int oldValue, final int ptnOrder, final int ptnComboIdx) {
    int valComboIdx = ptnComboIdx + groupSize;
    int value = oldValue - pdbCombo[valComboIdx];
    pdbCombo[valComboIdx] = patternSet[ptnOrder][pdbCombo[ptnComboIdx]];
    return value + pdbCombo[valComboIdx];
  }

  /**
   * Return the boolean value represents the puzzle is symmetry with mirror reflection, use
   * pattern database combo instead of tiles set.
   *
   * @return boolean value represents the puzzle is symmetry with mirror reflection
   */
  final boolean isNotSymmetryPdb() {
    int idx2 = groupSizeX2;
    for (int i = 0; i < groupSize; i++) {
      if (pdbCombo[i] != pdbCombo[idx2++]) {
        return true;
      }
    }
    return false;
  }

  /**
   * Restore the pattern database combo from the given copy.
   *
   * @param regPtnOrder the regular pattern order to be restored
   * @param mirrorPtnOrder the mirror pattern order to be restored
   * @param orgCopy the given integer array of original combo
   */
  final void rollbackPdbCombo(final int regPtnOrder, final int mirrorPtnOrder,
      final int[] orgCopy) {
    int comboIdx = regPtnOrder;
    pdbCombo[comboIdx] = orgCopy[comboIdx];
    comboIdx += groupSize;
    pdbCombo[comboIdx] = orgCopy[comboIdx];
    comboIdx = mirrorComboLookup[mirrorPtnOrder];
    pdbCombo[comboIdx] = orgCopy[comboIdx];
    comboIdx += groupSize;
    pdbCombo[comboIdx] = orgCopy[comboIdx];
  }
}


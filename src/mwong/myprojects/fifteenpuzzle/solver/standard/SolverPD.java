package mwong.myprojects.fifteenpuzzle.solver.standard;

import mwong.myprojects.fifteenpuzzle.solver.AbstractSolver;
import mwong.myprojects.fifteenpuzzle.solver.HeuristicOptions;
import mwong.myprojects.fifteenpuzzle.solver.SolverProperties;
import mwong.myprojects.fifteenpuzzle.solver.components.Board;
import mwong.myprojects.fifteenpuzzle.solver.components.Direction;
import mwong.myprojects.fifteenpuzzle.solver.components.PatternConstants;
import mwong.myprojects.fifteenpuzzle.solver.components.PatternDatabase;
import mwong.myprojects.fifteenpuzzle.solver.components.PatternElement;
import mwong.myprojects.fifteenpuzzle.solver.components.PatternElementMode;
import mwong.myprojects.fifteenpuzzle.solver.components.PatternOptions;

import java.util.HashMap;

/**
 * SolverPD extends AbstractSolver.  It is the 15 puzzle optimal solver.
 * It takes a Board object of the puzzle and solve it with IDA* using Additive
 * Pattern Database.  It may use predefined pattern from PatternOptions or
 * a set of user defined custom pattern.
 *
 * <p>Dependencies : AbstractSolver.java, Board.java, Direction.java, HeuristicOptions.java,
 *                   PatternDatabase.java, PatternElement.java, PatternElementMode.java,
 *                   PatternOptions.java, PatternPreoperties.java
 *
 * @author   Meisze Wong
 *           www.linkedin.com/pub/macy-wong/46/550/37b/
 */
public class SolverPD extends AbstractSolver {
    private final int offsetDir = 2;
    private final PatternElementMode mode = PatternElementMode.PUZZLE_SOLVER;

    // Additive Pattern Database Components
    private byte[] patternGroups;
    private int[] patternFormatSize;
    // # of pattern | szKeys | szFormats
    private byte[][] patternSet;
    private byte[] val2ptnKey;
    private byte[] val2ptnOrder;

    // Detached Pattern Database Keys and Formats Components with links
    private HashMap<Integer, Integer> keys;
    private HashMap<Integer, Integer> formats;
    private int[][] linkFormatMove;
    private int[][] rotateKeysByPos;
    private int[] maxShiftX2;

    protected PatternOptions inUsePattern;
    private byte[] inUsePtnArray;

    protected int[] pdKeys;
    protected int szGroup;
    protected int szPdKeys;
    protected int offsetPdSym;
    protected byte pdValReg = 0;
    protected byte pdValSym = 0;
    protected int idaCount;

    /**
     *  Initializes SolverPD object using default preset pattern.
     */
    public SolverPD() {
        this(SolverProperties.getDefaultPattern());
    }

    /**
     *  Initializes SolverPD object using given preset pattern.
     *
     *  @param presetPattern the given preset pattern type
     */
    public SolverPD(PatternOptions presetPattern) {
        this(presetPattern, 0);
    }

    /**
     *  Initializes SolverPD object with choice of given preset pattern.
     *
     *  @param presetPattern the given preset pattern type
     *  @param choice the number of preset pattern option
     */
    public SolverPD(PatternOptions presetPattern, int choice) {
        super();
        loadPDComponents(presetPattern, choice);
        loadPDElements(presetPattern.getElements());
        inUsePattern = presetPattern;
        inUsePtnArray = presetPattern.getPattern(choice);
        if (presetPattern == PatternOptions.Pattern_555) {
            inUseHeuristic = HeuristicOptions.PD555;
        } else if (presetPattern == PatternOptions.Pattern_663) {
            inUseHeuristic = HeuristicOptions.PD663;
        } else if (presetPattern == PatternOptions.Pattern_78) {
            inUseHeuristic = HeuristicOptions.PD78;
        } else {
            System.err.println("SolverPD init error");
        }
    }

    /**
     *  Initializes SolverPD object with user defined custom pattern.
     *
     *  @param customPattern byte array of user defined custom pattern
     *  @param elementGroups boolean array of groups reference to given pattern
     */
    public SolverPD(byte[] customPattern, boolean[] elementGroups) {
        customPDComponents(customPattern);
        loadPDElements(elementGroups);
        inUsePattern = PatternOptions.Pattern_Custom;
        inUsePtnArray = customPattern;
        inUseHeuristic = HeuristicOptions.PDCustom;
    }

    // load preset additive pattern database from a data file, if file not exists
    // generate a new set.  Estimate takes 15s for 555 pattern, 2 minutes for 663 pattern,
    // 2.5 - 3 hours for 78 pattern also require minimum 2gigabytes memory -Xms2g.
    private void loadPDComponents(PatternOptions presetPattern, int choice) {
        PatternDatabase pd15 = new PatternDatabase(presetPattern, choice);
        patternGroups = pd15.getPatternGroups();
        patternFormatSize = new int[patternGroups.length];
        for (int i = 0; i < patternGroups.length; i++) {
            patternFormatSize[i] = PatternConstants.getFormatSize()[patternGroups[i]];
        }
        patternSet = pd15.getPatternSet();
        val2ptnKey = pd15.getVal2ptnKey();
        val2ptnOrder = pd15.getVal2ptnOrder();
        szGroup = patternGroups.length;
        szPdKeys = szGroup * 4;
        offsetPdSym = szGroup * 2;
    }

    // generate the additive pattern database components with give user defined
    // custom pattern
    private void customPDComponents(byte[] customPattern) {
        PatternDatabase pd15 = new PatternDatabase(customPattern);
        patternGroups = pd15.getPatternGroups();
        patternFormatSize = new int[patternGroups.length];
        for (int i = 0; i < patternGroups.length; i++) {
            patternFormatSize[i] = PatternConstants.getFormatSize()[patternGroups[i]];
        }
        patternSet = pd15.getPatternSet();
        val2ptnKey = pd15.getVal2ptnKey();
        val2ptnOrder = pd15.getVal2ptnOrder();
        szGroup = patternGroups.length;
        szPdKeys = szGroup * 4;
        offsetPdSym = szGroup * 2;
    }

    // load detected pattern key and format from a data file, if file not exists,
    // generate a new set
    private void loadPDElements(boolean[] elementGroups) {
        PatternElement pd15e = new PatternElement(elementGroups, mode);
        keys = pd15e.getKeys();
        formats = pd15e.getFormats();
        linkFormatMove = new int[szGroup][];
        rotateKeysByPos = new int[szGroup][];
        maxShiftX2 = new int[szGroup];
        for (int i = 0; i < szGroup; i++) {
            int group = patternGroups[i];
            linkFormatMove[i] = pd15e.getLinkFormatMoveSet(group);
            rotateKeysByPos[i] = pd15e.getKeyShiftSet(group);
            maxShiftX2[i] = PatternConstants.getMaxShiftX2()[group];
        }
    }

    /**
     *  Print solver description with in use pattern.
     */
    @Override
    public void printDescription() {
        super.printDescription();
        printInUsePattern();
    }

    // Print the additive pattern currently in use.
    protected void printInUsePattern() {
        System.out.print("Pattern in use" + " : ");
        int ct = 0;
        for (int group : inUsePtnArray) {
            ct++;
            System.out.print(group + " ");
            if (ct % 4 == 0) {
                if (ct < 16) {
                    System.out.print("\n                 ");
                } else {
                    System.out.println();
                }
            }
        }
        System.out.println();
    }

    /**
     * Returns the heuristic value of the given board.
     *
     * @param board the initial puzzle Board object to solve
     * @return byte value of the heuristic value of the given board
     */
    @Override
    public byte heuristic(Board board) {
        if (board == null) {
            throw new IllegalArgumentException("Board is null");
        }
        if (!board.isSolvable()) {
            return -1;
        }

        if (!board.equals(lastBoard)) {
            initialize(board);
            byte[] tilesSym = board.getTilesSym();

            // additive pattern database components
            pdKeys = convert2pd(tiles, tilesSym, szGroup);
            pdValReg = 0;
            pdValSym = 0;
            for (int i = szGroup; i < szGroup * 2; i++) {
                pdValReg += pdKeys[i];
                pdValSym += pdKeys[i +  offsetPdSym];
            }
            priorityGoal = (byte) Math.max(pdValReg, pdValSym);
        }
        return priorityGoal;
    }

    // convert 16 tiles to a sequence of pairs of element key and format combo
    // of the static pattern
    protected int[] convert2pd(byte[] regular, byte[] symmetry, int sizeGroup) {
        int[] orgFmt = new int[offsetPdSym];
        int[] orgKey = new int[offsetPdSym];
        int[] pdFactor = new int[szPdKeys];

        for (int i = 0; i < puzzleSize; i++) {
            for (int j = 0; j < sizeGroup; j++) {
                orgFmt[j] <<= 1;
                orgFmt[j + sizeGroup] <<= 1;
            }

            int value = regular[i];
            if (value != 0) {
                int group = val2ptnOrder[value];
                orgFmt[group] |= 1;
                orgKey[group] = (orgKey[group] << 4) | val2ptnKey[value];
            }
            value = symmetry[i];
            if (value != 0) {
                int group = val2ptnOrder[value];
                orgFmt[group + sizeGroup] |= 1;
                orgKey[group + sizeGroup] = (orgKey[group + sizeGroup] << 4) | val2ptnKey[value];
            }
        }

        for (int i = 0; i < sizeGroup; i++) {
            pdFactor[i] = (keys.get(orgKey[i])) * patternFormatSize[i] + formats.get(orgFmt[i]);
            pdFactor[i + sizeGroup] = getPDvalue(i, pdFactor[i]);
            pdFactor[i + offsetPdSym] = (keys.get(orgKey[i + sizeGroup])) * patternFormatSize[i]
                    + formats.get(orgFmt[i + sizeGroup]);
            pdFactor[i + offsetPdSym + sizeGroup] = getPDvalue(i, pdFactor[i + offsetPdSym]);
        }
        return pdFactor;
    }

    // solve the puzzle using interactive deepening A* algorithm
    protected void idaStar(int limit) {
        // start searching for solution
        while (limit <= maxMoves) {
            idaCount = 0;
            if (flagMessage) {
                System.out.print("ida limit " + limit);
            }
            dfs1stPrio(zeroX, zeroY, 0, limit, pdValReg, pdValSym);
            searchDepth = limit;
            searchNodeCount += idaCount;

            if (timeout) {
                if (flagMessage) {
                    System.out.printf("\tNodes : %-15s timeout\n", Integer.toString(idaCount));
                }
                return;
            } else {
                if (flagMessage) {
                    System.out.printf("\tNodes : %-15s " + stopwatch.currentTime() + "s\n",
                            Integer.toString(idaCount));
                }

                if (solved) {
                    return;
                }
            }
            limit += 2;
        }
    }

    // recursive depth first search until it reach the goal state or timeout, the least
    // estimate and node counts will be use to determine the starting order of next search
    protected void dfs1stPrio(int orgX, int orgY, int cost, int limit, int orgValReg,
            int orgValSym) {
        int zeroPos = orgY * rowSize + orgX;
        int zeroSym = symmetryPos[zeroPos];
        int costPlus1 = cost + 1;
        int[] orgCopy = new int[szPdKeys];
        System.arraycopy(pdKeys, 0, orgCopy, 0, szPdKeys);
        int[] estimate1stMove = new int[rowSize * 2];
        System.arraycopy(priority1stMove, 0, estimate1stMove, 0, rowSize * 2);

        int estimate = limit;
        do {
            int firstMoveIdx = -1;
            int nodeCount = 0;

            estimate = endOfSearch;
            for (int i = 0; i < 4; i++) {
                if (estimate1stMove[i] == endOfSearch) {
                    continue;
                } else if (priority1stMove[i + rowSize] > nodeCount) {
                    estimate = estimate1stMove[i];
                    nodeCount = priority1stMove[i + rowSize];
                    firstMoveIdx = i;
                } else {
                    priority1stMove[i] = endOfSearch;
                }
            }

            if (estimate < endOfSearch) {
                int startCounter = idaCount++;
                if (firstMoveIdx == Direction.RIGHT.getValue()) {
                    priority1stMove[firstMoveIdx] = shiftRight(orgX, orgY, zeroPos, zeroSym,
                            costPlus1, limit, orgValReg, orgValSym, orgCopy) - 1;
                } else if (firstMoveIdx == Direction.DOWN.getValue()) {
                    priority1stMove[firstMoveIdx] = shiftDown(orgX, orgY, zeroPos, zeroSym,
                            costPlus1, limit, orgValReg, orgValSym, orgCopy) - 1;
                } else if (firstMoveIdx == Direction.LEFT.getValue()) {
                    priority1stMove[firstMoveIdx] = shiftLeft(orgX, orgY, zeroPos, zeroSym,
                            costPlus1, limit, orgValReg, orgValSym, orgCopy) - 1;
                } else if (firstMoveIdx == Direction.UP.getValue()) {
                    priority1stMove[firstMoveIdx] = shiftUp(orgX, orgY, zeroPos, zeroSym,
                            costPlus1, limit, orgValReg, orgValSym, orgCopy) - 1;
                }
                priority1stMove[firstMoveIdx + rowSize] = idaCount - startCounter;
                estimate1stMove[firstMoveIdx] = endOfSearch;
            }
        } while (!terminated && estimate != endOfSearch);
    }

    // recursive depth first search until it reach the goal state or timeout
    private int recursiveDFS(int orgX, int orgY, int cost, int limit, int orgValReg,
            int orgValSym) {
        idaCount++;
        if (terminated) {
            return endOfSearch;
        }
        if (flagTimeout && stopwatch.currentTime() > searchTimeoutLimit) {
            stopwatch.stop();
            timeout = true;
            terminated = true;
            return endOfSearch;
        }
        assert stopwatch.isActive() : "stopwatch is not running.";

        int zeroPos = orgY * rowSize + orgX;
        int zeroSym = symmetryPos[zeroPos];
        int costPlus1 = cost + 1;
        int newEstimate = Math.max(orgValReg, orgValSym);
        int[] orgCopy = new int[pdKeys.length];
        System.arraycopy(pdKeys, 0, orgCopy, 0, pdKeys.length);

        boolean nonIdentical = true;
        nonIdentical = false;
        for (int i = 0; i < szGroup; i++) {
            if (pdKeys[i] != pdKeys[i + offsetPdSym]) {
                nonIdentical = true;
                break;
            }
        }

        // hard code different order to next moves base on the current move
        Direction prevMove = solutionMove[cost];
        if (prevMove == Direction.RIGHT) {
            // RIGHT
            if (orgX < rowSize - 1) {
                newEstimate = Math.min(newEstimate, shiftRight(orgX, orgY, zeroPos, zeroSym,
                        costPlus1, limit, orgValReg, orgValSym, orgCopy));
            }
            if (nonIdentical) {
                // UP
                if (orgY > 0) {
                    newEstimate = Math.min(newEstimate, shiftUp(orgX, orgY, zeroPos, zeroSym,
                            costPlus1, limit, orgValReg, orgValSym, orgCopy));
                }
                // DOWN
                if (orgY < rowSize - 1) {
                    newEstimate = Math.min(newEstimate, shiftDown(orgX, orgY, zeroPos, zeroSym,
                            costPlus1, limit, orgValReg, orgValSym, orgCopy));
                }
            }
        } else if (prevMove == Direction.DOWN) {
            // DOWN
            if (orgY < rowSize - 1) {
                newEstimate = Math.min(newEstimate, shiftDown(orgX, orgY, zeroPos, zeroSym,
                        costPlus1, limit, orgValReg, orgValSym, orgCopy));
            }
            if (nonIdentical) {
                // LEFT
                if (orgX > 0) {
                    newEstimate = Math.min(newEstimate, shiftLeft(orgX, orgY, zeroPos, zeroSym,
                            costPlus1, limit, orgValReg, orgValSym, orgCopy));
                }
                // RIGHT
                if (orgX < rowSize - 1) {
                    newEstimate = Math.min(newEstimate, shiftRight(orgX, orgY, zeroPos, zeroSym,
                            costPlus1, limit, orgValReg, orgValSym, orgCopy));
                }
            }
        } else if (prevMove == Direction.LEFT) {
            // LEFT
            if (orgX > 0) {
                newEstimate = Math.min(newEstimate, shiftLeft(orgX, orgY, zeroPos, zeroSym,
                        costPlus1, limit, orgValReg, orgValSym, orgCopy));
            }
            if (nonIdentical) {
                // DOWN
                if (orgY < rowSize - 1) {
                    newEstimate = Math.min(newEstimate, shiftDown(orgX, orgY, zeroPos, zeroSym,
                            costPlus1, limit, orgValReg, orgValSym, orgCopy));
                }
                // UP
                if (orgY > 0) {
                    newEstimate = Math.min(newEstimate, shiftUp(orgX, orgY, zeroPos, zeroSym,
                            costPlus1, limit, orgValReg, orgValSym, orgCopy));
                }
            }
        } else if (prevMove == Direction.UP) {
            // UP
            if (orgY > 0) {
                newEstimate = Math.min(newEstimate, shiftUp(orgX, orgY, zeroPos, zeroSym,
                        costPlus1, limit, orgValReg, orgValSym, orgCopy));
            }
            if (nonIdentical) {
                // RIGHT
                if (orgX < rowSize - 1) {
                    newEstimate = Math.min(newEstimate, shiftRight(orgX, orgY, zeroPos, zeroSym,
                            costPlus1, limit, orgValReg, orgValSym, orgCopy));
                }
                // LEFT
                if (orgX > 0) {
                    newEstimate = Math.min(newEstimate, shiftLeft(orgX, orgY, zeroPos, zeroSym,
                            costPlus1, limit, orgValReg, orgValSym, orgCopy));
                }
            }
        }
        return newEstimate;
    }

    // shift the space to right
    private int shiftRight(int orgX, int orgY, int zeroPos, int zeroSym, int costPlus1, int limit,
            int orgValReg, int orgValSym, int[] orgCopy) {
        if (terminated) {
            return endOfSearch;
        }
        int nextPos = zeroPos + 1;
        int value = tiles[nextPos];
        int ptnReg = val2ptnOrder[value];
        int ptnSym = val2ptnOrder[symmetryVal[value]];
        int keySymPos = ptnSym + offsetPdSym;

        shift(zeroPos, ptnReg, ptnReg, zeroSym, ptnSym, keySymPos, 0);
        solutionMove[costPlus1] = Direction.RIGHT;
        return nextMove(orgX + 1, orgY, zeroPos, nextPos, costPlus1, limit,
                orgValReg, orgValSym, ptnReg, ptnSym, keySymPos, orgCopy);
    }

    // shift the space to down
    private int shiftDown(int orgX, int orgY, int zeroPos, int zeroSym, int costPlus1, int limit,
            int orgValReg, int orgValSym, int[] orgCopy) {
        if (terminated) {
            return endOfSearch;
        }
        int nextPos = zeroPos + rowSize;
        int value = tiles[nextPos];
        int ptnReg = val2ptnOrder[value];
        int ptnSym = val2ptnOrder[symmetryVal[value]];
        int keySymPos = ptnSym + offsetPdSym;

        shift(zeroSym, ptnSym, keySymPos, zeroPos, ptnReg, ptnReg, 0);
        solutionMove[costPlus1] = Direction.DOWN;
        return nextMove(orgX, orgY + 1, zeroPos, nextPos, costPlus1, limit,
                orgValReg, orgValSym, ptnReg, ptnSym, keySymPos, orgCopy);
    }

    // shift the space to left
    private int shiftLeft(int orgX, int orgY, int zeroPos, int zeroSym, int costPlus1, int limit,
            int orgValReg, int orgValSym, int[] orgCopy) {
        if (terminated) {
            return endOfSearch;
        }
        int nextPos = zeroPos - 1;
        int value = tiles[nextPos];
        int ptnReg = val2ptnOrder[value];
        int ptnSym = val2ptnOrder[symmetryVal[value]];
        int keySymPos = ptnSym + offsetPdSym;

        shift(zeroPos, ptnReg, ptnReg, zeroSym, ptnSym, keySymPos, offsetDir);
        solutionMove[costPlus1] = Direction.LEFT;
        return nextMove(orgX - 1, orgY, zeroPos, nextPos, costPlus1, limit,
                orgValReg, orgValSym, ptnReg, ptnSym, keySymPos, orgCopy);
    }

    // shift the space to up
    private int shiftUp(int orgX, int orgY, int zeroPos, int zeroSym, int costPlus1, int limit,
            int orgValReg, int orgValSym, int[] orgCopy) {
        if (terminated) {
            return endOfSearch;
        }
        int nextPos = zeroPos - rowSize;
        int value = tiles[nextPos];
        int ptnReg = val2ptnOrder[value];
        int ptnSym = val2ptnOrder[symmetryVal[value]];
        int keySymPos = ptnSym + offsetPdSym;

        shift(zeroSym, ptnSym, keySymPos, zeroPos, ptnReg, ptnReg, offsetDir);
        solutionMove[costPlus1] = Direction.UP;
        return nextMove(orgX, orgY - 1, zeroPos, nextPos, costPlus1, limit,
                orgValReg, orgValSym, ptnReg, ptnSym, keySymPos, orgCopy);
    }

    // update the pattern database estimate after the shift
    private void shift(int posColShift, int ptnColShift, int keyColShift,
            int posRowShift, int ptnRowShift, int keyRowShift, int offsetDir) {
        // LEFT or RIGHT
        int oldFmt = pdKeys[keyColShift] % patternFormatSize[ptnColShift];
        int move = linkFormatMove[ptnColShift][oldFmt * 64 + posColShift * 4 + offsetDir];
        pdKeys[keyColShift] += (move >> 8) - oldFmt;

        // UP or DOWN
        oldFmt = pdKeys[keyRowShift] % patternFormatSize[ptnRowShift];
        move = linkFormatMove[ptnRowShift][oldFmt * 64 + posRowShift * 4 + 1 + offsetDir];
        int shift = move & 0x000F;
        if (shift > 0) {
            pdKeys[keyRowShift] = getKeyPtnShift(ptnRowShift,
                    pdKeys[keyRowShift] / patternFormatSize[ptnRowShift],
                    (move >> 4) & 0x000F, shift - 1) * patternFormatSize[ptnRowShift]
                            + (move >> 8);
        } else {
            pdKeys[keyRowShift] += (move >> 8) - oldFmt;
        }
    }

    // continue to next move if not reach goal state or over limit
    private int nextMove(int orgX, int orgY, int zeroPos, int nextPos, int cost,
            int limit,int orgValReg, int orgValSym, int ptnReg, int ptnSym, int keySymPos,
            int[] orgCopy) {
        int updatePtnValReg = getPDvalue(ptnReg, pdKeys[ptnReg]);
        int updatePtnValSym = getPDvalue(ptnSym, pdKeys[keySymPos]);
        int posPdValReg = ptnReg + szGroup;
        int posPdValSym = keySymPos + szGroup;
        int updatePdValReg = orgValReg - pdKeys[posPdValReg] + updatePtnValReg;
        int updatePdValSym = orgValSym - pdKeys[posPdValSym] + updatePtnValSym;
        int priority = Math.max(updatePdValReg, updatePdValSym);
        int updatePrio = priority;

        if (priority == 0) {
            stopwatch.stop();
            steps = (byte) cost;
            solved = true;
            terminated = true;
            return endOfSearch;
        } else if (priority < limit) {
            tiles[zeroPos] = tiles[nextPos];
            tiles[nextPos] = 0;
            pdKeys[posPdValReg] = updatePtnValReg;
            pdKeys[posPdValSym] = updatePtnValSym;
            updatePrio = Math.min(updatePrio, recursiveDFS(orgX, orgY, cost, limit - 1,
                    updatePdValReg, updatePdValSym));
            tiles[nextPos] = tiles[zeroPos];
            tiles[zeroPos] = 0;
            pdKeys[posPdValReg] = orgCopy[posPdValReg];
            pdKeys[posPdValSym] = orgCopy[posPdValSym];
        }
        pdKeys[ptnReg] = orgCopy[ptnReg];
        pdKeys[keySymPos] = orgCopy[keySymPos];
        return updatePrio;
    }

    // return the additive pattern database value with the given pattern order,
    // 32 bits compress key and format values
    private byte getPDvalue(int ptnOrder, int ptnKey) {
        return patternSet[ptnOrder][ptnKey];
    }

    // return key index after the space tile shift up or down which impact the key order has changed
    private int getKeyPtnShift(int ptnOrder, int key, int keyOrder, int shift) {
        int group = patternGroups[ptnOrder];
        return rotateKeysByPos[ptnOrder][(key * group + keyOrder) * maxShiftX2[ptnOrder] + shift];
    }

    /**
     * Returns the boolean represents the advanced priority in use.
     *
     * @return boolean represents the advanced priority in use
     */
    public final boolean getTimeoutFlag() {
        return flagTimeout;
    }

    /**
     * Returns the boolean represents the advanced priority in use.
     *
     * @return boolean represents the advanced priority in use
     */
    public final boolean getMessageFlag() {
        return flagMessage;
    }
}

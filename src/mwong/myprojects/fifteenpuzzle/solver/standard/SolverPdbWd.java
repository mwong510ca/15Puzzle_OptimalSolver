package mwong.myprojects.fifteenpuzzle.solver.standard;

import mwong.myprojects.fifteenpuzzle.solver.HeuristicOptions;
import mwong.myprojects.fifteenpuzzle.solver.SolverProperties;
import mwong.myprojects.fifteenpuzzle.solver.components.ApplicationMode;
import mwong.myprojects.fifteenpuzzle.solver.components.Board;
import mwong.myprojects.fifteenpuzzle.solver.components.Direction;
import mwong.myprojects.fifteenpuzzle.solver.components.PatternConstants;
import mwong.myprojects.fifteenpuzzle.solver.components.PatternDatabase;
import mwong.myprojects.fifteenpuzzle.solver.components.PatternElement;
import mwong.myprojects.fifteenpuzzle.solver.components.PatternElementMode;
import mwong.myprojects.fifteenpuzzle.solver.components.PatternOptions;

import java.util.HashMap;

/**
 * SolverPdbWd extends SolverWd.  It is the 15 puzzle optimal solver.
 * It takes a Board object of the puzzle and solve it with IDA* using combination of
 * Walking Distance and Additive Pattern Database of predefined pattern from PatternOptions.
 *
 * <p>Dependencies : Board.java, Direction.java, HeuristicOptions.java, patternConstants.java,
 *                   PatternDatabase.java, PatternElement.java, PatternElementMode.java,
 *                   PatternOptions.java, SolverWD.java
 *
 * @author Meisze Wong
 *         www.linkedin.com/pub/macy-wong/46/550/37b/
 */
public class SolverPdbWd extends SolverWd {
    private final int offsetReverse = 2;
    private final PatternElementMode action = PatternElementMode.PUZZLE_SOLVER;

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

    private byte[] inUsePtnArray;
    private int[] pdwdKeys;
    protected int regVal;
    protected int symVal;
    private int szGroup;
    private int szPdWdKeys;
    private int offsetPdSym;
    private int wdKeyIdx;

    /**
     * Initializes SolverPdbWd object using default preset pattern.
     */
    public SolverPdbWd() {
        this(SolverProperties.getPattern());
    }

    /**
     * Initializes SolverPdbWd object with given preset pattern.
     *
     * @param presetPattern the given preset pattern type
     */
    public SolverPdbWd(PatternOptions presetPattern) {
        this(presetPattern, 0);
    }

    /**
     * Initializes SolverPdbWd object with given preset pattern.
     *
     * @param presetPattern the given preset pattern type
     * @param appMode the given applicationMode for GUI or CONSOLE
     */
    protected SolverPdbWd(PatternOptions presetPattern, ApplicationMode appMode) {
        this(presetPattern, 0, appMode);
    }

    /**
     * Initializes SolverPdbWd object with choice of given preset pattern.
     *
     * @param presetPattern the given preset pattern type
     * @param choice the number of preset pattern option
     */
    public SolverPdbWd(PatternOptions presetPattern, int choice) {
        this(presetPattern, choice, ApplicationMode.CONSOLE);
    }

    private SolverPdbWd(PatternOptions presetPattern, int choice, ApplicationMode appMode) {
        super(appMode);
        loadPdbComponents(presetPattern, choice);
        loadPdbElements(presetPattern.getElements());
        inUsePtnArray = presetPattern.getPattern(choice);

        switch (presetPattern) {
            case Pattern_555:
                inUseHeuristic = HeuristicOptions.PD555;
                break;
            case Pattern_663:
                inUseHeuristic = HeuristicOptions.PD663;
                break;
            case Pattern_78:
                inUseHeuristic = HeuristicOptions.PD78;
                break;
            default:
                System.err.println("Invalid argument: preset pattern");
        }
    }

    // load preset additive pattern database from a data file, if file not exists
    // generate a new set.  Estimate takes 15s for 555 pattern, 2 minutes for 663 pattern,
    // 2.5 - 3 hours for 78 pattern also require minimum 2gigabytes memory -Xms2g.
    private void loadPdbComponents(PatternOptions presetPattern, int choice) {
        PatternDatabase pdb = new PatternDatabase(presetPattern, choice, appMode);
        patternGroups = pdb.getPatternGroups();
        patternFormatSize = new int[patternGroups.length];
        for (int i = 0; i < patternGroups.length; i++) {
            patternFormatSize[i] = PatternConstants.getFormatSize(patternGroups[i]);
        }
        patternSet = pdb.getPatternSet();
        val2ptnKey = pdb.getVal2ptnKey();
        val2ptnOrder = pdb.getVal2ptnOrder();
        szGroup = patternGroups.length;
        szPdWdKeys = szGroup * 4 + 4;
        offsetPdSym = szGroup * 2;
        wdKeyIdx = szGroup * 4;
    }

    // load detected pattern key and format from a data file, if file not exists,
    // generate a new set
    private void loadPdbElements(boolean[] elementGroups) {
        PatternElement element = new PatternElement(elementGroups, action, appMode);
        keys = element.getKeys();
        formats = element.getFormats();
        linkFormatMove = new int[szGroup][];
        rotateKeysByPos = new int[szGroup][];
        maxShiftX2 = new int[szGroup];
        for (int i = 0; i < szGroup; i++) {
            int group = patternGroups[i];
            linkFormatMove[i] = element.getLinkFormatMoveSet(group);
            rotateKeysByPos[i] = element.getKeyShiftSet(group);
            maxShiftX2[i] = PatternConstants.getMaxShiftX2(group);
        }
    }

    /**
     * Print solver description.
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
            // walking distance from parent/superclass
            priorityGoal = super.heuristic(board);

            // additive pattern database components
            pdwdKeys = convert2pd(tiles, tilesSym, szGroup);
            regVal = 0;
            symVal = 0;
            for (int i = szGroup; i < szGroup * 2; i++) {
                regVal += pdwdKeys[i];
                symVal += pdwdKeys[i +  offsetPdSym];
            }
            pdwdKeys[wdKeyIdx] = getWdIdxH();
            pdwdKeys[wdKeyIdx + 1] = getWdIdxV();
            pdwdKeys[wdKeyIdx + 2] = getWdValueH();
            pdwdKeys[wdKeyIdx + 3] = getWdValueV();

            priorityGoal = (byte) Math.max(Math.max(regVal, symVal), priorityGoal);
        }
        return priorityGoal;
    }

    // convert 16 tiles to a sequence of pairs of element key and format combo
    // of the static pattern
    private int[] convert2pd(byte[] regular, byte[] symmetry, int sizeGroup) {
        int[] orgFmt = new int[offsetPdSym];
        int[] orgKey = new int[offsetPdSym];
        int[] pdFactor = new int[szPdWdKeys];

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
        searchCountBase = 0;
        while (limit <= maxMoves) {
            idaCount = 0;
            if (flagMessage) {
                System.out.print("ida limit " + limit);
            }

            dfsStartingOrder(zeroX, zeroY, limit, regVal, symVal);
            searchCountBase += idaCount;
            searchNodeCount = searchCountBase;

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

    // recursive depth first search until it reach the goal state or timeout, the least estimate and
    // node counts will be use to determine the starting order of next search
    protected void dfsStartingOrder(int orgX, int orgY, int limit, int orgValReg, int orgValSym) {
        searchDepth = limit;
        int zeroPos = orgY * rowSize + orgX;
        int zeroSym = symmetryPos[zeroPos];
        int[] orgCopy = new int[szPdWdKeys];
        System.arraycopy(pdwdKeys, 0, orgCopy, 0, szPdWdKeys);
        int[] estimate1stMove = new int[4 * 2];
        System.arraycopy(lastDepthSummary, 0, estimate1stMove, 0, 4 * 2);

        int estimate = limit;
        while (!terminated && estimate != endOfSearch) {
            int firstMoveIdx = -1;
            int nodeCount = Integer.MAX_VALUE;

            estimate = endOfSearch;
            for (int i = 0; i < 4; i++) {
                if (estimate1stMove[i] == endOfSearch) {
                    continue;
                } else if (lastDepthSummary[i] < estimate) {
                    estimate = estimate1stMove[i];
                    nodeCount = lastDepthSummary[i + 4];
                    firstMoveIdx = i;
                } else if (lastDepthSummary[i] == estimate && lastDepthSummary[i + 4] < nodeCount) {
                    nodeCount = lastDepthSummary[i + 4];
                    firstMoveIdx = i;
                }
            }

            if (estimate < endOfSearch) {
                int startCounter = idaCount++;
                switch (Direction.values()[firstMoveIdx]) {
                    case RIGHT:
                        lastDepthSummary[firstMoveIdx] = shiftRight(orgX, orgY, zeroPos, zeroSym,
                                1, limit, orgValReg, orgValSym, orgCopy, resetKey);
                        break;
                    case DOWN:
                        lastDepthSummary[firstMoveIdx] = shiftDown(orgX, orgY, zeroPos, zeroSym,
                                1, limit, orgValReg, orgValSym, orgCopy, resetKey);
                        break;
                    case LEFT:
                        lastDepthSummary[firstMoveIdx] = shiftLeft(orgX, orgY, zeroPos, zeroSym,
                                1, limit, orgValReg, orgValSym, orgCopy, resetKey);
                        break;
                    case UP:
                        lastDepthSummary[firstMoveIdx] = shiftUp(orgX, orgY, zeroPos, zeroSym,
                                1, limit, orgValReg, orgValSym, orgCopy, resetKey);
                        break;
                    default:
                        assert false : "Error: starting order switch statement";
                }
                lastDepthSummary[firstMoveIdx + rowSize] = idaCount - startCounter;
                estimate1stMove[firstMoveIdx] = endOfSearch;
            }
        }
    }

    // recursive depth first search until it reach the goal state or timeout
    private int recursiveDFS(int orgX, int orgY, int cost, int limit, int orgValReg, int orgValSym,
            int orgPriority, int swirlKey) {
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
        int[] orgCopy = new int[pdwdKeys.length];
        System.arraycopy(pdwdKeys, 0, orgCopy, 0, pdwdKeys.length);
        int newEstimate = orgPriority;

        boolean nonIdentical = true;
        if (zeroPos == zeroSym) {
            nonIdentical = false;
            for (int i = 0; i < szGroup; i++) {
                if (pdwdKeys[i] != pdwdKeys[i + offsetPdSym]) {
                    nonIdentical = true;
                    break;
                }
            }
        }

        Direction prevMove = solutionMove[cost];
        // hard code order of next moves base on the current move
        switch (prevMove) {
            case RIGHT:
                // RIGHT
                if (orgX < rowSize - 1) {
                    newEstimate = Math.min(newEstimate, shiftRight(orgX, orgY, zeroPos, zeroSym,
                            costPlus1, limit, orgValReg, orgValSym, orgCopy, resetKey));
                }
                if (nonIdentical) {
                    // UP
                    if (orgY > 0 && isValidCounterClockwise(swirlKey)) {
                        newEstimate = Math.min(newEstimate, shiftUp(orgX, orgY, zeroPos, zeroSym,
                                costPlus1, limit, orgValReg, orgValSym, orgCopy,
                                swirlKey << 2 | ccwKey));
                    }
                    // DOWN
                    if (orgY < rowSize - 1 && isValidClockwise(swirlKey)) {
                        newEstimate = Math.min(newEstimate, shiftDown(orgX, orgY, zeroPos, zeroSym,
                                costPlus1, limit, orgValReg, orgValSym, orgCopy,
                                swirlKey << 2 | cwKey));
                    }
                }
                break;
            case DOWN:
                // DOWN
                if (orgY < rowSize - 1) {
                    newEstimate = Math.min(newEstimate, shiftDown(orgX, orgY, zeroPos, zeroSym,
                            costPlus1, limit, orgValReg, orgValSym, orgCopy, resetKey));
                }
                if (nonIdentical) {
                    // LEFT
                    if (orgX > 0 && isValidClockwise(swirlKey)) {
                        newEstimate = Math.min(newEstimate, shiftLeft(orgX, orgY, zeroPos, zeroSym,
                                costPlus1, limit, orgValReg, orgValSym, orgCopy,
                                swirlKey << 2 | cwKey));
                    }
                    // RIGHT
                    if (orgX < rowSize - 1 && isValidCounterClockwise(swirlKey)) {
                        newEstimate = Math.min(newEstimate, shiftRight(orgX, orgY, zeroPos, zeroSym,
                                costPlus1, limit, orgValReg, orgValSym, orgCopy,
                                swirlKey << 2 | ccwKey));
                    }
                }
                break;
            case LEFT:
                // LEFT
                if (orgX > 0) {
                    newEstimate = Math.min(newEstimate, shiftLeft(orgX, orgY, zeroPos, zeroSym,
                            costPlus1, limit, orgValReg, orgValSym, orgCopy, resetKey));
                }
                if (nonIdentical) {
                    // DOWN
                    if (orgY < rowSize - 1 && isValidCounterClockwise(swirlKey)) {
                        newEstimate = Math.min(newEstimate, shiftDown(orgX, orgY, zeroPos, zeroSym,
                                costPlus1, limit, orgValReg, orgValSym, orgCopy,
                                swirlKey << 2 | ccwKey));
                    }
                    // UP
                    if (orgY > 0 && isValidClockwise(swirlKey)) {
                        newEstimate = Math.min(newEstimate, shiftUp(orgX, orgY, zeroPos, zeroSym,
                                costPlus1, limit, orgValReg, orgValSym, orgCopy,
                                swirlKey << 2 | cwKey));
                    }
                }
                break;
            case UP:
                // UP
                if (orgY > 0) {
                    newEstimate = Math.min(newEstimate, shiftUp(orgX, orgY, zeroPos, zeroSym,
                            costPlus1, limit, orgValReg, orgValSym, orgCopy, resetKey));
                }
                if (nonIdentical) {
                    // RIGHT
                    if (orgX < rowSize - 1 && isValidClockwise(swirlKey)) {
                        newEstimate = Math.min(newEstimate, shiftRight(orgX, orgY, zeroPos, zeroSym,
                                costPlus1, limit, orgValReg, orgValSym, orgCopy,
                                swirlKey << 2 | cwKey));
                    }
                    // LEFT
                    if (orgX > 0 && isValidCounterClockwise(swirlKey)) {
                        newEstimate = Math.min(newEstimate, shiftLeft(orgX, orgY, zeroPos, zeroSym,
                                costPlus1, limit, orgValReg, orgValSym, orgCopy,
                                swirlKey << 2 | ccwKey));
                    }
                }
                break;
            default:
                assert false : "Error: recursive DFS switch statement";
        }
        return newEstimate;
    }

    // shift the space to right
    private int shiftRight(int orgX, int orgY, int zeroPos, int zeroSym, int costPlus1, int limit,
            int orgValReg, int orgValSym, int[] orgCopy, int swirlKey) {
        if (terminated) {
            return endOfSearch;
        }
        searchNodeCount = searchCountBase + idaCount;
        searchTime = stopwatch.currentTime();
        int nextPos = zeroPos + 1;
        int value = tiles[nextPos];
        int newIdx = getWDPtnIdx(pdwdKeys[wdKeyIdx + 1], (value - 1) % rowSize, forward);
        int newValue = getWDValue(newIdx);
        int wdPriority = pdwdKeys[wdKeyIdx + 2] + newValue;
        if (wdPriority == 0) {
            return goalReached(Direction.RIGHT, costPlus1);
        } else if (wdPriority < limit) {
            int ptnReg = val2ptnOrder[value];
            int ptnSym = val2ptnOrder[symmetryVal[value]];
            int keySymPos = ptnSym + offsetPdSym;

            pdwdKeys[wdKeyIdx + 1] = newIdx;
            pdwdKeys[wdKeyIdx + 3] = newValue;
            shift(zeroPos, ptnReg, ptnReg, zeroSym, ptnSym, keySymPos, 0);
            solutionMove[costPlus1] = Direction.RIGHT;
            return nextMove(orgX + 1, orgY, zeroPos, nextPos, true, costPlus1, limit,
                    wdPriority, orgValReg, orgValSym, ptnReg, ptnSym, keySymPos, orgCopy, swirlKey);
        }
        return wdPriority;
    }

    // shift the space to down
    private int shiftDown(int orgX, int orgY, int zeroPos, int zeroSym, int costPlus1, int limit,
            int orgValReg, int orgValSym, int[] orgCopy, int swirlKey) {
        if (terminated) {
            return endOfSearch;
        }
        int nextPos = zeroPos + rowSize;
        int value = tiles[nextPos];
        int newIdx = getWDPtnIdx(pdwdKeys[wdKeyIdx], (value - 1) / rowSize, forward);
        int newValue = getWDValue(newIdx);
        int wdPriority = pdwdKeys[wdKeyIdx + 3] + newValue;
        if (wdPriority == 0) {
            return goalReached(Direction.DOWN, costPlus1);
        } else if (wdPriority < limit) {
            int ptnReg = val2ptnOrder[value];
            int ptnSym = val2ptnOrder[symmetryVal[value]];
            int keySymPos = ptnSym + offsetPdSym;

            pdwdKeys[wdKeyIdx] = newIdx;
            pdwdKeys[wdKeyIdx + 2] = newValue;

            shift(zeroSym, ptnSym, keySymPos, zeroPos, ptnReg, ptnReg, 0);
            solutionMove[costPlus1] = Direction.DOWN;
            return nextMove(orgX, orgY + 1, zeroPos, nextPos, false, costPlus1, limit,
                    wdPriority, orgValReg, orgValSym, ptnReg, ptnSym, keySymPos, orgCopy, swirlKey);
        }
        return wdPriority;
    }

    // shift the space to left
    private int shiftLeft(int orgX, int orgY, int zeroPos, int zeroSym, int costPlus1, int limit,
            int orgValReg, int orgValSym, int[] orgCopy, int swirlKey) {
        if (terminated) {
            return endOfSearch;
        }
        int nextPos = zeroPos - 1;
        int value = tiles[nextPos];
        int newIdx = getWDPtnIdx(pdwdKeys[wdKeyIdx + 1], (value - 1) % rowSize, backward);
        int newValue = getWDValue(newIdx);
        int wdPriority = pdwdKeys[wdKeyIdx + 2] + newValue;
        if (wdPriority == 0) {
            return goalReached(Direction.LEFT, costPlus1);
        } else if (wdPriority < limit) {
            int ptnReg = val2ptnOrder[value];
            int ptnSym = val2ptnOrder[symmetryVal[value]];
            int keySymPos = ptnSym + offsetPdSym;

            pdwdKeys[wdKeyIdx + 1] = newIdx;
            pdwdKeys[wdKeyIdx + 3] = newValue;

            shift(zeroPos, ptnReg, ptnReg, zeroSym, ptnSym, keySymPos, offsetReverse);
            solutionMove[costPlus1] = Direction.LEFT;
            return nextMove(orgX - 1, orgY, zeroPos, nextPos, true, costPlus1, limit,
                    wdPriority, orgValReg, orgValSym, ptnReg, ptnSym, keySymPos, orgCopy, swirlKey);
        }
        return wdPriority;
    }

    // shift the space to up
    private int shiftUp(int orgX, int orgY, int zeroPos, int zeroSym, int costPlus1, int limit,
            int orgValReg, int orgValSym, int[] orgCopy, int swirlKey) {
        if (terminated) {
            return endOfSearch;
        }
        int nextPos = zeroPos - rowSize;
        int value = tiles[nextPos];
        int newIdx = getWDPtnIdx(pdwdKeys[wdKeyIdx], (value - 1) / rowSize, backward);
        int newValue = getWDValue(newIdx);
        int wdPriority = pdwdKeys[wdKeyIdx + 3] + newValue;
        if (wdPriority == 0) {
            return goalReached(Direction.UP, costPlus1);
        } else if (wdPriority < limit) {
            int ptnReg = val2ptnOrder[value];
            int ptnSym = val2ptnOrder[symmetryVal[value]];
            int keySymPos = ptnSym + offsetPdSym;

            pdwdKeys[wdKeyIdx] = newIdx;
            pdwdKeys[wdKeyIdx + 2] = newValue;

            shift(zeroSym, ptnSym, keySymPos, zeroPos, ptnReg, ptnReg, offsetReverse);
            solutionMove[costPlus1] = Direction.UP;
            return nextMove(orgX, orgY - 1, zeroPos, nextPos, false, costPlus1, limit,
                    wdPriority, orgValReg, orgValSym, ptnReg, ptnSym, keySymPos, orgCopy, swirlKey);
        }
        return wdPriority;
    }

    // continue to next move if not reach goal state or over limit
    private int nextMove(int orgX, int orgY, int zeroPos, int nextPos, boolean horizontalMove,
            int cost, int limit,int wdPriority, int orgValReg, int orgValSym, int ptnReg,
            int ptnSym, int keySymPos, int[] orgCopy, int swirlKey) {
        int updatePdValReg = getPDvalue(ptnReg, pdwdKeys[ptnReg]);
        int updatePdValSym = getPDvalue(ptnSym, pdwdKeys[keySymPos]);
        int posPdValReg = ptnReg + szGroup;
        int posPdValSym = keySymPos + szGroup;
        int pdValReg = orgValReg - pdwdKeys[posPdValReg] + updatePdValReg;
        int pdValSym = orgValSym - pdwdKeys[posPdValSym] + updatePdValSym;
        int updatePrio = Math.max(wdPriority, Math.max(pdValReg, pdValSym));

        if (updatePrio < limit) {
            tiles[zeroPos] = tiles[nextPos];
            tiles[nextPos] = 0;
            pdwdKeys[posPdValReg] = updatePdValReg;
            pdwdKeys[posPdValSym] = updatePdValSym;
            updatePrio = recursiveDFS(orgX, orgY, cost, limit - 1, pdValReg, pdValSym,
                    updatePrio, swirlKey);
            tiles[nextPos] = tiles[zeroPos];
            tiles[zeroPos] = 0;
            pdwdKeys[posPdValReg] = orgCopy[posPdValReg];
            pdwdKeys[posPdValSym] = orgCopy[posPdValSym];
        }

        pdwdKeys[ptnReg] = orgCopy[ptnReg];
        pdwdKeys[keySymPos] = orgCopy[keySymPos];
        if (horizontalMove) {
            pdwdKeys[wdKeyIdx + 1] = orgCopy[wdKeyIdx + 1];
            pdwdKeys[wdKeyIdx + 3] = orgCopy[wdKeyIdx + 3];
        } else {
            pdwdKeys[wdKeyIdx] = orgCopy[wdKeyIdx];
            pdwdKeys[wdKeyIdx + 2] = orgCopy[wdKeyIdx + 2];
        }
        return updatePrio;
    }

    // update the pattern database estimate after the shift
    private void shift(int posColShift, int ptnColShift, int keyColShift,
            int posRowShift, int ptnRowShift, int keyRowShift, int offset) {
        // Left or Right
        int oldFmt = pdwdKeys[keyColShift] % patternFormatSize[ptnColShift];
        int move = linkFormatMove[ptnColShift][oldFmt * 64 + posColShift * 4 + offset];
        pdwdKeys[keyColShift] += (move >> 8) - oldFmt;

        // Up or Down
        oldFmt = pdwdKeys[keyRowShift] % patternFormatSize[ptnRowShift];
        move = linkFormatMove[ptnRowShift][oldFmt * 64 + posRowShift * 4 + 1 + offset];
        int shift = move & 0x000F;
        if (shift > 0) {
            pdwdKeys[keyRowShift] = getKeyPtnShift(ptnRowShift, pdwdKeys[keyRowShift]
                    / patternFormatSize[ptnRowShift], (move >> 4) & 0x000F, shift - 1)
                    * patternFormatSize[ptnRowShift] + (move >> 8);
        } else {
            pdwdKeys[keyRowShift] += (move >> 8) - oldFmt;
        }
    }

    // update solution after reached goal state
    private int goalReached(Direction dir, int cost) {
        stopwatch.stop();
        solutionMove[cost] = dir;
        steps = (byte) cost;
        solved = true;
        terminated = true;
        return endOfSearch;
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
}

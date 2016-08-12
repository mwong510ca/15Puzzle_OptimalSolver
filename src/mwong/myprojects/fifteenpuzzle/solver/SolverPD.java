/****************************************************************************
 *  @author   Meisze Wong
 *            www.linkedin.com/pub/macy-wong/46/550/37b/
 *
 *  Compilation  : javac SolverPD.java
 *  Dependencies : Board.java, Direction.java, Stopwatch.java,
 *                 PDElement.java, PDCombo.java, PDPresetPatterns.java,
 *                 SolverAbstract, AdvancedAccumulator.java
 *                 AdvancedBoard.java, AdvancedMoves.java
 *
 *  SolverPD implements SolverInterface.  It take a Board object and solve the
 *  puzzle with IDA* using additive pattern database.
 *
 ****************************************************************************/

package mwong.myprojects.fifteenpuzzle.solver;

import mwong.myprojects.fifteenpuzzle.solver.components.AdvancedAccumulator;
import mwong.myprojects.fifteenpuzzle.solver.components.AdvancedBoard;
import mwong.myprojects.fifteenpuzzle.solver.components.AdvancedMoves;
import mwong.myprojects.fifteenpuzzle.solver.components.Board;
import mwong.myprojects.fifteenpuzzle.solver.components.Direction;
import mwong.myprojects.fifteenpuzzle.solver.components.PDCombo;
import mwong.myprojects.fifteenpuzzle.solver.components.PDElement;
import mwong.myprojects.fifteenpuzzle.solver.components.PDPresetPatterns;
import mwong.myprojects.utilities.Stopwatch;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class SolverPD extends SolverAbstract {
    private static final PDPresetPatterns defaultPattern = PDPresetPatterns.Pattern_663;
    private static final int offsetDir = 2;
    private static final PDElement.Mode mode = PDElement.Mode.PuzzleSolver;

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

    private PDPresetPatterns inUsePattern;
    private byte[] inUsePtnArray;

    private int[] pdKeys;
    private int szGroup;
    private int szPdKeys;
    private int offsetPdSym;
    private byte pdValReg = 0;
    private byte pdValSym = 0;
    private Board lastSearchBoard;

    /**
     *  Initializes SolverPD object using default preset pattern.
     */
    public SolverPD() {
        this(defaultPattern);
    }

    /**
     *  Initializes SolverPD object using given preset pattern.
     *
     *  @param presetPattern the given preset pattern type
     */
    public SolverPD(PDPresetPatterns presetPattern) {
        this(presetPattern, 0);
    }

    /**
     *  Initializes SolverPD object with choice of given preset pattern.
     *
     *  @param presetPattern the given preset pattern type
     *  @param choice the number of preset pattern option
     */
    public SolverPD(PDPresetPatterns presetPattern, int choice) {
        flagMessage = SWITCH_ON;
        loadPDComponents(presetPattern, choice);
        loadPDElements(presetPattern.getElements());
        inUsePattern = presetPattern;
        inUsePtnArray = presetPattern.getPattern(choice);
        if (presetPattern == PDPresetPatterns.Pattern_555) {
            inUseHeuristic = HeuristicType.PD555;
        } else if (presetPattern == PDPresetPatterns.Pattern_663) {
            inUseHeuristic = HeuristicType.PD663;
        } else if (presetPattern == PDPresetPatterns.Pattern_78) {
            inUseHeuristic = HeuristicType.PD78;
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
        flagMessage = SWITCH_ON;
        flagTimeout = SWITCH_ON;
        searchTimeoutLimit = 30;
        customPDComponents(customPattern);
        loadPDElements(elementGroups);
        inUsePattern = PDPresetPatterns.Custom;
        inUsePtnArray = customPattern;
        inUseHeuristic = HeuristicType.PDCustom;
    }

    // load preset additive pattern database from a data file, if file not exists
    // generate a new set.  Estimate takes 15s for 555 pattern, 2 minutes for 663 pattern,
    // 2.5 - 3 hours for 78 pattern also require minimum 2gigabytes memory -Xms2g.
    private void loadPDComponents(PDPresetPatterns presetPattern, int choice) {
        PDCombo pd15 = new PDCombo(presetPattern, choice);
        patternGroups = pd15.getPatternGroups();
        patternFormatSize = new int[patternGroups.length];
        for (int i = 0; i < patternGroups.length; i++) {
            patternFormatSize[i] = PDElement.getFormatSize(patternGroups[i]);
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
        PDCombo pd15 = new PDCombo(customPattern);
        patternGroups = pd15.getPatternGroups();
        patternFormatSize = new int[patternGroups.length];
        for (int i = 0; i < patternGroups.length; i++) {
            patternFormatSize[i] = PDElement.getFormatSize(patternGroups[i]);
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
        PDElement pd15e = new PDElement(elementGroups, mode);
        keys = pd15e.getKeys();
        formats = pd15e.getFormats();
        linkFormatMove = new int[szGroup][];
        rotateKeysByPos = new int[szGroup][];
        maxShiftX2 = new int[szGroup];
        for (int i = 0; i < szGroup; i++) {
            int group = patternGroups[i];
            linkFormatMove[i] = pd15e.getLinkFormatMoveSet(group);
            rotateKeysByPos[i] = pd15e.getKeyShiftSet(group);
            maxShiftX2[i] = PDElement.getShiftMaxX2(group);
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
    private void printInUsePattern() {
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
     *  Find the optimal path to goal state if the given board is solvable.
     *  Overload findOptimalPath with given heuristic value (for AdvancedAccumulator)
     *
     *  @param board the initial puzzle Board object to solve
     *  @param estimate the given initial limit to solve the puzzle
     */
    public void findOptimalPath(Board board, byte estimate) {
        if (board.isSolvable()) {
            clearHistory();
            stopwatch = new Stopwatch();
            priority1stMove = new int[ROW_SIZE * 2];
            System.arraycopy(board.getValidMoves(), 0, priority1stMove, ROW_SIZE, ROW_SIZE);
            // initializes the board by calling heuristic function using original priority
            // then solve the puzzle with given estimate instead
            heuristic(board, tagOriginal, tagSearch);
            idaStar(estimate);
            stopwatch = null;
        }
    }

    // calculate the heuristic value of the given board and save the properties
    byte heuristic(Board board, boolean inAdvanced, boolean inSearch) {
        if (!board.isSolvable()) {
            return -1;
        }

        priorityAdvanced = -1;
        if (!board.equals(lastBoard) || inSearch) {
            lastBoard = board;
            tiles = board.getTiles();
            zeroX = board.getZeroX();
            zeroY = board.getZeroY();
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

        if (!inAdvanced) {
            return priorityGoal;
        }

        if (priorityAdvanced != -1) {
            if (inSearch) {
                advancedContains(board, inSearch);
            }
            return priorityAdvanced;
        }

        advancedContains(board, inSearch);
        if (priorityAdvanced != -1) {
            return priorityAdvanced;
        }

        priorityAdvanced = priorityGoal;
        if (priorityAdvanced < CUTOFF_ADV_PRIORITY) {
            return priorityAdvanced;
        }

        int[] orgKeys = pdKeys.clone();
        byte orgReg = pdValReg;
        byte orgSym = pdValSym;

        if (advancedEstimate(board)) {
            clearHistory();
            zeroX = board.getZeroX();
            zeroY = board.getZeroY();
            tiles = board.getTiles();
            pdKeys = orgKeys;
            pdValReg = orgReg;
            pdValSym = orgSym;
            priority1stMove = new int [ROW_SIZE * 2];
            System.arraycopy(board.getValidMoves(), 0, priority1stMove, ROW_SIZE, ROW_SIZE);
        }

        if ((priorityAdvanced - priorityGoal) % 2 == 1) {
            priorityAdvanced++;
        }
        return priorityAdvanced;
    }

    // calculate the advanced estimate from stored reference boards
    private boolean advancedEstimate(Board board) {
        Map<AdvancedBoard, AdvancedMoves> advMap;
        if (advAccumulator == null) {
            advMap = AdvancedAccumulator.getDefaultMap();
        } else {
            advMap = advAccumulator.getActiveMap();
        }

        byte maxEstimate = priorityGoal;
        byte[] orgTiles = tiles.clone();
        boolean reset = false;
        for (Entry<AdvancedBoard, AdvancedMoves> entry
                : advMap.entrySet()) {
            byte[] transTiles = entry.getKey().transformer(orgTiles);
            byte[] transTilesSym = tiles2sym(transTiles);

            int[] transPD = convert2pd(transTiles, transTilesSym, szGroup);
            int sumPdReg = 0;
            int sumPdSym = 0;
            for (int j = szGroup; j < szGroup * 2; j++) {
                sumPdReg += transPD[j];
                sumPdSym += transPD[j + szGroup * 2];
            }

            int transPriority = Math.max(sumPdReg, sumPdSym);
            if (transPriority > CUTOFF_ADV_PRIORITY) {
                continue;
            }
            if (entry.getValue().getEstimate() - transPriority <= maxEstimate) {
                continue;
            }

            reset = true;
            clearHistory();
            Board temp = new Board(transTiles);
            zeroX = temp.getZeroX();
            zeroY = temp.getZeroY();
            tiles = temp.getTiles();
            pdKeys = transPD;
            pdValReg = (byte) sumPdReg;
            pdValSym = (byte) sumPdSym;
            System.arraycopy(temp.getValidMoves(), 0, priority1stMove, ROW_SIZE, ROW_SIZE);

            idaStar(transPriority, entry.getValue().getEstimate() - maxEstimate);
            if (!stopwatch.isActive()) {
                stopwatch.start();
            }
            if (solved) {
                maxEstimate = (byte) (entry.getValue().getEstimate() - steps);
            }
        }
        priorityAdvanced = maxEstimate;
        return reset;
    }

    // convert 16 tiles to a sequence of pairs of element key and format combo
    // of the static pattern
    private int[] convert2pd(byte[] regular, byte[] symmetry, int sizeGroup) {
        int[] orgFmt = new int[offsetPdSym];
        int[] orgKey = new int[offsetPdSym];
        int[] pdFactor = new int[szPdKeys];

        for (int i = 0; i < BOARD_SIZE; i++) {
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
    void idaStar(int limit) {
        if (inUsePattern == PDPresetPatterns.Pattern_78) {
            lastSearchBoard = new Board(tiles);
        }

        if (solutionMove[1] != null) {
            advancedSearch(limit);
            return;
        }

        int countDir = 0;
        for (int i = 0; i < ROW_SIZE; i++) {
            if (priority1stMove[i + ROW_SIZE] > 0) {
                countDir++;
            }
        }

        // quick scan for advanced priority, determine the start order for optimization
        if (flagAdvancedPriority && countDir > 1) {
            int initLimit = priorityGoal;
            while (initLimit < limit) {
                idaCount = 0;
                dfs1stPrio(zeroX, zeroY, 0, initLimit, pdValReg, pdValSym);
                initLimit += 2;

                boolean overload = false;
                for (int i = ROW_SIZE; i < ROW_SIZE * 2; i++) {
                    if (priority1stMove[i] > 10000) {
                        overload = true;
                        break;
                    }
                }
                if (overload) {
                    break;
                }
            }
        }

        // start searching for solution
        while (limit <= MAX_MOVES) {
            idaCount = 0;
            if (flagMessage) {
                System.out.print("ida limit " + limit);
            }
            dfs1stPrio(zeroX, zeroY, 0, limit, pdValReg, pdValSym);
            searchDepth = limit;
            searchNodeCount += idaCount;

            if (timeout) {
                if (flagMessage) {
                	System.out.println("\tNodes : " + num2string(idaCount) + "timeout");
                }
                return;
            } else {
            	if (flagMessage) {
                    System.out.println("\tNodes : " + num2string(idaCount) + stopwatch.currentTime() + "s");
                } 
            	
            	if (solved) {
            		// if currently using pattern database 7-8 and it takes long than cutoff limit
                    // to solve, add the board and solutions to reference boards collection.
                    if (inUseHeuristic == HeuristicType.PD78
                            && stopwatch.currentTime() >  advAccumulator.getCutoffLimit()) {
                        // backup original solutions
                        final Stopwatch backupTime = stopwatch;
                        final byte backupSteps = steps;
                        final int backupIdaCount = searchNodeCount;
                        final Direction[] backupSolution = solutionMove.clone();

                        searchTime = stopwatch.currentTime();
                        stopwatch = new Stopwatch();
                        // only update cached advanced priority if using original priority search
                        // and the initial board has added to the reference boards
                        if (advAccumulator.addBoard(this) && !flagAdvancedPriority) {
                            priorityAdvanced = backupSteps;
                        }

                        // restore original solutions
                        stopwatch = backupTime;
                        steps = backupSteps;
                        searchNodeCount = backupIdaCount;
                        solutionMove = backupSolution;
                    }
                    return;
            	}
            }
            limit += 2;
        }
    }

    // overload idaStar to solve the puzzle with the given max limit for advancedEstimate
    void idaStar(int limit, int maxLimit) {
        while (limit <= maxLimit) {
            dfs1stPrio(zeroX, zeroY, 0, limit, pdValReg, pdValSym);
            if (solved) {
                return;
            }
            limit += 2;
        }
    }

    // skip the first 8 moves from stored record then solve the remaining puzzle
    // using depth first search with exact number of steps of optimal solution
    private void advancedSearch(int limit) {
        Direction[] dupSolution = new Direction[limit + 1];
        System.arraycopy(solutionMove, 1, dupSolution, 1, ADV_PARTIAL_MOVES);

        Board board = new Board(tiles);
        for (int i = 1; i < ADV_PARTIAL_MOVES; i++) {
            board = board.shift(dupSolution[i]);
            if (board == null) {
                System.out.println(i + "board is null");
                System.out.println(Arrays.toString(solutionMove));
                System.out.println(new Board(tiles));
            }
        }
        heuristic(board, tagOriginal, tagSearch);

        int firstDirValue = dupSolution[ADV_PARTIAL_MOVES].getValue();
        for (int i = 0; i < 4; i++) {
            if (i != firstDirValue) {
                priority1stMove[i + 4] = 0;
            } else {
                priority1stMove[i + 4] = 1;
            }
        }

        idaCount = ADV_PARTIAL_MOVES;
        if (flagMessage) {
            System.out.print("ida limit " + limit);
        }
        dfs1stPrio(zeroX, zeroY, 0, limit - ADV_PARTIAL_MOVES + 1, pdValReg, pdValSym);
        if (solved) {
            System.arraycopy(solutionMove, 2, dupSolution, ADV_PARTIAL_MOVES + 1,
                    limit - ADV_PARTIAL_MOVES);
            solutionMove = dupSolution;
        }
        steps = (byte) limit;
        searchDepth = limit;
        searchNodeCount += idaCount;

        if (flagMessage) {
            if (timeout) {
            	System.out.println("\tNodes : " + num2string(idaCount) + "\ttimeout");
            } else {
            	System.out.println("\tNodes : " + num2string(idaCount) + stopwatch.currentTime() + "s");
            }
        }
    }

    // recursive depth first search until it reach the goal state or timeout, the least estimate and
    // node counts will be use to determine the starting order of next search
    private void dfs1stPrio(int orgX, int orgY, int cost, int limit, int orgValReg, int orgValSym) {
        int zeroPos = orgY * ROW_SIZE + orgX;
        int zeroSym = SYMMETRY_POS[zeroPos];
        int costPlus1 = cost + 1;
        int[] orgCopy = new int[szPdKeys];
        System.arraycopy(pdKeys, 0, orgCopy, 0, szPdKeys);
        int[] estimate1stMove = new int[ROW_SIZE * 2];
        System.arraycopy(priority1stMove, 0, estimate1stMove, 0, ROW_SIZE * 2);

        int estimate = limit;
        do {
            int firstMoveIdx = -1;  // 0 - Right, 1 - Down, 2 - Left, 3 - Up
            int nodeCount = 0;

            estimate = END_OF_SEARCH;
            for (int i = 0; i < 4; i++) {
                if (estimate1stMove[i] == END_OF_SEARCH) {
                    continue;
                } else if (priority1stMove[i + ROW_SIZE] > nodeCount) {
                    estimate = estimate1stMove[i];
                    nodeCount = priority1stMove[i + ROW_SIZE];
                    firstMoveIdx = i;
                } else {
                    priority1stMove[i] = END_OF_SEARCH;
                }
            }

            if (estimate < END_OF_SEARCH) {
                int startCounter = idaCount++;
                if (firstMoveIdx == 0) {
                    priority1stMove[firstMoveIdx] = shiftRight(orgX, orgY, zeroPos, zeroSym,
                            costPlus1, limit, orgValReg, orgValSym, orgCopy) - 1;
                } else if (firstMoveIdx == 1) {
                    priority1stMove[firstMoveIdx] = shiftDown(orgX, orgY, zeroPos, zeroSym,
                            costPlus1, limit, orgValReg, orgValSym, orgCopy) - 1;
                } else if (firstMoveIdx == 2) {
                    priority1stMove[firstMoveIdx] = shiftLeft(orgX, orgY, zeroPos, zeroSym,
                            costPlus1, limit, orgValReg, orgValSym, orgCopy) - 1;
                } else if (firstMoveIdx == 3) {
                    priority1stMove[firstMoveIdx] = shiftUp(orgX, orgY, zeroPos, zeroSym,
                            costPlus1, limit, orgValReg, orgValSym, orgCopy) - 1;
                }
                priority1stMove[firstMoveIdx + ROW_SIZE] = idaCount - startCounter;
                estimate1stMove[firstMoveIdx] = END_OF_SEARCH;
            }
        } while (!terminated && estimate != END_OF_SEARCH);
    }

    // recursive depth first search until it reach the goal state or timeout
    private int recursiveDFS(int orgX, int orgY, int cost, int limit, int orgValReg,
            int orgValSym) {
        idaCount++;
        if (terminated) {
            return END_OF_SEARCH;
        }
        if (flagTimeout && stopwatch.currentTime() > searchTimeoutLimit) {
            stopwatch.stop();
            timeout = true;
            terminated = true;
            return END_OF_SEARCH;
        }
        assert stopwatch.isActive() : "stopwatch is not running.";

        int zeroPos = orgY * ROW_SIZE + orgX;
        int zeroSym = SYMMETRY_POS[zeroPos];
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
            if (orgX < ROW_SIZE - 1) {
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
                if (orgY < ROW_SIZE - 1) {
                    newEstimate = Math.min(newEstimate, shiftDown(orgX, orgY, zeroPos, zeroSym,
                            costPlus1, limit, orgValReg, orgValSym, orgCopy));
                }
            }
        } else if (prevMove == Direction.DOWN) {
            // DOWN
            if (orgY < ROW_SIZE - 1) {
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
                if (orgX < ROW_SIZE - 1) {
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
                if (orgY < ROW_SIZE - 1) {
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
                if (orgX < ROW_SIZE - 1) {
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
            return END_OF_SEARCH;
        }
        int nextPos = zeroPos + 1;
        int value = tiles[nextPos];
        int ptnReg = val2ptnOrder[value];
        int ptnSym = val2ptnOrder[SYMMETRY_VAL[value]];
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
            return END_OF_SEARCH;
        }
        int nextPos = zeroPos + ROW_SIZE;
        int value = tiles[nextPos];
        int ptnReg = val2ptnOrder[value];
        int ptnSym = val2ptnOrder[SYMMETRY_VAL[value]];
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
            return END_OF_SEARCH;
        }
        int nextPos = zeroPos - 1;
        int value = tiles[nextPos];
        int ptnReg = val2ptnOrder[value];
        int ptnSym = val2ptnOrder[SYMMETRY_VAL[value]];
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
            return END_OF_SEARCH;
        }
        int nextPos = zeroPos - ROW_SIZE;
        int value = tiles[nextPos];
        int ptnReg = val2ptnOrder[value];
        int ptnSym = val2ptnOrder[SYMMETRY_VAL[value]];
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
            return END_OF_SEARCH;
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
    public final boolean getPriorityFlag() {
        return flagAdvancedPriority;
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

    /**
     * Returns the board object of last search.
     *
     * @return board object of last search
     */
    public final Board lastSearchBoard() {
        if (inUsePattern != PDPresetPatterns.Pattern_78) {
            throw new UnsupportedOperationException();
        }
        return lastSearchBoard;
    }
}

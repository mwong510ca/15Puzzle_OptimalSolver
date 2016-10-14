package mwong.myprojects.fifteenpuzzle;

import mwong.myprojects.fifteenpuzzle.solver.SolverConstants;
import mwong.myprojects.fifteenpuzzle.solver.advanced.SmartSolverMd;
import mwong.myprojects.fifteenpuzzle.solver.advanced.SmartSolverPdb;
import mwong.myprojects.fifteenpuzzle.solver.advanced.SmartSolverPdbWd;
import mwong.myprojects.fifteenpuzzle.solver.advanced.SmartSolverWd;
import mwong.myprojects.fifteenpuzzle.solver.advanced.SmartSolverWdMd;
import mwong.myprojects.fifteenpuzzle.solver.ai.ReferenceFactory;
import mwong.myprojects.fifteenpuzzle.solver.ai.ReferenceRemote;
import mwong.myprojects.fifteenpuzzle.solver.components.Board;
import mwong.myprojects.fifteenpuzzle.solver.components.PatternOptions;
import mwong.myprojects.fifteenpuzzle.solver.components.PuzzleConstants;
import mwong.myprojects.fifteenpuzzle.solver.components.PuzzleDifficultyLevel;
import py4j.GatewayServer;

import java.io.IOException;

/**
 * GatewayServerFifteenPuzzle for pyqt5 GUI front end to connect to 15 puzzle solvers.
 * It use standalone reference collection.
 *
 * @author Meisze Wong
 *         www.linkedin.com/pub/macy-wong/46/550/37b/
 */
public class GatewayServerFifteenPuzzle {
    private Board board;
    private SmartSolverMd solverMd;
    private SmartSolverWd solverWd;
    private SmartSolverWdMd solverWdMd;
    private SmartSolverPdbWd solverPdbWd555;
    private SmartSolverPdbWd solverPdbWd663;
    private SmartSolverPdb solverPdb78;
    private static ReferenceRemote refConnection;
    private int timeoutLimit;
    
    public GatewayServerFifteenPuzzle() {
        loadReferenceConnection();
    }

    void loadReferenceConnection() {
        try {
            refConnection = (new ReferenceFactory()).getReferenceLocal();
        } catch (IOException ex) {
            refConnection = null;
        }
        boolean messageOff = !SolverConstants.isOnSwitch();
        timeoutLimit = 10;
        if (PropertiesCache.getInstance().containsKey("guiTimeoutLimit")) {
            try {
                timeoutLimit = Integer.parseInt(PropertiesCache.getInstance().getProperty(
                        "guiTimeoutLimit"));
            } catch (NumberFormatException ex) {
                // do nothing
            }
        }

        solverMd = new SmartSolverMd(refConnection);
        solverMd.messageSwitch(messageOff);
        solverMd.setTimeoutLimit(timeoutLimit);

        solverWd = new SmartSolverWd(refConnection);
        solverWd.messageSwitch(messageOff);
        solverWd.setTimeoutLimit(timeoutLimit);

        solverWdMd = new SmartSolverWdMd(refConnection);
        solverWdMd.messageSwitch(messageOff);
        solverWdMd.setTimeoutLimit(timeoutLimit);

        solverPdbWd555 = new SmartSolverPdbWd(PatternOptions.Pattern_555, refConnection);
        solverPdbWd555.messageSwitch(messageOff);
        solverPdbWd555.setTimeoutLimit(timeoutLimit);

        solverPdbWd663 = new SmartSolverPdbWd(PatternOptions.Pattern_663, refConnection);
        solverPdbWd663.messageSwitch(messageOff);
        solverPdbWd663.setTimeoutLimit(timeoutLimit);

        solverPdb78 = new SmartSolverPdb(PatternOptions.Pattern_78, refConnection);
        solverPdb78.messageSwitch(messageOff);
        solverPdb78.timeoutSwitch(!SolverConstants.isOnSwitch());
    }

    public boolean isConnected() {
        return true;
    }

    public Board getGoal() {
        board = new Board(PuzzleConstants.getGoalTiles());
        return board;
    }

    public Board getRandom() {
        board = new Board();
        return board;
    }

    public Board getEasy() {
        board = new Board(PuzzleDifficultyLevel.EASY);
        return board;
    }

    public Board getModerate() {
        board = new Board(PuzzleDifficultyLevel.MODERATE);
        return board;
    }

    public Board getHard() {
        board = new Board(PuzzleDifficultyLevel.HARD);
        return board;
    }

    public Board getBoard(byte[] block) {
        board = new Board(block);
        return board;
    }

    public SmartSolverPdb getSolver_0() {
        return solverPdb78;
    }

    public SmartSolverPdbWd getSolver_1() {
        return solverPdbWd663;
    }

    public SmartSolverPdbWd getSolver_2() {
        return solverPdbWd555;
    }

    public SmartSolverWdMd getSolver_3() {
        return solverWdMd;
    }

    public SmartSolverWd getSolver_4() {
        return solverWd;
    }

    public SmartSolverMd getSolver_5() {
        solverMd.linearConflictSwitch(SolverConstants.isTagLinearConflict());
        return solverMd;
    }

    public SmartSolverMd getSolver_6() {
        solverMd.linearConflictSwitch(SolverConstants.isTagLinearConflict());
        return solverMd;
    }

    public int getTimeoutLimit() {
    	return timeoutLimit;
    }
    
    /**
     * Main application to start the gateway server.
     * @param args standard argument main function
     */
    public static void main(String[] args) {
        GatewayServer gatewayServer = new GatewayServer(new GatewayServerFifteenPuzzle());
        gatewayServer.start();
        System.out.println("Gateway server for 15 puzzle started");
    }
}
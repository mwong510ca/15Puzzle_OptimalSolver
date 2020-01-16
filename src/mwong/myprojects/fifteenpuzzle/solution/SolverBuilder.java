package mwong.myprojects.fifteenpuzzle.solution;

import java.rmi.RemoteException;

import mwong.myprojects.fifteenpuzzle.puzzle.HeuristicOptions;
import mwong.myprojects.fifteenpuzzle.puzzle.PatternOptions;
import mwong.myprojects.fifteenpuzzle.solution.Solver.ApplicationMode;
import mwong.myprojects.fifteenpuzzle.solution.Solver.SolverVersion;
import mwong.myprojects.fifteenpuzzle.solution.ai.ReferenceRecorder;
import mwong.myprojects.fifteenpuzzle.solution.ai.ReferenceRemote;

/**
 * SolverBuilder is the main class to create the solvers.  It collected the setting
 * and simply create the solver object.  All Solver classes can't access outside
 * the package.  It must create from SolverBuilder.
 *
 * <p>Dependencies : HeuristicOptions.java, PatternOptions.java, ApplicationMode.java,
 *                   SolverVersion.java, ReferenceRemote.java, SolverMd.java,
 *                   SolverPdb.java, SolverPdb78.java, SolverPdb78Enh.java,
 *                   SolverPdbWd.java, SolverWd.java, SolverWdMd.java
 *
 * @author <a href="http://www.linkedin.com/pub/macy-wong/46/550/37b/"
 *            target="_blank">Meisze Wong (linkedin)</a>
 * @see <a href="http://www.github.com/mwong510ca/15PuzzleOptimalSolver/"
 *         target="_blank">GitHub (full project)</a>
 */
public final class SolverBuilder {
  /** A static instance of solver using pattern 78.  Reuse the data set
   *  to create new solvers. */
  private static Solver instancePdb78;
  /** Default time out setting. */
  private static final int DEFAULT_TIMEOUT = 10;
  /** The ApplicationMode of the application. */
  private final ApplicationMode mode;
  /** An instance of ReferenceRemote object. */
  private ReferenceRemote refObj = null;
  /** The SolverVersion to be use. */
  private SolverVersion versionSetting;
  /** The boolean of status display setting. */
  private boolean statusSetting;
  /** The timer feature on/off of solver using pattern database 7-8. */
  private boolean timerSettingPdb78;
  /** The time out limit to be use. If no preference, use default setting. */
  private int timeoutLimit;

  /**
   * Internal use only, default application type system.
   */
  SolverBuilder() {
    this(ApplicationMode.SYSTEM, false);
  }

  /**
   * Initial the builder with the mandatory type of application.
   *
   * @param mode the mandatory ApplicationMode
   */
  public SolverBuilder(final ApplicationMode mode) {
    this(mode, false);
  }

  /**
   * Initial the builder with the mandatory type of application and optional
   * timeout limit.
   *
   * @param mode the mandatory ApplicationMode
   * @param timeoutLimit the optional timeout limit
   */
  public SolverBuilder(final ApplicationMode mode, final int timeoutLimit) {
    this(mode, false, timeoutLimit);
  }

  /**
   * Initial the builder with the mandatory type of application and optional
   * status printing feature.
   *
   * @param mode the mandatory ApplicationMode
   * @param statusFlag boolean represent the status printing feature on
   */
  public SolverBuilder(final ApplicationMode mode, final boolean statusFlag) {
    this(mode, statusFlag, DEFAULT_TIMEOUT);
  }

  /**
   * Initial the builder with the mandatory type of application and optional
   * status printing feature and timeout limit.
   *
   * @param mode the mandatory ApplicationMode
   * @param statusFlag boolean represent the status printing feature on
   * @param timeoutLimit the optional timeout limit
   */
  public SolverBuilder(final ApplicationMode mode, final boolean statusFlag,
      final int timeoutLimit) {
    this.mode = mode;
    if (mode == ApplicationMode.CONSOLE) {
      this.statusSetting = statusFlag;
    }
    timerSettingPdb78 = false;
    setTimeoutLimit(timeoutLimit);
    versionSetting = SolverVersion.PRIME;
  }

  /**
   * Store the ReferenceRemote object.
   *
   * @param referenceObj the given ReferenceRemote object.
   */
  public void setReference(final ReferenceRemote referenceObj) {
    try {
      if (referenceObj == null || referenceObj.getActiveMap() == null) {
        throw new IllegalArgumentException("Null object - setReference in SolverBuilder");
      }
      this.refObj = referenceObj;
      versionSetting = SolverVersion.OPTIMUM;
    } catch (RemoteException e) {
      e.printStackTrace();
      System.err.println("Connection lost -- SolverBuilder setReference");
      versionSetting = SolverVersion.PRIME;
    }
  }

  /**
   * Set the prefer solver version.
   *
   * @param version the given SolverVersion
   */
  public void setVersion(final SolverVersion version) {
    versionSetting = version;
  }

  /**
   * Set the timeout feature for pattern database 78 only.
   *
   * @param timerFlag boolean represent the timeout feature.
   */
  public void setTimerOnPdb78(final boolean timerFlag) {
    this.timerSettingPdb78 = timerFlag;
  }

  /**
   * Set the prefer timeout limit.
   *
   * @param timeoutLimit integer value of the timeout limit
   */
  public void setTimeoutLimit(final int timeoutLimit) {
    this.timeoutLimit = timeoutLimit;
  }

  /**
   * Returns the Solver object with the given heuristic choice.
   *
   * @param heuristic the given HeuristicOptions
   * @return Solver object
   */
  public Solver createSolver(final HeuristicOptions heuristic) {
    SolverTemplate solver = null;
    if (heuristic == HeuristicOptions.MD) {
      solver = new SolverMd();
    }
    if (heuristic == HeuristicOptions.MDLC) {
      solver = new SolverMd(HeuristicOptions.MDLC);
    }
    if (heuristic == HeuristicOptions.WD) {
      solver = new SolverWd(mode);
    }
    if (heuristic == HeuristicOptions.WDMD) {
      solver = new SolverWdMd(mode);
    }
    if (heuristic == HeuristicOptions.PD555) {
      solver = new SolverPdb(PatternOptions.Pattern_555, mode);
    }
    if (heuristic == HeuristicOptions.PD663) {
      solver = new SolverPdb(PatternOptions.Pattern_663, mode);
    }
    if (heuristic == HeuristicOptions.PD78) {
      if (instancePdb78 == null) {
        solver = new SolverPdb(PatternOptions.Pattern_78, mode);
        instancePdb78 = solver;
      } else {
        solver = new SolverPdb(instancePdb78, mode);
      }
    }
    if (solver == null) {
      throw new IllegalArgumentException(this.getClass().getSimpleName() + " " + heuristic);
    }
    setup(solver);
    return solver;
  }

  /**
   * Returns the Solver object with the given heuristic choice and walking distance option.
   *
   * @param heuristic the given HeuristicOptions
   * @param wdFlag optional walking distance
   * @return Solver object
   */
  public Solver createSolver(final HeuristicOptions heuristic, final boolean wdFlag) {
    SolverTemplate solver = null;
    if (!wdFlag) {
      return createSolver(heuristic);
    }
    if (heuristic == HeuristicOptions.PD555) {
      solver = new SolverPdbWd(PatternOptions.Pattern_555, mode);
    }
    if (heuristic == HeuristicOptions.PD663) {
      solver = new SolverPdbWd(PatternOptions.Pattern_663, mode);
    }
    if (heuristic == HeuristicOptions.PD78) {
      if (instancePdb78 == null) {
        solver = new SolverPdb(PatternOptions.Pattern_78, mode);
        instancePdb78 = solver;
      }
      solver = new SolverPdbWd(instancePdb78, mode);
    }
    if (heuristic == HeuristicOptions.MD || heuristic == HeuristicOptions.MDLC) {
      return createSolver(HeuristicOptions.WDMD);
    }
    if (heuristic == HeuristicOptions.WD) {
      return createSolver(heuristic);
    }
    if (heuristic == HeuristicOptions.WDMD) {
      return createSolver(heuristic);
    }
    if (solver == null) {
      throw new IllegalArgumentException(this.getClass().getSimpleName() + " add wd " + heuristic);
    }
    setup(solver);
    return solver;
  }

  /**
   * Returns the Solver object using preset pattern database only, the given heuristic and choice
   * of preset pattern.
   *
   * @param heuristic the given HeuristicOptions must be one of the pattern database
   * @param choice the choice of preset pattern
   * @return Solver object
   */
  public Solver createSolverPdb(final HeuristicOptions heuristic, final int choice) {
    SolverPdb solver = null;
    if (heuristic == HeuristicOptions.PD555) {
      solver = new SolverPdb(PatternOptions.Pattern_555, choice, mode);
    }
    if (heuristic == HeuristicOptions.PD663) {
      solver = new SolverPdb(PatternOptions.Pattern_663, choice, mode);
    }
    if (heuristic == HeuristicOptions.PD78) {
      if (choice == 0) {
        return createSolver(heuristic);
      } else {
        solver = new SolverPdb(PatternOptions.Pattern_78, choice, mode);
      }
    }
    if (solver == null) {
      throw new IllegalArgumentException(this.getClass().getSimpleName() + " pdb " + heuristic);
    }
    setup(solver);
    return solver;
  }

  /**
   * Returns the Solver object using custom pattern database pattern.
   *
   * @param customPattern the given byte array of the custom pattern
   * @return Solver object
   */
  public Solver createSolverPdb(final byte[] customPattern) {
    SolverPdb solver = new SolverPdb(customPattern, mode);
    setup(solver);
    return solver;
  }

  /**
   * Returns SolverMd, internal use only.
   *
   * @return SolverMd object.
   */
  SolverMd internalSolverMd() {
    SolverMd solver = new SolverMd();
    setup(solver);
    return solver;
  }

  /**
   * Returns the SolverPdb78Enh object.
   *
   * @return SolverPdb78Enh object
   */
  public SolverPdb78Enh createSolverPdb78Enh() {
    SolverPdb78Enh solver = new SolverPdb78Enh(mode);
    setup(solver);
    return solver;
  }

  /**
   * Returns the SolverPdb78 object use the data set from the given solver.
   *
   * @param copySovler the given solver using pattern database 78 only
   * @return SolverPdb78 object
   */
  public SolverPdb78 duplicateSolverPdb78(final Solver copySovler) {
    SolverPdb78 solver = new SolverPdb78(copySovler, mode);
    setup(solver);
    return solver;
  }

  /**
   * Returns the SolverPdb78 object use ReferenceRecorder, special for administrative tool.
   *
   * @param recorder the given ReferenceRecorder object
   * @return SolverPdb78 object
   */
  public SolverPdb78 createSolverPdb78(final ReferenceRecorder recorder) {
    SolverPdb78 solver = new SolverPdb78(recorder, mode);
    solver.setReferenceRecorder(recorder);
    solver.setStatusOn(false);
    solver.setTimerOn(false);
    return solver;
  }

  /**
   * Apply all setting to the given solver.
   *
   * @param solver the given SolverTemplate object.
   */
  private void setup(final SolverTemplate solver) {
    solver.setReferenceConnection(refObj);
    solver.setStatusOn(statusSetting);
    solver.setTimeoutLimit(timeoutLimit);
    if (solver.getHeuristic() == HeuristicOptions.PD78) {
      solver.setTimerOn(timerSettingPdb78);
    }
    if (versionSetting.isPrime()) {
      solver.shiftPrime();
    } else {
      solver.shiftOptimum();
    }
  }
}

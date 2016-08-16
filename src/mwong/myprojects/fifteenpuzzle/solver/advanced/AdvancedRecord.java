package mwong.myprojects.fifteenpuzzle.solver.advanced;

import mwong.myprojects.fifteenpuzzle.solver.components.Direction;

class AdvancedRecord {
	private byte moves;
	private Direction[] partialMoves;
	private boolean initialMoves;
	
	AdvancedRecord (byte moves) {
		this.moves = moves;
		initialMoves = false;
		partialMoves = null;
	}
	
	AdvancedRecord (byte moves, Direction[] partialMoves) {
		this.moves = moves;
		initialMoves = true;
		this.partialMoves = partialMoves;
	}

	/**
	 * @return the moves
	 */
	final byte getMoves() {
		return moves;
	}

	/**
	 * @return the partialMoves
	 */
	final Direction[] getPartialMoves() {
		return partialMoves;
	}

	/**
	 * @return the hasInitialMoves
	 */
	final boolean hasInitialMoves() {
		return initialMoves;
	}
	
}
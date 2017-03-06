"""
" SearchEngine is the QThread object to solve the puzzle
" SearchStatus is the Qthread object to update the status to main
" application while the SearchEngine is running.
"
" author Meisze Wong
"        www.linkedin.com/pub/macy-wong/46/550/37b/
"        www.github.com/mwong510ca/15Puzzle_OptimalSolver
"""

# !/usr/bin/env python3

import time
from PyQt5.QtCore import pyqtSignal, QThread
from PyQt5.QtGui import QPixmap


class SearchEngine(QThread):
    def __init__(self):
        super(SearchEngine, self).__init__()
        self._isRunning = False
        self._board = None
        self.solver = None

    def setProperties(self, board, solver):
        self._isRunning = False
        self._board = board
        self._solver = solver

    def run(self):
        if not self._isRunning:
            self._isRunning = True
        self._solver.findOptimalPath(self._board)

        while not self._solver.isSearchTimeout() and self._solver.moves() == -1:
            time.sleep(1)
            continue
        self._isRunning = False

    def getSolver(self):
        return self._solver

    def isRunning(self):
        return self._isRunning


class SearchStatus(QThread):
    statusTime = pyqtSignal(str)
    statusNodes = pyqtSignal(str)
    statusResult = pyqtSignal(str)
    statusMoves = pyqtSignal(str)
    searchRunning = pyqtSignal(bool)
    tileValue = pyqtSignal(int, str)
    tileImage = pyqtSignal(int, QPixmap)
    searchTimeout = pyqtSignal(bool)

    def __init__(self):
        super(SearchStatus, self).__init__()
        self._isRunning = False
        self.search = None
        self.solver = None
        self.tiles = None
        self.zero = None
        self.use_image = None
        self.image_list = None
        self.display_rate = 0

    def setProperties(self, search_engine, original, zero, use_image, image_list, speed):
        self._isRunning = False
        self.search = search_engine
        self.solver = search_engine.getSolver()
        self.tiles = []
        for tile in original:
            self.tiles.append(tile)
        self.zero = zero
        self.use_image = use_image
        self.image_list = image_list
        self.display_rate = speed / 1000.0

    def run(self):
        self.searchRunning.emit(True)

        if not self._isRunning:
            self._isRunning = True

        while self.search.isRunning():
            status_time = "Time: " + str(self.solver.searchTime()) + "s"
            status_nodes = "Nodes: " + str(self.solver.searchNodeCount())
            status_depth = "Depth: " + str(self.solver.searchDepth())
            self.statusTime.emit(status_time)
            self.statusNodes.emit(status_nodes)
            self.statusResult.emit(status_depth)
            time.sleep(0.1)

        status_time = "Time: " + str(self.solver.searchTime()) + "s"
        status_nodes = "Nodes: " + str(self.solver.searchNodeCount())
        self.statusTime.emit(status_time)
        self.statusNodes.emit(status_nodes)
        if self.solver.isSearchTimeout():
            self.searchTimeout.emit(True)
            self.statusResult.emit("Result: timeout")
        else:
            steps = self.solver.moves()
            self.statusResult.emit("Result: " + str(steps) + " moves")
            moves_string = self.solver.solutionQtString()
            self.statusMoves.emit(moves_string)
            directions = self.solver.solution()
            str_dir = []
            for idx in range(1, steps + 1):
                str_dir.append(str(directions[idx]))

            time.sleep(1)

            for idx in range(0, steps):
                pos0 = self.zero
                pos = pos0
                str_direction = str_dir[idx]
                if str_direction == 'RIGHT':
                    pos += 1
                elif str_direction == 'DOWN ':
                    pos += 4
                elif str_direction == 'LEFT ':
                    pos -= 1
                elif str_direction == 'UP   ':
                    pos -= 4
                else:
                    print("ERROR " + str(directions))
                self.statusMoves.emit(moves_string + "\nStep " + str(idx + 1) + " : " + str_direction)
                val = self.tiles[pos]

                if self.use_image:
                    self.tileImage.emit(pos, QPixmap(""))
                    self.tileImage.emit(pos0, QPixmap(self.image_list[val]))
                else:
                    self.tileValue.emit(pos, "")
                    self.tileValue.emit(pos0, str(val))
                self.tiles[pos0] = val
                self.tiles[pos] = 0
                self.zero = pos
                time.sleep(self.display_rate)

        if self.use_image:
            self.tileImage.emit(15, QPixmap(self.image_list[0]))

        time.sleep(0.5)
        self.searchRunning.emit(False)
        self._isRunning = False

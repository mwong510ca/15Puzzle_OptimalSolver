"""
" Stopwatch is the QThread object for app15Puzzle.  It is time counter of
" the game and send it back to the main application.
"
" author Meisze Wong
"        www.linkedin.com/pub/macy-wong/46/550/37b/
"        www.github.com/mwong510ca/15Puzzle_OptimalSolver
"""

# !/usr/bin/env python3

import time
from PyQt5.QtCore import pyqtSignal, QThread


class Stopwatch(QThread):
    gameTime = pyqtSignal(str)

    def __init__(self,):
        super(Stopwatch, self).__init__()
        self._isRunning = True

    def run(self):
        if not self._isRunning:
            self._isRunning = True

        time_increment = 0
        while self._isRunning:
            self.gameTime.emit("Time: " + str(time_increment/10) + "s")
            time.sleep(0.1)
            time_increment += 1

    def stop(self):
        self._isRunning = False

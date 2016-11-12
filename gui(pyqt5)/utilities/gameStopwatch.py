
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

        timeIncerment = 0
        while self._isRunning:
            self.gameTime.emit("Time: " + str(timeIncerment/10) + "s")
            time.sleep(0.1)
            timeIncerment += 1

    def stop(self):
        self._isRunning = False

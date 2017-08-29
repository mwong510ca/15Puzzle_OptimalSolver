"""
" app15PuzzleGameSolver is the GUI application of 15 Puzzle game with solver.  User
" interface created by Qt and implements with pyqt5.  It can play with number or any
" image.  The solver has 7 heuristic functions.  Advanced version of 7-8 pattern
" database eventually will solve any puzzle within 10 seconds.
"
" author Meisze Wong
"        www.linkedin.com/pub/macy-wong/46/550/37b/
"        www.github.com/mwong510ca/15Puzzle_OptimalSolver
"""

# !/usr/bin/env python3

import sys
import os
import time
import subprocess
import socket

from PyQt5.QtWidgets import QApplication, QMainWindow, QFrame, QMessageBox, QFileDialog, QInputDialog
from PyQt5.QtCore import QDir, QFile
from PyQt5.QtGui import QPixmap

from gui.mainWindow import Ui_MainWindow as MainWindow
from utilities.gameStopwatch import Stopwatch
from utilities.solverTools import SearchEngine
from utilities.solverTools import SearchStatus
from utilities.gameImages import TileImages
from py4j.java_gateway import JavaGateway
from py4j.java_gateway import GatewayClient


# Setting
class GameSolver15Puzzle(QMainWindow, MainWindow):
    def __init__(self, gateway):
        super().__init__()
        self._gateway = gateway
        self.setupUi(self)
        time.sleep(2)

        # mainWindow connection settings
        #    menu bar
        self.actionAbout15Puzzle.triggered.connect(self.about_15_puzzle)
        self.actionAboutAuthor.triggered.connect(self.about_author)
        self.actionExit.triggered.connect(self.custom_quit)
        self.actionNumbers.triggered.connect(lambda: self.puzzle_images_change(0))
        self.actionPandaBabies.triggered.connect(lambda: self.puzzle_images_change(1))
        self.actionCustomImage.triggered.connect(self.load_custom_image)
        self.actionTimeoutLimit.triggered.connect(self.change_timeout_limit)
        self.actionAutoMoveSpeed.triggered.connect(self.change_display_rate)
        self.actionInstructions.triggered.connect(self.popup_instructions)
        self.instructions = {1: "Shuffle the puzzle:\n" +
                                "Click 2 tiles and swap each other. Or generate a random puzzle.\n\n" +
                                "When you ready, click 'Play' to start the game.\n\n" +
                                "You may use your image from the menu bar (Puzzle Settings).",
                             2: "Play the game:\n" +
                                "Click the tile (next to space) to move.\n" +
                                "'Go Back' - back to previous move\n" +
                                "'Reset'   - start over the same puzzle, clear moves history.\n\n" +
                                "Stop the game - 'New' (puzzle section)\n" +
                                "Auto solve    - 'Find Optimal Solution' (above)",
                             3: "Auto solver:\n" +
                                "Choose the heuristic function and solver version to display the initial estimate.\n" +
                                "'Find optimal solution' - Reset the puzzle, search for optimal solution up to ",
                             4: " seconds, except pattern 7-8.\n\n" +
                                "It will display solution and apply the moves if solution found."}

        # game setup
        self.puzzleGenerate.clicked.connect(self.puzzle_create)
        self.puzzlePlay.clicked.connect(self.game_start)
        self.puzzleNew.clicked.connect(self.puzzle_reset)
        self.gameBackward.clicked.connect(self.moves_go_back)
        self.gameReset.clicked.connect(self.game_reset)

        self.tile0.clickedLabel.connect(lambda: self.tile_clicked(0))
        self.tile1.clickedLabel.connect(lambda: self.tile_clicked(1))
        self.tile2.clickedLabel.connect(lambda: self.tile_clicked(2))
        self.tile3.clickedLabel.connect(lambda: self.tile_clicked(3))
        self.tile4.clickedLabel.connect(lambda: self.tile_clicked(4))
        self.tile5.clickedLabel.connect(lambda: self.tile_clicked(5))
        self.tile6.clickedLabel.connect(lambda: self.tile_clicked(6))
        self.tile7.clickedLabel.connect(lambda: self.tile_clicked(7))
        self.tile8.clickedLabel.connect(lambda: self.tile_clicked(8))
        self.tile9.clickedLabel.connect(lambda: self.tile_clicked(9))
        self.tile10.clickedLabel.connect(lambda: self.tile_clicked(10))
        self.tile11.clickedLabel.connect(lambda: self.tile_clicked(11))
        self.tile12.clickedLabel.connect(lambda: self.tile_clicked(12))
        self.tile13.clickedLabel.connect(lambda: self.tile_clicked(13))
        self.tile14.clickedLabel.connect(lambda: self.tile_clicked(14))
        self.tile15.clickedLabel.connect(lambda: self.tile_clicked(15))
        self.tiles = [self.tile0, self.tile1, self.tile2, self.tile3,
                      self.tile4, self.tile5, self.tile6, self.tile7,
                      self.tile8, self.tile9, self.tile10, self.tile11,
                      self.tile12, self.tile13, self.tile14, self.tile15]

        self.solverVersion.buttonClicked.connect(self.print_estimate)
        self.solverHeuristic.currentIndexChanged.connect(self.print_estimate)
        self.solverSearchNow.clicked.connect(self.solve_puzzle)

        # initialize local variables
        self.active_puzzle_setup = True
        self.active_game = False
        self.active_solver = False
        self.tile_use_images = False
        self.tile_images_list = []
        self.tile_swap_pairing = -1
        self.game_win = False
        self.game_initial_tiles = None
        self.game_initial_zero = -1
        self.tiles_value = None
        self.zero_pos = -1
        self.tile_movable = None
        self.moves_trace = []
        self.moves_count = 0
        self.standard_level = [True, True, True, True, True, True, True]
        self.advanced_level = [True, True, True, True, True, True, True]
        self.display_rate = 700

        # load image tool
        self.image_tool = TileImages()
        if self.image_tool.hasNumberImages():
            self.tile_images_list = self.image_tool.getNumberImages()
            self.tile_use_images = True
        if not self.image_tool.hasPandaImages():
            self.actionPandaBabies.setEnabled(False)

        # load search engine
        self.search_engine = SearchEngine()
        self.search_status = SearchStatus()
        self.search_status.statusTime.connect(self.solverTime.setText)
        self.search_status.statusNodes.connect(self.solverNodes.setText)
        self.search_status.statusResult.connect(self.solverResult.setText)
        self.search_status.statusMoves.connect(self.informationDetails.setText)
        self.search_status.tileValue.connect(self.tile_value_change)
        self.search_status.tileImage.connect(self.tile_image_change)
        self.search_status.searchTimeout.connect(self.search_timeout)
        self.search_status.searchRunning.connect(self.search_in_process)
        self.solver = None

        self.game_thread = Stopwatch()
        self.game_thread.stop()
        self.game_thread.gameTime.connect(self.gameTime.setText)
        self.puzzle_reset()

    def puzzle_reset(self):
        self.game_thread.stop()
        self.active_puzzle_setup = True
        self.active_game = False
        self.active_solver = False
        self.puzzleLevel.setEnabled(True)
        self.puzzleGenerate.setEnabled(True)
        self.puzzlePlay.setEnabled(True)
        self.puzzleNew.setEnabled(False)
        self.gameCounter.setEnabled(False)
        self.gameTime.setEnabled(False)
        self.gameBackward.setEnabled(False)
        self.gameReset.setEnabled(False)
        self.solverStandard.setEnabled(False)
        self.solverAdvanced.setEnabled(False)
        self.solverHeuristic.setEnabled(False)
        self.solverSearchNow.setEnabled(False)
        self.solverEstimate.setEnabled(False)
        self.solverResult.setEnabled(False)
        self.solverTime.setEnabled(False)
        self.solverNodes.setEnabled(False)
        self.standard_level = [True, True, True, True, True, True, True]
        self.advanced_level = [True, True, True, True, True, True, True]
        self.tile_swap_pairing = -1
        puzzle_level_index = self.puzzleLevel.currentIndex()
        self.puzzleLevel.setCurrentIndex(0)
        self.puzzle_create()
        if puzzle_level_index == 0:
            self.puzzleLevel.setCurrentIndex(1)
        else:
            self.puzzleLevel.setCurrentIndex(puzzle_level_index)
        self.moves_trace = []
        self.moves_count = 0
        self.gameCounter.setText("Count: ")
        self.gameTime.setText("Time: ")
        self.solverEstimate.setText("Estimate: ")
        self.solverTime.setText("Time:")
        self.solverResult.setText("Result:")
        self.solverNodes.setText("Nodes:")
        self.refresh_msg(self.instructions[1])

    def puzzle_create(self):
        board_types = {0: self.puzzle_goal,
                       1: self.puzzle_random,
                       2: self.puzzle_easy,
                       3: self.puzzle_moderate,
                       4: self.puzzle_hard
                       }
        option = self.puzzleLevel.currentIndex()
        board = board_types[option]()
        self.tiles_value = bytearray(board.getTiles())
        self.zero_pos = board.getZero1d()
        self.puzzle_build()
        self.puzzle_movable()

    def puzzle_goal(self):
        board = self._gateway.entry_point.getGoal()
        return board

    def puzzle_random(self):
        board = self._gateway.entry_point.getRandom()
        return board

    def puzzle_easy(self):
        board = self._gateway.entry_point.getEasy()
        return board

    def puzzle_moderate(self):
        board = self._gateway.entry_point.getModerate()
        return board

    def puzzle_hard(self):
        board = self._gateway.entry_point.getHard()
        return board

    def puzzle_build(self):
        if self.tile_use_images:
            for idx in range(16):
                self.tiles[idx].setFrameStyle(QFrame.NoFrame | QFrame.Plain)
                self.tiles[idx].setPixmap(QPixmap(self.tile_images_list[self.tiles_value[idx]]))
            if self.active_puzzle_setup:
                self.tiles[self.zero_pos].setPixmap(QPixmap(self.tile_images_list[0]))
            elif not self.game_win:
                self.tiles[self.zero_pos].setPixmap(QPixmap(""))
        else:
            for idx in range(16):
                self.tiles[idx].setFrameStyle(QFrame.Box | QFrame.Plain)
                self.tiles[idx].setText(str(self.tiles_value[idx]))
            self.tiles[self.zero_pos].setText("")

    def puzzle_movable(self):
        self.tile_movable = [False, False, False, False, False, False, False, False,
                             False, False, False, False, False, False, False, False]
        if self.zero_pos % 4 < 3:
            self.tile_movable[self.zero_pos + 1] = True
        if self.zero_pos / 4 < 3:
            self.tile_movable[self.zero_pos + 4] = True
        if self.zero_pos % 4 > 0:
            self.tile_movable[self.zero_pos - 1] = True
        if self.zero_pos / 4 >= 1:
            self.tile_movable[self.zero_pos - 4] = True

    def tile_clicked(self, pos):
        if self.active_puzzle_setup:
            if self.tile_swap_pairing == -1:
                self.tile_swap_pairing = pos
                if self.tiles_value[pos] == 0:
                    self.puzzleSetup.setText("Click a tiles to swap with space")
                else:
                    self.puzzleSetup.setText("Click a tiles to swap with " + str(self.tiles_value[pos]))
            elif self.tile_swap_pairing == pos:
                self.tile_swap_pairing = -1
                self.puzzleSetup.setText("Click 2 tiles to swap or generate new puzzle:")
            else:
                self.tile_swap(self.tile_swap_pairing, pos)
                self.puzzleSetup.setText("Click 2 tiles to swap or generate new puzzle:")
        elif self.active_game and not self.game_win:
            if self.tile_movable[pos]:
                if self.moves_count == 0:
                    self.game_thread.start()
                self.moves_trace.append(self.zero_pos)
                self.moves_count += 1
                self.gameCounter.setText("Count: " + str(self.moves_count))
                self.tile_swap(self.zero_pos, pos)
                if self.is_goal():
                    self.game_win = True
                    self.game_thread.stop()
                    self.active_game = False
                    self.print_trace()
                    if self.tile_use_images:
                        self.tiles[self.zero_pos].setPixmap(QPixmap(self.tile_images_list[0]))
                    self.gameCounter.setText("Solved: " + str(self.moves_count))
                    self.gameCounter.setEnabled(False)
                    self.gameTime.setEnabled(False)
                    self.gameBackward.setEnabled(False)
                    QMessageBox.information(None, 'Message',
                                            "Congratulations!\n\nPuzzle solved in " + str(self.moves_count) + " moves.",
                                            QMessageBox.Close, QMessageBox.Close)
                else:
                    self.print_trace()
                    self.gameBackward.setEnabled(True)

    def tile_swap(self, pos1, pos2):
        val1 = self.tiles_value[pos1]
        val2 = self.tiles_value[pos2]
        del self.tiles_value[pos1: (pos1 + 1)]
        self.tiles_value.insert(pos1, val2)
        del self.tiles_value[pos2: (pos2 + 1)]
        self.tiles_value.insert(pos2, val1)
        if self.tiles_value[pos1] == 0:
            self.zero_pos = pos1
            self.puzzle_movable()
        elif self.tiles_value[pos2] == 0:
            self.zero_pos = pos2
            self.puzzle_movable()

        if self.tile_use_images:
            self.tiles[pos1].setPixmap(QPixmap(self.tile_images_list[self.tiles_value[pos1]]))
            self.tiles[pos2].setPixmap(QPixmap(self.tile_images_list[self.tiles_value[pos2]]))
            if self.active_game:
                self.tiles[pos2].setPixmap(QPixmap(""))
        else:
            self.tiles[pos1].setText(str(self.tiles_value[pos1]))
            self.tiles[pos2].setText(str(self.tiles_value[pos2]))
            if self.tiles_value[pos1] == 0:
                self.tiles[pos1].setText("")
            elif self.tiles_value[pos2] == 0:
                self.tiles[pos2].setText("")
        self.tile_swap_pairing = -1

    def game_start(self):
        board = self._gateway.entry_point.getBoard(self.tiles_value)
        if board.isSolvable():
            if self.is_goal():
                QMessageBox.information(None, 'Message',
                                        'Puzzle is the goal state.\n\nPlease shuffle the puzzle.',
                                        QMessageBox.Close, QMessageBox.Close)
            else:
                self.active_puzzle_setup = False
                self.active_game = True
                self.active_solver = True
                self.puzzleLevel.setEnabled(False)
                self.puzzleGenerate.setEnabled(False)
                self.puzzlePlay.setEnabled(False)
                self.puzzleNew.setEnabled(True)
                self.gameCounter.setEnabled(True)
                self.gameTime.setEnabled(True)
                self.gameBackward.setEnabled(False)
                self.gameReset.setEnabled(True)
                self.solverStandard.setEnabled(True)
                self.solverAdvanced.setEnabled(True)
                self.solverHeuristic.setEnabled(True)
                self.solverSearchNow.setEnabled(True)
                self.solverEstimate.setEnabled(True)
                self.tile_swap_pairing = -1
                self.game_win = False

                self.game_initial_tiles = []
                for idx in range(16):
                    self.game_initial_tiles.append(self.tiles_value[idx])
                self.game_initial_zero = self.zero_pos
                self.moves_trace = []
                self.moves_count = 0
                self.print_estimate()
                self.refresh_msg(self.instructions[2])
                self.gameCounter.setText("Count: " + str(self.moves_count))
                if self.tile_use_images:
                    self.tiles[self.zero_pos].setPixmap(QPixmap(""))
        else:
            QMessageBox.information(None, 'Message',
                                    'Puzzle is not solvable.\n\nPlease swap an adjacent pair or\n"generate a random '
                                    'board.',
                                    QMessageBox.Close, QMessageBox.Close)

    def is_goal(self):
        for idx in range(15):
            if self.tiles_value[idx] != idx + 1:
                return False
        return True

    def print_trace(self):
        msg_string = ""
        if self.game_win:
            msg_string += "Puzzle solved.\n'New' starts a new game or try auto solver above.\n"
        if self.moves_count > 0:
            msg_string += "Trace:\n"
            zero_trace = self.zero_pos
            index = self.moves_count
            while index > 0:
                backward_move = self.moves_trace[index - 1]
                if backward_move + 1 == zero_trace:
                    msg_string += str(index) + ":R "
                elif backward_move + 4 == zero_trace:
                    msg_string += str(index) + ":D "
                elif backward_move - 1 == zero_trace:
                    msg_string += str(index) + ":L "
                elif backward_move - 4 == zero_trace:
                    msg_string += str(index) + ":U "
                zero_trace = backward_move
                index -= 1
        else:
            msg_string = self.instructions[2]
        self.refresh_msg(msg_string)

    def moves_go_back(self):
        if self.moves_count > 0:
            self.moves_count -= 1
            pos = self.moves_trace[self.moves_count]
            del self.moves_trace[self.moves_count: self.moves_count + 1]
            self.gameCounter.setText("Count: " + str(self.moves_count))
            self.tile_swap(self.zero_pos, pos)
            if self.moves_count == 0:
                self.gameBackward.setEnabled(False)
                self.game_thread.stop()
                time.sleep(0.5)
                self.gameTime.setText("Time: ")
            self.print_trace()

    def game_reset(self):
        self.game_thread.stop()
        time.sleep(0.5)
        self.tiles_value = bytearray(self.game_initial_tiles)
        self.zero_pos = self.game_initial_zero
        self.puzzle_build()
        self.puzzle_movable()
        self.game_start()
        self.moves_trace = []
        self.moves_count = 0
        self.gameCounter.setText("Count: " + str(self.moves_count))
        self.gameTime.setText("Time: ")

    def get_solver(self):
        heuristic_choice = {0: self._gateway.entry_point.getSolver_0,
                            1: self._gateway.entry_point.getSolver_1,
                            2: self._gateway.entry_point.getSolver_2,
                            3: self._gateway.entry_point.getSolver_3,
                            4: self._gateway.entry_point.getSolver_4,
                            5: self._gateway.entry_point.getSolver_5,
                            6: self._gateway.entry_point.getSolver_6
                            }
        option = self.solverHeuristic.currentIndex()
        solver = heuristic_choice[option]()
        if option == 5:
            solver.linearConflictSwitch(True)
        elif option == 6:
            solver.linearConflictSwitch(False)
        if self.solverStandard.isChecked():
            solver.versionSwitch(False)
        elif self.solverAdvanced.isChecked():
            solver.versionSwitch(True)
        return solver

    def print_estimate(self):
        if self.solverStandard.isChecked():
            for idx in range(7):
                self.solverHeuristic.model().item(idx).setEnabled(self.standard_level[idx])
        else:
            for idx in range(7):
                self.solverHeuristic.model().item(idx).setEnabled(self.advanced_level[idx])
        index = self.solverHeuristic.currentIndex()
        if self.active_solver and 0 <= index <= 6:
            self.solver = self.get_solver()
            tiles = bytearray(self.game_initial_tiles)
            board = self._gateway.entry_point.getBoard(tiles)
            estimate = str(self.solver.heuristic(board))
            self.solverEstimate.setText("Estimate: " + estimate)
            self.refresh_msg(self.instructions[3] + str(self._gateway.entry_point.getTimeoutLimit())
                             + self.instructions[4])
        if not self.solverStandard.isChecked():
            if not self.standard_level[index]:
                self.solverStandard.setEnabled(False)
            else:
                self.solverStandard.setEnabled(True)

    def search_in_process(self, active):
        if active:
            self.solverTime.setEnabled(True)
            self.solverNodes.setEnabled(True)
            self.solverResult.setEnabled(True)
            self.puzzleNew.setEnabled(False)
            self.gameReset.setEnabled(False)
            self.solverStandard.setEnabled(False)
            self.solverAdvanced.setEnabled(False)
            self.solverHeuristic.setEnabled(False)
            self.solverSearchNow.setEnabled(False)
            self.actionNumbers.setEnabled(False)
            self.actionPandaBabies.setEnabled(False)
            self.actionCustomImage.setEnabled(False)
            self.actionTimeoutLimit.setEnabled(False)
            self.actionAutoMoveSpeed.setEnabled(False)
        else:
            self.solverTime.setEnabled(False)
            self.solverNodes.setEnabled(False)
            self.solverResult.setEnabled(False)
            self.puzzleNew.setEnabled(True)
            self.gameReset.setEnabled(True)
            self.solverStandard.setEnabled(True)
            self.solverAdvanced.setEnabled(True)
            self.solverHeuristic.setEnabled(True)
            self.solverSearchNow.setEnabled(True)
            self.actionNumbers.setEnabled(True)
            self.actionPandaBabies.setEnabled(self.image_tool.hasPandaImages())
            self.actionCustomImage.setEnabled(True)
            self.actionTimeoutLimit.setEnabled(True)
            self.actionAutoMoveSpeed.setEnabled(True)

    def tile_value_change(self, pos, value):
        self.tiles[pos].setText(value)

    def tile_image_change(self, pos, image):
        self.tiles[pos].setPixmap(image)

    def solve_puzzle(self):
        self.game_thread.stop()
        if self.solverHeuristic.currentIndex() < 0:
            print("ERROR: No solver")
            return

        self.active_game = False
        self.gameCounter.setEnabled(False)
        self.gameTime.setEnabled(False)
        self.gameBackward.setEnabled(False)
        board = self._gateway.entry_point.getGoal()
        self.tiles_value = bytearray(board.getTiles())
        self.zero_pos = board.getZero1d()
        self.game_win = True
        if self.tile_use_images:
            for idx in range(0, 16):
                self.tiles[idx].setPixmap(QPixmap(self.tile_images_list[self.game_initial_tiles[idx]]))
            self.tiles[self.game_initial_zero].setPixmap(QPixmap(""))
        else:
            for idx in range(0, 16):
                self.tiles[idx].setText(str(self.game_initial_tiles[idx]))
            self.tiles[self.game_initial_zero].setText("")

        solver = self.get_solver()
        board = self._gateway.entry_point.getBoard(bytearray(self.game_initial_tiles))

        self.search_engine.setProperties(board, solver)
        self.search_status.setProperties(self.search_engine, self.game_initial_tiles, self.game_initial_zero,
                                         self.tile_use_images, self.tile_images_list, self.display_rate)
        self.search_engine.start()
        self.search_status.start()

    def search_timeout(self):
        self.tiles_value = bytearray(self.game_initial_tiles)
        self.zero_pos = self.game_initial_zero
        self.game_win = False
        index = self.solverHeuristic.currentIndex()
        if self.solverStandard.isChecked():
            for idx in range(index, 7):
                self.solverHeuristic.model().item(idx).setEnabled(False)
                self.standard_level[idx] = False
        else:
            for idx in range(index, 7):
                self.solverHeuristic.model().item(idx).setEnabled(False)
                self.standard_level[idx] = False
                self.advanced_level[idx] = False
        self.solverHeuristic.setCurrentIndex(index - 1)

    def refresh_msg(self, custom_msg):
        self.informationDetails.setText(custom_msg)

    # ---------------------------------------

    def custom_quit(self):
        if QMessageBox.question(None, '', 'Are you sure to quit?',
                                QMessageBox.Yes | QMessageBox.No, QMessageBox.No) == QMessageBox.Yes:
            QApplication.quit()

    def puzzle_images_change(self, option):
        if option == 0 and self.image_tool.hasNumberImages():
            self.tile_use_images = True
            self.tile_images_list = self.image_tool.getNumberImages()
        elif option == 1:
            self.tile_use_images = True
            self.tile_images_list = self.image_tool.getPandaImages()
        else:
            self.tile_use_images = False
        self.puzzle_build()

    def load_custom_image(self):
        if not QDir(self.image_tool.getFolderName()).exists():
            return
        filename, _ = QFileDialog.getOpenFileName(self, 'Open File', os.getenv('HOME'))
        if filename == "":
            return
        if not QFile.exists(filename):
            QMessageBox.information(None, 'Error message',
                                    'System error, unable to locate file.',
                                    QMessageBox.Close, QMessageBox.Close)
        else:
            try:
                loaded_image = self.image_tool.loadCustomImages(filename)
                if loaded_image is None:
                    QMessageBox.information(None, 'Message',
                                            'Image file too small, minimum requirement 16 x 16.',
                                            QMessageBox.Close, QMessageBox.Close)
                    return
                self.tile_use_images = True
                self.tile_images_list = loaded_image
                self.puzzle_build()

            except IOError:
                QMessageBox.information(None, 'Message',
                                        'This is not an image file.',
                                        QMessageBox.Close, QMessageBox.Close)

    def change_timeout_limit(self):
        old_limit = self._gateway.entry_point.getTimeoutLimit()
        limit, ok_pressed = QInputDialog.getInt(None, "Change Timeout Limit",
                                                "Please enter timeout limit from 5 to 60 seconds:",
                                                old_limit, 5, 60, 1)
        if ok_pressed and old_limit != limit:
            self._gateway.entry_point.setTimeoutLimit(limit)
            if not self.active_puzzle_setup and not self.active_game:
                self.refresh_msg(self.instructions[3] + str(self._gateway.entry_point.getTimeoutLimit())
                                 + self.instructions[4])
                self.standard_level = [True, True, True, True, True, True, True]
                self.advanced_level = [True, True, True, True, True, True, True]
                if self.solverStandard.isChecked():
                    for idx in range(7):
                        self.solverHeuristic.model().item(idx).setEnabled(self.standard_level[idx])
                else:
                    for idx in range(7):
                        self.solverHeuristic.model().item(idx).setEnabled(self.advanced_level[idx])

    def change_display_rate(self):
        speed, ok_pressed = QInputDialog.getInt(None, "Change Auto Move Speed",
                                                "Please enter speed from 100 (0.1s) to 2500 (2.5s) milliseconds:",
                                                self.display_rate, 100, 2500, 100)
        if ok_pressed:
            self.display_rate = speed

    def popup_instructions(self):
        QMessageBox.information(None, '15 puzzle: How to play', 'You may play with numbers or any images\n\n' +
                                '1. Shuffle the puzzle to start the game.\n' +
                                '2. Try to solve the puzzle yourself,\n' +
                                '3. or let the system solve for you.\n\n' +
                                'Have fun!!!',
                                QMessageBox.Close, QMessageBox.Close)

    def about_15_puzzle(self):
        QMessageBox.information(None, 'About 15 puzzle game and solver', 'A 2-in-1 15 puzzle game and solver.\n' +
                                'You may play with any custom board or 3 difficulty level to choose from.\n' +
                                'You display the puzzle in 15 numbers or any images you like.\n' +
                                'Also, you can let the computer solve the puzzle for you at anytime.\n\n' +
                                'Have fun and enjoy!',
                                QMessageBox.Close, QMessageBox.Close)

    def about_author(self):
        QMessageBox.information(None, 'About Author', 'Author: Meisze Wong\n' +
                                'www.linkedin.com/pub/macy-wong/46/550/37b/\n\n' +
                                'view source code:\nhttps://github.com/mwong510ca/15Puzzle_OptimalSolver',
                                QMessageBox.Close, QMessageBox.Close)


if __name__ == "__main__":
    host = '127.0.0.1'
    port_number = 25334
    while port_number < 25335:
        s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        s.bind(('', 0))
        port_number = s.getsockname()[1]
        s.close()
    try:
        p = subprocess.Popen(['java', '-jar', 'FifteenPuzzleGateway.jar', str(port_number)])
        count = 0;
        while count < 15:
            time.sleep(1)
            gateway_server = JavaGateway(GatewayClient(address=host, port=port_number))
            count += 1
            connected = True
            try:
                gateway_server.entry_point.isConnected()
            except:
                connected = False
            if connected:
                break
            else:
                print("Connecting to server ... " + str(count) + " seconds.  Please wait.")
        if not connected:
            print("Connection time out over " + str(count) + " seconds")
            gateway_server.shutdown()
            p.kill()
            sys.exit()
        else:
            gateway_server.entry_point.getGoal()
            app = QApplication(sys.argv)
            window = GameSolver15Puzzle(gateway_server)
            window.show()
            while app.exec_() > 0:
                time.sleep(1)
            gateway_server.shutdown()
            sys.exit()
    except:
        gateway_server.shutdown()
        p.kill()
        sys.exit()


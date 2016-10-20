# -*- coding: utf-8 -*-
# !/usr/bin/env python3

import sys
import os
import math
import time
import subprocess

from PyQt5 import QtCore, QtGui, QtWidgets
from PyQt5.QtWidgets import QApplication, QMainWindow, QFrame, QMessageBox, \
        QWidget, QFileDialog, QInputDialog
from PyQt5.QtCore import QDir, QFile, QObject, pyqtSignal, QThread, QRect
from PyQt5.QtGui import QPixmap
from PIL import Image

from gui.mainWindow import Ui_MainWindow as MainWindow
from utilities.gameStopwatch import Stopwatch
from utilities.solverTools import SearchEngine
from utilities.solverTools import SearchStatus
from py4j.java_gateway import JavaGateway

# Globals
IMG_FOLDER_NAME = "images"
IMG_SIZE = 128
        
# Setting
class GameSolver15Puzzle(QMainWindow, MainWindow):
    def __init__(self, gateway):
        super().__init__()
        self._gateway = gateway
        self._move_speed = 700
        time.sleep(2)
        self.setupUi(self)
        self.default_setting()

    def default_setting(self):
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
        self.tiles = [self.tile0,  self.tile1,  self.tile2,  self.tile3,
                      self.tile4,  self.tile5,  self.tile6,  self.tile7, 
                      self.tile8,  self.tile9,  self.tile10, self.tile11, 
                      self.tile12, self.tile13, self.tile14, self.tile15]
        self.load_stock_images()
        self.load_search_engine()
        
        self.instructions = {1 : "Shuffle the puzzle:\n" + 
                    "Click 2 tiles and swap each other. Or generate a random puzzle.\n\n" +
                    "When you ready, click 'Play' to start the game.\n\n" +
                    "You may use your image from the munu bar (Puzzle Settings).",
                    2 : "Play the game:\n" +
                    "Click the tile (next to space) to move.\n" +
                    "'Go Back' - back to previous move\n" +
                    "'Reset'   - start over the same puzzle, clear moves history.\n\n" +
                    "Stop the game - 'New' (puzzle section)\n" +
                    "Auto solve    - 'Find Optimal Solution' (above)",
                    3 : "Auto solver:\n" +
                    "Choose the heuristic function and solver version to display the initial estimate.\n" +
                    "'Find optimal solution' - Reset the puzzle, search for optimal solution up to ",
                    4 : " seconds, except patterh 7-8.\n\n" +
                    "It will display solution and apply the moves if solution found."}

        self.game_thread = Stopwatch()
        self.game_thread.stop()
        self.game_thread.gameTime.connect(self.gameTime.setText) 
        self.puzzle_reset()
        self.puzzleGenerate.clicked.connect(self.puzzle_create)        
        self.puzzlePlay.clicked.connect(self.game_start)
        self.puzzleNew.clicked.connect(self.puzzle_reset)
        self.gameBackward.clicked.connect(self.moves_go_back)
        self.gameReset.clicked.connect(self.game_reset)
        self.solverVersion.buttonClicked.connect(self.print_estimate)      
        self.solverHeuristic.currentIndexChanged.connect(self.print_estimate)      
        self.solverSearchNow.clicked.connect(self.solve_puzzle)
        
        self.actionExit.triggered.connect(self.custom_quit)
        self.actionNumbers.triggered.connect(lambda: self.puzzle_images_change(0))
        self.actionPandaBabies.triggered.connect(lambda: self.puzzle_images_change(1))
        self.actionCustomImage.triggered.connect(self.load_custom_image)
        self.actionTimeoutLimit.triggered.connect(self.change_timeout_limit)
        self.actionAutoMoveSpeed.triggered.connect(self.change_move_speed)
        self.actionInstructions.triggered.connect(self.popup_instructions)
        self.actionAbout.triggered.connect(self.about_author)
    
    def load_stock_images(self):
        self.tile_images = False 
        self.images_list = []
        self.images_in_use = []
        self.images_number = False
        self.images_panda = False
        self.images_custom = False
        self.images_folder = IMG_FOLDER_NAME + QDir.separator()

        dir = QtCore.QDir()
        if dir.exists(IMG_FOLDER_NAME):    
            self.images_number = True
            filelist = []
            for idx in range(16):
                filepath = self.images_folder + "number_" + str(idx) + ".png"
                if not QFile(filepath).exists():
                    self.images_number = False
                    filelist = []
                    break
                try:
                    img = Image.open(filepath)
                    imageWidth, imageHeight = img.size # Get image dimensions
                    if imageWidth != IMG_SIZE or imageHeight != IMG_SIZE:
                        resize_slice = Image.open(filepath)
                        resize_slice = resize_slice.resize((IMG_SIZE, IMG_SIZE), Image.ANTIALIAS)
                        resize_slice.save(os.path.join(os.getcwd(), filepath))
                    filelist.append(filepath)
                except IOError:
                    self.images_number = False
                    filelist = []
                    break
            self.images_list.append(filelist)
            if self.images_number:
                self.tile_images = True
                self.images_in_use = []
                for img in self.images_list[0]:
                    self.images_in_use.append(img)

            self.images_panda = True
            filelist = []
            for idx in range(16):
                filepath = self.images_folder + "panda_" + str(idx) + ".png"
                if not QFile(filepath).exists():
                    self.images_panda = False
                    filelist = []
                    break
                try:
                    img = Image.open(filepath)
                    imageWidth, imageHeight = img.size # Get image dimensions
                    if imageWidth != IMG_SIZE or imageHeight != IMG_SIZE:
                        resize_slice = Image.open(filepath)
                        resize_slice = resize_slice.resize((IMG_SIZE, IMG_SIZE), Image.ANTIALIAS)
                        resize_slice.save(os.path.join(os.getcwd(), filepath))
                    filelist.append(filepath)
                except IOError:
                    self.images_panda = False
                    filelist = []
                    break
            self.images_list.append(filelist)
            if not self.images_panda:
                self.actionPandaBabies.setEnabled(False)
        else:
            dir.mkpath(IMG_FOLDER_NAME)
            self.actionPandaBabies.setEnabled(False)

    def load_search_engine(self):
        self.search_engine = SearchEngine()
        self.search_status = SearchStatus(self.images_panda)
        self.search_status.statusTime.connect(self.solverTime.setText)
        self.search_status.statusNodes.connect(self.solverNodes.setText)
        self.search_status.statusResult.connect(self.solverResult.setText)
        self.search_status.statusMoves.connect(self.informationDetails.setText)

        self.search_status.widgetAccessable.connect(self.puzzleNew.setEnabled)
        self.search_status.widgetAccessable.connect(self.solverStandard.setEnabled)
        self.search_status.widgetAccessable.connect(self.solverAdvanced.setEnabled)
        self.search_status.widgetAccessable.connect(self.solverHeuristic.setEnabled)
        self.search_status.widgetAccessable.connect(self.solverSearchNow.setEnabled)
        self.search_status.widgetAccessable.connect(self.actionNumbers.setEnabled)
        self.search_status.widgetAccessablePanda.connect(self.actionPandaBabies.setEnabled)
        self.search_status.widgetAccessable.connect(self.actionCustomImage.setEnabled)
        self.search_status.widgetAccessable.connect(self.actionTimeoutLimit.setEnabled)
        self.search_status.widgetAccessable.connect(self.actionAutoMoveSpeed.setEnabled)
        self.search_status.widgetInaccessable.connect(self.solverTime.setEnabled)
        self.search_status.widgetInaccessable.connect(self.solverNodes.setEnabled)
        self.search_status.widgetInaccessable.connect(self.solverResult.setEnabled)

        self.search_status.val0.connect(self.tile0.setText)
        self.search_status.val1.connect(self.tile1.setText)
        self.search_status.val2.connect(self.tile2.setText)
        self.search_status.val3.connect(self.tile3.setText)
        self.search_status.val4.connect(self.tile4.setText)
        self.search_status.val5.connect(self.tile5.setText)
        self.search_status.val6.connect(self.tile6.setText)
        self.search_status.val7.connect(self.tile7.setText)
        self.search_status.val8.connect(self.tile8.setText)
        self.search_status.val9.connect(self.tile9.setText)
        self.search_status.val10.connect(self.tile10.setText)
        self.search_status.val11.connect(self.tile11.setText)
        self.search_status.val12.connect(self.tile12.setText)
        self.search_status.val13.connect(self.tile13.setText)
        self.search_status.val14.connect(self.tile14.setText)
        self.search_status.val15.connect(self.tile15.setText)

        self.search_status.pic0.connect(self.tile0.setPixmap)
        self.search_status.pic1.connect(self.tile1.setPixmap)
        self.search_status.pic2.connect(self.tile2.setPixmap)
        self.search_status.pic3.connect(self.tile3.setPixmap)
        self.search_status.pic4.connect(self.tile4.setPixmap)
        self.search_status.pic5.connect(self.tile5.setPixmap)
        self.search_status.pic6.connect(self.tile6.setPixmap)
        self.search_status.pic7.connect(self.tile7.setPixmap)
        self.search_status.pic8.connect(self.tile8.setPixmap)
        self.search_status.pic9.connect(self.tile9.setPixmap)
        self.search_status.pic10.connect(self.tile10.setPixmap)
        self.search_status.pic11.connect(self.tile11.setPixmap)
        self.search_status.pic12.connect(self.tile12.setPixmap)
        self.search_status.pic13.connect(self.tile13.setPixmap)
        self.search_status.pic14.connect(self.tile14.setPixmap)
        self.search_status.pic15.connect(self.tile15.setPixmap)
        self.search_status.searchTimeout.connect(lambda: self.search_timeout())

    def puzzle_reset(self):
        self.game_thread.stop()
        self.zeroPos = 15
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
        self.tile_pairing = -1
        puzzle_level_index = self.puzzleLevel.currentIndex()
        self.puzzleLevel.setCurrentIndex(0)
        self.puzzle_create()
        if puzzle_level_index == 0:
            self.puzzleLevel.setCurrentIndex(1)
        else:
            self.puzzleLevel.setCurrentIndex(puzzle_level_index)
        self.moves_trace = []
        self.moves_count = 0
        _translate = QtCore.QCoreApplication.translate
        self.gameCounter.setText(_translate("MainWindow", "Count: "))
        self.gameTime.setText(_translate("MainWindow", "Time: "))
        self.solverEstimate.setText(_translate("MainWindow", "Estimate: "))
        self.solverTime.setText(_translate("MainWindow", "Time:"))
        self.solverResult.setText(_translate("MainWindow", "Result:"))
        self.solverNodes.setText(_translate("MainWindow", "Nodes:"))
        self.refresh_msg(self.instructions[1])
        self.solverHidden.setText(_translate("MainWindow", ""))
        #for index in range(0, 7):
        #   self.solverHeuristic.model().item(index).setEnabled(True)
        
    def puzzle_create(self):
        board_types = {0 : self.puzzle_goal,
           1 : self.puzzle_random,
           2 : self.puzzle_easy,
           3 : self.puzzle_moderate,
           4 : self.puzzle_hard
        }
        option = 0
        option = self.puzzleLevel.currentIndex()
        board = board_types[option]()
        self.tiles_value = bytearray(board.getTiles())
        self.zero_pos = board.getZero1d()
        self.puzzle_build()
        self.puzzle_moves()     

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
        if self.tile_images:
            for idx in range(16):
                self.tiles[idx].setFrameStyle(QFrame.NoFrame | QFrame.Plain)
                self.tiles[idx].setPixmap(QPixmap(self.images_in_use[self.tiles_value[idx]]))
            if self.active_puzzle_setup:
                self.tiles[self.zero_pos].setPixmap(QPixmap(self.images_in_use[0]))
            elif not self.game_win:
                self.tiles[self.zero_pos].setPixmap(QPixmap(""))  
        else:
            _translate = QtCore.QCoreApplication.translate
            for idx in range(16):
                self.tiles[idx].setFrameStyle(QFrame.Box | QFrame.Plain)
                self.tiles[idx].setText(_translate("MainWindow", str(self.tiles_value[idx])))
            self.tiles[self.zero_pos].setText(_translate("MainWindow", ""))
        
    def puzzle_images_change(self, option):
        if self.images_number or option == 1:
            self.tile_images = True
            self.images_in_use = []
            for img in self.images_list[option]:
                self.images_in_use.append(img)
        else:
            self.tile_images = False
        self.puzzle_build()

    def puzzle_moves(self):
        self.zero_moves = [False, False, False, False, False, False, False, False, 
                          False, False, False, False, False, False, False, False]
        if (self.zero_pos % 4 < 3):
            self.zero_moves[self.zero_pos + 1] = True
        if (self.zero_pos / 4 < 3):
            self.zero_moves[self.zero_pos + 4] = True
        if (self.zero_pos % 4 > 0):
            self.zero_moves[self.zero_pos - 1] = True
        if (self.zero_pos / 4 >= 1):
            self.zero_moves[self.zero_pos - 4] = True

    def tile_clicked(self, pos):
        _translate = QtCore.QCoreApplication.translate
        if self.active_puzzle_setup:
            if self.tile_pairing == -1:
                self.tile_pairing = pos
                if self.tiles_value[pos] == 0:
                    self.puzzleSetup.setText(_translate("MainWindow", "Click a tiles to swap with space"))
                else:
                    self.puzzleSetup.setText(_translate("MainWindow", "Click a tiles to swap with " + str(self.tiles_value[pos])))
            elif self.tile_pairing == pos:
                self.tile_pairing = -1
                self.puzzleSetup.setText(_translate("MainWindow", "Click 2 tiles to swap or generate new puzzle:"))
            else:
                self.tile_swap(self.tile_pairing, pos)
                self.puzzleSetup.setText(_translate("MainWindow", "Click 2 tiles to swap or generate new puzzle:"))                
        elif self.active_game and not self.game_win:
            if self.zero_moves[pos]:
                if self.moves_count == 0:
                    self.game_thread.start()
                _translate = QtCore.QCoreApplication.translate
                self.moves_trace.append(self.zero_pos)
                self.moves_count = self.moves_count + 1
                self.gameCounter.setText(_translate("MainWindow", "Count: " + str(self.moves_count)))
                self.tile_swap(self.zero_pos, pos)
                if self.is_goal():
                    self.game_win = True
                    self.game_thread.stop()
                    self.active_game = False
                    self.print_trace()
                    if self.tile_images:
                        self.tiles[self.zero_pos].setPixmap(QPixmap(self.images_in_use[0]))
                    self.gameCounter.setText(_translate("MainWindow", "Solved: " + str(self.moves_count)))
                    self.gameCounter.setEnabled(False)
                    self.gameTime.setEnabled(False)
                    self.gameBackward.setEnabled(False)
                    self.gameReset.setEnabled(False)
                    QMessageBox.information(None, 'Message', 
                    "Congratulations!\n\nPuzzle solved in " + str(self.moves_count) + " moves.",
                    QMessageBox.Close, QMessageBox.Close)
                else:
                    self.print_trace()
                    self.gameBackward.setEnabled(True) 

    def tile_swap(self, pos1, pos2):
        val1 = self.tiles_value[pos1]
        val2 = self.tiles_value[pos2]
        del self.tiles_value[pos1 : (pos1 + 1)]
        self.tiles_value.insert(pos1, val2)
        del self.tiles_value[pos2 : (pos2 + 1)]
        self.tiles_value.insert(pos2, val1)
        if self.tiles_value[pos1] == 0:
            self.zero_pos = pos1
            self.puzzle_moves()
        elif self.tiles_value[pos2] == 0:
            self.zero_pos = pos2
            self.puzzle_moves()

        if self.tile_images:
            self.tiles[pos1].setPixmap(QPixmap(self.images_in_use[self.tiles_value[pos1]]))
            self.tiles[pos2].setPixmap(QPixmap(self.images_in_use[self.tiles_value[pos2]]))
            if self.active_game:
                self.tiles[pos2].setPixmap(QPixmap(""))
        else:
            _translate = QtCore.QCoreApplication.translate
            self.tiles[pos1].setText(_translate("MainWindow", str(self.tiles_value[pos1])))
            self.tiles[pos2].setText(_translate("MainWindow", str(self.tiles_value[pos2])))
            if self.tiles_value[pos1] == 0:
                self.tiles[pos1].setText(_translate("MainWindow", ""))
            elif self.tiles_value[pos2] == 0:
                self.tiles[pos2].setText(_translate("MainWindow", ""))
        self.tile_pairing = -1

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
                self.tile_pairing = -1
                self.game_win = False
                
                self.game_tiles = []
                for idx in range(16):
                    self.game_tiles.append(self.tiles_value[idx])
                self.game_zero = self.zero_pos
                self.moves_trace = []
                self.moves_count = 0
                self.print_estimate()
                self.refresh_msg(self.instructions[2])
                _translate = QtCore.QCoreApplication.translate
                self.gameCounter.setText(_translate("MainWindow", "Count: " + str(self.moves_count)))
                if self.tile_images:
                    self.tiles[self.zero_pos].setPixmap(QPixmap(""))
        else:
            QMessageBox.information(None, 'Message', 
                    'Puzzle is not solvable.\n\nPlease swap an adjacent pair or\ngenerate a random board.',
                    QMessageBox.Close, QMessageBox.Close)

    def is_goal(self):
        for idx in range(15):
            if (self.tiles_value[idx] != idx + 1):
                return False
        return True

    def print_trace(self):
        msgString = ""
        if self.game_win:
            msgString += "Puzzle solved.\n'New' starts a new game or try auto solver above.\n"
        if self.moves_count > 0:
            msgString += "Trace:\n"
            zero_trace = self.zero_pos
            index = self.moves_count
            while index > 0:
                backward_move = self.moves_trace[index - 1]
                if backward_move + 1 == zero_trace:
                    msgString += str(index) + ":R "
                elif backward_move + 4 == zero_trace:
                    msgString += str(index) + ":D "
                elif backward_move - 1 == zero_trace:
                    msgString += str(index) + ":L "
                elif backward_move - 4 == zero_trace:
                    msgString += str(index) + ":U "
                zero_trace = backward_move
                index -= 1
        else:
            msgString = self.instructions[2]
        self.refresh_msg(msgString)

    def moves_go_back(self):
        if self.moves_count > 0:
            self.moves_count = self.moves_count - 1
            pos = self.moves_trace[self.moves_count]
            del self.moves_trace[self.moves_count : self.moves_count + 1]
            _translate = QtCore.QCoreApplication.translate
            self.gameCounter.setText(_translate("MainWindow", "Count: " + str(self.moves_count)))
            self.tile_swap(self.zero_pos, pos)
            if self.moves_count == 0:
                self.gameBackward.setEnabled(False)
                self.game_thread.stop()
                time.sleep(0.5)
                self.gameTime.setText(_translate("MainWindow", "Time: "))
            self.print_trace()

    def game_reset(self):
        self.game_thread.stop()
        time.sleep(0.5)
        self.tiles_value = bytearray(self.game_tiles)
        self.zero_pos = self.game_zero
        self.puzzle_build()
        self.puzzle_moves()
        self.game_start()
        self.moves_trace = []
        self.moves_count = 0
        _translate = QtCore.QCoreApplication.translate
        self.gameCounter.setText(_translate("MainWindow", "Count: " + str(self.moves_count)))
        self.gameTime.setText(_translate("MainWindow", "Time: "))
        
    def get_solver(self):
        heuristic_choice = {0 : self._gateway.entry_point.getSolver_0,
            1 : self._gateway.entry_point.getSolver_1,
            2 : self._gateway.entry_point.getSolver_2,
            3 : self._gateway.entry_point.getSolver_3,
            4 : self._gateway.entry_point.getSolver_4,
            5 : self._gateway.entry_point.getSolver_5,
            6 : self._gateway.entry_point.getSolver_6
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
        if self.active_solver and index >= 0 and index <= 6:
            self.solver = self.get_solver()
            tiles = bytearray(self.game_tiles)
            board = self._gateway.entry_point.getBoard(tiles)
            estimate = str(self.solver.heuristic(board))
            _translate = QtCore.QCoreApplication.translate
            self.solverEstimate.setText(_translate("MainWindow", "Estimate: " + estimate))
            self.refresh_msg(self.instructions[3] + str(self._gateway.entry_point.getTimeoutLimit())
                    + self.instructions[4])
        if not self.solverStandard.isChecked():
            if not self.standard_level[index]:
                self.solverStandard.setEnabled(False)
            else:
                self.solverStandard.setEnabled(True)
      
    def solve_puzzle(self):
        self.game_thread.stop()
        if self.solverHeuristic.currentIndex() < 0:
            print("no solver")
            return

        self.active_game = False
        self.gameCounter.setEnabled(False)
        self.gameTime.setEnabled(False)
        self.gameBackward.setEnabled(False)
        self.gameReset.setEnabled(False)
        board = self._gateway.entry_point.getGoal()
        self.tiles_value = bytearray(board.getTiles())
        self.zero_pos = board.getZero1d()
        self.game_win = True
        if self.tile_images:
            for idx in range(0, 16):
                self.tiles[idx].setPixmap(QPixmap(self.images_in_use[self.game_tiles[idx]]))
            self.tiles[self.game_zero].setPixmap(QPixmap(""))
        else:
            for idx in range(0, 16):
                self.tiles[idx].setText(str(self.game_tiles[idx]))
            self.tiles[self.game_zero].setText("")
        
        solver = self.get_solver()
        board = self._gateway.entry_point.getBoard(bytearray(self.game_tiles))

        self.search_engine.setProperties(board, solver)
        self.search_status.setProperties(self.search_engine, self.game_tiles, self.game_zero, 
                self.tile_images, self.images_in_use, self._move_speed)
        self.search_engine.start()
        self.search_status.start()
                   
    def search_timeout(self):
        self.tiles_value = bytearray(self.game_tiles)
        self.zero_pos = self.game_zero
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

    def refresh_msg(self, custom_msgsg):
        _translate = QtCore.QCoreApplication.translate
        self.informationDetails.setText(_translate("MainWindow", custom_msgsg))

    #---------------------------------------

    def custom_quit(self):
        if QMessageBox.question(None, '', 'Are you sure to quit?', 
                    QMessageBox.Yes | QMessageBox.No, QMessageBox.No) == QMessageBox.Yes:
                    QApplication.quit()

    def load_custom_image(self):
        if not QDir(IMG_FOLDER_NAME).exists():
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
                img = Image.open(filename)
                imageWidth, imageHeight = img.size # Get image dimensions
                if imageWidth < 16 or imageHeight < 16:
                    QMessageBox.information(None, 'Message', 
                    'Image file too small, minimum requirement 16 x 16.',
                    QMessageBox.Close, QMessageBox.Close)
                    return
                
                left = 0 # Set the left-most edge
                upper = 0 # Set the top-most edge
                width = math.floor(imageWidth / 4)
                height = math.floor(imageHeight / 4)
                
                count = 1
                for row in range(4):
                    for col in range(4):
                        bbox = (left, upper, left + width, upper + height)
                        working_slice = img.crop(bbox) # Crop image based on created bounds
                        # Save your new cropped image.
                        working_slice.save(os.path.join(os.getcwd(), self.images_folder + "temp.png"))
                        resize_slice = Image.open(self.images_folder + "temp.png")
                        resize_slice = resize_slice.resize((IMG_SIZE, IMG_SIZE), Image.ANTIALIAS)
                        
                        if count == 16:
                            count = 0
                        resize_slice.save(os.path.join(os.getcwd(), self.images_folder + "custom_" + str(count) + ".png"))
                        count += 1
                        left += width
                    left = 0
                    upper += height
                QFile.remove(self.images_folder + "temp.png")
                self.tile_images = True
                self.images_in_use = []
                for idx in range(16):
                    self.images_in_use.append(self.images_folder + "custom_" + str(idx) + ".png")
                self.puzzle_build()

            except IOError:
                QMessageBox.information(None, 'Message', 
                    'This is not an image file.',
                    QMessageBox.Close, QMessageBox.Close)

    def change_timeout_limit(self):
        old_limit = self._gateway.entry_point.getTimeoutLimit()
        limit, okPressed = QInputDialog.getInt(None, "Change Timeout Limit", "Please enter timeout limit from 5 to 60 seconds:",
                old_limit, 5, 60, 1)
        if okPressed and old_limit != limit:
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

    def change_move_speed(self):
        speed, okPressed = QInputDialog.getInt(None, "Change Auto Move Speed", "Please enter speed from 100 (0.1s) to 2500 (2.5s) miliseconds:",
                self._move_speed, 100, 2500, 100)
        if okPressed:
            self._move_speed = speed

    def popup_instructions(self):
        QMessageBox.information(None, '15 puzzle: How to play', 'You may play with numbers or any images\n\n' +
            '1. Shuffle the puzzle to start the game.\n' + 
            '2. Try to solve the puzzle yourself,\n' + 
            '3. or let the system solve for you.\n\n' + 
            'Have fun!!!', 
                QMessageBox.Close, QMessageBox.Close)

    def about_author(self):
        QMessageBox.information(None, 'About 15 puzzle game and solver', 'Author: Meisze Wong\n' + 
            'www.linkedin.com/pub/macy-wong/46/550/37b/\n\n' + 
            'view source code:\nhttps://github.com/mwong510ca/HeuristicSearch-AdditivePatternDatabase-15Puzzle', 
                    QMessageBox.Close, QMessageBox.Close)

if __name__ == "__main__":
    app = QApplication(sys.argv)
    subprocess.Popen(['java', '-jar', 'FifteenPuzzleGateway.jar'])
    time.sleep(3)
    gateway = JavaGateway()
    time.sleep(0.5)
    window = GameSolver15Puzzle(gateway)
    window.show()
    while app.exec_() > 0:
        time.sleep(1)   
    gateway.shutdown() 
    sys.exit()
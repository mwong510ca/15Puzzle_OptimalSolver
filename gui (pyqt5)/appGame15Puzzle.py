#!/usr/bin/env python3
# -*- coding: utf-8 -*-

# Form implementation generated from reading ui file 'game_new.ui'
#
# Created by: PyQt5 UI code generator 5.7
#
# WARNING! All changes made in this file will be lost!

import sys
import time

from PyQt5 import QtCore, QtGui, QtWidgets
from PyQt5.QtWidgets import QMainWindow, QApplication, QMessageBox
from PyQt5.QtGui import QPainter
from PyQt5.QtCore import QObject, pyqtSignal, QThread

from py4j.java_gateway import JavaGateway

gateway = JavaGateway()

class GameWindow(QMainWindow):
    def __init__(self):
        super().__init__()
        self.initUI()
        """
        self.javaConnection = False
        if gateway.entry_point.isConnected():
            self.javaConnection = True
        print(self.javaConnection)
        """

    def initUI(self):
        self.setObjectName("MainWindow")
        self.resize(720, 540)
        self.setMinimumSize(QtCore.QSize(720, 540))
        self.centralwidget = QtWidgets.QWidget(self)
        self.centralwidget.setObjectName("centralwidget")
        self.gridLayout = QtWidgets.QGridLayout(self.centralwidget)
        self.gridLayout.setObjectName("gridLayout")
        self.gridLayoutWindow = QtWidgets.QGridLayout()
        self.gridLayoutWindow.setObjectName("gridLayoutWindow")

        self.gridLayout.addLayout(self.gridLayoutWindow, 0, 0, 2, 2)
        self.setCentralWidget(self.centralwidget)
        self.menubar = QtWidgets.QMenuBar(self)
        self.menubar.setGeometry(QtCore.QRect(0, 0, 720, 22))
        self.menubar.setObjectName("menubar")
        self.menuFile = QtWidgets.QMenu(self.menubar)
        self.menuFile.setObjectName("menuFile")
        self.menuHelp = QtWidgets.QMenu(self.menubar)
        self.menuHelp.setObjectName("menuHelp")
        self.setMenuBar(self.menubar)
        self.statusbar = QtWidgets.QStatusBar(self)
        self.statusbar.setObjectName("statusbar")
        self.setStatusBar(self.statusbar)
        self.actionInstruction = QtWidgets.QAction(self)
        self.actionInstruction.setObjectName("actionInstruction")
        self.actionExit = QtWidgets.QAction(self)
        self.actionExit.setObjectName("actionExit")
        self.actionInstructions = QtWidgets.QAction(self)
        self.actionInstructions.setObjectName("actionInstructions")
        self.actionAbout = QtWidgets.QAction(self)
        self.actionAbout.setObjectName("actionAbout")
        self.menuFile.addAction(self.actionExit)
        self.menuHelp.addAction(self.actionInstructions)
        self.menuHelp.addAction(self.actionAbout)
        self.menubar.addAction(self.menuFile.menuAction())
        self.menubar.addAction(self.menuHelp.menuAction())
        menubar = self.menuBar()
        menubar.setNativeMenuBar(False)

        sizePolicy = QtWidgets.QSizePolicy(QtWidgets.QSizePolicy.Preferred, QtWidgets.QSizePolicy.Preferred)
        sizePolicy.setHorizontalStretch(0)
        sizePolicy.setVerticalStretch(0)
        font = QtGui.QFont()
        font.setFamily("Arial Black")
        font.setPointSize(14)

        self.tile0 = QtWidgets.QPushButton(self.centralwidget)
        self.tile0.setSizePolicy(sizePolicy)
        self.tile0.setMinimumSize(QtCore.QSize(20, 20))
        self.tile0.setSizeIncrement(QtCore.QSize(1, 1))
        self.tile0.setFont(font)
        self.tile0.setObjectName("tile0")
        self.gridLayoutWindow.addWidget(self.tile0, 0, 0, 2, 1)

        self.tile1 = QtWidgets.QPushButton(self.centralwidget)
        self.tile1.setSizePolicy(sizePolicy)
        self.tile1.setMinimumSize(QtCore.QSize(20, 20))
        self.tile1.setSizeIncrement(QtCore.QSize(1, 1))
        self.tile1.setFont(font)
        self.tile1.setObjectName("tile1")
        self.gridLayoutWindow.addWidget(self.tile1, 0, 1, 2, 1)

        self.tile2 = QtWidgets.QPushButton(self.centralwidget)
        self.tile2.setSizePolicy(sizePolicy)
        self.tile2.setMinimumSize(QtCore.QSize(20, 20))
        self.tile2.setSizeIncrement(QtCore.QSize(1, 1))
        self.tile2.setFont(font)
        self.tile2.setObjectName("tile2")
        self.gridLayoutWindow.addWidget(self.tile2, 0, 2, 2, 1)

        self.tile3 = QtWidgets.QPushButton(self.centralwidget)
        self.tile3.setSizePolicy(sizePolicy)
        self.tile3.setMinimumSize(QtCore.QSize(20, 20))
        self.tile3.setSizeIncrement(QtCore.QSize(1, 1))
        self.tile3.setFont(font)
        self.tile3.setObjectName("tile3")
        self.gridLayoutWindow.addWidget(self.tile3, 0, 3, 2, 1)

        self.tile4 = QtWidgets.QPushButton(self.centralwidget)
        self.tile4.setSizePolicy(sizePolicy)
        self.tile4.setMinimumSize(QtCore.QSize(20, 20))
        self.tile4.setSizeIncrement(QtCore.QSize(1, 1))
        self.tile4.setFont(font)
        self.tile4.setObjectName("tile4")
        self.gridLayoutWindow.addWidget(self.tile4, 2, 0, 2, 1)

        self.tile5 = QtWidgets.QPushButton(self.centralwidget)
        self.tile5.setSizePolicy(sizePolicy)
        self.tile5.setMinimumSize(QtCore.QSize(20, 20))
        self.tile5.setSizeIncrement(QtCore.QSize(1, 1))
        self.tile5.setFont(font)
        self.tile5.setObjectName("tile5")
        self.gridLayoutWindow.addWidget(self.tile5, 2, 1, 2, 1)

        self.tile6 = QtWidgets.QPushButton(self.centralwidget)
        self.tile6.setSizePolicy(sizePolicy)
        self.tile6.setMinimumSize(QtCore.QSize(20, 20))
        self.tile6.setSizeIncrement(QtCore.QSize(1, 1))
        self.tile6.setFont(font)
        self.tile6.setObjectName("tile6")
        self.gridLayoutWindow.addWidget(self.tile6, 2, 2, 2, 1)

        self.tile7 = QtWidgets.QPushButton(self.centralwidget)
        self.tile7.setSizePolicy(sizePolicy)
        self.tile7.setMinimumSize(QtCore.QSize(20, 20))
        self.tile7.setSizeIncrement(QtCore.QSize(1, 1))
        self.tile7.setFont(font)
        self.tile7.setObjectName("tile7")
        self.gridLayoutWindow.addWidget(self.tile7, 2, 3, 2, 1)

        self.tile8 = QtWidgets.QPushButton(self.centralwidget)
        self.tile8.setSizePolicy(sizePolicy)
        self.tile8.setMinimumSize(QtCore.QSize(20, 20))
        self.tile8.setSizeIncrement(QtCore.QSize(1, 1))
        self.tile8.setFont(font)
        self.tile8.setObjectName("tile8")
        self.gridLayoutWindow.addWidget(self.tile8, 4, 0, 2, 1)

        self.tile9 = QtWidgets.QPushButton(self.centralwidget)
        self.tile9.setSizePolicy(sizePolicy)
        self.tile9.setMinimumSize(QtCore.QSize(20, 20))
        self.tile9.setSizeIncrement(QtCore.QSize(1, 1))
        self.tile9.setFont(font)
        self.tile9.setObjectName("tile9")
        self.gridLayoutWindow.addWidget(self.tile9, 4, 1, 2, 1)

        self.tile10 = QtWidgets.QPushButton(self.centralwidget)
        self.tile10.setSizePolicy(sizePolicy)
        self.tile10.setMinimumSize(QtCore.QSize(20, 20))
        self.tile10.setSizeIncrement(QtCore.QSize(1, 1))
        self.tile10.setFont(font)
        self.tile10.setObjectName("tile10")
        self.gridLayoutWindow.addWidget(self.tile10, 4, 2, 2, 1)

        self.tile11 = QtWidgets.QPushButton(self.centralwidget)
        self.tile11.setSizePolicy(sizePolicy)
        self.tile11.setMinimumSize(QtCore.QSize(20, 20))
        self.tile11.setSizeIncrement(QtCore.QSize(1, 1))
        self.tile11.setFont(font)
        self.tile11.setObjectName("tile11")
        self.gridLayoutWindow.addWidget(self.tile11, 4, 3, 2, 1)

        self.tile12 = QtWidgets.QPushButton(self.centralwidget)
        self.tile12.setSizePolicy(sizePolicy)
        self.tile12.setMinimumSize(QtCore.QSize(20, 20))
        self.tile12.setSizeIncrement(QtCore.QSize(1, 1))
        self.tile12.setFont(font)
        self.tile12.setObjectName("tile12")
        self.gridLayoutWindow.addWidget(self.tile12, 6, 0, 2, 1)

        self.tile13 = QtWidgets.QPushButton(self.centralwidget)
        self.tile13.setSizePolicy(sizePolicy)
        self.tile13.setMinimumSize(QtCore.QSize(20, 20))
        self.tile13.setSizeIncrement(QtCore.QSize(1, 1))
        self.tile13.setFont(font)
        self.tile13.setObjectName("tile13")
        self.gridLayoutWindow.addWidget(self.tile13, 6, 1, 2, 1)

        self.tile14 = QtWidgets.QPushButton(self.centralwidget)
        self.tile14.setSizePolicy(sizePolicy)
        self.tile14.setMinimumSize(QtCore.QSize(20, 20))
        self.tile14.setSizeIncrement(QtCore.QSize(1, 1))
        self.tile14.setFont(font)
        self.tile14.setObjectName("tile14")
        self.gridLayoutWindow.addWidget(self.tile14, 6, 2, 2, 1)

        self.tile15 = QtWidgets.QPushButton(self.centralwidget)
        self.tile15.setSizePolicy(sizePolicy)
        self.tile15.setMinimumSize(QtCore.QSize(20, 20))
        self.tile15.setSizeIncrement(QtCore.QSize(1, 1))
        self.tile15.setFont(font)
        self.tile15.setObjectName("tile15")
        self.gridLayoutWindow.addWidget(self.tile15, 6, 3, 2, 1)

        #-----------------------------

        self.labelHowTo = QtWidgets.QLabel(self.centralwidget)
        self.labelHowTo.setMinimumSize(QtCore.QSize(0, 20))
        self.labelHowTo.setSizeIncrement(QtCore.QSize(0, 1))
        self.labelHowTo.setAlignment(QtCore.Qt.AlignCenter)
        self.labelHowTo.setObjectName("labelHowTo")
        self.gridLayoutWindow.addWidget(self.labelHowTo, 0, 4, 1, 5)

        self.puzzleLevel = QtWidgets.QComboBox(self.centralwidget)
        self.puzzleLevel.setSizePolicy(sizePolicy)
        self.puzzleLevel.setMinimumSize(QtCore.QSize(0, 20))
        self.puzzleLevel.setSizeIncrement(QtCore.QSize(0, 1))
        self.puzzleLevel.setObjectName("puzzleLevel")
        self.puzzleLevel.addItem("")
        self.puzzleLevel.addItem("")
        self.puzzleLevel.addItem("")
        self.puzzleLevel.addItem("")
        self.puzzleLevel.addItem("")
        self.gridLayoutWindow.addWidget(self.puzzleLevel, 1, 4, 1, 1)

        self.puzzleGenerate = QtWidgets.QPushButton(self.centralwidget)
        self.puzzleGenerate.setSizePolicy(sizePolicy)
        self.puzzleGenerate.setObjectName("puzzleGenerate")
        self.gridLayoutWindow.addWidget(self.puzzleGenerate, 1, 5, 1, 1)

        self.puzzlePlay = QtWidgets.QPushButton(self.centralwidget)
        self.puzzlePlay.setSizePolicy(sizePolicy)
        self.puzzlePlay.setObjectName("puzzlePlay")
        self.gridLayoutWindow.addWidget(self.puzzlePlay, 1, 7, 1, 1)

        self.gameEnd = QtWidgets.QPushButton(self.centralwidget)
        self.gameEnd.setSizePolicy(sizePolicy)
        self.gameEnd.setObjectName("gameEnd")
        self.gridLayoutWindow.addWidget(self.gameEnd, 1, 8, 1, 1)

        self.gameCounter = QtWidgets.QLabel(self.centralwidget)
        self.gameCounter.setMinimumSize(QtCore.QSize(0, 20))
        self.gameCounter.setSizeIncrement(QtCore.QSize(0, 1))
        self.gameCounter.setObjectName("gameCounter")
        self.gridLayoutWindow.addWidget(self.gameCounter, 2, 4, 1, 1)

        self.gameTime = QtWidgets.QLabel(self.centralwidget)
        self.gameTime.setMinimumSize(QtCore.QSize(0, 20))
        self.gameTime.setSizeIncrement(QtCore.QSize(0, 1))
        self.gameTime.setObjectName("gameTime")
        self.gridLayoutWindow.addWidget(self.gameTime, 2, 5, 1, 1)

        self.gameBackward = QtWidgets.QPushButton(self.centralwidget)
        self.gameBackward.setSizePolicy(sizePolicy)
        self.gameBackward.setObjectName("gameBackward")
        self.gridLayoutWindow.addWidget(self.gameBackward, 2, 7, 1, 1)
        
        self.gameReset = QtWidgets.QPushButton(self.centralwidget)
        self.gameReset.setSizePolicy(sizePolicy)
        self.gameReset.setObjectName("gameReset")
        self.gridLayoutWindow.addWidget(self.gameReset, 2, 8, 1, 1)

        self.lineV = QtWidgets.QFrame(self.centralwidget)
        self.lineV.setFrameShape(QtWidgets.QFrame.VLine)
        self.lineV.setFrameShadow(QtWidgets.QFrame.Sunken)
        self.lineV.setObjectName("lineV")
        self.gridLayoutWindow.addWidget(self.lineV, 1, 6, 2, 1)

        self.lineH = QtWidgets.QFrame(self.centralwidget)
        self.lineH.setSizePolicy(sizePolicy)
        self.lineH.setFrameShape(QtWidgets.QFrame.HLine)
        self.lineH.setFrameShadow(QtWidgets.QFrame.Sunken)
        self.lineH.setObjectName("lineH")
        self.gridLayoutWindow.addWidget(self.lineH, 3, 4, 1, 5)

        #-----------------------------

        self.solverVersion = QtWidgets.QButtonGroup(self)
        self.solverVersion.setObjectName("solverVersion")

        self.solverStandard = QtWidgets.QRadioButton(self.centralwidget)
        self.solverStandard.setSizePolicy(sizePolicy)
        self.solverStandard.setMinimumSize(QtCore.QSize(0, 20))
        self.solverStandard.setSizeIncrement(QtCore.QSize(0, 1))
        self.solverStandard.setObjectName("solverStandard")
        self.solverVersion.addButton(self.solverStandard)
        self.gridLayoutWindow.addWidget(self.solverStandard, 4, 4, 1, 1)

        self.solverAdvanced = QtWidgets.QRadioButton(self.centralwidget)
        self.solverAdvanced.setSizePolicy(sizePolicy)
        self.solverAdvanced.setMinimumSize(QtCore.QSize(0, 20))
        self.solverAdvanced.setSizeIncrement(QtCore.QSize(0, 1))
        self.solverAdvanced.setObjectName("solverAdvanced")
        self.solverVersion.addButton(self.solverAdvanced)
        self.gridLayoutWindow.addWidget(self.solverAdvanced, 5, 4, 1, 1)

        self.solverHeuristic = QtWidgets.QComboBox(self.centralwidget)
        self.solverHeuristic.setSizePolicy(sizePolicy)
        self.solverHeuristic.setObjectName("solverHeuristic")
        self.solverHeuristic.addItem("")
        self.solverHeuristic.addItem("")
        self.solverHeuristic.addItem("")
        self.solverHeuristic.addItem("")
        self.solverHeuristic.addItem("")
        self.solverHeuristic.addItem("")
        self.solverHeuristic.addItem("")
        self.gridLayoutWindow.addWidget(self.solverHeuristic, 4, 5, 1, 4)

        self.solverSearchNow = QtWidgets.QPushButton(self.centralwidget)
        self.solverSearchNow.setSizePolicy(sizePolicy)
        self.solverSearchNow.setMinimumSize(QtCore.QSize(0, 20))
        self.solverSearchNow.setSizeIncrement(QtCore.QSize(0, 1))
        self.solverSearchNow.setObjectName("solverSearchNow")
        self.gridLayoutWindow.addWidget(self.solverSearchNow, 5, 5, 1, 4)

        self.solverEstimate = QtWidgets.QLabel(self.centralwidget)
        self.solverEstimate.setMinimumSize(QtCore.QSize(0, 20))
        self.solverEstimate.setSizeIncrement(QtCore.QSize(0, 2))
        self.solverEstimate.setSizeIncrement(QtCore.QSize(0, 1))
        self.solverEstimate.setObjectName("solverEstimate")
        self.gridLayoutWindow.addWidget(self.solverEstimate, 6, 4, 1, 1)

        self.solverResult = QtWidgets.QLabel(self.centralwidget)
        self.solverResult.setMinimumSize(QtCore.QSize(0, 20))
        self.solverResult.setSizeIncrement(QtCore.QSize(0, 1))
        self.solverResult.setObjectName("solverResult")
        self.gridLayoutWindow.addWidget(self.solverResult, 6, 5, 1, 1)

        self.solverTime = QtWidgets.QLabel(self.centralwidget)
        self.solverTime.setObjectName("solverTime")
        self.gridLayoutWindow.addWidget(self.solverTime, 6, 7, 1, 2)

        self.solverNodes = QtWidgets.QLabel(self.centralwidget)
        self.solverNodes.setObjectName("solverNodes")
        self.gridLayoutWindow.addWidget(self.solverNodes, 7, 5, 1, 4)

        self.textBrowser = QtWidgets.QTextBrowser(self.centralwidget)
        self.textBrowser.setMinimumSize(QtCore.QSize(0, 40))
        self.textBrowser.setMaximumSize(QtCore.QSize(16777215, 200))
        font = QtGui.QFont()
        font.setFamily("Courier")
        self.textBrowser.setFont(font)
        self.textBrowser.setObjectName("textBrowser")
        self.gridLayoutWindow.addWidget(self.textBrowser, 8, 0, 1, 9)

        self.tiles = [self.tile0,  self.tile1,  self.tile2,  self.tile3,
                      self.tile4,  self.tile5,  self.tile6,  self.tile7, 
                      self.tile8,  self.tile9,  self.tile10, self.tile11, 
                      self.tile12, self.tile13, self.tile14, self.tile15]
        self.strInstructions = {1 : "Shuffle the puzzle:\n" + 
                    "Click 2 tiles and swap each other. Or generate a random puzzle.\n" +
                    "When you ready, click 'Play' to start the game.",
                    2 : "Play the game:\n" +
                    "Click the tile next to space to move the puzzle.\n" +
                    "'Go Back' - back to previous move\n" +
                    "'Reset'   - start over the same puzzle, clear moves history.\n" +
                    "'New'     - start a new game, choose the puzzle." +
                    "'Find Optimal Solution' - reset the puzzle and search for optimal solution.",
                    3 : "Auto solver:\n" +
                    "Choose the heuristic function and solver version, it will display the initial estimate.\n" +
                    "'Find optimal solution' - It will reset the puzzle and search for optimal solution up to 60 seconds.  " + 
                    "You may try another heuristic function after the search.\n\n" +
                    "If solution found, it will display the moves and apply solution.\n" +
                    "If timeout. You can only try another heuristic with better performance only."}
        self.retranslateUi(self)
        QtCore.QMetaObject.connectSlotsByName(self)

    def retranslateUi(self, MainWindow):
        _translate = QtCore.QCoreApplication.translate
        MainWindow.setWindowTitle(_translate("MainWindow", "MainWindow"))
        self.menuFile.setTitle(_translate("MainWindow", "File"))
        self.menuHelp.setTitle(_translate("MainWindow", "Help"))
        self.actionExit.setText(_translate("MainWindow", "Exit"))
        self.actionInstructions.setText(_translate("MainWindow", "Instructions"))
        self.actionAbout.setText(_translate("MainWindow", "About"))

        self.tile0.clicked.connect(lambda: self.tileClicked(0))
        self.tile1.clicked.connect(lambda: self.tileClicked(1))
        self.tile2.clicked.connect(lambda: self.tileClicked(2))
        self.tile3.clicked.connect(lambda: self.tileClicked(3))
        self.tile4.clicked.connect(lambda: self.tileClicked(4))
        self.tile5.clicked.connect(lambda: self.tileClicked(5))
        self.tile6.clicked.connect(lambda: self.tileClicked(6))
        self.tile7.clicked.connect(lambda: self.tileClicked(7))
        self.tile8.clicked.connect(lambda: self.tileClicked(8))
        self.tile9.clicked.connect(lambda: self.tileClicked(9))
        self.tile10.clicked.connect(lambda: self.tileClicked(10))
        self.tile11.clicked.connect(lambda: self.tileClicked(11))
        self.tile12.clicked.connect(lambda: self.tileClicked(12))
        self.tile13.clicked.connect(lambda: self.tileClicked(13))
        self.tile14.clicked.connect(lambda: self.tileClicked(14))
        self.tile15.clicked.connect(lambda: self.tileClicked(15))

        self.labelHowTo.setText(_translate("MainWindow", "Click 2 tiles swap or generate a new puzzle."))
        self.puzzleLevel.setItemText(0, _translate("MainWindow", "Goal State"))
        self.puzzleLevel.setItemText(1, _translate("MainWindow", "Random"))
        self.puzzleLevel.setItemText(2, _translate("MainWindow", "Easy"))
        self.puzzleLevel.setItemText(3, _translate("MainWindow", "Moderate"))
        self.puzzleLevel.setItemText(4, _translate("MainWindow", "Hard"))
        self.puzzleGenerate.setText(_translate("MainWindow", "Generate"))
        self.puzzleGenerate.clicked.connect(self.puzzleCreate)        
        self.puzzlePlay.setText(_translate("MainWindow", "Play"))
        self.puzzlePlay.clicked.connect(self.gameStart)

        self.gameCounter.setText(_translate("MainWindow", "Count:"))
        self.gameTime.setText(_translate("MainWindow", "Time:"))
        self.gameBackward.setText(_translate("MainWindow", "Go Back"))
        self.gameBackward.clicked.connect(self.movesGoBack)
        self.gameReset.setText(_translate("MainWindow", "Reset"))
        self.gameReset.clicked.connect(self.gameStartOver)
        self.gameEnd.setText(_translate("MainWindow", "New"))
        self.gameEnd.clicked.connect(self.gameNew)

        self.solverVersion.buttonClicked.connect(self.printEstimate)      
        self.solverStandard.setText(_translate("MainWindow", "Standard"))
        self.solverAdvanced.setText(_translate("MainWindow", "Advanced"))
        self.solverAdvanced.setChecked(True)
        self.solverHeuristic.setItemText(0, _translate("MainWindow", "7-8 Pattern Database"))
        self.solverHeuristic.setItemText(1, _translate("MainWindow", "6-6-3 Pattern db + Walking dist"))
        self.solverHeuristic.setItemText(2, _translate("MainWindow", "5-5-5- Pattern db + Walking dist"))
        self.solverHeuristic.setItemText(3, _translate("MainWindow", "Walking + Manhattan (linear conflict)"))
        self.solverHeuristic.setItemText(4, _translate("MainWindow", "Walking distance"))
        self.solverHeuristic.setItemText(5, _translate("MainWindow", "Manhattan distance with linear confilct"))
        self.solverHeuristic.setItemText(6, _translate("MainWindow", "Manhattan distance"))
        self.solverHeuristic.setCurrentIndex(1)
        self.solverHeuristic.currentIndexChanged.connect(self.printEstimate)      
        self.solverSearchNow.setText(_translate("MainWindow", "Find Optimal Solution"))
        self.solverSearchNow.clicked.connect(self.solvePuzzle)
        self.solverEstimate.setText(_translate("MainWindow", "Estimate:"))
        self.solverResult.setText(_translate("MainWindow", "Moves:"))
        self.solverTime.setText(_translate("MainWindow", "Time:"))
        self.solverNodes.setText(_translate("MainWindow", "Nodes:"))

        self.puzzleLevel.setCurrentIndex(0) 
        self.zeroPos = 15
        self.puzzleCreate()
        self.thread = GameStopwatch()
        self.thread.stop()
        self.thread.gTime.connect(self.gameTime.setText) 
        self.gameNew()
        self.puzzleLevel.setCurrentIndex(1)
        self.show()

    def customQuit(self):
        if QMessageBox.question(None, '', 'Are you sure to quit?', 
                    QMessageBox.Yes | QMessageBox.No, QMessageBox.No) == QMessageBox.Yes:
                    QApplication.quit()
                    sys.exit(app.exec_())

    def gameNew(self):
        self.thread.stop()
        self.activePuzzleSetup = True
        self.activeGame = False
        self.activeSolver = False
        self.puzzleLevel.setEnabled(True)
        self.puzzleGenerate.setEnabled(True)
        self.puzzlePlay.setEnabled(True)
        self.gameCounter.setEnabled(False)
        self.gameTime.setEnabled(False)
        self.gameBackward.setEnabled(False)
        self.gameReset.setEnabled(False)
        self.gameEnd.setEnabled(False)
        self.solverStandard.setEnabled(False)
        self.solverAdvanced.setEnabled(False)
        self.solverHeuristic.setEnabled(False)
        self.solverSearchNow.setEnabled(False)
        self.solverEstimate.setEnabled(False)
        self.solverResult.setEnabled(False)
        self.solverTime.setEnabled(False)
        self.solverNodes.setEnabled(False)
        self.tileClick1 = -1

        levelCached = self.puzzleLevel.currentIndex()
        self.puzzleLevel.setCurrentIndex(0)
        self.puzzleCreate()
        if levelCached == 0:
            self.puzzleLevel.setCurrentIndex(1)
        else:
            self.puzzleLevel.setCurrentIndex(levelCached)
        for idx in range(0, 16):
            self.tiles[idx].setEnabled(True)
        self.movesTrace = []
        self.movesTraceTile = []
        self.movesCount = 0
        _translate = QtCore.QCoreApplication.translate
        self.gameCounter.setText(_translate("MainWindow", "Count: "))
        self.gameTime.setText(_translate("MainWindow", "Time: "))
        self.solverEstimate.setText(_translate("MainWindow", "Estimate: "))
        self.solverTime.setText(_translate("MainWindow", "Time:"))
        self.solverResult.setText(_translate("MainWindow", "Result:"))
        self.solverNodes.setText(_translate("MainWindow", "Nodes:"))
        self.statusMsg(self.strInstructions[1])
        for index in range(0, 7):
                self.solverHeuristic.model().item(index).setEnabled(True)

    def statusMsg(self, customMsg):
        _translate = QtCore.QCoreApplication.translate
        self.textBrowser.setText(_translate("MainWindow", customMsg))

    def puzzleCreate(self):
        boardCases = {0 : self.puzzleGoal,
           1 : self.puzzleRandom,
           2 : self.puzzleEasy,
           3 : self.puzzleModerate,
           4 : self.puzzleHard
        }
        option = 0
        option = self.puzzleLevel.currentIndex()
        board = boardCases[option]()
        self.tiles[self.zeroPos].setFlat(False)
        self.tilesValue = bytearray(board.getTiles())
        self.zeroPos = board.getZero1d()
        self.puzzleBuild();
        self.tiles[self.zeroPos].setFlat(True)
        self.setMoves()     

    def puzzleGoal(self):
        board = gateway.entry_point.getGoal()
        return board

    def puzzleRandom(self):
        board = gateway.entry_point.getRandom()
        return board

    def puzzleEasy(self):
        board = gateway.entry_point.getEasy()
        return board

    def puzzleModerate(self):
        board = gateway.entry_point.getModerate()
        return board

    def puzzleHard(self):
        board = gateway.entry_point.getHard()
        return board

    def puzzleBuild(self):
        _translate = QtCore.QCoreApplication.translate
        for idx in range(0, 16):
            self.tiles[idx].setText(_translate("MainWindow", str(self.tilesValue[idx])))
        self.tiles[self.zeroPos].setText(_translate("MainWindow", "  "))

    def setMoves(self):
        self.zeroMoves = [False, False, False, False, False, False, False, False, 
                          False, False, False, False, False, False, False, False]
        if (self.zeroPos % 4 < 3):
            self.zeroMoves[self.zeroPos + 1] = True
        if (self.zeroPos / 4 < 3):
            self.zeroMoves[self.zeroPos + 4] = True
        if (self.zeroPos % 4 > 0):
            self.zeroMoves[self.zeroPos - 1] = True
        if (self.zeroPos / 4 >= 1):
            self.zeroMoves[self.zeroPos - 4] = True

    def tileClicked(self, pos):
        if self.activePuzzleSetup:
            if self.tileClick1 == -1:
                font = QtGui.QFont()
                font.setFamily("Arial Black")
                font.setPointSize(14)
                font.setBold(True)
                font.setWeight(75)
                font.setUnderline(True)
                self.tiles[pos].setFont(font)
                self.tileClick1 = pos
            elif self.tileClick1 == pos:
                font = QtGui.QFont()
                font.setFamily("Arial Black")
                font.setPointSize(14)
                font.setBold(True)
                font.setWeight(75)
                font.setUnderline(False)
                self.tiles[pos].setFont(font)
                self.tileClick1 = -1
            else:
                font = QtGui.QFont()
                font.setFamily("Arial Black")
                font.setPointSize(14)
                font.setBold(True)
                font.setWeight(75)
                font.setUnderline(False)
                self.tiles[self.tileClick1].setFont(font)
                self.tileSwap(self.tileClick1, pos)
        elif self.activeGame:
            if self.zeroMoves[pos]:
                if self.movesCount == 0:
                    self.thread.start()
                _translate = QtCore.QCoreApplication.translate
                self.movesTrace.append(self.zeroPos)
                self.movesTraceTile.append(self.tilesValue[pos])
                self.movesCount = self.movesCount + 1
                self.gameCounter.setText(_translate("MainWindow", "Count: " + str(self.movesCount)))
                self.tileSwap(self.zeroPos, pos)
                self.printTrace()
                for idx in range(0, 16):
                        if self.zeroMoves[idx]:
                            self.tiles[idx].setEnabled(True)
                        else:
                            self.tiles[idx].setEnabled(False)
                if self.isGoal():
                    self.gameCounter.setText(_translate("MainWindow", "Solved: " + str(self.movesCount)))
                    for idx in range(0, 16):
                        self.tiles[idx].setEnabled(False)
                    self.gameCounter.setEnabled(False)
                    self.gameBackward.setEnabled(False)
                    self.gameReset.setEnabled(False)
                    QMessageBox.information(None, 'Message', 
                    "Congratulations!\n\nPuzzle solved in " + str(self.movesCount) + " moves.",
                    QMessageBox.Close, QMessageBox.Close)
                else:
                    self.gameBackward.setEnabled(True)

    def tileSwap(self, pos1, pos2):
        _translate = QtCore.QCoreApplication.translate
        val1 = self.tilesValue[pos1]
        val2 = self.tilesValue[pos2]
        del self.tilesValue[pos1 : (pos1 + 1)]
        self.tilesValue.insert(pos1, val2)
        del self.tilesValue[pos2 : (pos2 + 1)]
        self.tilesValue.insert(pos2, val1)
        self.tiles[pos1].setText(_translate("MainWindow", str(self.tilesValue[pos1])))
        self.tiles[pos2].setText(_translate("MainWindow", str(self.tilesValue[pos2])))
        if self.tilesValue[pos1] == 0:
            self.tiles[pos1].setText(_translate("MainWindow", "  "))
            self.tiles[pos2].setFlat(False)
            self.tiles[pos1].setFlat(True)
            self.zeroPos = pos1
            self.setMoves()
        elif self.tilesValue[pos2] == 0:
            self.tiles[pos2].setText(_translate("MainWindow", "  "))
            self.tiles[pos1].setFlat(False)
            self.tiles[pos2].setFlat(True)
            self.zeroPos = pos2
            self.setMoves()
        self.tileClick1 = -1

    def printTrace(self):
        msgString = self.strInstructions[2]
        if self.movesCount > 0:
            msgString += "\nTrace:\n"
            for index in range(0, self.movesCount):
                if self.movesTraceTile[index] < 10:
                    msgString += " " + str(self.movesTraceTile[index]) + " "
                else:
                    msgString += str(self.movesTraceTile[index]) + " "
            msgString += "\n";
        self.statusMsg(msgString)

    def gameStart(self):
        board = gateway.entry_point.getBoard(self.tilesValue)
        if board.isSolvable():
            if self.isGoal():
                QMessageBox.information(None, 'Message', 
                    'Puzzle is the goal state.\n\nPlease shuffle the puzzle.',
                    QMessageBox.Close, QMessageBox.Close)
            else:
                self.activePuzzleSetup = False
                self.activeGame = True
                self.activeSolver = True
                self.puzzleLevel.setEnabled(False)
                self.puzzleGenerate.setEnabled(False)
                self.puzzlePlay.setEnabled(False)
                self.gameCounter.setEnabled(True)
                self.gameTime.setEnabled(True)
                self.gameBackward.setEnabled(True)
                self.gameReset.setEnabled(True)
                self.gameEnd.setEnabled(True)
                self.solverStandard.setEnabled(True)
                self.solverAdvanced.setEnabled(True)
                self.solverHeuristic.setEnabled(True)
                self.solverSearchNow.setEnabled(True)
                self.solverEstimate.setEnabled(True)
                for idx in range(0, 16):
                    if self.zeroMoves[idx]:
                        self.tiles[idx].setEnabled(True)
                    else:
                        self.tiles[idx].setEnabled(False)
                if self.tileClick1 > -1:
                    font = QtGui.QFont()
                    font.setFamily("Arial Black")
                    font.setPointSize(14)
                    font.setBold(True)
                    font.setWeight(75)
                    font.setUnderline(False)
                    self.tiles[self.tileClick1].setFont(font)
                    self.tileClick1 = -1
                self.gameTiles = []
                for idx in range(0, 16):
                    self.gameTiles.append(self.tilesValue[idx])
                self.gameZero = self.zeroPos
                self.movesTrace = []
                self.movesTraceTile = []
                self.movesCount = 0
                self.gameBackward.setEnabled(False)
                self.printEstimate()
                self.statusMsg(self.strInstructions[2])
        else:
            QMessageBox.information(None, 'Message', 
                    'Puzzle is not solvable.\n\nPlease swap an adjacent pair or\ngenerate a random board.',
                    QMessageBox.Close, QMessageBox.Close)

    def isGoal(self):
        for idx in range(0, 15):
            if (self.tilesValue[idx] != idx + 1):
                return False
        self.thread.stop()
        return True

    def gameStartOver(self):
        self.thread.stop()
        self.tiles[self.zeroPos].setFlat(False)
        self.tilesValue = bytearray(self.gameTiles)
        self.zeroPos = self.gameZero
        self.puzzleBuild()
        self.setMoves()
        self.gameStart()
        self.movesTrace = []
        self.movesTraceTile = []
        self.movesCount = 0
        _translate = QtCore.QCoreApplication.translate
        self.gameCounter.setText(_translate("MainWindow", "Count: " + str(self.movesCount)))
        self.gameTime.setText(_translate("MainWindow", "Time: "))
        self.tiles[self.zeroPos].setFlat(True) 

    def movesGoBack(self):
        if self.movesCount > 0:
            self.movesCount = self.movesCount - 1
            pos = self.movesTrace[self.movesCount]
            del self.movesTrace[self.movesCount : self.movesCount + 1]
            del self.movesTraceTile[self.movesCount : self.movesCount + 1]
            _translate = QtCore.QCoreApplication.translate
            self.gameCounter.setText(_translate("MainWindow", "Count: " + str(self.movesCount)))
            self.tileSwap(self.zeroPos, pos)
            if self.movesCount == 0:
                self.gameBackward.setEnabled(False)
                self.thread.stop()
                self.gameTime.setText(_translate("MainWindow", "Time: "))
            for idx in range(0, 16):
                if self.zeroMoves[idx]:
                    self.tiles[idx].setEnabled(True)
                else:
                    self.tiles[idx].setEnabled(False)
            self.printTrace()

    def getSolver(self):
        heuristicCases = {0 : gateway.entry_point.getSolver_0,
            1 : gateway.entry_point.getSolver_1,
            2 : gateway.entry_point.getSolver_2,
            3 : gateway.entry_point.getSolver_3,
            4 : gateway.entry_point.getSolver_4,
            5 : gateway.entry_point.getSolver_5,
            6 : gateway.entry_point.getSolver_6
        }
        option = self.solverHeuristic.currentIndex()
        solver = heuristicCases[option]()
        if option == 5:
            solver.linearConflictSwitch(True)
        elif option == 6:
            solver.linearConflictSwitch(False)  
        if self.solverStandard.isChecked():
            solver.versionSwitch(False)
        elif self.solverAdvanced.isChecked():
            solver.versionSwitch(True)
        return solver

    def printEstimate(self):
        index = self.solverHeuristic.currentIndex()
        if self.activeSolver and index >= 0 and index <= 6:
            self.solver = self.getSolver()
            tiles = bytearray(self.gameTiles)
            board = gateway.entry_point.getBoard(tiles)
            estimate = str(self.solver.heuristic(board))
            _translate = QtCore.QCoreApplication.translate
            self.solverEstimate.setText(_translate("MainWindow", "Estimate: " + estimate))
            self.statusMsg(self.strInstructions[3])
        print ()

    def solvePuzzle(self):
        self.thread.stop()
        if self.solverHeuristic.currentIndex() < 0:
            print("no solver")
            return

        self.gameCounter.setEnabled(False)
        self.gameTime.setEnabled(False)
        self.gameBackward.setEnabled(False)
        self.gameReset.setEnabled(False)
        self.gameEnd.setEnabled(False)
        self.solverStandard.setEnabled(False)
        self.solverAdvanced.setEnabled(False)
        self.solverHeuristic.setEnabled(False)
        self.solverSearchNow.setEnabled(False)
        self.solverResult.setEnabled(True)
        self.solverTime.setEnabled(True)
        self.solverNodes.setEnabled(True)
        for idx in range(0, 16):
            self.tiles[idx].setText(str(self.gameTiles[idx]))
            self.tiles[idx].setEnabled(False)
        self.tiles[self.zeroPos].setFlat(False)
        self.tiles[15].setFlat(False)
        self.tiles[self.gameZero].setText("  ")
        self.tiles[self.gameZero].setFlat(True)

        solver = self.getSolver()
        search = SearchStart(self.gameTiles, solver)
        status = SearchStatus(search, self.gameTiles, self.gameZero)
        status.statusTime.connect(self.solverTime.setText)
        status.statusNodes.connect(self.solverNodes.setText)
        status.statusResult.connect(self.solverResult.setText)
        status.statusMoves.connect(self.textBrowser.setText)

        status.widgetAccessable.connect(self.gameEnd.setEnabled)
        status.widgetAccessable.connect(self.solverStandard.setEnabled)
        status.widgetAccessable.connect(self.solverAdvanced.setEnabled)
        status.widgetAccessable.connect(self.solverHeuristic.setEnabled)
        status.widgetAccessable.connect(self.solverSearchNow.setEnabled)
        status.widgetInaccessable.connect(self.solverTime.setEnabled)
        status.widgetInaccessable.connect(self.solverNodes.setEnabled)
        status.widgetInaccessable.connect(self.solverResult.setEnabled)

        status.val0.connect(self.tile0.setText)
        status.val1.connect(self.tile1.setText)
        status.val2.connect(self.tile2.setText)
        status.val3.connect(self.tile3.setText)
        status.val4.connect(self.tile4.setText)
        status.val5.connect(self.tile5.setText)
        status.val6.connect(self.tile6.setText)
        status.val7.connect(self.tile7.setText)
        status.val8.connect(self.tile8.setText)
        status.val9.connect(self.tile9.setText)
        status.val10.connect(self.tile10.setText)
        status.val11.connect(self.tile11.setText)
        status.val12.connect(self.tile12.setText)
        status.val13.connect(self.tile13.setText)
        status.val14.connect(self.tile14.setText)
        status.val15.connect(self.tile15.setText)

        status.flat0.connect(self.tile0.setFlat)
        status.flat1.connect(self.tile1.setFlat)
        status.flat2.connect(self.tile2.setFlat)
        status.flat3.connect(self.tile3.setFlat)
        status.flat4.connect(self.tile4.setFlat)
        status.flat5.connect(self.tile5.setFlat)
        status.flat6.connect(self.tile6.setFlat)
        status.flat7.connect(self.tile7.setFlat)
        status.flat8.connect(self.tile8.setFlat)
        status.flat9.connect(self.tile9.setFlat)
        status.flat10.connect(self.tile10.setFlat)
        status.flat11.connect(self.tile11.setFlat)
        status.flat12.connect(self.tile12.setFlat)
        status.flat13.connect(self.tile13.setFlat)
        status.flat14.connect(self.tile14.setFlat)
        status.flat15.connect(self.tile15.setFlat)

        self.threads = []
        self.threads.append(search)
        self.threads.append(status)
        search.start()
        status.start()

class GameStopwatch(QThread):
    gTime = pyqtSignal(str)

    def __init__(self,):
        super(GameStopwatch, self).__init__()
        self._isRunning = True

    def run(self):
        if not self._isRunning:
            self._isRunning = True

        timeIncerment = 0
        while self._isRunning:
            self.gTime.emit("Time: " + str(timeIncerment/10) + "s")
            time.sleep(0.1)
            timeIncerment += 1

    def stop(self):
        self._isRunning = False

class SearchStart(QThread):
    def __init__(self, original, solver):
        super(SearchStart, self).__init__()
        self._isRunning = True
        self._board = board = gateway.entry_point.getBoard(bytearray(original))
        self._solver = solver

    def run(self):
        if not self._isRunning:
            self._isRunning = Ture
        self._solver.findOptimalPath(self._board)

        while not self._solver.isSearchTimeout() and self._solver.moves() == -1:
            time.sleep(1)
            continue

        self._isRunning = False

class SearchStatus(QThread):
    statusTime = pyqtSignal(str)
    statusNodes = pyqtSignal(str)
    statusResult = pyqtSignal(str)
    statusMoves = pyqtSignal(str)
    widgetInaccessable = pyqtSignal(bool)
    widgetAccessable = pyqtSignal(bool)
    val0 = pyqtSignal(str)
    val1 = pyqtSignal(str)
    val2 = pyqtSignal(str)
    val3 = pyqtSignal(str)
    val4 = pyqtSignal(str)
    val5 = pyqtSignal(str)
    val6 = pyqtSignal(str)
    val7 = pyqtSignal(str)
    val8 = pyqtSignal(str)
    val9 = pyqtSignal(str)
    val10 = pyqtSignal(str)
    val11 = pyqtSignal(str)
    val12 = pyqtSignal(str)
    val13 = pyqtSignal(str)
    val14 = pyqtSignal(str)
    val15 = pyqtSignal(str)
    flat0 = pyqtSignal(bool)
    flat1 = pyqtSignal(bool)
    flat2 = pyqtSignal(bool)
    flat3 = pyqtSignal(bool)
    flat4 = pyqtSignal(bool)
    flat5 = pyqtSignal(bool)
    flat6 = pyqtSignal(bool)
    flat7 = pyqtSignal(bool)
    flat8 = pyqtSignal(bool)
    flat9 = pyqtSignal(bool)
    flat10 = pyqtSignal(bool)
    flat11 = pyqtSignal(bool)
    flat12 = pyqtSignal(bool)
    flat13 = pyqtSignal(bool)
    flat14 = pyqtSignal(bool)
    flat15 = pyqtSignal(bool)

    def __init__(self, thread, original, zero):
        super(SearchStatus, self).__init__()
        self._isRunning = True
        self._thread = thread
        self._solver = thread._solver
        self._tiles = []
        for tile in original:
            self._tiles.append(tile)
        self._zero = zero

    def run(self):
        if not self._isRunning:
            self._isRunning = Ture

        estTime = 0
        while self._thread._isRunning:
            self.statusTime.emit("Time: " + str(estTime/10) + "s")
            time.sleep(0.1)
            estTime += 1

        self._time = "Time: " + str(self._solver.searchTime()) + "s"
        self._nodes = "Nodes: " + str(self._solver.searchNodeCount())        
        self.statusTime.emit(self._time)
        self.statusNodes.emit(self._nodes)
        if self._solver.isSearchTimeout():
            self.statusResult.emit("Result: timeout")
        else:
            steps = self._solver.moves()
            self.statusResult.emit("Result: " + str(steps) + " moves")
            self.statusMoves.emit(self._solver.solutionQtString())
            directions = self._solver.solution()
            strDir = []
            for idx in range (1, steps + 1):
                strDir.append(str(directions[idx]))

            time.sleep(1)

            for idx in range (0, steps):
                pos0 = self._zero
                pos = pos0
                strDirection = strDir[idx]
                if strDirection == 'RIGHT':
                    pos += 1
                elif strDirection == 'DOWN ':
                    pos +=4
                elif strDirection == 'LEFT ':
                    pos -= 1
                elif strDirection == 'UP   ':
                    pos -= 4
                else:
                    print("ERROR " + str(directions))
                val = self._tiles[pos]

                if pos == 0:
                    self.val0.emit("  ")
                    self.flat0.emit(True)
                elif pos == 1:
                    self.val1.emit("  ")
                    self.flat1.emit(True)
                elif pos == 2:
                    self.val2.emit("  ")
                    self.flat2.emit(True)
                elif pos == 3:
                    self.val3.emit("  ")
                    self.flat3.emit(True)
                elif pos == 4:
                    self.val4.emit("  ")
                    self.flat4.emit(True)
                elif pos == 5:
                    self.val5.emit("  ")
                    self.flat5.emit(True)
                elif pos == 6:
                    self.val6.emit("  ")
                    self.flat6.emit(True)
                elif pos == 7:
                    self.val7.emit("  ")
                    self.flat7.emit(True)
                elif pos == 8:
                    self.val8.emit("  ")
                    self.flat8.emit(True)
                elif pos == 9:
                    self.val9.emit("  ")
                    self.flat9.emit(True)
                elif pos == 10:
                    self.val10.emit("  ")
                    self.flat10.emit(True)
                elif pos == 11:
                    self.val11.emit("  ")
                    self.flat11.emit(True)
                elif pos == 12:
                    self.val12.emit("  ")
                    self.flat12.emit(True)
                elif pos == 13:
                    self.val13.emit("  ")
                    self.flat13.emit(True)
                elif pos == 14:
                    self.val14.emit("  ")
                    self.flat14.emit(True)
                elif pos == 15:
                    self.val15.emit("  ")
                    self.flat15.emit(True)
                
                if pos0 == 0:
                    self.val0.emit(str(val))
                    self.flat0.emit(False)
                elif pos0 == 1:
                    self.val1.emit(str(val))
                    self.flat1.emit(False)
                elif pos0 == 2:
                    self.val2.emit(str(val))
                    self.flat2.emit(False)
                elif pos0 == 3:
                    self.val3.emit(str(val))
                    self.flat3.emit(False)
                elif pos0 == 4:
                    self.val4.emit(str(val))
                    self.flat4.emit(False)
                elif pos0 == 5:
                    self.val5.emit(str(val))
                    self.flat5.emit(False)
                elif pos0 == 6:
                    self.val6.emit(str(val))
                    self.flat6.emit(False)
                elif pos0 == 7:
                    self.val7.emit(str(val))
                    self.flat7.emit(False)
                elif pos0 == 8:
                    self.val8.emit(str(val))
                    self.flat8.emit(False)
                elif pos0 == 9:
                    self.val9.emit(str(val))
                    self.flat9.emit(False)
                elif pos0 == 10:
                    self.val10.emit(str(val))
                    self.flat10.emit(False)
                elif pos0 == 11:
                    self.val11.emit(str(val))
                    self.flat11.emit(False)
                elif pos0 == 12:
                    self.val12.emit(str(val))
                    self.flat12.emit(False)
                elif pos0 == 13:
                    self.val13.emit(str(val))
                    self.flat13.emit(False)
                elif pos0 == 14:
                    self.val14.emit(str(val))
                    self.flat14.emit(False)
                elif pos0 == 15:
                    self.val15.emit(str(val))
                    self.flat15.emit(False)

                self._tiles[pos0] = val
                self._tiles[pos] = 0
                self._zero = pos
                time.sleep(0.5)

        time.sleep(1)
        self.widgetInaccessable.emit(False)
        self.widgetAccessable.emit(True)
        self._isRunning = False

if __name__ == '__main__':
    app = QApplication(sys.argv)
    ex = GameWindow()
    sys.exit(app.exec_())

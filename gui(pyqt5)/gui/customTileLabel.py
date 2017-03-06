"""
" ImageLabel is the custom QLabel object of image tiles for app15Puzzle.
" It supported click function for the number/picture image.
"
" author Meisze Wong
"        www.linkedin.com/pub/macy-wong/46/550/37b/
"        github.com/mwong510ca/Boggle_TrieDataStructure
"""

# !/usr/bin/env python3

from PyQt5.QtWidgets import QLabel
from PyQt5.QtCore import pyqtSignal


class ImageLabel(QLabel):
    clickedLabel = pyqtSignal()
    
    def __init(self, parent):
        QLabel.__init__(self, parent)
     
    def mousePressEvent(self, ev):
        self.clickedLabel.emit()


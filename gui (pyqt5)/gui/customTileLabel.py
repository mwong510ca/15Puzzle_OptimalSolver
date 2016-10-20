#!/usr/bin/env python3

from PyQt5.QtWidgets import *
from PyQt5.QtCore import pyqtSignal

class ImageLabel(QLabel):
    clickedLabel = pyqtSignal(str)
    
    def __init(self, parent):
        QLabel.__init__(self, parent)
     
    def mousePressEvent(self, ev):
        self.clickedLabel.emit('clicked()')


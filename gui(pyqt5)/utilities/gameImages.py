"""
" TileImages is the QObject to load the images for app15Puzzle.
"
" author Meisze Wong
"        www.linkedin.com/pub/macy-wong/46/550/37b/
"        www.github.com/mwong510ca/15Puzzle_OptimalSolver
"""

# !/usr/bin/env python3

import os

import math
from PyQt5.QtCore import QObject, QDir, QFile
from PIL import Image

# Globals
IMG_FOLDER_NAME = "images"
IMG_SIZE = 128


class TileImages(QObject):
    def __init__(self):
        super(TileImages, self).__init__()
        self.images_list = []
        self.images_number = False
        self.images_panda = False
        self.images_folder = IMG_FOLDER_NAME + QDir.separator()

        path_dir = QDir()
        if path_dir.exists(IMG_FOLDER_NAME):
            self.images_number = True
            file_list = []
            for idx in range(16):
                file_path = self.images_folder + "number_" + str(idx) + ".png"
                if not QFile(file_path).exists():
                    self.images_number = False
                    file_list = []
                    break
                try:
                    img = Image.open(file_path)
                    image_width, image_height = img.size  # Get image dimensions
                    if image_width != IMG_SIZE or image_height != IMG_SIZE:
                        resize_slice = Image.open(file_path)
                        resize_slice = resize_slice.resize((IMG_SIZE, IMG_SIZE), Image.ANTIALIAS)
                        resize_slice.save(os.path.join(os.getcwd(), file_path))
                    file_list.append(file_path)
                except IOError:
                    self.images_number = False
                    file_list = []
                    break
            self.images_list.append(file_list)
            if self.images_number:
                self.tile_images = True
                self.images_in_use = []
                for img in self.images_list[0]:
                    self.images_in_use.append(img)

            self.images_panda = True
            file_list = []
            for idx in range(16):
                file_path = self.images_folder + "panda_" + str(idx) + ".png"
                if not QFile(file_path).exists():
                    self.images_panda = False
                    file_list = []
                    break
                try:
                    img = Image.open(file_path)
                    image_width, image_height = img.size  # Get image dimensions
                    if image_width != IMG_SIZE or image_height != IMG_SIZE:
                        resize_slice = Image.open(file_path)
                        resize_slice = resize_slice.resize((IMG_SIZE, IMG_SIZE), Image.ANTIALIAS)
                        resize_slice.save(os.path.join(os.getcwd(), file_path))
                    file_list.append(file_path)
                except IOError:
                    self.images_panda = False
                    file_list = []
                    break
            self.images_list.append(file_list)
        else:
            path_dir.mkpath(IMG_FOLDER_NAME)
            self.actionPandaBabies.setEnabled(False)


    def loadCustomImages(self, filename):
        img = Image.open(filename)
        image_width, image_height = img.size  # Get image dimensions
        if image_width < 16 or image_height < 16:
            return None
        left = 0  # Set the left-most edge
        upper = 0  # Set the top-most edge
        width = math.floor(image_width / 4)
        height = math.floor(image_height / 4)

        count = 1
        for row in range(4):
            for col in range(4):
                bbox = (left, upper, left + width, upper + height)
                working_slice = img.crop(bbox)  # Crop image based on created bounds
                # Save your new cropped image.
                working_slice.save(os.path.join(os.getcwd(), self.images_folder + "temp.png"))
                resize_slice = Image.open(self.images_folder + "temp.png")
                resize_slice = resize_slice.resize((IMG_SIZE, IMG_SIZE), Image.ANTIALIAS)

                if count == 16:
                    count = 0
                resize_slice.save(
                    os.path.join(os.getcwd(), self.images_folder + "custom_" + str(count) + ".png"))
                count += 1
                left += width
            left = 0
            upper += height

        QFile.remove(self.images_folder + "temp.png")
        images_files = []
        for idx in range(16):
            images_files.append(self.images_folder + "custom_" + str(idx) + ".png")
        return images_files

    @staticmethod
    def getFolderName():
        return IMG_FOLDER_NAME

    def hasNumberImages(self):
        return self.images_number

    def hasPandaImages(self):
        return self.images_panda

    def getNumberImages(self):
        if not self.images_number:
            return None
        else:
            return self.images_list[0]

    def getPandaImages(self):
        if not self.images_panda:
            return None
        else:
            return self.images_list[1]

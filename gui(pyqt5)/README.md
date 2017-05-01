### Fifteen Puzzle 2 in 1 - Game and Solver

### How to Install
System Requirement :  
* [Java Virtual Machine version 8]: (Most of the machines already have it)  
* [Python 3.5]
* [py4j] for Python
* [PIL] or Pillow for Python 3.5
* [Qt] 5.6 or 5.7 (may not work on older versions)
* [PyQt5]

Download and unzip the [GuiApp15Puzzle.zip] file from my cloud drive. (Include all components - jar files, images, data files, etc)  

Start from Terminal: python3 app15PuzzleGameSolver.py

### How to Play
Meun bar - Settings  
Puzzle - You may display the puzzle in numbers, default image (panda babies), or use your own photo.  
Solver - You may change the timeout limit from 5 tp 60 seconds.  And auto move speed from 0.1 to 2.5 seconds.  

Puzzle Section - Shuffle the puzzle  
You may sway a pair of tiles each time to shuffle the puzzle.  Or generate a random puzzle by itself.  An option of 3 difficulty level to choose from.  

Game Section - Play the game  
Simple click on the puzzle to move the tile.  Timer start counting from the first move.  
A option to backward one move at a time, or reset to original setting.  

Solver Section - Search for optimal solution and apply the solution  
Choose your choice of solver version and heuristic function, then click "Find optimal solution."  
It will timeout after the preset limit excpet pattern database 7-8.  If a solution found before timeout, it will display a list of moves and apply the solution automatically.  

[Java Virtual Machine version 8]: http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html
[Python 3.5]: https://www.python.org/downloads/
[py4j]: https://www.py4j.org/install.html
[PIL]: https://wp.stolaf.edu/it/installing-pil-pillow-cimage-on-windows-and-mac/
[Qt]: https://www.qt.io
[PyQt5]: http://pyqt.sourceforge.net/Docs/PyQt5/installation.html
[GuiApp15Puzzle.zip]: https://my.pcloud.com/publink/show?code=XZqDDNZVM7tX7qmwGzU2i8qPXqyiRt06n9V

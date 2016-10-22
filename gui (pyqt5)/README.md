### Fifteen Puzzle 2 in 1 - Game and Solver

### How to Install
Requirement :  
* Java Virtual Machine version 8 (Most of the machines already have it)  
* Python 3.5
* PIL or Pillow for Python 3.5
* PyQt 5.6 or 5.7 (may not work on older versions)
* download and replace the database folder (optional)  

Click appGameSolver_15puzzle.py to start the application.

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

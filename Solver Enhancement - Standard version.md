### Standard version
  In the IDA*, it will loop on the same board more than once before it reach the solution.  In addition to avoid the backward move, I add the following optimization to improve performance.

### 1. Symmetry reduction  
  Let's look at the symmetry, take the diagonal axis from upper left corner to lower right corner:  
  <pre>
        Goal state       Tile conversion                      Position conversion
         1  2  3  4       1  2  3  4         1  5  9 13        0  1  2  3       0  4  8 12
         5  6  7  8       5  6  7  8   ->    2  6 10 14        4  5  6  7  ->   1  5  9 13
         9 10 11 12       9 10 11 12         3  7 11 15        8  9 10 11       2  6 10 14
        13 14 15  0      13 14 15  0         4  8 12  0       12 13 14 15       3  7 11 15  
        With these conversion:
         1  2  3  0       1  2  3  4         6  1 10 14       6  2  3  4
         5  6  7  4  ->   5  6  7  8         5  2  7  8  ->   1  5  9 13
         9 10 11  8       9 10 11 12         9  3 11 12       7 10 11 12
        13 14 15 12       0 13 14 15        13  4 15  0       8 14 15  0</pre>
        
        
  But some boards will be exactly the same after these conversion, such as goal state.  Or swap each pair tile with these conversion tables.  I called it "identical symmetry board".
  <pre>
         0  A  B  C
         A  5  D  E      0 will be land on position 0, 5, 10 or 15 only.
         B  D 10  F      If it land on corner (0 or 15), it has 2 moves.
         C  E  F 15      It it land on center (5 or 10), it has 4 moves. </pre>  
         
  * If it cannot find a solution by shift right, it cannot find a solution by shift down either.  Same for shift left and shift up pairs.  If it starts from the identical symmetry board, it will reduce the expansion by 50%.  
  * If it shift to the corner, the search is done.  Such as left shift to position 0 from position 1:  
    * Right shift to position 1 is the backward move - eliminated.
    * Down shift to position 4 is the symmetry move of Right shift - eliminated.  
  * If if shift to the center, the search will drop from 3 remaining moves to 1 move.  Such as left shift to position 5 from position 6:  
    * Right shift to position 6 is the backward move - eliminated.
    * Down shift to position 9 is the symmetry move of right shift - eliminated.
    * Left shift to position 4 is free - continue.
    * Up shift to position 1 is the symmetry move of Up shift - eliminated.  

### 2. Circular reduction
  While it keep making clockwise(cw) turns or counterclockwise(ccw) turns only, after 12 moves it will back to original stated.  At the 6th moves, both clockwise turns and counterclockwise turns will end at the same state.   
  <pre>
   move: 0      move: 1      move: 2      move: 3      move: 4      move: 5      move: 6
   0 1 x x      1 0 x x      1 2 x x      1 2 x x      0 2 x x      2 0 x x      2 3 x x
   3 2 x x      3 2 x x      3 0 x x      0 3 x x      1 3 x x      1 3 x x      1 0 x x
   x x x x      x x x x      x x x x      x x x x      x x x x      x x x x      x x x x
   x x x x      x x x x      x x x x      x x x x      x x x x      x x x x      x x x x
   original     1st move     cw:   1      cw:   2      cw:   3      cw:   4      cw:   5
   ccw: 11      ccw: 10      ccw:  9      ccw:  8      ccw:  7      ccw:  6      ccw:  5  
                move: 7      move: 8      move: 9      move: 10     move: 11     move: 12
                2 3 x x      0 3 x x      3 0 x x      3 1 x x      3 1 x x      0 1 x x
                0 1 x x      2 1 x x      2 1 x x      2 0 x x      0 2 x x      3 2 x x
                x x x x      x x x x      x x x x      x x x x      x x x x      x x x x
                x x x x      x x x x      x x x x      x x x x      x x x x      x x x x
                cw:   6      cw:   7      cw:   8      cw:   9      cw:  10      cw:  11
                ccw:  4      ccw:  3      ccw:  2      ccw:  1      1st move     original</pre>  
                
  I only need one path to moves 6.  At moves 5, I will take 4 clockwise turns and eliminate 6 counterclockwise turns.  Same for moves 7, I will take 4 counterclockwise turns and eliminate 6 clockwise turns. And so on...  To eliminate these re-visited boards:
  * The maximum limit of clockwise turns is 5.
  * The maximum limit of counterclockwise turns 4.

Notes: There are other longer complex circular paths, but those are too expansive to eliminate.  Those are not appear frequently as the about circular path.

### 3. Starting order detection:  
  Instead of using the hard code order Right -> Down -> Left -> Up, determine the starting order based on least estimate terminated at previous depth expansion.  It terminated at the same estimate, least nodes generated will go first.  When it hit the solution depth, it increase the possibility to solve the puzzle by the first move instead of loop to the last one.  But sometimes takes longer than the hard cord order.  
  
  * Example 1: Starting order changed in each depth level increment.
  * Example 2: Puzzle solved on 1st expansion at depth 67.
  * Example 3: Puzzle solved on 2nd expansion at depth 70.  It slow than hard code order, right will be the 1st move.

  
<pre>
        Example 1:         Depth
             6  5  9 14     54    (54) Right   -> (54) Down  -> (54) Left    -> (54) Up
             2  0  1 10     56    (37) Left    -> (41) Up    -> (45) Down    -> (51) Right
             8  3 12 13     58    (33) Up      -> (33) Down  -> (35) Left    -> (39) Right
             4  7 15 11                147             427
            estimate 54     60    (23) Left    -> (32) Down  -> (33) Up      -> (35) Right
                            62    (15) Down    -> (18) Left  -> (25) Up      -> (32) Right
                            64    (11) Down       (11) Left     (14) Up         (20) Right
                                       163182          164378
                                  Solved  
        Example 2:         Depth
            12 11 13 10     59    (59) Right   -> (59) Left  -> (59) Up
             6 15  3 14     61    (56) Right   -> (56) Left  -> (60) Up
             5  4  9  2                3               4
             8  7  0  1     63    (40) Up      -> (50) Right -> (53) Left
            estimate 59     65    (34) Up      -> (41) Right -> (45) Left
                            67    (16) Up         (26) Right    (38) Left
                                  Solved  
        Example 3:         Depth
            12 10  0 13     64    (64) Right   -> (64) Down  -> (64) Left
            15  9  5 14     66    (53) Right   -> (54) Down  -> (62) Left
             7 11  6  1     68    (44) Down    -> (45) Right -> (53) Left
             3  4  8  2     70    (31) Down    -> (36) Right    (42) Left
            estimate 64                           Solved</pre>

###Advanced Version  
Use a reference board to boost the initial estimate closer to solution moves:  

1.  If the initial estimate is way off the optimal solution, it waste a lot of time to scan all the boards until it reach the solution depth.  Boost the initial estimate will reduce the search time.

  The maximum moves of 15 puzzle is 80, and there 17 boards of it.  Let's take a start point of the 80 move board (as below) and the end point is goal state.  The distance between them is 80.  
  Now I also use the start point as goal state.  I can have 2 estimates, one towards the goal state and the other one toward to 80 move board.
  If I take a board from the shortest path from 80 move board, and it's 5 moves to the 80 move board.  Then it has exactly (80 - 5) 75 moves towards the goal state.
  If the is off the shortest path and it take 5 moves to the 80 move board, it take at least 75+ moves to the goal state.
    <pre>
        (80 moves)
      Reference board:      Example 1:      Example 2:      Example 3:      Example 4:
       0 15  9 13           11  0  9 13      0 15  9 13     12 11  9 13     11 15  9 13
      11 12 10 14           12 15 10 14     11 12 10 14      3 15 10 14     12 10 14  2
       3  7  6  2            3  7  6  2      3  7  6  2      0  7  6  2      3  7  6  0 
       4  8  5  1            4  8  5  1      4  8  5  1      4  8  5  1      4  8  5  1 
      Estimate to goal state         67              66              66              65
      Estimate to reference board     3               0               6               5
      Advanced estimate     80 - 3 = 77     80 - 0 = 80     80 - 6 = 74     80 - 5 = 75
      Actual solution                79              80              78              75</pre>
    
2.  It's not only true for 80 move boards, but also vaild for any solvable puzzle to be use as the reference board.  
  First step, move the zero space to the corner.  
  Second step, rotate the board and make zero at position 15.  
  Thrid step, generate the conversion key.  
    <pre>
    Example
    68 moves board    Step 1         Step 2                            Step 3
    15 11  8  3    15 11  8  0     9 14 12 15    0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15
    12  7  4  0 -> 12  7  4  3 -> 13 10  7 11 -> |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |
    14 10  6  5    14 10  6  5     2  6  4  8    0 13  9 15 11 14 10  7 12  1  6  8  3  5  2  4
     9 13  2  1     9 13  2  1     1  5  3  0</pre>

  I divided the board in 4 groups.  For each entry, it will store 4 boards per group.
    <pre>
        1 1 2 2      Group     0 1 x x    1 0 x x    1 2 x x    1 2 x x   
        1 1 2 2      idx 2:    3 2 x x    3 2 x x    3 0 x x    0 3 x x
        3 3 4 4                x x x x    x x x x    x x x x    x x x x
        3 3 4 4                x x x x    x x x x    x x x x    x x x x  

                     Group     x x 3 0    x x 3 1    x x 3 1    x x 0 1
                     idx 1:    x x 2 1    x x 2 0    x x 0 2    x x 3 2
                               x x x x    x x x x    x x x x    x x x x
                               x x x x    x x x x    x x x x    x x x x  
                               
                     Group     Transfer to symmetry board and store as Group 1   
                     idx 3:    x x x x    x x x x    x x x x    x x x x
                               x x x x    x x x x    x x x x    x x x x
                               3 2 x x    3 2 x x    3 0 x x    0 3 x x
                               0 1 x x    1 0 x x    1 2 x x    1 2 x x 

                     Group     x x x x    x x x x    x x x x    x x x x
                     idx 0:    x x x x    x x x x    x x x x    x x x x
                               x x 2 3    x x 2 3    x x 0 3    x x 3 0
                               x x 1 0    x x 0 1    x x 2 1    x x 2 1</pre>
                               
    If the board matched exactly the same these board, it will use pre-stored value.  Otherwise, only the corner zero will be use for advanced estimate calculation.

3.  Also store the partial solution.  
  Even the estimate matched with the solution moves, some board still take over 20 seconds to find the solution.  Such as the 68 moves example board above.  
  In order to store these value, there boards has been solved.  In addition to store it's solution moves, it also store the first eight directions of moves.  It will reduce the searching from 68 limit to 60 limit.  It's good enough to solve the puzzle within a second.  It is not necessary to store the full path.  It only take 16 bits (2 bits per direction x 8) for each partial solution.

4.  Automatcally save the board after search by pattern database 7-8.  
  I set the cutoff to 10 seconds with 5% buffer, which will make all puzzles solve within 10 seconds eventually.  For any puzzle that take more than 9.5 seconds to solve, the system will automatically store this board as reference board.  (A few line of code added in SolverPD.java)
  * over 10 seconds using Original Search, it will compare original estimate and advanced estimate.
    If they are the same, store the borad.
    If advanced estimate > original estimate, skip.  Undetmine runtime for advanced estimate.
  * over 10 seconds using Advanced Search, always store the board.
  * Each store board contain a set of 4 boards as describe above.  These other 3 boards will store an estimate without solution.  It will wait for next update to complete the full set.
  <pre>
    Example 1:    Original search: 11.8s Add      Advanced search: 11.8s Add
    org est 58     6  5  9 13          6  5  9 13          6  5  9 13          6  5  9 13 
    adv est 58     2  1 15 14          2  1 15 14          2  1 15 14          2  1 15 14 
    moves   70     3  8  0 10          3  8 12 10          3  8 12 10          3  8 10  0 
                   4  7 12 11          4  7  0 11          4  7 11  0          4  7 12 11 
    store value            70     est (70 - 1) 69     est (70 - 2) 68     est (70 - 1) 69
    partial solution      YES                  NO                  NO                  NO

    Example 2:    Original search: 10.6s Skip      Advanced search: 9.9s Add
    org est 58     6  5  9 13                                    6  2  3  4
    adv est 68     2  1 15 14                 ->                 5  1 14  0
    moves 70       3  8 12 10     This is group 3, convert to    9 12 15 10
                   4  0  7 11     to group 2 and store it.      13  8  7 11
                   
    Example 3:    Original search: 9.7s Skip      Advanced search: 4.8s  Skip   
    org est 56     2  5  9 13     
    adv est 70     3  6 10 14      This board will not store.  
    moves          0  7  1 15 
                   4  8 12 11</pre>


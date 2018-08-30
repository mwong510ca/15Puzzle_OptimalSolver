### Advanced Version  
Use a reference board to boost the initial estimate closer to solution moves:  

1.  If the initial estimate is way off the optimal solution, it waste a lot of time to scan all the boards until it reach the solution depth.  Boost the initial estimate will reduce the search time.

  The maximum moves of 15 puzzle is 80, and there are 17 boards of it.  Let's take a start point of the selected 80 move board as below and the end point is goal state.  
  
  I also use the start point as the goal state.  Now I can have 2 estimates instead of 1, one towards the goal state and the other one towards the 80 move board.  
  
  If I take a board on the shortest path to the 80 move board that is 5 moves to the 80 move board.  Which means it is exactly (80 - 5) 75 moves towards the goal state.  
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
  It will boost the estimate of the puzzles that near by the stored reference board.
    
2.  It's not only true for 80 move boards, but also valid for any solvable puzzle to be use as the reference board.  Each reference board will store the values for a group of 4 position as describe below. 
  * First step, move the zero space to the corner.  
  * Second step, rotate the board and make zero at position 15.  
  * Third step, generate the conversion key.  
<pre>
     Example
     68 moves board    Step 1         Step 2                            Step 3
     15 11  8  3    15 11  8  0     9 14 12 15    0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15
     12  7  4  0 -> 12  7  4  3 -> 13 10  7 11 -> |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |
     14 10  6  5    14 10  6  5     2  6  4  8    0 13  9 15 11 14 10  7 12  1  6  8  3  5  2  4
      9 13  2  1     9 13  2  1     1  5  3  0</pre>

  I divided the board in 4 groups.  For each entry, it will store 4 boards per group.
<pre>
        2 2 1 1      Group     0 1 x x    1 0 x x    1 2 x x    1 2 x x
        2 2 1 1      idx 2:    3 2 x x    3 2 x x    3 0 x x    0 3 x x
        3 3 0 0                x x x x    x x x x    x x x x    x x x x
        3 3 0 0                x x x x    x x x x    x x x x    x x x x  

                     Group     x x 3 0    x x 3 1    x x 3 1    x x 0 1
                     idx 1:    x x 2 1    x x 2 0    x x 0 2    x x 3 2
                               x x x x    x x x x    x x x x    x x x x
                               x x x x    x x x x    x x x x    x x x x  

                     Group     x x x x    x x x x    x x x x    x x x x
                     idx 3:    x x x x    x x x x    x x x x    x x x x
                               3 2 x x    3 2 x x    3 0 x x    0 3 x x
                               0 1 x x    1 0 x x    1 2 x x    1 2 x x
                               Transfer to symmetry board and store as Group 1  

                     Group     x x x x    x x x x    x x x x    x x x x
                     idx 0:    x x x x    x x x x    x x x x    x x x x
                               x x 2 3    x x 2 3    x x 0 3    x x 3 0
                               x x 1 0    x x 0 1    x x 2 1    x x 2 1 </pre>

If the board matched exactly the same these board, it will use pre-stored value.  Otherwise, only the corner zero will be use for advanced estimate calculation.

3.  The cost is tiny.  Compare to million of expansions, checking an additional thousand of reference boards is cheap.  To pick a good range is a little tricky, I try my best to explain in English. 
  * At least 30 initial estimate from goal state. - For any puzzle is far away from the goal state, it will check the reference collection.  
  * At most 30 estimate to the reference board. - For any puzzle is near by the reference board, it will use it as advanced estimate.
  * Reduce the range between best estimate and reference estimate.  Find the shortest path to the reference board, update the best estimate if it is closer to the reference board.
    <pre>
       Example 1:                  Reference 1:                Reference 2:
       12 15  4  8                 12 15  8  0                 12 15  4  0 
       11  0 10  3                 11 10  4  3                 11 10  3  8 
       14 13  6  2                 13  7  6  2                 14 13  6  2 
        9  5  7  1    19 ref       14  9  5  1    8 ref         9  5  7  1
       init est 52    boards                      boards                     Adv est  62
       ref moves:     reviewed              67    reviewed              65   Actual   66 moves
       max range:     out of      (67 - 52) 15    out of      (65 - 56)  9
       dist to ref:   range                 11    range                  3
       new est:       no change   (67 - 11) 56    no change   (65 -  3) 62   
       
       Example 2:    Reference 1:   Reference 2:   Reference 3:   Reference 4:   Reference 5:
       12 15 4 8       0 15 4 8       0 12 8 3       0 15 8 3       0 15 4 8      12 15 8 0
       14  7 6 3      12 11 7 3      14 15 7 4      12 14 7 4      12 14 7 3      14  7 4 3
       11 13 0 2      10 14 6 2      11 13 6 2      11 13 6 2      11 13 6 2      11 13 6 2
       10  9 5 1       9 13   1      10  9 5 1      10  9 5 1      10  9 5 1      10  9 5 1
       init est 52
       ref moves:            68             68             70             68             69
       max range:    (68-52) 16     (68-58) 10     (70-60) 10      (68-62) 6      (69-64) 5
       dist to ref:          10              8              8              4              3
       new est:  ->  (68-10) 58  ->  (68-8) 60  ->  (70-8) 62  ->  (68-4) 64  ->  (69-3) 66
                          Advanced estimate 66          Total reviewed reference boards: 13
                          Actual solution   68 moves    include 8 board are out of range
  </pre>

4.  Also store the partial solution.  
  Even the estimate matched with the solution moves, some board still take over 20 seconds to find the solution.  Such as the 68 moves example board above.  
  While storing these value, there boards has been solved.  In addition to store it's solution moves, it also store the first 8 directions of solution moves.  It will reduce the searching time from 68 limit to 60 limit.  It's good enough to solve the puzzle within a second.  It is not necessary to store the full path.  I stored them as 16 bits short value (2 bits per direction x 8) for each partial solution.

5.  Automatically save the board after solved by pattern database 7-8.  ([video])  
  I set the cutoff to 8 seconds with 5% buffer, which will make all puzzles solve within 8 seconds eventually.  For any puzzle that take more than 7.6 seconds to solve, the system will automatically store this board as reference board.  (A few lines of code added in SmartSolverPdbBase.java idaStar functions)
  * Over 8 seconds using Standard Search, it will compare original estimate and advanced estimate.
    If they are the same, store the board.
    If advanced estimate > original estimate, skip.  Unable to determine the runtime for advanced estimate.
  * Over 8 seconds using Advanced Search, always store the board.

  Each store board contain a set of 4 boards as describe above.  These other 3 boards will store an estimate without solution.  It will wait for next system update to complete the full set.  
<pre>
    Example 1:    Standard estimate: 52    Advanced estimate: same      Actual moves: 68    
                  Search time: 26.4s       
                  Add to reference collection after the search, either std/adv version.
                  Group 3 -> convert to Group 1
                       lookup 0           lookup 1           lookup 2           lookup 3
    15 11  8  3        12 15  4  0        12 15  4  7        12 15  4  7        12 15  0  7 
    12 14  7  4        11  8  3  7        11  8  3  0        11  8  0  3        11  8  4  3 
     0 13  6  2        14 10  6  2        14 10  6  2        14 10  6  2        14 10  6  2        
    10  9  5  1         4  3  5  1         4  3  5  1         4  3  5  1         4  3  5  1   
    store value    est (68 - 3) 65    est (68 - 2) 66    est (68 - 1) 67                 68
    partial solution            NO                 NO                 NO                YES
    after review full set
    store value change to       67                 66                 67                 68
    with partial solution      YES                YES                YES                YES  
    
    Example 2:    Standard estimate: 48    Advanced estimate: 64        Actual moves: 66
     0 15 11  3   Search time: 10.6s       Search time: 10.2s
    12  7  8  4   Skip                     Add to reference collection
    14 10  6  5                            after advanced search only.
     9 13  2  1 
                   
    Example 3:    Standard estimate: 48    Advanced estimate: 66        Actual moves: 66
    15 11  0  3   Search time: 8.4s        Search time: 0.47s      
    12  7  8  4   Skip                     Skip         
    14 10  6  5 
     9 13  2  1 </pre>

[video]: https://youtu.be/QBhoM1RySPQ


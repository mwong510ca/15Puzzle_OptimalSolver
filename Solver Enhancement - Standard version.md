###Standard Version - Symmetry reduction and Starting order detection:  
1. Symmetry reduction  
  Let's look at the symmetry, take the diagional axis from upper left corner to lower right corner:  
  <pre>
        Goal state       Tile conversion                      Position conversion
         1  2  3  4       1  2  3  4         1  5  9 13        0  1  2  3       0  4  8 12
         5  6  7  8       5  6  7  8   ->    2  6 10 14        4  5  6  7  ->   1  5  9 13
         9 10 11 12       9 10 11 12         3  7 11 15        8  9 10 11       2  6 10 14
        13 14 15  0      13 14 15  0         4  8 12  0       12 13 14 15       3  7 11 15  

        With these converstion:
         1  2  3  0       1  2  3  4         6  1 10 14       6  2  3  4
         5  6  7  4  ->   5  6  7  8         5  2  7  8  ->   1  5  9 13
         9 10 11  8       9 10 11 12         9  3 11 12       7 10 11 12
        13 14 15 12       0 13 14 15        13  4 15  0       8 14 15  0</pre>

  But some board will be exactly the same after these conversion, such as goal state.  Or swap each pair tile with these conversion tables.  I called it "identical symmetry board".
  <pre>
         0  A  B  C
         A  5  D  E      0 will be position 0, 5, 10 or 15 only.
         B  D 10  F      If it land on corner (0 or 15), it has 2 moves.
         C  E  F 15      It it land on center (5 or 10), it has 4 moves. </pre>
         
  * If it cannot find a solution by shift right, it cannot find a solution by shift down either.  Same for shift left and shift up pairs.  If it starts from the identical symmetry board, it will reduce the expansion by 50%.  
  * If it shift to the corner, the serach is done.  Such as left shift to position 0:  
    * Right shift to position 1 is the backward move - eliminated.
    * Down shift to position 4 is the symmetry move of Right shift - eliminated as well.  
  * If if shift to the center, the search will drop from 3 remaining moves to 1 move.  Such as left shift to position 5:  
    * Right shift to position 6 is the backward move - eliminated.
    * Down shift to position 9 is the symmetry move of right shift - eliminated as well.
    * Left shift to position 4 is free - continue.
    * Up shift to position 1 is the symmetry move of Up shift - eliminated duplicated move.  

2. Starting order detection:  
  Instead of using the hard code order Right -> Down -> Left -> Up, determine the starting order during the depth expansion based on total number of nodes expended from previous depth.  When it hit the solution depth, it increase the possibility to solve the puzzle by the first move instead of loop to the last one.  
  Example 1 find the solution on first move and example 2 find the solution on the third move.  
    <pre>
        Example 1:         Limit
            15 12 14 13     64    Right(0)     -> Down(0)    -> Left(0)      -> Up(0)
             7  3  9 10     66    Up(8)        -> Left(6)    -> Right(1)     -> Down(1)
            11  8  0  2     68    Left(35)     -> Up(29)     -> Right(11)    -> Left(4)
             4  5  6  1     70    Up(451)      -> Left(272)  -> Right(194)   -> Down(64)
            estimate 64     72    Up(6901)     -> Left(3719) -> Right(2504)  -> Down(566)
                            74    Up(86458) solved
        Example 2:
            15 12  9 13     64    Right(0)     -> Down(0)    -> Left(0)      -> Up(0)
            11  0  5 10     66    Up(32)       -> Right(25)  -> Left(11)     -> Down(2)
             3  7  8 14     68    Right(415)   -> Up(273)    -> Left(231)    -> Down(33)
             4  2  1  6     70    Right(4827)  -> Up(3243)   -> Left(2136)   -> Down(771)
            estimate 64     72    Right(47068) -> Up(34204)  -> Left(19504) solved</pre>

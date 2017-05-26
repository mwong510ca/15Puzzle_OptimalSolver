###How it works:  
Start from the goal state until it filled all slots of the static pattern.  [View Source Code]

Recall [PatternElement.java] - Here are the size for each group:  
    <pre>
        size     # of keys        # of formats        total size
        2        2! = 2           16C2 = 120          240
        3        3! = 6           16C3 = 560          3360
        4        4! = 24          16C4 = 1820         43680
        5        5! = 120         16C5 = 4368         524160
        6        6! = 720         16C6 = 8008         5765760
        7        7! = 5040        16C7 = 11440        57657600
        8        8! = 40320       16C8 = 12870        518918400  
        Example 5-5-5 static pattern:   1 1 1 1
                                        1 2 2 2
                                        3 3 2 2
                                        3 3 3 0
                    1st set:                2nd set:                3rd set:
                    1  2  3  4              x  x  x  x              x  x  x  x
                    5  x  x  x              x  6  7  8              x  x  x  x
                    x  x  x  x              x  x 11 12              9 10  x  x
                    x  x  x  x              x  x  x  x             13 14 15  x
        key:        0-1-2-3-4               0-1-2-3-4               0-1-2-3-4
        key code:   0                       0                       0
                    Notes: keys code of goal states is always 0 for any pattern.
        formats:    1111 1000 0000 0000     0000 0111 0011 0000     0000 0000 1100 1110</pre>   

------

1. Analysis the pattern and get all keys and formats ready to start.  
Validate the pattern in appropriate format.  Save a 2 ways conversions of tiles to and from pattern components.  Either load from the saved file or generate a new set keys and formats from PDElement.java.  Now ready to generate the pattern database.

2.  Determine the memory usage to generate the each pattern.  
The maximum move is 80, so the pattern value can store in byte value. For group 8, it take about 518918400 * 1 byte = .52 gigabytes.  
For pattern expansions, I need 2 set values.  One for current patterns, and one for next patterns after zero space shift with a tile.  To record these moves, all I need to store are the zeroes positions.  Group 2 - 7 use short value, and group 8 use byte value.  They will store in 2D \[key size\]\[format size\] array.

<pre>Group 2 - 7: pattern size x (1 byte pattern value + 2 byte x 2 current and next moves)
Group 8    : pattern size x (1 byte pattern value + 1 byte x 2 current and next moves)
                518918400 x (1 byte + 1 byte x 2) = 1.6 GB.</pre>
3. Think of the moves of zero space, instead of 1 shift at a time.  Here is how my FreeMove functions works:  
  * Zero is free to move on any (x) until it trigger changed.  It will stop next to any tiles.  If the moves are block by the tiles, it cannot across to the other side.  Group of 2 to 7, it will pass in 16 bits of its position 0000 0000 0000 0001, and group 8 as 8 bits of space order 0000 0001.  But both will return in 16 bits.  

    <pre>
        Example 1 - Pattern 6-6-3 : 1 1 1 1 
                                    1 1 2 2 
                                    3 3 3 2 
                                    3 3 3 0
                 Group 1 (6 tiles):       Group 2 (3 tiles):       Group 3 (6 tiles):
                     1  2  3  4               x  x  x  x               x  x  x  x
                     5  6  x  x               x  x  7  8               x  x  x  x
                     x  x  x  x               x  x  x 12               9 10 11  x
                     x  x  x  0               x  x  x  0              13 14 15  0
                         |                        |                        |
                         V                        V                        V
                     1  2  3  4               x  x  0  0               x  x  x  x
                     5  6  0  0               x  0  7  8               0  0  0  x
                     0  0  x  x               x  x  0 12               9 10 11  0
                     x  x  x  x               x  x  x  0              13 14 15  0
        key:        0-1-2-3-4-5              0-1-2                    0-1-2-3-4-5
        format:     1111 1100 0000 0000      0000 0011 0001 0000      0000 0000 1110 1110
        returns:    0000 0011 1100 0000      0011 0100 0010 0001      0000 1110 0001 0001    
  
        Example 2 - Pattern 7-8  :  1 1 1 1 
                                    1 1 1 1 
                                    2 2 2 2 
                                    2 2 2 0
                    Group 1 (8 tiles):           Group 2 (7 tiles):
                         1  2  3  4                  x  x  x  x
                         5  6  7  8                  x  x  x  x
                         x  x  x  x                  9 10 11 12
                         x  x  x  0                 13 14 15  0
                             |                           |
                             V                           V
                         1  2  3  4                  x  x  x  x
                         5  6  7  8                  x  x  x  x
                         0  0  0  0                  9 10 11 12
                         x  x  x  x                 13 14 15  0
        pass in:        0000 0001 (8 bits)          0000 0000 0000 0001 (16 bits)
        key:            0-1-2-3-4-5-6-7             0-1-2-3-4-5-6
        format:         1111 1111 0000 0000         0000 0000 1111 1110
        returns:        0000 0000 1111 0000         0000 0000 0000 0001</pre>
  * Each pattern may take multiple changes.  On the other hand, multiple changes may end in the same pattern.  So the pass in value may carry 1+ zeroes.

    <pre>
        Example 1 (16 bits):
        case 1:             case 2:
         5  x  x  x          5  x  x  x
        (0) 4  x  x          2  4  x  x
         2  x  1  3          x  1 (0) 3
         x  x  x  x          x  x  x  x
        zero shift down     zero shift left
             |                   |
             V                   V                                      Combine Together
         5  x  x  x          5  x  x  x         5  x  x  x    pass in:  0000 0000 1100 0000
         2  4  x  x          2  4  x  x    ->   2  4  x  x    key:      4-1-3-0-2
        (0) x  1  3          x (0) 1  3        (0)(0) 1  3    format:   1000 1100 0011 0000
         x  x  x  x          x  x  x  x         x  x  0  0    returns:  0000 0000 1100 0011    
         
        Example 2 (8 bits): 8 tiles after the shifts
        case 1: 1000 0000   case 2: 0001 0000   case 3: 0000 0001
        (0) x  x  6         x  x  x  6          x  x  x  6             (0) x  0  6
         3  x  7  x         3 (0) 7  x          3  x  7  x       ->     3 (0) 7  x
         4  1  2  8         4  1  2  8          4  1  2  8              4  1  2  8
         x  5  x  x         x  5  x  x          x  5  x (0)             x  5  0 (0)
                                                              pass in:  1001 0001
                                                              key:      5-2-6-3-0-1-7-4
                                                              format:   0001 1010 1111 0100
                                                              returns:  1010 0100 0000 0011</pre>

4. Now, Ready to generate the pattern.  
For each pattern set starts with the goal state, only one combo will carry the initial zero.  Loop through all combo; if the pattern value still zero, fill with current number of moves.  Repeat the process until all combo fill with value.  
Notes: Unlike the solver, eliminated backward moves will result in incorrect pattern values.  
See [sample output] of each default pattern.  

5.  Save a re-useable copy.  
Use FileChannel to save a copy and reuse by the solver.  

[View Source Code]: https://github.com/mwong510ca/HeuristicSearch-AdditivePatternDatabase-15Puzzle/blob/master/src/mwong/myprojects/fifteenpuzzle/solver/components/PatternDatabase.java
[PatternElement.java]: https://github.com/mwong510ca/HeuristicSearch-AdditivePatternDatabase-15Puzzle/blob/master/PatternElement.java%20-%20details.md
[Sample Output]: https://github.com/mwong510ca/HeuristicSearch-AdditivePatternDatabase-15Puzzle/tree/master/output/log_generate_data_files_osx_-Xmx4g.txt

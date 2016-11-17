###How it works:  
Generate the components will be use by the pattern database generator 
and puzzle solver.  [View Source Code]

1.  Determine the size of each pattern group:
    <pre>
            Example size of 3:     x  2  3  4 
                                   x  x  x  x
                                   x  x  x  x
                                   x  x  x  x
            There are 6 combinations 3!: 2 3 4, 2 4 3, 3 2 4, 3 4 2, 4 2 3, 4 3 2      

            And they are freely to land on 16 tiles:
            x  2  3  4        2  x  3  4        x  x  3  4        x  x  3  x
            x  x  x  x        x  x  x  x        2  x  x  x        2  x  x  4
            x  x  x  x        x  x  x  x        x  x  x  x        x  x  x  x
            x  x  x  x        x  x  x  x        x  x  x  x        x  x  x  x      etc...      
            The total are 16 * 15 * 14 / 6 = 16C3 = 16!/(3! * (16 - 3)!) = 560
            
            size     # of keys        # of formats        total size
            2        2! = 2           16C2 = 120          240
            3        3! = 6           16C3 = 560          3360
            4        4! = 24          16C4 = 1820         43680
            5        5! = 120         16C5 = 4368         524160
            6        6! = 720         16C6 = 8008         5765760
            7        7! = 5040        16C7 = 11440        57657600
            8        8! = 40320       16C8 = 12870        518918400</pre>   

2.  Make these keys and formats are universal and assigned a code from 0 to (size - 1).
    <pre>
            Example 5-5-5 static pattern:   1 1 1 1
                                            1 2 2 2
                                            3 3 2 2 
                                            3 3 3 0

                        1st set:                2nd set:                3rd set:
                         1  2  3  4              x  x  x  x              x  x  x  x
                         5  x  x  x              x  6  7  8              x  x  x  x
                         x  x  x  x              x  x 11 12              9 10  x  x
                         x  x  x  x              x  x  x  x             13 14 15  x      
            keys:       0-1-2-3-4               0-1-2-3-4               0-1-2-3-4
            formats:    1111 1000 0000 0000     0000 0111 0011 0000     0000 0000 1100 1110</pre>
            
No matter how I choose the group 5 pattern, I still use the same set of keys and formats.  And the key index and format index also represent it's pattern.
genKeys() and genFormats() will create all these keys and formats and assigned a code for them.  
Each key and format can be store in 16 bits, so each key x format individual pattern can be 
store in 32 bits as an integer value.

3.  Think of the possible changes of these keys:  
Each key may land on any format, the may shift left or right up to 3 space.
genRotateKeys() will create all links for each key.
    <pre>
            Here are some examples when tile 1 move down:
            0 (1) x  x     0 (1) 2  x      0 (1) 2  3      0 (1) 2  3        
            x  x  2  3     x  x  3  x      x  x  4  x      4  x  x  x        
                 |             |               |               |
                 V             V               V               V
            0  x  x  x     0  x  2  x      0  x  2  3      0  x  2  3        
            x (1) 2  3     x (1) 3  4      x (1) 4  x      4 (1) x  x        

            0-(1)-2-3      0-(1)-2-3-4     0-(1)-2-3-4     0-(1)-2-3-4
                 |             |               |               |
                 V             V               V               V
            no change      0-2-(1)-3-4     0-2-3-(1)-4     0-2-3-4-(1)
                           shift 1 right   shift 2 right   shift 3 right
</pre>
Tile 1 may rotate to right side when if move downward.  
Same as reverse, tile 1 may rotate to left side when if move upward.

4.  Think of the possible changes of these formats:
Similar to keys changes, format changes involve 4 direction move:  
    <pre>
            Left  : link to format change
            Right : link to format change
            Up    : link to format change plus the moved key shift to left
            Down  : link to format change plus the moved key shift to right
            Examples:
            Original        Left             Right           Up              Down
            1  0  0  0      Not              1  0  0  0      1 (1) 0  0      1  0  0  0
            1 (1) 0  0      Available        1  0 (1) 0      1  0  0  0      1  0  0  0
            0  0  1  1                       0  0  1  1      0  0  1  1      0 (1) 1  1                       
            0  0  0  0                       0  0  0  0      0  0  0  0      0  0  0  0
                                             no shift        shift 1 left    no shift     

            0  0  1  1      0  0  1  1       0  0  1  1      0 (1) 1  1      0  0  1  1     
            0 (1) 0  1     (1) 0  0  1       0  0 (1) 1      0  0  0  1      0  0  0  1 
            1  0  0  1      1  0  0  1       1  0  0  1      1  0  0  1      1 (1) 0  1                       
            0  0  0  0      0  0  0  0       0  0  0  0      0  0  0  0      0  0  0  0
                            no shift         no shift        shift 2 left    shift 2 right</pre>

5. Save a local copy for re-useable (optional):  
    Notes: Once Steps 1 - 4 has completed, It's ready to move on to next process, generate the pattern database [PatternDatabase.java]   
The common groups are size 3, 5, 6, 7, are 8.  Save these groups for re-useable.  If groups or file is not exists, it will generate a new set.  Since the generation time is less than 2 seconds, this step is optional.

[View Source Code]: https://github.com/mwong510ca/HeuristicSearch-AdditivePatternDatabase-15Puzzle/blob/master/src/mwong/myprojects/fifteenpuzzle/solver/components/PatternElement.java
[PatternDatabase.java]: https://github.com/mwong510ca/HeuristicSearch-AdditivePatternDatabase-15Puzzle/blob/master/PatternDatabase.java%20-%20details.md

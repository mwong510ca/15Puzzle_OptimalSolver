Beyond the lecture of the [priority queue] by Princeton University, [A* algorithm] with Manhattan distance is not able to solve all boards of [15 slide puzzle] due to out of memory.  After search on internet with coding practices, [Pattern database] seems to be the best solution for it. 

Unlike the 8 puzzle, full pattern database for 15 slide puzzle is too large, I have to use additive pattern database.  The most common statically partitioned additive pattern databases for 15 puzzle are 5-5-5, 6-6-3 or 7-8.  Generate 5-5-5 or 6-6-3 patterns are straight forward, but 7-8 pattern is challenge due to memory issue.  For a group of 8 tiles, there are 518,918,400 (40320 tiles combinations x 12870 group 8 pattern) patterns.  While I learn about the [Walking Distance by Ken'ichiro Takahashi] and reading his codes, I figure out a way to generate the 7-8 pattern with minimum 2GB ram and takes about 2.5 - 3 hours.  First separate the tile and format components and generate in [PatternElement.java], then use these components to generate the patterns in [PatternDatabase.java].

My solvers find only one optimal solution; once a solution found, it will stop searching.  Unlike [Herbert Kociemba's] .exe version has an option to display all optimal solutions.  To speed up the process, I added [symmetry reduction (Section 4)] and prioritize first move order during depth increment.  Until the depth has solution, it increase the possibility to solve first direction, instead of hard coded the fixed order such as Right -> Down -> Left -> Up.  With using pattern database 7-8, most of the puzzles are solved within a second.  A few boards may takes longer and a very rare board takes about 1 minute, examples:
<pre>
 0 15  8  3       6  5  9 13      11  5  9 13       0 15  8 13       0 15  8 13       0 11  9 13
12 11  7  4       2  1 10 14       2  6 10 14      12 11  3  7      12 11  9 10      12 15 10 14
14 10  6  5       3  7  0 15       3  7  0 15      14  9  6  2      14  3  6  2       3  7  6  2
 9 13  2  1       4  8 12 11       4  8 12  1 	    4 10  5  1       4  7  5  1       4  8  5  1
48 estimate      58 estimate      58 estimate      58 estimate      62 estimate      66 estimate
70 moves         72 moves         74 moves         76 moves         78 moves         80 moves
56.4 seconds     26.1 seconds     6.5 seconds      2.9 seconds      2.5 seconds      3.1 seconds
</pre>
To boost the performance, I added a self learning feature to store the puzzle that takes over 10 seconds to solve when using pattern database 7-8.  It will boost the estimate closer to the actual number of moves, over pattern database 7-8 maximum estimate 68.  For those stored reference boards, also stored first 8 solution moves, it boost the search time update to 1 second.

Read [Solver Enhancement] for details.

----

Heuristic Functions - 7 heuristic functions to choose from:  
Manhattan Distance  
Manhattan Distance with Linear Conflict  
Walking Distance  
Walking Distance + Manhattan Distance with Linear Conflict  
Additive Pattern Database 5-5-5 + Walking Distance  
Additive Pattern Database 6-6-3 + Walking Distance  
Additive Pattern Database 7-8  

Here are the console applications:  
* ApplicationSolver - Choose the heuristic function and a 15 puzzle, it will display the search time and number of node generated per depth increment.  And you have can view the solution moves end of the search.  Timeout if over 10 seconds except additive pattern database 7-8.
* ApplicationCompareHeuristic - Enter a 15 puzzle, it will run all type of heuristic functions, display the total run time and number of nodes generated.  Timeout if over 10 seconds except additive pattern database 7-8.
* ApplicationSolverStats - Choose the heuristic function, time limit between 1 seconds to 60 seconds, difficulty level of random board, number of times (T).  It will repeat T times of random boards, exclude the boards are out of time limit, and display the average time to solver the puzzle.
* ApplicationCustomPattern - You may choose your choice of preset patterns or enter the user defined pattern between group 2 to 7.  You can change timeout limit between 3 seconds to 5 minutes (300 seconds).

Notes:  If you want to try my solver application using static 7-8 pattern, highlight recommended to [download] the pre-generated database files from the cloud storage.

[15 slide puzzle]: https://en.wikipedia.org/wiki/15_puzzle
[priority queue]: http://algs4.cs.princeton.edu/24pq/
[A* algorithm]: https://en.wikipedia.org/wiki/A*_search_algorithm
[Pattern database]: https://www.aaai.org/Papers/JAIR/Vol22/JAIR-2209.pdf
[symmetry reduction (Section 4)]: https://heuristicswiki.wikispaces.com/file/view/Searching+with+pattern+database.pdf
[Herbert Kociemba's]: http://kociemba.org/fifteen/fifteensolver.html
[Walking Distance by Ken'ichiro Takahashi]: http://www.ic-net.or.jp/home/takaken/e/15pz/index.html
[PatternElement.java]: https://github.com/mwong510ca/HeuristicSearch-AdditivePatternDatabase-15Puzzle/blob/master/PatternElement.java%20-%20details.md
[PatternDatabase.java]: https://github.com/mwong510ca/HeuristicSearch-AdditivePatternDatabase-15Puzzle/blob/master/PatternDatabase.java%20-%20details.md
[Solver Enhancement]: https://github.com/mwong510ca/HeuristicSearch-AdditivePatternDatabase-15Puzzle/blob/master/Solver%20Enhancement%20-%20details.md
[download]: https://my.pcloud.com/publink/show?code=kZSoaLZgNeLhO2eu0RQcu9D2aXeOFgtioUV

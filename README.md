### Preface
I finished the programming assignment of [8puzzle] by Princeton University on Coursera.  It use priority queue to implement [A* algorithm] with Manhattan distance .  But it is not able to solve all boards of [15 slide puzzle] due to out of memory.  So I try to build an optimal solver for 15 puzzle.

### 15 puzzle optimal solver using additive pattern database 7-8
I search the information on internet, I found the [Pattern database].  The concept is clear, but I can't figure out how transfer to a program.  I also found the [Walking Distance by Ken'ichiro Takahashi], so I try that first.  I read his codes and write my version in java.  It improve my 15 puzzle solver, but it can solve up to 70 moves in resonable time.  So I go back to the pattern database.  

Unlike the 8 puzzle, full pattern database for 15 slide puzzle is too large, I have to use additive pattern database.  The most common statically partitioned additive pattern databases for 15 puzzle are 5-5-5, 6-6-3 or 7-8.  Generate 5-5-5 or 6-6-3 patterns are straight forward, but 7-8 pattern is challenge due to memory issue again.  For a group of 8 tiles, there are 518,918,400 (40320 tiles combinations x 12870 group 8 pattern) patterns.  Since [Herbert Kociemba's] can build the 7-8 pattern in c++, I may able to build my version in java.

While I learn about the Walking Distance by Ken'ichiro Takahashi, his techinque inspire me to figure out a way to generate the 7-8 pattern with minimum 2GB ram and takes about 2.5 - 3 hours.  First separate the tile and format components, and generate the links in [PatternElement.java].  Then I use these components to generate the patterns in [PatternDatabase.java].  
Generation time:  [pattern 5-5-5] 15 seconds, [pattern 6-6-3] 2 minutes, [pattern 7-8] 2.5 hours.

### Enhancement - optizimation
After I finished my 15 puzzle optimal solver, most of the puzzles are solved within a second.  Only a few puzzles still take about 2 minutes to solve. 
<pre>
             0 15  8  3     6  5  9 13    11  5  9 13     0 15  8 13     0 15  8 13     0 11  9 13
            12 11  7  4     2  1 10 14     2  6 10 14    12 11  3  7    12 11  9 10    12 15 10 14
            14 10  6  5     3  7  0 15     3  7  0 15    14  9  6  2    14  3  6  2     3  7  6  2
             9 13  2  1     4  8 12 11     4  8 12  1 	  4 10  5  1     4  7  5  1     4  8  5  1
Estimate:            48             58             58             58             62             66
Moves:               70             72             74             76             78             80
Time:            111.4s          56.6s          13.7s           5.8s           5.7s           3.es
Nodes:        492357819      231367077       46383751       20187376       18363209       12715201
</pre>
I analysis the behavior of 15 puzzle and my solver.  I added [symmetry reduction (Section 4)] and circular reduction to speed up the process. It improve the process time from 2 mintue to 45 seconds.
<pre>
Added symmetry reduction:
Time:             57.3s          27.9s           7.1s           2.9s           2.8s           3.2s
Nodes:        239416302      114874953       24447275       10207716        9123607       11456863
Added circular reduction:
Time:             44.2s          22.4s           5.9s           2.5s           2.4s           2.8s
Nodes:        177653815       89470609       20109676        8436494        7693686        9785986
</pre>
I also added starting ordering detection to increase the possibility to solve first move depth increment.  The starting order may vary each depth instead of hard coded the fixed order such as Right -> Down -> Left -> Up.  
Read [Solver Enhancement - standard version] for details.  

### Enhancement - [self learning feature]
45 seconds seems pretty good, but I still not satify with it.  The maximum estimate is 68 and the maximum moves is 80, so the [interactive deepening A*] has to loop through all nodes that will not have solution before it reach the solution depth.  If I can boost the estimate to the solution depth, it will drop the search time dreamically.   

I started with 17 known 80 moves puzzles as reference boards to boost the estimate over 68.  It works but still missed a lot.  I applied the same concept to any puzzle that takes over 8 seconds to solve, the solver with pattern database 7-8 will automatically stored it as reference board.  Also stored first 8 solution moves to boost the search time within a second.  

Now the solver has the self learning feature.   When it accumulate enough reference boards, the solver will solve any puzzle within 8 seconds (the preset cutoff setting) eventually.  

Read [Solver Enhancement - advanced version] for details.

----
###Console applications (screen recording on youtube - upload soon!)
option 1 - demo [self learning feature]  
option 2 - [compare enhancement]  
option 3 (default) - [compare heuristic functions]  
option 4 - [custom pattern]  
option 5 - [solver with display solution]  
option 6 - [timeout counter and average time per puzzle]  
Notes: runtime takes a little longer due to screen recording.

Heuristic Functions - 7 heuristic functions to choose from:  
Manhattan Distance  
Manhattan Distance with Linear Conflict  
Walking Distance  
Walking Distance + Manhattan Distance with Linear Conflict  
Additive Pattern Database 5-5-5 + Walking Distance  
Additive Pattern Database 6-6-3 + Walking Distance  
Additive Pattern Database 7-8  

Notes:  If you want to try my solver application using static 7-8 pattern, highlight recommended to [download] the pre-generated database files from the cloud storage.

[15 slide puzzle]: https://en.wikipedia.org/wiki/15_puzzle
[8puzzle]: http://algs4.cs.princeton.edu/24pq/
[A* algorithm]: https://en.wikipedia.org/wiki/A*_search_algorithm
[interactive deepening A*]: https://en.wikipedia.org/wiki/Iterative_deepening_A*
[Pattern database]: https://www.aaai.org/Papers/JAIR/Vol22/JAIR-2209.pdf
[symmetry reduction (Section 4)]: https://heuristicswiki.wikispaces.com/file/view/Searching+with+pattern+database.pdf
[Herbert Kociemba's]: http://kociemba.org/fifteen/fifteensolver.html
[Walking Distance by Ken'ichiro Takahashi]: http://www.ic-net.or.jp/home/takaken/e/15pz/index.html
[PatternElement.java]: https://github.com/mwong510ca/HeuristicSearch-AdditivePatternDatabase-15Puzzle/blob/master/PatternElement.java%20-%20details.md
[PatternDatabase.java]: https://github.com/mwong510ca/HeuristicSearch-AdditivePatternDatabase-15Puzzle/blob/master/PatternDatabase.java%20-%20details.md
[Solver Enhancement - standard version]: https://github.com/mwong510ca/HeuristicSearch-AdditivePatternDatabase-15Puzzle/blob/master/Solver%20Enhancement%20-%20Standard%20version.md
[Solver Enhancement - advanced version]: https://github.com/mwong510ca/HeuristicSearch-AdditivePatternDatabase-15Puzzle/blob/master/Solver%20Enhancement%20-%20Advanced%20version.md
[download]: https://my.pcloud.com/publink/show?code=kZSoaLZgNeLhO2eu0RQcu9D2aXeOFgtioUV
[pattern 5-5-5]: https://github.com/mwong510ca/HeuristicSearch-AdditivePatternDatabase-15Puzzle/blob/master/output/default%20pattern%205-5-5.txt
[pattern 6-6-3]: https://github.com/mwong510ca/HeuristicSearch-AdditivePatternDatabase-15Puzzle/blob/master/output/default%20pattern%206-6-3.txt
[pattern 7-8]: https://github.com/mwong510ca/HeuristicSearch-AdditivePatternDatabase-15Puzzle/blob/master/output/default%20pattern%207-8.txt
<!---
[self learning feature]: 
[compare enhancement]:
[compare heuristic functions]:
[custom pattern]:
[solver with display solution]:
[timeout counter and average time per puzzle]: 
-->

A guideline of custom settings - 15 puzzle solver (config.properties)

solutionDisplayRate in milliseconds:
minimum 100 (0.1s) to maximum 5000 (5s)
invalid setting restore to default 1000 (1s)

solverPatternIndex (PatternOptions.java):
0 for pattern 5-5-5
1 for pattern 6-6-3
2 for pattern 7-8
invalid setting restore to default 1 (pattern 6-6-3)

solverTimeoutLimit in seconds:
minimum 1 to maximum 300 (5 mins)
invalid setting restore to default 10

referenceCutoffBuffer:
minimum -5 (105%) to maximum 15 (85%)
invalid setting restore to default 5 (95%)

Strings for directory and filename:
directory:				database
fileExtension:			db
fileWd:					walking_distance
filePdbPrefix:			pattern_
filePdbElement:			element_
filePdbDefault:			_default
filePdbOption:			_option
fileRefCollection:		reference_accumulator
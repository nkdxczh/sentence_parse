#!/bin/bash

# pretty printing
cat results/sentence.txt | src/prettyprint.pl
# parsing
cat results/sentence.txt | java -cp bin/ Parser data/grammar.gr > results/parse.txt

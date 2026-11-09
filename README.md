# Assignment 4. Patient Zero

Student ID: **250245**  
Name: **Diyar Daniyarov**  
Group: **SE-2510**

## Run

Use Java 17 or newer, and run commands from this folder:

```sh
javac -encoding UTF-8 --release 17 -d out src/*.java
java -cp out Main
```

The four personal inputs were generated with:

```sh
java tools/MakeVariant.java 250245 data
```

Strain A has 916 letters, strain B has 921 letters, the genome has 27,958 letters, and the infection tree has 83 people. The marker is `AGCGTGAGC`. The personal run, including HTML generation, took about 0.06 seconds on this machine.

## Program output

```text
Mutations: 90
Shared core: 857
Marker hits: 6
First hit: 8160
Border length: 3
Shared words: 261
Tests needed: 35
Cheapest testing: 144
Longest chain: 31
```

## Lab console screenshot

The lab console is generated at `output/outbreak.html`. All five missions and all nine results agree with the lab.

![Patient Zero lab console](output/outbreak.png)

## Questions

### Mission 1: edit distance

`d[i][j]` is the minimum number of operations needed to turn the first i letters of A into the first j letters of B. It takes the minimum of deleting a letter, inserting a letter, or matching/replacing the last letter using the diagonal cell. The first row and column count the insertions or deletions needed when one prefix is empty.

### Mission 2: shared core

A subsequence keeps the order of letters but allows gaps; a substring must be consecutive. LCS can match a letter in A with a letter at a different position in B, which is useful when an insertion or deletion shifts later letters. That is why Shared core can be larger than the number of equal letters at the same positions.

### Mission 3: prefix function and KMP

`pi[i]` gives the length of the longest proper prefix of the marker prefix ending at i that is also its suffix. KMP uses these saved borders to shorten the matched prefix after a mismatch, so it never moves the text pointer backwards. The marker `AGCGTGAGC` has Border length 3 because its first and last three letters are both `AGC`, and no longer proper prefix is also a suffix.

### Mission 4: rolling hash

Different words can have the same hash, so the program compares their letters before counting a match. Moving a window updates its hash in constant time by removing the old letter, shifting the remaining hash, and adding the new letter. If many different windows share a hash, the program must do many comparisons and can approach quadratic time in the numbers of windows, multiplied by the window length.

### Mission 5: tree DP

`take[v]` is the cheapest cost in v's subtree when v is tested, and `skip[v]` is the cheapest cost when v is not tested. If v is tested, each child may use its cheaper state; if v is skipped, every child must be tested, and a second traversal reconstructs these choices. A greedy rule based on the number of links ignores testing costs: in a star with a centre costing 100 and three leaves costing 1 each, testing the leaves costs 3 instead of 100.

## AI use

I used OpenAI Codex to assist with understanding the assignment requirements, writing and debugging Java implementations, and preparing the README.

I compiled and tested the algorithms, checked their outputs against the expected results, and reviewed their implementation.

No external graph or DSU libraries were used.

## Timing (Mission 7)

Measured on 2026-11-08 with Temurin Java 25.0.1 using the unchanged `Bench` class. Each time is the average of three runs, in milliseconds. Raw output is in `output/benchmark.txt`.

| test | size | time (ms) |
|---|---|---|
| naive search | 20,000 / 40,000 / 80,000 text letters | 25.0 / 61.5 / 126.4 |
| KMP | 20,000 / 40,000 / 80,000 text letters | 1.6 / 1.1 / 0.3 |
| edit distance | 500 / 1,000 / 2,000 / 4,000 letters in each strain | 3.8 / 2.7 / 10.7 / 43.3 |

With the pattern length fixed at 2,000, naive search takes roughly twice as long when the larger text doubles, while KMP's theoretical time is O(n + m); its small measured times are dominated by JVM warm-up and timing noise. When both strain lengths double, the edit-distance table has about four times as many cells: the larger measurements increase from 2.7 to 10.7 to 43.3 ms. These timings illustrate the trends but do not prove complexity bounds.

## Complexity

For edit distance and LCS, n and m are the two strain lengths. For KMP, n is text length and m is marker length. For shared words, n and m are strain lengths and k is window length; v is the number of people.

| algorithm | time | extra space | assumptions |
|---|---|---|---|
| Edit distance | O(n*m) | O(n*m) | Full table; insertion, deletion and replacement cost 1 |
| LCS | O(n*m) | O(n*m) | Full table; subsequence can contain gaps |
| Prefix function | O(m) | O(m) | Fallbacks use previous prefix values |
| KMP | O(n + m) | O(m + h) | h returned match positions; overlapping matches count |
| Rabin-Karp shared words | Expected O(n + m + c*k); worst O(n*m*k) when n,m >= k | O(m) | c candidate word comparisons; expected constant-time hash-map operations; many collisions can force comparisons against all B windows |
| Testing DP and reconstruction | O(v) | O(v) | Tree with v-1 links, rooted at 0; includes recursion stack |
| Longest chain | O(v) | O(v) | Two deepest branches per person; includes recursion stack |

For a fixed window of 12 letters and small hash buckets, shared-word search is usually close to linear. Hash matches are always verified, so collisions affect speed, not correctness. Recursive tree traversals are suitable for the supplied size of about 100 people.

## Validation

```sh
java tools/Check.java
javac --release 17 -cp out -d out tools/AlgorithmTest.java
java -cp out AlgorithmTest
java -cp out Bench
```

- The supplied checker passes all nine output lines on tiny, small and dense.
- 200 short strain pairs pass independent edit-distance and exhaustive LCS checks.
- 300 random string cases pass brute-force prefix, KMP and shared-word checks.
- Additional checks cover overlapping marker hits, repeated A positions, and two different DNA words with an actual equal rolling hash.
- 150 small trees pass exhaustive minimum-cost and minimum-count cover checks, and longest-chain checks against BFS from every vertex.

The raw validation logs are saved in `output/check-results.txt` and `output/extra-checks.txt`. The supplied checker also generates lab pages for its examples, so run `java -cp out Main` again afterwards to restore the page for your personal data.

## Git and submission

Each coding mission has its own commit on main. All commits use the requested author `daniarov <daniarovdiar29@gmail.com>`.

- [x] Java code and four personal input files are included.
- [x] Examples and additional checks pass.
- [x] Output, explanation answers, AI use, timings and complexity are recorded.
- [x] Full name and group confirmed: Diyar Daniyarov, SE-2510.
- [ ] Review/adapt the code and explanations.
- [x] Real browser screenshot saved as `output/outbreak.png` and included above.
- [x] Main is published in the public GitHub repository.
- [x] Repository link submitted in Moodle (confirmed by the student).

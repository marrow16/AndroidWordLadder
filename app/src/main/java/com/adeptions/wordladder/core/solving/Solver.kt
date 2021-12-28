package com.adeptions.wordladder.core.solving

import com.adeptions.wordladder.core.words.Word
import java.util.*

class Solver(private val firstWord: Word, private val lastWord: Word, private val ladderLength: Int) {
    private val solutions: MutableList<Solution> = ArrayList()
    private var beginWord: Word = firstWord
    private var endWord: Word = lastWord
    private var reversed = false

    private lateinit var endDistances: WordDistanceMap

    private var solved: Boolean = false

    init {
        if (ladderLength < 1) {
            solved = true
        } else {
            // check for short-circuits...
            when (beginWord - endWord) {
                0 -> {
                    // same word - so there's only one solution...
                    solutions.add(Solution(beginWord))
                    solved = true
                }
                1 -> {
                    // the two words are only one letter different...
                    solutions.add(Solution(beginWord, endWord))
                    if (ladderLength == 2) {
                        // maximum ladder is 2 so we already have the only answer...
                        solved = true
                    } else if (ladderLength == 3) {
                        shortCircuitLadderLength3()
                        solved = true
                    }
                }
                2 -> if (ladderLength == 3) {
                    shortCircuitLadderLength3()
                    solved = true
                }
            }
        }
    }

    private fun shortCircuitLadderLength3() {
        // we can determine solutions by convergence of the two linked word sets...
        val startLinkedWords: MutableSet<Word> = HashSet(beginWord.linkedWords)
        startLinkedWords.retainAll(endWord.linkedWords)
        for (intermediateWord in startLinkedWords) {
            solutions.add(Solution(beginWord, intermediateWord, endWord))
        }
    }

    fun solve(): List<Solution> {
        if (!solved) {
            reversed = false
            // begin with the word that has the least number of linked words...
            // (this limits the number of pointless candidates explored!)
            reversed = beginWord.linkedWords.size > endWord.linkedWords.size
            if (reversed) {
                beginWord = lastWord
                endWord = firstWord
            }
            endDistances = WordDistanceMap(endWord, ladderLength - 1)
            beginWord.linkedWords
                .parallelStream()
                .filter { linkedWord -> endDistances.reachable(linkedWord, ladderLength) }
                .map { linkedWord -> CandidateSolution(this, beginWord, linkedWord) }
                .forEach(this::solve)
            solved = true
        }
        return solutions
    }

    private fun solve(candidate: CandidateSolution) {
        val last = candidate.lastWord
        if (last == endWord) {
            foundSolution(candidate)
        } else if (candidate.ladder.size < ladderLength) {
            last.linkedWords
                .parallelStream()
                .filter { linkedWord -> !candidate.seenWords.contains(linkedWord)
                        && endDistances.reachable(linkedWord, ladderLength, candidate.ladder.size) }
                .map { linkedWord -> CandidateSolution(candidate, linkedWord) }
                .forEach(this::solve)
        }
    }

    @Synchronized
    private fun foundSolution(candidate: CandidateSolution) {
        val solution = Solution(candidate, reversed)
        solutions.add(solution)
    }
}
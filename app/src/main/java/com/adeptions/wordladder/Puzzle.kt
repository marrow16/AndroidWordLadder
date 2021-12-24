package com.adeptions.wordladder

import com.adeptions.wordladder.core.solving.Solution
import com.adeptions.wordladder.core.words.Dictionary
import com.adeptions.wordladder.core.words.Word

class Puzzle(val dictionary: Dictionary, val startWord: Word, val endWord: Word, val ladderLength: Int, val solutions: List<Solution>) {
    private val letterPoints: Map<Char, Int> = mapOf(
        Pair('A', 1), Pair('B', 3), Pair('C', 3), Pair('D', 2), Pair('E', 1), Pair('F', 4), Pair('G', 2), Pair('H', 4),
        Pair('I', 1), Pair('J', 8), Pair('K', 5), Pair('L', 1), Pair('M', 3), Pair('N', 1), Pair('O', 1), Pair('P', 3),
        Pair('Q', 10), Pair('R', 1), Pair('S', 1), Pair('T', 1), Pair('U', 1), Pair('V', 4), Pair('W', 4), Pair('X', 8),
        Pair('Y', 4), Pair('Z', 10)
    )

    val wordLength: Int = dictionary.wordLength
    var points: Int = 0

    init {
        val distinctWords: MutableSet<Word> = HashSet()
        solutions.forEach {
            for (w in 0 until it.size) {
                distinctWords.add(it[w])
            }
        }
        for (word in distinctWords) {
            points += calculateWordScore(word)
        }
        points = ((points / solutions.size) * ladderLength) * 10
    }

    private fun calculateWordScore(word: Word): Int {
        var result: Int = 0
        for (ch in word.chars) {
            result += letterPoints[ch]?: 0
        }
        return result * wordLength
    }
}
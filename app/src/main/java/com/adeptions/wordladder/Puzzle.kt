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
    var rungScore: Int = 0
    var deductionWholeWord: Int = 0
    var deductionPatternHint: Int = 0
    var deductionPositionHint: Int = 0
    var pointsRemaining: Int = 0
    private val deductions: MutableSet<String> = HashSet()

    init {
        calculatePoints()
    }

    private fun calculatePoints() {
/*
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
        pointsRemaining = points
 */
        points = Math.round(
            ((ladderLength.toDouble() * 5.0) +
                    (wordLength.toDouble() * 3.5) -
                    (kotlin.math.log2(solutions.size.toDouble()) * 2.5)) * 100.0
        ).toInt()
        pointsRemaining = points
        val hiddenSteps = ladderLength - 2
        rungScore = kotlin.math.ceil(points.toDouble() / hiddenSteps.toDouble()).toInt()
        deductionWholeWord = rungScore
        deductionPatternHint = kotlin.math.ceil(rungScore * 0.50).toInt()
        deductionPositionHint = kotlin.math.ceil(rungScore.toDouble() / wordLength.toDouble()).toInt()
    }

    private fun calculateWordScore(word: Word): Int {
        var result = 0
        for (ch in word.chars) {
            result += letterPoints[ch]?: 0
        }
        return result * wordLength
    }

    fun deduct(deductionType: DeductionType, wordNumber: Int, letterNumber: Int): Boolean {
        val result = !deductions.contains(deductionType.name + ":" + wordNumber + ":" + letterNumber)
        if (result) {
            deductions.add(deductionType.name + ":" + wordNumber + ":" + letterNumber)
            pointsRemaining -= calculateDeduction(deductionType)
            if (pointsRemaining < 0) {
                pointsRemaining = 0
            }
        }
        return result
    }

    private fun calculateDeduction(deductionType: DeductionType): Int {
        return when(deductionType) {
            DeductionType.WORD_SUGGEST -> this.deductionWholeWord
            DeductionType.WORD_HINT -> this.deductionPatternHint
            DeductionType.LETTER_HINT -> this.deductionPositionHint
            DeductionType.SOLUTIONS_LOOK -> this.points
        }
/*
        return when(deductionType) {
            DeductionType.WORD_SUGGEST -> this.points / (this.ladderLength - 2)
            DeductionType.WORD_HINT -> (this.points / (this.ladderLength - 2)) / 2
            DeductionType.LETTER_HINT -> this.points / ((this.ladderLength - 2) * this.wordLength)
            DeductionType.SOLUTIONS_LOOK -> this.points
        }
 */
    }

    enum class DeductionType {
        WORD_SUGGEST,
        WORD_HINT,
        LETTER_HINT,
        SOLUTIONS_LOOK
    }
}
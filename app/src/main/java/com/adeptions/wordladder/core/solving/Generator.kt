package com.adeptions.wordladder.core.solving

import com.adeptions.wordladder.core.words.Dictionary
import com.adeptions.wordladder.core.words.Word
import java.util.*

private val RANDOM = Random()

class Generator(private val dictionary: Dictionary, private val ladderLength: Int) {
    fun generate(): List<Word> {
        val words = dictionary.wordsWithLadderLength(ladderLength)
        if (words.isEmpty()) {
            throw Exception("Oops! Sorry, couldn't generate word ladder")
        }
        val firstWord = words[RANDOM.nextInt(words.size)]
        val distanceMap = WordDistanceMap(firstWord, null)
        val lastWords = distanceMap.findAtDistance(ladderLength)
        if (lastWords.isEmpty()) {
            throw Exception("Oops! Sorry, couldn't generate word ladder")
        }
        return listOf(firstWord, lastWords[RANDOM.nextInt(lastWords.size)])
    }
}
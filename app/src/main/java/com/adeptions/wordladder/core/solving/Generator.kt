package com.adeptions.wordladder.core.solving

import com.adeptions.wordladder.core.words.Dictionary
import com.adeptions.wordladder.core.words.Word
import java.util.*

private const val MAGIC_NUMBER = 100
private const val MAX_RANDOM_ATTEMPTS = 16
private const val MAX_BRUTE_ATTEMPTS = 20
private val RANDOM = Random()

class Generator(private var dictionary: Dictionary, private var ladderLength: Int) {

    fun generate(): List<Word> {
        var attempts = 0
        while (attempts < MAX_RANDOM_ATTEMPTS) {
            val firstWord = randomWord()
            val distanceMap = WordDistanceMap(firstWord)
            val words = distanceMap.findAtDistance(ladderLength)
            if (!words.isEmpty()) {
                return listOf(firstWord, words[RANDOM.nextInt(words.size)])
            }
            attempts++
        }
        // brute force on all...
        val candidates: MutableList<List<Word>> = ArrayList()
        var foundStarts = 0
        val maxTime = System.currentTimeMillis() + 4000
        for (word in dictionary.words) {
            val distanceMap = WordDistanceMap(word)
            val words = distanceMap.findAtDistance(ladderLength)
            if (!words.isEmpty()) {
                foundStarts++
                candidates.add(listOf(word, words[RANDOM.nextInt(words.size)]))
            }
            if (foundStarts >= MAX_BRUTE_ATTEMPTS || System.currentTimeMillis() > maxTime) {
                break
            }
        }
        if (!candidates.isEmpty()) {
            return candidates[RANDOM.nextInt(candidates.size)]
        }
        throw Exception("Oops! Sorry, couldn't generate word ladder")
    }

/*
    fun generate(): List<Word> {
        var result: MutableList<Word> = ArrayList()
        var found = false
        var attempts = 0
        while (!found && attempts < MAX_RANDOM_ATTEMPTS) {
            val firstWord = randomWord()
            val distanceMap = WordDistanceMap(firstWord)
            val words = distanceMap.findAtDistance(ladderLength)
            if (!words.isEmpty()) {
                result.add(firstWord)
                result.add(words[RANDOM.nextInt(words.size)])
                found = true
            }
            attempts++
        }

        if (!found) {
            // brute force on all...
            val candidates: MutableList<MutableList<Word>> = ArrayList()
            var foundStarts = 0
            for (word in dictionary.words) {
                val distanceMap = WordDistanceMap(word)
                val words = distanceMap.findAtDistance(ladderLength)
                if (!words.isEmpty()) {
                    foundStarts++
                    candidates.add(mutableListOf(word, words[RANDOM.nextInt(words.size)]))
                }
                if (foundStarts >= MAX_BRUTE_ATTEMPTS) {
                    break
                }
            }
            if (!candidates.isEmpty()) {
                result = candidates[RANDOM.nextInt(candidates.size)]
                found = true
            }
        }
        check(found) { "Oops! Sorry, couldn't generate word ladder" }
        return result
    }
*/

    private fun randomWord(): Word {
        val words: List<Word> = dictionary.words
        var result = words[RANDOM.nextInt(words.size)]
        while (result.isIslandWord) {
            result = words[RANDOM.nextInt(words.size)]
        }
        return result
    }

}
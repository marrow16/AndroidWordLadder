package com.adeptions.wordladder.core.solving

import com.adeptions.wordladder.core.words.Word
import java.util.*
import java.util.Map
import kotlin.streams.toList

class WordDistanceMap(word: Word) {
    private val distances: MutableMap<Word, Int> = HashMap<Word, Int>()
    private var maximumLadderLength = 0

    init {
        distances[word] = 1
        val queue: Queue<Word> = ArrayDeque()
        queue.add(word)
        while (!queue.isEmpty()) {
            val nextWord: Word = queue.remove()
            nextWord.linkedWords.stream()
                .filter { linkedWord -> !distances.containsKey(linkedWord) }
                .forEach { linkedWord ->
                    queue.add(linkedWord)
                    distances.computeIfAbsent(linkedWord) { w: Word? ->
                        1 + distances[nextWord]!!
                    }
                }
        }
    }

    fun getDistance(toWord: Word): Int? =
        distances[toWord]

    fun setMaximumLadderLength(maximumLadderLength: Int) {
        this.maximumLadderLength = maximumLadderLength
    }

    fun findAtDistance(distance: Int): List<Word> {
        return distances.entries.stream()
            .filter { entry -> entry.value == distance }
            .map { entry -> entry.key }
            .toList()
    }

    fun reachable(word: Word): Boolean {
        val distance = distances.getOrDefault(word, -1)
        return (distance != -1
                && distance <= maximumLadderLength)
    }

    fun reachable(word: Word, existingSize: Int): Boolean {
        val distance = distances.getOrDefault(word, -1)
        return (distance != -1
                && distance + existingSize <= maximumLadderLength)
    }

    val minimum: Int
        get() {
            return distances.values.stream().min { o1, o2 -> compareValues(o1, o2) }.orElse(0)
        }

    val maximum: Int
        get() {
            return distances.values.stream().max { o1, o2 -> compareValues(o1, o2) }.orElse(0)
        }
}
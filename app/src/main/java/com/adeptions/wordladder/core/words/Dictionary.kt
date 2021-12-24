package com.adeptions.wordladder.core.words

import android.content.res.Resources
import com.adeptions.wordladder.R
import com.adeptions.wordladder.core.exceptions.BadWordException
import com.adeptions.wordladder.core.solving.WordDistanceMap
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer
import kotlin.streams.toList

const val MIN_WORD_LENGTH: Int = 2
const val MAX_WORD_LENGTH: Int = 15

class Dictionary(res: Resources, val wordLength: Int) {
    private val wordsMap: MutableMap<String, Word> = HashMap()
    private lateinit var nonIslands: List<Word>
    private val wordCountsByLadderLength: MutableMap<Int, AtomicInteger> = HashMap()
    private val wordsByLadderLength: MutableMap<Int, List<Word>> = HashMap()

    init {
        loadWordsFromResources(res)
        buildWordVariations()
    }

    private fun addWord(line: String) {
        if (line.isNotEmpty()) {
            val firstTab = line.indexOf('\t')
            val actualWord = line.substring(0, firstTab)
            val maxSteps: Int = Integer.parseInt(line.substring(firstTab + 1))
            if (actualWord.length != wordLength) {
                throw BadWordException(
                    "Word '" + actualWord + "' (length = "
                            + actualWord.length + ") cannot be loaded into " + wordLength + " letter word dictionary"
                )
            }
            wordsMap[actualWord] = Word(actualWord, maxSteps)
            for (i in 2 until maxSteps + 1) {
                wordCountsByLadderLength.computeIfAbsent(i) { _ -> AtomicInteger(0)}.incrementAndGet()
            }
        }
    }

    private fun buildWordVariations() {
        val variations: MutableMap<String, MutableList<Word>> = HashMap()
        wordsMap.values
            .forEach(Consumer { word: Word ->
                word.variationPatterns
                    .forEach { variationPattern ->
                        variations.computeIfAbsent(
                            variationPattern
                        ) { s: String? -> ArrayList() }
                            .add(word)
                    }
            })
        variations.values
            .forEach(Consumer<List<Word>> { wordVariants: List<Word> ->
                wordVariants.forEach(
                    Consumer { word: Word -> word.addLinkedWords(wordVariants) })
            })
    }

    private fun loadWordsFromResources(resources: Resources) {
        val ids = intArrayOf(
            R.raw.dict_2_letter_words,
            R.raw.dict_3_letter_words,
            R.raw.dict_4_letter_words,
            R.raw.dict_5_letter_words,
            R.raw.dict_6_letter_words,
            R.raw.dict_7_letter_words,
            R.raw.dict_8_letter_words,
            R.raw.dict_9_letter_words,
            R.raw.dict_10_letter_words,
            R.raw.dict_11_letter_words,
            R.raw.dict_12_letter_words,
            R.raw.dict_13_letter_words,
            R.raw.dict_14_letter_words,
            R.raw.dict_15_letter_words
        )
        val id = ids[wordLength - 2]
        resources.openRawResource(id).use { inputStream: InputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { br: BufferedReader ->
                var line: String? = null
                while (br.readLine().also { line = it } != null) {
                    addWord(line?: "")
                }
            }
        }
    }

    val size: Int
        get() = wordsMap.size

    val words: List<Word>
        get() = wordsMap.values.toList()

    val nonIslandWords: List<Word>
        get() {
            if (!::nonIslands.isInitialized) {
                nonIslands = wordsMap.values.filter { word -> !word.isIslandWord }
            }
            return nonIslands
        }

    fun wordsWithLadderLength(ladderLength: Int): List<Word> {
        return wordsByLadderLength.computeIfAbsent(ladderLength) {
            wordsMap.values.stream().filter{ word -> word.maximumSteps >= ladderLength }.toList()
        }
    }

    fun wordCountsByLadderLength(ladderLength: Int): Int {
        return wordCountsByLadderLength[ladderLength]!!.get()
    }
    operator fun get(word: String): Word? = wordsMap[word.uppercase()]

    operator fun contains(word: String): Boolean {
        return wordsMap.containsKey(word.uppercase())
    }

    object Factory {
        private val CACHE: MutableMap<Int, Dictionary> = HashMap()

        fun fromWord(res: Resources, word: String): Dictionary =
            forWordLength(res, word.length)

        fun forWordLength(res: Resources, wordLength: Int): Dictionary =
            CACHE.computeIfAbsent(wordLength) { len: Int -> Dictionary(res, len) }
    }
}
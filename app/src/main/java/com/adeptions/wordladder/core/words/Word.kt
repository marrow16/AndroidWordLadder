package com.adeptions.wordladder.core.words

import kotlin.streams.toList

private const val VARIATION_CHAR = '_'

class Word(actualWord: String, private val maxSteps: Int) {
    internal val actualWord = actualWord.uppercase()
    private val wordChars = this.actualWord.toCharArray()
    private val hash = this.actualWord.hashCode()
    internal val links: MutableList<Word> = ArrayList()

    val variationPatterns: List<String>
        get() = List(wordChars.size) { i ->
            val chars = wordChars.clone()
            chars[i] = VARIATION_CHAR
            String(chars)
        }

    val length: Int
        get() = actualWord.length

    val isIslandWord: Boolean
        get() = links.isEmpty()

    val maximumSteps: Int
        get() = maxSteps

    val linkedWords: List<Word>
        get() = this.links

/*
    fun differences(other: Word): Int {
        var result = 0
        for (ch in wordChars.indices) {
            result += if (wordChars[ch] != other.wordChars[ch]) 1 else 0
        }
        return result
    }
 */

    /**
     * Subtracting one word from another calculates differences
     */
    operator fun minus(other: Word): Int {
        var result = 0
        for (ch in wordChars.indices) {
            result += if (wordChars[ch] != other.wordChars[ch]) 1 else 0
        }
        return result
    }

    fun firstDifference(other: Word): Int {
        var result = -1
        for (ch in wordChars.indices) {
            if (wordChars[ch] != other.wordChars[ch]) {
                result = ch
                break
            }
        }
        return result
    }

    val chars: CharArray
        get() = wordChars.clone()

    operator fun get(index: Int): Char = wordChars[index]

    override fun toString(): String = this.actualWord

    override fun hashCode(): Int = this.hash

    override fun equals(other: Any?): Boolean
        = (other is Word)
                && other.actualWord == this.actualWord
}
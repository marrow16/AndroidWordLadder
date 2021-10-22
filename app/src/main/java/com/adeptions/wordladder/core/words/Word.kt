package com.adeptions.wordladder.core.words

import kotlin.streams.toList

private const val VARIATION_CHAR = '_'

class Word(actualWord: String, private val means: String) {
    private val actualWord = actualWord.uppercase()
    private val wordChars = this.actualWord.toCharArray()
    private val hash = this.actualWord.hashCode()
    private val links: MutableList<Word> = ArrayList()

    fun addLinkedWords(variants: List<Word>) {
        links.addAll(
            variants.stream()
                .filter { word -> this != word }
                .toList()
        )
    }

    val variationPatterns: List<String>
        get() = List(wordChars.size) { i ->
            val chars = wordChars.clone()
            chars[i] = VARIATION_CHAR
            String(chars)
        }

    val length: Int
        get() = actualWord.length

    val meaning: String
        get() = means

    val isIslandWord: Boolean
        get() = links.isEmpty()

    val linkedWords: List<Word>
        get() = this.links

    fun differences(other: Word): Int {
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
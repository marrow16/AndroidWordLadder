package com.adeptions.wordladder

import com.adeptions.wordladder.core.solving.Solution
import com.adeptions.wordladder.core.words.Dictionary
import com.adeptions.wordladder.core.words.Word

class Puzzle(val dictionary: Dictionary, val startWord: Word, val endWord: Word, val ladderLength: Int, val solutions: List<Solution>) {
    val wordLength: Int = dictionary.wordLength
}
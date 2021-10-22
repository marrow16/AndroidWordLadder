package com.adeptions.wordladder.core.solving

import com.adeptions.wordladder.core.words.Word
import java.util.HashSet

class CandidateSolution {
    private var solver: Solver
    internal val seenWords: MutableSet<Word> = HashSet<Word>()
    internal val ladder: MutableList<Word> = ArrayList()

    constructor(solver: Solver, startWord: Word, nextWord: Word) {
        this.solver = solver
        addWord(startWord)
        addWord(nextWord)
    }

    constructor(ancestor: CandidateSolution, nextWord: Word) {
        solver = ancestor.solver
        seenWords.addAll(ancestor.seenWords)
        ladder.addAll(ancestor.ladder)
        addWord(nextWord)
    }

    private fun addWord(word: Word) {
        seenWords.add(word)
        ladder.add(word)
    }

    internal val lastWord: Word
        get() = ladder[ladder.size - 1]
}
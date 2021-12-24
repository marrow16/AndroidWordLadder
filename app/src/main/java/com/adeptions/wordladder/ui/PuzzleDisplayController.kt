package com.adeptions.wordladder.ui

import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.GestureDetector
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.core.widget.addTextChangedListener
import com.adeptions.wordladder.MainActivity
import com.adeptions.wordladder.Puzzle
import com.adeptions.wordladder.core.solving.Solver
import com.adeptions.wordladder.core.words.Dictionary
import com.adeptions.wordladder.core.words.Word
import com.adeptions.wordladder.ui.SwipeDetector.*
import java.util.*
import kotlin.collections.HashSet
import kotlin.streams.toList

private val RANDOM = Random()

class PuzzleDisplayController(val main: MainActivity) {
    val controls: MainActivityControls = main.controls

    private lateinit var puzzle: Puzzle

    private var hintsOn: Boolean = true

    private var showingSolution: Int = -1;
    private var solutionsShowing = false

    private var internalUpdate: Boolean = false

    init {
        controls.previousSolutionButton.setOnClickListener { onPreviousSolutionClick() }
        controls.nextSolutionButton.setOnClickListener { onNextSolutionClick() }
        internalUpdate = true
        for (i in controls.puzzleEdits.indices) {
            val edit = controls.puzzleEdits[i]
            edit.addTextChangedListener(afterTextChanged = { s: Editable? ->
                if (!internalUpdate) {
                    onAfterLadderWordChanged(i + 1, edit, s)
                }
            })
            edit.addTextChangedListener(beforeTextChanged = { text: CharSequence?, start: Int, count: Int, after: Int ->
                onBeforeLadderWordChanged(edit, text, start, count, after)
            })
            edit.setOnFocusChangeListener { v, hasFocus ->
                if (!internalUpdate) {
                    onLadderWordFocusChanged(i + 1, edit, hasFocus)
                }
            }
            edit.setOnLongClickListener(View.OnLongClickListener { v: View? ->
                lookupEditWord(edit)
                true})
            val row = controls.puzzleRows[i]
            val rowTapDetector = GestureDetector(main, object : GestureDetector.SimpleOnGestureListener() {
                override fun onShowPress(e: MotionEvent) {
                    onLadderRowTap(i + 1, edit, row, e)
                }
            })
            row.setOnTouchListener { v, event -> rowTapDetector.onTouchEvent(event) }
        }
        for (s in controls.solutionWords.indices) {
            val wordView = controls.solutionWords[s]
            wordView.setOnLongClickListener(View.OnLongClickListener { v: View? ->
                    showSolutionWordMeaning(s, wordView)
                    true})
        }
        controls.firstWordTextView.setOnLongClickListener(View.OnLongClickListener { v: View? ->
            main.lookupWord(puzzle.startWord, null)
            true})
        controls.endWordTextView.setOnLongClickListener(View.OnLongClickListener { v: View? ->
            main.lookupWord(puzzle.endWord, null)
            true})
        internalUpdate = false
        SwipeDetector(controls.solutionsHeader) { _, swipeType -> onHeaderSwipe(swipeType) }
    }

    fun show(puzzle: Puzzle) {
        this.puzzle = puzzle
        showingSolution = -1
        resetViews()
        controls.pointsTotal.setText("" + puzzle.points)
        showSolutions(false)
        controls.show(DisplayView.PUZZLE)
        controls.solutionWords[0].requestFocus()
    }

    private fun resetViews() {
        internalUpdate = true
        val emsLength = puzzle.wordLength
        controls.firstWordTextView.setEms(emsLength)
        controls.firstWordTextView.setBackgroundResource(controls.backgroundNormal)
        controls.firstWordTextView.text = puzzle.startWord.toString()
        controls.endWordTextView.setEms(emsLength)
        controls.endWordTextView.setBackgroundResource(controls.backgroundNormal)
        controls.endWordTextView.text = puzzle.endWord.toString()
        val maxEditRow = puzzle.ladderLength - 1
        for (row in 0 until controls.solutionRows.size) {
            controls.solutionRows[row].visibility = if (row < puzzle.ladderLength) {
                View.VISIBLE
            } else {
                View.GONE
            }
            controls.solutionWords[row].setEms(emsLength)
            if (row in 1..78) {
                val edit = controls.puzzleEdits[row - 1]
                edit.setBackgroundResource(controls.backgroundNormal)
                edit.filters = arrayOf(InputFilter.LengthFilter(puzzle.wordLength), InputFilter.AllCaps())
                edit.setEms(emsLength)
                edit.setText("")
                controls.puzzleRows[row - 1].visibility = if (row < maxEditRow) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            }
        }
        internalUpdate = false
    }

    private fun onBeforeLadderWordChanged(edit: EditText, text: CharSequence?, start: Int, count: Int, after: Int) {
        val len = (text?:"").length
        if (count == 1 && after == 1 && len == puzzle.wordLength) {
            val next = Math.min(len - 1, start + 1)
            edit.post { selectNextCharacter(edit, next) }
        } else if (count == 0 && after == 1 && (len + 1) == puzzle.wordLength) {
            // last letter being added
            edit.post { edit.setSelection(len, len + 1) }
        }
    }

    private fun selectNextCharacter(edit: EditText, next: Int) {
        if (hintsOn) {
            val text = edit.text.toString()
            if (text.substring(next - 1, next) != ".") {
                edit.setSelection(next, next + 1)
            }
        } else {
            edit.setSelection(next, next + 1)
        }
    }

    private fun onAfterLadderWordChanged(position: Int, edit: EditText, s: Editable?) {
        if (s != null && this::puzzle.isInitialized) {
            controls.cancelToaster()
            val current = s.toString()
            if (hintsOn && (current == "?" || current == ",")) {
                suggestWord(position, edit, true)
            } else if (hintsOn && current == ".") {
                suggestLetterChanges(position, edit)
            } else if (hintsOn && current.length == puzzle.wordLength && hasJustOneFullstop(current)) {
                suggestLetters(position, edit, current)
            } else {
                updateHints(position, edit, current)
            }
        }
    }

    fun suggestWord(position: Int, edit: EditText, forced: Boolean) {
        if (hintsOn) {
            val current = edit.text.toString()
            val new: String = if (!forced && current.length > 0) {
                ""
            } else {
                val previousWord = getPreviousWord(position, false)
                val nextWord = getNextWord(position, false)
                val words: List<Word> = puzzle.solutions.stream()
                    .filter { solution -> previousWord == null || solution[position - 1] == previousWord }
                    .filter { solution -> nextWord == null || solution[position + 1] == nextWord}
                    .map { solution -> solution[position] }
                    .distinct()
                    .toList()
                if (words.isNotEmpty()) {
                    words[RANDOM.nextInt(words.size)].toString()
                } else {
                    ""
                }
            }
            edit.setText(new)
            edit.post { edit.setSelection(0, edit.text.length) }
            updateHints(position, edit, new)
        }
    }

    private fun suggestLetterChanges(position: Int, edit: EditText) {
        if (hintsOn) {
            val previousWord = getPreviousWord(position, true)
            val nextWord = getNextWord(position, true)
            var template = puzzle.solutions[0][position].chars
            if (previousWord == null && nextWord == null) {
                puzzle.solutions.forEach { solution ->
                    templatedDifference(template, solution[position], solution[position - 1], solution[position + 1])
                }
            } else if (previousWord != null && nextWord != null) {
                template = previousWord.chars
                templatedDifference(template, nextWord)
            } else if (previousWord != null) {
                template = previousWord.chars
                val isFirst = position == 1
                puzzle.solutions.stream()
                    .filter { solution -> isFirst || solution[position - 1] == previousWord }
                    .forEach { solution ->
                        templatedDifference(template, solution[position])
                    }
            } else {
                template = nextWord!!.chars
                val isLast = position == (puzzle.ladderLength - 2)
                puzzle.solutions.stream()
                    .filter { solution -> isLast || solution[position + 1] == nextWord }
                    .forEach { solution ->
                        templatedDifference(template, solution[position])
                    }
            }
            edit.setText(String(template))
            val firstUnderscore = template.indexOf('_')
            if (firstUnderscore > -1) {
                edit.setSelection(firstUnderscore, firstUnderscore + 1)
            }
        }
    }

    private fun suggestLetters(position: Int, edit: EditText, current: String) {
        if (hintsOn) {
            val fullStopAt: Int = current.indexOf('.')
            if (fullStopAt != -1) {
                val previousWord = getPreviousWord(position, true)
                val nextWord = getNextWord(position, true)
                val letters: List<Char> = puzzle.solutions.stream()
                    .filter { solution -> previousWord == null || solution[position - 1] == previousWord }
                    .filter { solution -> nextWord == null || solution[position + 1] == nextWord}
                    .map { solution -> solution[position][fullStopAt] }
                    .distinct()
                    .toList()
                    .sorted()
                if (letters.isNotEmpty()) {
                    val tryText = buildString {
                        append(" Try - ")
                        for (i in letters.indices) {
                            if (i > 0) {
                                if (i == (letters.size - 1)) {
                                    append(" or ")
                                } else {
                                    append(", ")
                                }
                            }
                            append(letters[i])
                        }
                    }
                    showHintForLadderWord(position, edit, tryText)
                    edit.setSelection(fullStopAt, fullStopAt + 1)
                } else {
                    showHintForLadderWord(position, edit, "Sorry, no suggested letters")
                }
            }
        }
    }

    private fun templatedDifference(template: CharArray, vararg words: Word) {
        for (ch in template.indices) {
            if (template[ch] != '_') {
                for (word in words) {
                    if (template[ch] != word[ch]) {
                        template[ch] = '_'
                    }
                }
            }
        }
    }

    private fun getPreviousWord(position: Int, incStart: Boolean): Word? {
        return if (position > 1) {
            val prevPosition = position - 1
            var result = puzzle.dictionary[controls.puzzleEdits[prevPosition - 1].text.toString()]
            if (result != null) {
                if (!puzzle.solutions.stream()
                        .anyMatch { solution -> solution[prevPosition] == result }) {
                    result = null
                }
            }
            result
        } else if (incStart) {
            puzzle.startWord
        } else {
            null
        }
    }

    private fun getNextWord(position: Int, incEnd: Boolean): Word? {
        return if (position < (puzzle.ladderLength - 2)) {
            val nextPosition = position + 1
            var result = puzzle.dictionary[controls.puzzleEdits[nextPosition - 1].text.toString()]
            if (result != null) {
                if (!puzzle.solutions.stream()
                        .anyMatch { solution -> solution[nextPosition] == result }) {
                    result = null
                }
            }
            result
        } else if (incEnd) {
            puzzle.endWord
        } else {
            null
        }
    }

    private fun hasJustOneFullstop(s: String): Boolean {
        return s.chars().filter { ch -> ch == '.'.code }.count() == 1L
    }

    fun nightModeChanged() {
        updateHints()
        showSolution()
    }

    private fun updateHints() {
        updateHints(-1, null, null)
    }

    private fun updateHints(position: Int, edit: EditText?, changed: String?) {
        var previousWord: Word? = puzzle.startWord
        var okCount = 0
        controls.firstWordTextView.setBackgroundResource(controls.backgroundNormal)
        controls.endWordTextView.setBackgroundResource(controls.backgroundNormal)
        val warningBackground = if (hintsOn) {
            controls.backgroundWarning
        } else {
            controls.backgroundNormal
        }
        for (i in 0 until puzzle.ladderLength - 2) {
            val onEdit = controls.puzzleEdits[i]
            val currentText: String = if (onEdit == edit) {
                changed?: ""
            } else {
                onEdit.text.toString()
            }
            var background = controls.backgroundNormal
            var word: Word? = null
            if (currentText.length == puzzle.wordLength) {
                if (hasAnyPunctuation(currentText)) {
                    background = controls.backgroundUnknown
                } else {
                    word = puzzle.dictionary[currentText]
                    if (word == null) {
                        background = controls.backgroundError
                        if (onEdit == edit) {
                            showHintForLadderWord(
                                position,
                                edit,
                                "Can't find \"$currentText\" in my dictionary!"
                            )
                        }
                    } else {
                        var addToOkCount = 1
                        if (hintsOn) {
                            background = controls.backgroundGood
                        }
                        var differencesOk = true
                        if (previousWord != null && word.differences(previousWord) != 1) {
                            background = warningBackground
                            addToOkCount = 0
                            differencesOk = false
                            if (hintsOn && onEdit == edit) {
                                showHintForLadderWord(
                                    position, edit,
                                    "\"$word\" not one letter different to previous"
                                )
                            }
                        }
                        if (differencesOk) {
                            val nextWord = if (i == puzzle.ladderLength - 3) {
                                puzzle.endWord
                            } else {
                                puzzle.dictionary[controls.puzzleEdits[i + 1].text.toString()]
                            }
                            if (nextWord != null && word.differences(nextWord) != 1) {
                                background = warningBackground
                                addToOkCount = 0
                                differencesOk = false
                                if (hintsOn && onEdit == edit) {
                                    showHintForLadderWord(
                                        position, edit,
                                        "\"$word\" not one letter different to next"
                                    )
                                }
                            }
                        }
                        if (differencesOk) {
                            val possibleWords: Set<Word> = HashSet(puzzle.solutions.stream()
                                .map { solution -> solution[i + 1] }
                                .toList())
                            if (!possibleWords.contains(word)) {
                                background = warningBackground
                                addToOkCount = 0
                                if (hintsOn && onEdit == edit) {
                                    showHintForLadderWord(
                                        position, edit,
                                        "\"$word\" not in any solutions here"
                                    )
                                }
                            }
                        }
                        okCount += addToOkCount
                    }
                }
            } else if (currentText.length > 0) {
                background = controls.backgroundUnknown
            }
            onEdit.setBackgroundResource(background)
            previousWord = word
        }
        if (okCount == puzzle.ladderLength - 2) {
            controls.firstWordTextView.setBackgroundResource(controls.backgroundGood)
            controls.endWordTextView.setBackgroundResource(controls.backgroundGood)
            for (i in 0 until puzzle.ladderLength - 2) {
                controls.puzzleEdits[i].setBackgroundResource(controls.backgroundGood)
            }
            //Toast.makeText(main, "You solved it!", Toast.LENGTH_SHORT).show()
            controls.createToaster("You solved it!").show()
        }
    }

    private fun hasAnyPunctuation(s: String): Boolean {
        return s.chars().anyMatch { ch -> ch < 'A'.code || ch > 'Z'.code }
    }

    private fun showSolutionWordMeaning(position: Int, wordView: TextView) {
        main.lookupWord(puzzle.solutions[showingSolution][position], null)
    }

    private fun showHintForLadderWord(position: Int, edit: EditText, message: String) {
        val itemHeight = controls.firstWordTextView.height
        val yPosition = (16 + controls.solutionsHeaderArea.height + (position * itemHeight)) - controls.puzzleScroller.scrollY
        val toaster = controls.createToaster()
        toaster.setText(message)
        toaster.setGravity(Gravity.TOP + Gravity.CENTER_HORIZONTAL, 0, yPosition)
        toaster.show()
    }

    private fun showSolutions(show: Boolean) {
        solutionsShowing = show
        if (!solutionsShowing) {
            controls.viewSwitching = true
            val sy = controls.puzzleScroller.scrollY
            controls.solutionLadderView.visibility = View.GONE
            controls.puzzleLadderView.visibility = View.VISIBLE
            updateSolutionsHeader()
            controls.puzzleScroller.scrollY = sy
        } else {
            val sy = controls.puzzleScroller.scrollY
            controls.puzzleLadderView.visibility = View.GONE
            controls.solutionLadderView.visibility = View.VISIBLE
            showSolution()
            controls.puzzleScroller.scrollY = sy
        }
    }

    private fun updateSolutionsHeader() {
        val num = puzzle.solutions.size.toString()
        if (!solutionsShowing) {
            controls.previousSolutionButton.visibility = View.INVISIBLE
            controls.nextSolutionButton.visibility = if (hintsOn) {
                View.VISIBLE
            } else {
                View.INVISIBLE
            }
            controls.solutionsHeader.text = if (puzzle.solutions.size == 1) {
                val result = SpannableString("There is 1 solution")
                result.setSpan(ForegroundColorSpan(-0xff8000), 9, 10, 0)
                result.setSpan(StyleSpan(Typeface.BOLD), 9, 10, 0)
                result
            } else {
                val result = SpannableString("There are $num solutions")
                result.setSpan(ForegroundColorSpan(-0xff8000), 10, 10 + num.length, 0)
                result.setSpan(StyleSpan(Typeface.BOLD), 10, 10 + num.length, 0)
                result
            }
        } else {
            controls.previousSolutionButton.visibility = View.VISIBLE
            controls.nextSolutionButton.visibility = if (showingSolution < (puzzle.solutions.size - 1)) {
                View.VISIBLE
            } else {
                View.INVISIBLE
            }
            val on = (showingSolution + 1).toString()
            val spannable = SpannableString("Solution $on of $num")
            spannable.setSpan(ForegroundColorSpan(-0xff8000), 9, 9 + on.length, 0)
            spannable.setSpan(StyleSpan(Typeface.BOLD), 9, 9 + on.length, 0)
            spannable.setSpan(ForegroundColorSpan(-0xff8000), 13 + on.length, 13 + on.length + num.length, 0)
            spannable.setSpan(StyleSpan(Typeface.BOLD), 13 + on.length, 13 + on.length + num.length, 0)
            controls.solutionsHeader.text = spannable
        }
    }

    private fun showPreviousSolution() {
        showingSolution--
        if (showingSolution < 0) {
            showSolutions(false)
        } else {
            showSolution()
        }
    }

    private fun showNextSolution() {
        showingSolution++
        showSolution()
    }

    private fun showSolution() {
        if (showingSolution < 0) {
            showingSolution = 0
        } else if (showingSolution >= puzzle.solutions.size) {
            showingSolution = puzzle.solutions.size - 1
        }
        updateSolutionsHeader()
        val solution = puzzle.solutions[showingSolution]
        var previousWord: Word? = null
        val enteredWords: Array<Word?> = Array(puzzle.ladderLength - 2) {
            i -> puzzle.dictionary[controls.puzzleEdits[i].text.toString()]
        }
        for (i in 0 until puzzle.ladderLength) {
            val word = solution[i]
            val spannable = SpannableString(word.toString())
            if (previousWord != null) {
                val diffAt = previousWord.firstDifference(word)
                spannable.setSpan(ForegroundColorSpan(-0xff8000), diffAt, diffAt + 1, 0)
            }
            controls.solutionWords[i].text = spannable
            previousWord = word
            if (i > 0 && i < (puzzle.ladderLength - 1)) {
                controls.solutionWords[i].setBackgroundResource(if (word == enteredWords[i - 1]) {
                    controls.backgroundGood
                } else {
                    controls.backgroundNormal
                })
            } else {
                controls.solutionWords[i].setBackgroundResource(controls.backgroundGood)
            }
        }
    }

    private fun onPreviousSolutionClick() {
        if (hintsOn) {
            showPreviousSolution()
        }
    }

    private fun onNextSolutionClick() {
        if (hintsOn) {
            if (!solutionsShowing) {
                showSolutions(true)
            } else {
                showNextSolution()
            }
        }
    }

    internal fun hintsToggle() {
        hintsOn = !hintsOn
        updateHints()
        showSolutions(false)
    }

    internal var isHintsOn: Boolean
        get() = hintsOn
        set(value) {
            if (value != hintsOn) {
                hintsOn = value
                updateHints()
            }
        }

    fun onLadderWordFocusChanged(position: Int, edit: EditText, hasFocus: Boolean) {
        if (controls.viewSwitching) {
            controls.viewSwitching = false
            if (hasFocus && controls.lastFocused != null && edit != controls.lastFocused) {
                controls.lastFocused!!.requestFocus()
            }
        } else if (hasFocus) {
            controls.lastFocused = edit
        }
    }

    private fun lookupEditWord(edit: EditText) {
        main.lookupWord(puzzle.dictionary[edit.text.toString()], edit.text.toString())
    }

    private fun onHeaderSwipe(type: SwipeType) {
        if (hintsOn) {
            when (type) {
                SwipeType.LEFT_TO_RIGHT -> {
                    if (!solutionsShowing) {
                        showSolutions(true)
                    } else if (showingSolution < (puzzle.solutions.size - 1)) {
                        showNextSolution()
                    }
                }
                SwipeType.RIGHT_TO_LEFT -> {
                    if (solutionsShowing) {
                        showSolutions(false)
                    }
                }
            }
        }
    }

    internal fun saveState(bundle: Bundle) {
        bundle.putInt("puzzleWordLength", puzzle.wordLength)
        bundle.putInt("puzzleLadderLength", puzzle.ladderLength)
        bundle.putString("puzzleStartWord", puzzle.startWord.toString())
        bundle.putString("puzzleEndWord", puzzle.endWord.toString())
        bundle.putBoolean("puzzleHints", hintsOn)
        bundle.putBoolean("puzzleSolutionsShowing", solutionsShowing)
        bundle.putInt("puzzleShowingSolution", showingSolution)

        PuzzleCache.save(puzzle)
    }

    internal fun restoreState(bundle: Bundle) {
        val startWord: String? = bundle.getString("puzzleStartWord")
        val endWord: String? = bundle.getString("puzzleEndWord")
        if (startWord != null && endWord != null) {
            val cachedPuzzle = PuzzleCache.retrieve(startWord, endWord)
            if (cachedPuzzle != null) {
                puzzle = cachedPuzzle
            } else {
                // unlikely, but we may have to rebuild the puzzle...
                val dictionary = Dictionary.Factory.fromWord(main.resources, startWord)
                val actualStartWord = dictionary[startWord]
                val actualEndWord = dictionary[endWord]
                val ladderLength = bundle.getInt("puzzleLadderLength")
                val solver = Solver(actualStartWord!!, actualEndWord!!, ladderLength)
                val solutions = solver.solve().sorted()
                puzzle = Puzzle(dictionary, actualStartWord, actualEndWord, ladderLength, solutions)
            }
            hintsOn = bundle.getBoolean("puzzleHints", true)
            solutionsShowing = bundle.getBoolean("puzzleSolutionsShowing", false)
            showingSolution = bundle.getInt("puzzleShowingSolution", -1)
            controls.setHintsOn(hintsOn)
            resetViews()
            showSolutions(solutionsShowing)
        }
    }

    private fun onLadderRowTap(position: Int, edit: EditText, row: TableRow, event: MotionEvent) {
        val half = row.width / 2
        val text = edit.text.toString()
        if (edit.hasFocus() && text.isNotEmpty()) {
            val start = Math.min(edit.selectionStart, edit.selectionEnd)
            val end = Math.max(edit.selectionStart, edit.selectionEnd)
            if (event.x > half) {
                // move right...
                if (start == -1 || start >= text.length - 1) {
                    edit.setSelection(text.length - 1, text.length)
                } else if (start < end) {
                    edit.setSelection(start + 1, start + 2)
                } else {
                    edit.setSelection(start, start + 1)
                }
            } else {
                // move left
                if (start < 1) {
                    edit.setSelection(0, 1)
                } else {
                    edit.setSelection(start - 1, start)
                }
            }
        } else {
            edit.requestFocus()
            if (text.isNotEmpty()) {
                if (event.x > half) {
                    edit.setSelection(text.length - 1, text.length)
                } else {
                    edit.setSelection(0, 1)
                }
            }
        }
    }

    object PuzzleCache {
        private val CACHE: MutableMap<String, Puzzle> = HashMap()

        fun save(puzzle: Puzzle) {
            CACHE.putIfAbsent(puzzle.startWord.toString() + ">" + puzzle.endWord.toString(), puzzle)
        }

        fun retrieve(startWord: String, endWord: String): Puzzle? {
            return CACHE.get(startWord.uppercase() + ">" + endWord.uppercase())
        }
    }
}
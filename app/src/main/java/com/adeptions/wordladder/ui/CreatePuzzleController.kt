package com.adeptions.wordladder.ui

import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import com.adeptions.wordladder.*
import com.adeptions.wordladder.core.solving.WordDistanceMap
import com.adeptions.wordladder.core.words.Dictionary
import com.adeptions.wordladder.core.words.MAX_WORD_LENGTH
import com.adeptions.wordladder.core.words.MIN_WORD_LENGTH
import com.adeptions.wordladder.core.words.Word
import java.text.DecimalFormat
import java.util.*

const val MIN_LADDER_LENGTH: Int = 3
val MAX_LADDER_LENGTHS = arrayOf(
    5,  // 2-letter
    9,  // 3-letter
    16, // 4-letter
    27, // 5-letter
    43, // 6-letter
    65, // 7-letter
    80, // 8-letter
    34, // 9-letter
    11, // 10-letter
    27, // 11-letter
    7,  // 12-letter
    5,  // 13-letter
    7,  // 14-letter
    5   // 15-letter
)

private val RANDOM = Random()

class CreatePuzzleController(private val main: MainActivity) {
    private var wordLength: Int = main.wordLength
    private var dictionary: Dictionary = main.dictionary
    private val controls: MainActivityControls = main.controls
    private val countFormatter = DecimalFormat("#,##0")

    private lateinit var ladderLengthSpinnerAdapters: Array<ArrayAdapter<Int>>

    init {
        populateSpinners()

        controls.createButton.setOnClickListener { onCreateClick() }
        controls.cancelButton.setOnClickListener { onCancelClick() }
        controls.randomStartWordButton.setOnClickListener { onRandomStartWordClick() }
        controls.randomEndWordButton.setOnClickListener { onRandomEndWordClick() }

        controls.startWordEdit.addTextChangedListener(afterTextChanged = {
            onStartEndWordChanged(controls.startWordEdit)
        })
        controls.startWordEdit.filters = arrayOf(InputFilter.LengthFilter(wordLength), InputFilter.AllCaps())

        controls.endWordEdit.addTextChangedListener(afterTextChanged = {
            onStartEndWordChanged(controls.endWordEdit)
        })
        controls.endWordEdit.filters = arrayOf(InputFilter.LengthFilter(wordLength), InputFilter.AllCaps())

        controls.startWordEdit.setOnLongClickListener(View.OnLongClickListener {
            showWordMeaning(controls.startWordEdit)
            true})
        controls.endWordEdit.setOnLongClickListener(View.OnLongClickListener {
            showWordMeaning(controls.endWordEdit)
            true})

    }

    private fun showWordMeaning(edit: EditText) {
        main.lookupWord(dictionary[edit.text.toString()], edit.text.toString())
    }

    fun show() {
        controls.show(DisplayView.CREATE)
        wordLength = main.wordLength
        dictionary = main.dictionary
        controls.dictionaryWordCount.setText(countFormatter.format(dictionary.size) + " words")
        resetControls(main.ladderLength)
    }

    fun nightModeChanged() {
        controls.startWordEdit.setBackgroundResource(controls.backgroundNormal)
        controls.endWordEdit.setBackgroundResource(controls.backgroundNormal)
    }

    private fun onStartEndWordChanged(edit: EditText) {
        val text = edit.text.toString()
        if (text.isEmpty()) {
            edit.setBackgroundResource(controls.backgroundNormal)
        } else if (text == "?") {
            if (edit == controls.startWordEdit) {
                onRandomStartWordClick()
            } else {
                onRandomEndWordClick()
            }
        } else if (text.length == wordLength) {
            val word: Word? = dictionary[text]
            if (word == null) {
                edit.setBackgroundResource(controls.backgroundError)
                showHintForStartEndWordEdit(edit, "Can't find \"$text\" in my dictionary!")
            } else {
                edit.setBackgroundResource(controls.backgroundNormal)
            }
        } else {
            edit.setBackgroundResource(controls.backgroundWarning)
        }
    }

    private fun showHintForStartEndWordEdit(edit: EditText, message: String) {
        val yPosition:Int = edit.y.toInt() - controls.createScroller.scrollY
        showToast(message, edit.x.toInt(), yPosition)
    }

    private fun showToast(message: String) {
        showToast(message, null, null)
    }

    private fun showToast(message: String, xPos: Int?, yPos: Int?) {
        controls.cancelToaster()
        val toast = controls.createToaster()
        toast.setText(message)
        if (xPos != null && yPos != null) {
            toast.setGravity(Gravity.TOP + Gravity.LEFT, xPos, yPos)
        }
        toast.show()
    }

    private fun setCreateControlsEnabled(enabled: Boolean) {
        controls.wordLengthSpinner.isEnabled = enabled
        controls.ladderLengthSpinner.isEnabled = enabled
        controls.randomStartWordButton.isEnabled = enabled
        controls.startWordEdit.isEnabled = enabled
        controls.randomEndWordButton.isEnabled = enabled
        controls.endWordEdit.isEnabled = enabled
        controls.createButton.isEnabled = enabled
        controls.cancelButton.isEnabled = enabled
    }

    private fun populateSpinners() {
        val wordLengths = Array(MAX_WORD_LENGTH - MIN_WORD_LENGTH + 1) { wl -> wl + MIN_WORD_LENGTH }
        controls.wordLengthSpinner.adapter = ArrayAdapter(main, android.R.layout.simple_spinner_dropdown_item, wordLengths)
        selectedWordLength = wordLength
        controls.wordLengthSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                onWordLengthSelected(position + MIN_WORD_LENGTH)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) { }
        }

        ladderLengthSpinnerAdapters = Array(MAX_WORD_LENGTH - MIN_WORD_LENGTH + 1) { i ->
            val maxLadderLength = MAX_LADDER_LENGTHS[i]
            val maxLengths = Array(maxLadderLength - MIN_LADDER_LENGTH + 1) { ll -> ll + MIN_LADDER_LENGTH }
            ArrayAdapter(main, android.R.layout.simple_spinner_dropdown_item, maxLengths)
        }

        controls.ladderLengthSpinner.adapter = ladderLengthSpinnerAdapters[main.wordLength - MIN_WORD_LENGTH]
        controls.ladderLengthSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                onLadderLengthSelected(position + MIN_LADDER_LENGTH)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) { }
        }
    }

    private fun onWordLengthSelected(wordLength: Int) {
        if (this.wordLength != wordLength) {
            this.wordLength = wordLength
            dictionary = Dictionary.Factory.forWordLength(main.resources, wordLength)
            controls.dictionaryWordCount.setText(countFormatter.format(dictionary.size) + " words")
            updateCreationControls()
        }
    }

    private fun onLadderLengthSelected(ladderLength: Int) {
        val count = dictionary.wordCountsByLadderLength(ladderLength)
        controls.ladderLengthWordCount.setText(countFormatter.format(count) + " words")
    }

    private fun resetControls(ladderLength: Int) {
        resetCreationStartEndWordEdits()
        selectedWordLength = wordLength
        controls.ladderLengthSpinner.adapter = ladderLengthSpinnerAdapters[wordLength - MIN_WORD_LENGTH]
        selectedLadderLength = ladderLength
    }

    private fun updateCreationControls() {
        resetCreationStartEndWordEdits()
        val wasLadderLength = selectedLadderLength
        controls.ladderLengthSpinner.adapter = ladderLengthSpinnerAdapters[wordLength - MIN_WORD_LENGTH]
        selectedLadderLength = wasLadderLength
    }

    private fun resetCreationStartEndWordEdits() {
        controls.startWordEdit.setText("")
        controls.endWordEdit.setText("")
        controls.startWordEdit.filters = arrayOf(InputFilter.LengthFilter(wordLength), InputFilter.AllCaps())
        controls.endWordEdit.filters = arrayOf(InputFilter.LengthFilter(wordLength), InputFilter.AllCaps())
    }

    private fun onCreateClick() {
        setCreateControlsEnabled(false)
        var isOk = false
        var puzzleStartWord: Word? = null
        var puzzleEndWord: Word? = null
        var ladderLength = selectedLadderLength
        try {
            val startWordEntry: String = controls.startWordEdit.text.toString()
            if (startWordEntry.isNotEmpty()) {
                if (startWordEntry.length != wordLength) {
                    throw Exception("Start word length incorrect")
                }
                val word = dictionary[startWordEntry]
                    ?: throw Exception("Start word \"$startWordEntry\" not in my dictionary")
                if (word.isIslandWord) {
                    throw Exception("Start word \"$startWordEntry\" is an 'island word' (cannot be converted to any other word)")
                }
                puzzleStartWord = word
            }
            val endWordEntry: String = controls.endWordEdit.text.toString()
            if (endWordEntry.isNotEmpty()) {
                if (endWordEntry.length != wordLength) {
                    throw Exception("End word length incorrect")
                }
                val word = dictionary[endWordEntry]
                    ?: throw Exception("End word \"$endWordEntry\" not in my dictionary")
                if (word.isIslandWord) {
                    throw Exception("End word \"$startWordEntry\" is an 'island word' (cannot be converted to any other word)")
                }
                puzzleEndWord = word
            }
            if (puzzleStartWord != null && puzzleEndWord != null) {
                if ((puzzleStartWord - puzzleEndWord) < 2) {
                    throw Exception("Start and end word must be at least two characters difference")
                }
                val distanceMap = WordDistanceMap(puzzleStartWord, null)
                val distance: Int = distanceMap.getDistance(puzzleEndWord)
                    ?: throw Exception("Cannot reach end word \"$endWordEntry\" from start word \"$startWordEntry\"")
                if (distance != ladderLength) {
                    ladderLength = distance
                    selectedLadderLength = ladderLength
                    throw Exception("Ladder steps changed automatically - press 'CREATE' again")
                }
            } else if (puzzleStartWord != null) {
                val distanceMap = WordDistanceMap(puzzleStartWord, null)
                val possibles = distanceMap.findAtDistance(ladderLength)
                if (possibles.isEmpty()) {
                    val max = distanceMap.maximum
                    if (max >= MIN_LADDER_LENGTH) {
                        selectedLadderLength = max
                        throw Exception("Ladder steps changed automatically - press 'CREATE' again")
                    } else {
                        throw Exception("Sorry, just cannot create a useful puzzle using start word \"$startWordEntry\"")
                    }
                }
                puzzleEndWord = possibles[RANDOM.nextInt(possibles.size)]
            } else if (puzzleEndWord != null) {
                val distanceMap = WordDistanceMap(puzzleEndWord, null)
                val possibles = distanceMap.findAtDistance(ladderLength)
                if (possibles.isEmpty()) {
                    val max = distanceMap.maximum
                    if (max >= MIN_LADDER_LENGTH) {
                        selectedLadderLength = max
                        throw Exception("Ladder steps changed automatically - press 'CREATE' again")
                    } else {
                        throw Exception("Sorry, just cannot create a useful puzzle using end word \"$endWordEntry\"")
                    }
                }
                puzzleStartWord = possibles[RANDOM.nextInt(possibles.size)]
            }
            isOk = true
        } catch (e: Exception) {
            isOk = false
            showToast(e.message?: "Internal Error")
        } finally {
            setCreateControlsEnabled(true)
        }
        if (isOk) {
            main.wordLength = wordLength
            main.ladderLength = ladderLength
            main.dictionary = dictionary
            if (puzzleStartWord != null && puzzleEndWord != null) {
                main.showPuzzle(puzzleStartWord, puzzleEndWord)
                controls.show(DisplayView.PUZZLE)
            } else if (main.generatePuzzle()) {
                controls.show(DisplayView.PUZZLE)
            } else {
                showToast("Sorry, couldn't seem to generate a puzzle - please try again")
            }
        }
    }

    private fun onCancelClick() {
        controls.show(DisplayView.PUZZLE)
    }

    private fun onRandomStartWordClick() {
        val endWordEntry: String = controls.endWordEdit.text.toString()
        val endWord: Word? = dictionary[endWordEntry]
        val words = if (endWord != null) {
            val distanceMap = WordDistanceMap(endWord, null)
            distanceMap.findAtDistance(selectedLadderLength)
        } else {
            dictionary.wordsWithLadderLength(selectedLadderLength)
        }
        if (words.isNotEmpty()) {
            controls.startWordEdit.setText(words[RANDOM.nextInt(words.size)].toString())
        } else {
            controls.startWordEdit.setText("")
        }
    }

    private fun onRandomEndWordClick() {
        val startWordEntry: String = controls.startWordEdit.text.toString()
        val startWord: Word? = dictionary[startWordEntry]
        val words = if (startWord != null) {
            val distanceMap = WordDistanceMap(startWord, null)
            distanceMap.findAtDistance(selectedLadderLength)
        } else {
            dictionary.wordsWithLadderLength(selectedLadderLength)
        }
        if (words.isNotEmpty()) {
            controls.endWordEdit.setText(words[RANDOM.nextInt(words.size)].toString())
        } else {
            controls.endWordEdit.setText("")
        }
    }

    private var selectedWordLength: Int
        get() = controls.wordLengthSpinner.selectedItemPosition + MIN_WORD_LENGTH
        set(value) {
            controls.wordLengthSpinner.setSelection(value - MIN_WORD_LENGTH)
        }

    private var selectedLadderLength: Int
        get() = controls.ladderLengthSpinner.selectedItemPosition + MIN_LADDER_LENGTH
        set(value) {
            val newIndex = if ((value - MIN_LADDER_LENGTH) >= controls.ladderLengthSpinner.adapter.count) {
                controls.ladderLengthSpinner.adapter.count - 1
            } else {
                value - MIN_LADDER_LENGTH
            }
            controls.ladderLengthSpinner.setSelection(newIndex)
        }

    internal fun saveState(bundle: Bundle) {
        bundle.putInt("creatorWordLength", wordLength)
        bundle.putInt("creatorLadderLength", selectedLadderLength)
        bundle.putString("creatorStartWord", controls.startWordEdit.text.toString())
        bundle.putString("creatorEndWord", controls.endWordEdit.text.toString())
    }

    internal fun restoreState(bundle: Bundle) {
        wordLength = bundle.getInt("creatorWordLength", 3)
        dictionary = Dictionary.Factory.forWordLength(main.resources, wordLength)
        selectedWordLength = wordLength
        controls.dictionaryWordCount.setText(countFormatter.format(dictionary.size) + " words")
        controls.ladderLengthSpinner.adapter = ladderLengthSpinnerAdapters[wordLength - MIN_WORD_LENGTH]
        selectedLadderLength = bundle.getInt("creatorLadderLength", 4)
        controls.startWordEdit.filters = arrayOf(InputFilter.LengthFilter(wordLength), InputFilter.AllCaps())
        controls.endWordEdit.filters = arrayOf(InputFilter.LengthFilter(wordLength), InputFilter.AllCaps())
        controls.startWordEdit.setText(bundle.getString("creatorStartWord", ""))
        controls.endWordEdit.setText(bundle.getString("creatorEndWord", ""))
    }
}
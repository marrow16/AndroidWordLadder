package com.adeptions.wordladder.ui

import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import com.adeptions.wordladder.*
import com.adeptions.wordladder.core.solving.WordDistanceMap
import com.adeptions.wordladder.core.words.Dictionary
import com.adeptions.wordladder.core.words.MAX_WORD_LENGTH
import com.adeptions.wordladder.core.words.MIN_WORD_LENGTH
import com.adeptions.wordladder.core.words.Word
import java.util.*

const val MIN_LADDER_LENGTH: Int = 3
val MAX_LADDER_LENGTHS = arrayOf(
    5,  // 2-letter
    9,  // 3-letter
    15, // 4-letter - actual max is 16, but it takes too long to find them
    19, // 5-letter
    36, // 6-letter - limited for performance
    50, // 7-letter - limited to reasonable
    50, // 8-letter - limited to reasonable
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

    private lateinit var ladderLengthSpinnerAdapters: Array<ArrayAdapter<Int>>

    init {
        populateSpinners()

        controls.createButton.setOnClickListener { onCreateClick() }
        controls.cancelButton.setOnClickListener { onCancelClick() }
        controls.randomStartWordButton.setOnClickListener { onRandomStartWordClick() }
        controls.randomEndWordButton.setOnClickListener { onRandomEndWordClick() }

        controls.startWordEdit.addTextChangedListener( object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                onStartEndWordChanged(controls.startWordEdit, s)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        controls.startWordEdit.filters = arrayOf(InputFilter.LengthFilter(wordLength), InputFilter.AllCaps())
        controls.endWordEdit.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                onStartEndWordChanged(controls.endWordEdit, s)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        controls.endWordEdit.filters = arrayOf(InputFilter.LengthFilter(wordLength), InputFilter.AllCaps())
    }

    fun show() {
        controls.show(DisplayView.CREATE)
        wordLength = main.wordLength
        dictionary = main.dictionary
        resetControls(main.ladderLength)
    }

    fun nightModeChanged() {
        controls.startWordEdit.setBackgroundResource(controls.backgroundNormal)
        controls.endWordEdit.setBackgroundResource(controls.backgroundNormal)
    }

    private fun onStartEndWordChanged(edit: EditText, s: Editable?) {
        if (s != null) {
            if (s.length == 0) {
                edit.setBackgroundResource(controls.backgroundNormal)
            } else if (s.length == wordLength) {
                if (s.toString() !in dictionary) {
                    edit.setBackgroundResource(controls.backgroundError)
                    val toast: Toast = Toast.makeText(main, "Can't find \"$s\" in my dictionary!",
                        Toast.LENGTH_LONG)
                    toast.setGravity(Gravity.TOP + Gravity.LEFT, edit.x.toInt(), edit.y.toInt() + 48)
                    toast.show()
                } else {
                    edit.setBackgroundResource(controls.backgroundNormal)
                }
            } else {
                edit.setBackgroundResource(controls.backgroundWarning)
            }
        }
    }

    private fun onWordLengthSelected(wordLength: Int) {
        if (this.wordLength != wordLength) {
            this.wordLength = wordLength
            dictionary = Dictionary.Factory.forWordLength(main.resources, wordLength)
            updateCreationControls()
        }
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
                val distanceMap = WordDistanceMap(puzzleStartWord)
                val distance: Int = distanceMap.getDistance(puzzleEndWord)
                    ?: throw Exception("Cannot reach end word \"$endWordEntry\" from start word \"$startWordEntry\"")
                if (distance != ladderLength) {
                    ladderLength = distance
                    selectedLadderLength = ladderLength
                    throw Exception("Ladder length changed automatically - press 'Create' again")
                }
            } else if (puzzleStartWord != null) {
                val distanceMap = WordDistanceMap(puzzleStartWord)
                val possibles = distanceMap.findAtDistance(ladderLength)
                if (possibles.isEmpty()) {
                    throw Exception("No words at ladder length $ladderLength can be reached from start word \"$startWordEntry\"")
                }
                puzzleEndWord = possibles[RANDOM.nextInt(possibles.size)]
            } else if (puzzleEndWord != null) {
                val distanceMap = WordDistanceMap(puzzleEndWord)
                val possibles = distanceMap.findAtDistance(ladderLength)
                if (possibles.isEmpty()) {
                    throw Exception("No words at ladder length $ladderLength can be reached from end word \"$endWordEntry\"")
                }
                puzzleStartWord = possibles[RANDOM.nextInt(possibles.size)]
            }

            isOk = true
        } catch (e: Exception) {
            isOk = false
            Toast.makeText(main, e.message, Toast.LENGTH_LONG).show()
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
                Toast.makeText(main, "Sorry, couldn't seem to generate a puzzle - please try again", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun onCancelClick() {
        controls.show(DisplayView.PUZZLE)
    }

    private fun onRandomStartWordClick() {
        val words: List<Word> = dictionary.nonIslandWords
        controls.startWordEdit.setText(words[RANDOM.nextInt(words.size)].toString())
    }

    private fun onRandomEndWordClick() {
        val words: List<Word> = dictionary.nonIslandWords
        controls.endWordEdit.setText(words[RANDOM.nextInt(words.size)].toString())
    }

    private var selectedWordLength: Int
        get() = controls.wordLengthSpinner.selectedItemPosition + MIN_WORD_LENGTH
        set(value) {
            controls.wordLengthSpinner.setSelection(value - MIN_WORD_LENGTH)
        }

    private var selectedLadderLength: Int
        get() = controls.ladderLengthSpinner.selectedItemPosition + MIN_LADDER_LENGTH
        set(value) {
            val newIndex = value - MIN_LADDER_LENGTH
            if (newIndex >= controls.ladderLengthSpinner.adapter.count) {
                controls.ladderLengthSpinner.setSelection(controls.ladderLengthSpinner.adapter.count - 1)
            } else {
                controls.ladderLengthSpinner.setSelection(newIndex)
            }
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
        controls.ladderLengthSpinner.adapter = ladderLengthSpinnerAdapters[wordLength - MIN_WORD_LENGTH]
        selectedLadderLength = bundle.getInt("creatorLadderLength", 4)
        controls.startWordEdit.filters = arrayOf(InputFilter.LengthFilter(wordLength), InputFilter.AllCaps())
        controls.endWordEdit.filters = arrayOf(InputFilter.LengthFilter(wordLength), InputFilter.AllCaps())
        controls.startWordEdit.setText(bundle.getString("creatorStartWord", ""))
        controls.endWordEdit.setText(bundle.getString("creatorEndWord", ""))
    }
}
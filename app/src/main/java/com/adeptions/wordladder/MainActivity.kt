package com.adeptions.wordladder

import android.content.res.Configuration
import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import com.adeptions.wordladder.core.words.Dictionary
import com.adeptions.wordladder.core.words.Word

import com.adeptions.wordladder.core.solving.*
import com.adeptions.wordladder.ui.*
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import kotlin.Exception


class MainActivity : AppCompatActivity() {
    var wordLength: Int = 3
    var ladderLength: Int = 4
    lateinit var dictionary: Dictionary

    lateinit var controls: MainActivityControls
    lateinit var createPuzzleController: CreatePuzzleController
    lateinit var puzzleDisplayController: PuzzleDisplayController
    lateinit var wordLookupController: WordLookupController
    lateinit var customKeyboardController: CustomKeyboardController

    lateinit var requestQueue: RequestQueue

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestQueue = Volley.newRequestQueue(this)

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        controls = MainActivityControls(this)

        dictionary = Dictionary.Factory.forWordLength(this.resources, wordLength)

        createPuzzleController = CreatePuzzleController(this)
        puzzleDisplayController = PuzzleDisplayController(this)
        wordLookupController = WordLookupController(this)
        customKeyboardController = CustomKeyboardController(this)

        if (savedInstanceState != null) {
            restoreState(savedInstanceState)
        } else {
            controls.show(DisplayView.PUZZLE)
            generatePuzzle()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val nightMode = (newConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        controls.setNightMode(nightMode)
        puzzleDisplayController.nightModeChanged()
        createPuzzleController.nightModeChanged()
        customKeyboardController.configChanged()
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)

        saveState(savedInstanceState)
    }

    private fun saveState(bundle: Bundle) {
        val viewingPuzzle = controls.currentView == DisplayView.PUZZLE
        bundle.putString("view", controls.currentView.toString())
        puzzleDisplayController.saveState(bundle)
        if (!viewingPuzzle) {
            createPuzzleController.saveState(bundle)
        }
    }

    private fun restoreState(bundle: Bundle) {
        val view = DisplayView.valueOf(bundle.getString("view", DisplayView.PUZZLE.toString()))
        puzzleDisplayController.restoreState(bundle)
        when (view) {
            DisplayView.CREATE -> {
                createPuzzleController.restoreState(bundle)
                controls.show(DisplayView.CREATE)
            }
            else -> controls.show(view)
        }
        customKeyboardController.configChanged()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        controls.menu = menu
        controls.setHintsOn(puzzleDisplayController.isHintsOn)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.hints_switch -> {
                puzzleDisplayController.hintsToggle()
                item.isChecked = puzzleDisplayController.isHintsOn
                customKeyboardController.hintsChanged(puzzleDisplayController.isHintsOn)
                true
            }
            R.id.action_new_puzzle -> {
                createPuzzleController.show()
                true
            }
            R.id.help_item -> {
                controls.show(DisplayView.HELP)
                true
            }
            R.id.word_lookup_item -> {
                controls.show(DisplayView.WORD_LOOKUP)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun generatePuzzle(): Boolean {
        val generator = Generator(dictionary, ladderLength)
        var ok = true
        try {
            val newPuzzle = generator.generate()
            showPuzzle(newPuzzle[0], newPuzzle[1])
        } catch (e: Exception) {
            ok = false
        }
        return ok
    }

    fun showPuzzle(startWord: Word, endWord: Word) {
        val solver = Solver(startWord, endWord, ladderLength)
        val solutions = solver.solve().sorted()
        val puzzle = Puzzle(dictionary, startWord, endWord, ladderLength, solutions)
        puzzleDisplayController.show(puzzle)
    }

    fun lookupWord(word: Word?, enteredWord: String?) {
        wordLookupController.lookupWord(word, enteredWord)
    }
}
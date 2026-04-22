package com.adeptions.wordladder

import android.content.res.Configuration
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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class MainActivity : AppCompatActivity() {
    var wordLength: Int = 4
    var ladderLength: Int = 5
    var hintsOn: Boolean = true
    lateinit var dictionary: Dictionary

    lateinit var controls: MainActivityControls
    lateinit var createPuzzleController: CreatePuzzleController
    lateinit var puzzleDisplayController: PuzzleDisplayController
    lateinit var wordLookupController: WordLookupController
    lateinit var customKeyboardController: CustomKeyboardController

    lateinit var requestQueue: RequestQueue

    var highScores: List<HighScore> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestQueue = Volley.newRequestQueue(this)

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        controls = MainActivityControls(this)

        loadPrefs()
        dictionary = Dictionary.Factory.forWordLength(this.resources, wordLength)

        createPuzzleController = CreatePuzzleController(this)
        puzzleDisplayController = PuzzleDisplayController(this)
        wordLookupController = WordLookupController(this)
        customKeyboardController = CustomKeyboardController(this)

        controls.highScoresClearButton.setOnClickListener { onClearHighScoresClick() }
        loadHighScores()
        controls.newPuzzleButton.setOnClickListener { onNewPuzzleClick() }

        if (savedInstanceState != null) {
            restoreState(savedInstanceState)
        } else {
            controls.show(DisplayView.PUZZLE)
            generatePuzzle()
        }
    }

    private val PREFS_NAME = "game_prefs"
    private val KEY_WORD_LENGTH = "word_length"
    private val KEY_LADDER_LENGTH = "ladder_length"
    private val KEY_HINTS_ON = "hints_on"

    private fun loadPrefs() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        var n = prefs.getInt(KEY_WORD_LENGTH, 0)
        if (n >= 2 && n <= 15) {
            wordLength = n
        }
        n = prefs.getInt(KEY_LADDER_LENGTH, 0)
        if (n >= 3) {
           ladderLength = n
        }
        hintsOn = prefs.getBoolean(KEY_HINTS_ON, true)
    }

    fun updatePrefs(newWordLength: Int, newLadderLength: Int) {
        if (wordLength != newWordLength || ladderLength != newLadderLength) {
            wordLength = newWordLength
            ladderLength = newLadderLength
            val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            prefs.edit()
                .putInt(KEY_WORD_LENGTH, newWordLength)
                .putInt(KEY_LADDER_LENGTH, newLadderLength)
                .apply()
        }
    }

    fun updateHintsPref(on: Boolean) {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_HINTS_ON, on).apply()
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
            R.id.high_scores_item -> {
                controls.show(DisplayView.HIGH_SCORES)
                true
            }
            R.id.word_lookup_item -> {
                controls.show(DisplayView.WORD_LOOKUP)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun onNewPuzzleClick() {
        if (generatePuzzle()) {
            controls.puzzleEdits[0].requestFocus()
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

    fun newScore(
        score: Int,
        max: Int,
        ladderLength: Int,
        startWord: String,
        endWord: String
    ): Boolean {
        var isHighScore: Boolean = false
        if (score > 0) {
            val entry = HighScore(
                score = score,
                maxScore = max,
                dateTime = LocalDateTime.now().toString(),
                ladderLength = ladderLength,
                startWord = startWord,
                endWord = endWord
            )
            val updated = highScores
                .plus(entry)
                .sortedByDescending { it.score }
                .take(10)
            isHighScore = updated.any {
                it.score == entry.score &&
                        it.maxScore == entry.maxScore &&
                        it.dateTime == entry.dateTime &&
                        it.ladderLength == entry.ladderLength &&
                        it.startWord == entry.startWord &&
                        it.endWord == entry.endWord
            }
            if (isHighScore) {
                highScores = updated
                saveHighScores()
                updateHighScores()
            }
        }
        return isHighScore
    }

    private fun loadHighScores() {
        highScores = loadHighScoresCsv()
            .lineSequence()
            .filter { it.isNotBlank() }
            .mapNotNull { HighScore.fromCsvRow(it) }
            .toList()
        updateHighScores()
    }

    private fun onClearHighScoresClick() {
        highScores = emptyList()
        saveHighScores()
        updateHighScores()
    }

    private val HIGH_SCORES_FILE = "high_scores.csv"

    fun loadHighScoresCsv(): String {
        return try {
            openFileInput(HIGH_SCORES_FILE).bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            ""
        }
    }

    private fun saveHighScores() {
        val csv = highScores.joinToString("\n") { it.toCsvRow() }
        openFileOutput(HIGH_SCORES_FILE, MODE_PRIVATE).use { output ->
            output.write(csv.toByteArray())
        }
    }

    private fun updateHighScores() {
        if (highScores.isEmpty()) {
            controls.highScoresClearButton.visibility = View.GONE
            controls.highScoresNoneRow.visibility = View.VISIBLE
            for (i in 0 until controls.highScoreHeaderRows.size) {
                controls.highScoreHeaderRows[i].visibility = View.GONE
                controls.highScoreDetailRows[i].visibility = View.GONE
            }
        } else {
            controls.highScoresClearButton.visibility = View.VISIBLE
            controls.highScoresNoneRow.visibility = View.GONE
            for (i in 0 until Math.min(10, highScores.size)) {
                val score: HighScore = highScores[i]
                var pct: String = " (100%)"
                if (score.score < score.maxScore) {
                    val pc: Double = (score.score.toDouble() / score.maxScore.toDouble()) * 100.0
                    pct = " (" + pc.toInt() + "%)"
                }
                controls.highScoreValues[i].setText(score.score.toString() + pct)
                controls.highScoreDetails[i].setText(score.startWord + " to\n" + score.endWord + " (" + score.ladderLength.toString() + " rungs)\n"+score.formatDateTime())
                controls.highScoreHeaderRows[i].visibility = View.VISIBLE
                controls.highScoreDetailRows[i].visibility = View.VISIBLE
            }
        }
    }

    data class HighScore(
        val score: Int,
        val maxScore: Int,
        val dateTime: String,
        val ladderLength: Int,
        val startWord: String,
        val endWord: String
    ) {
        fun toCsvRow(): String {
            return listOf(
                score.toString(),
                maxScore.toString(),
                dateTime,
                ladderLength.toString(),
                startWord,
                endWord
            ).joinToString(",")
        }
        fun formatDateTime(): String {
            val dt = LocalDateTime.parse(dateTime)
            val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")
            return dt.format(formatter)
        }
        companion object {
            fun fromCsvRow(row: String): HighScore? {
                val parts = row.split(",")
                if (parts.size != 6) return null
                return try {
                    HighScore(
                        score = parts[0].toInt(),
                        maxScore = parts[1].toInt(),
                        dateTime = parts[2],
                        ladderLength = parts[3].toInt(),
                        startWord = parts[4],
                        endWord = parts[5]
                    )
                } catch (e: Exception) {
                    null
                }
            }
        }
    }
}
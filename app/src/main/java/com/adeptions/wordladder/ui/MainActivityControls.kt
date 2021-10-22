package com.adeptions.wordladder.ui

import android.content.res.Configuration
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.iterator
import com.adeptions.wordladder.*

class MainActivityControls(val main: MainActivity) {
    // containers...
    val puzzleContainer: ConstraintLayout = main.findViewById(R.id.view_puzzle)
    val createContainer: ConstraintLayout = main.findViewById(R.id.view_create)
    val helpContainer: ConstraintLayout = main.findViewById(R.id.view_help)
    val customActionBar: Toolbar = main.findViewById(R.id.custom_actionbar)

    // main puzzle controls...
    val solutionsHeaderArea: View = main.findViewById(R.id.puzzle_header_area)
    val solutionsHeader: TextView = main.findViewById(R.id.puzzle_header)
    val previousSolutionButton: Button = main.findViewById(R.id.previous_solution_button)
    val nextSolutionButton: Button = main.findViewById(R.id.next_solution_button)
    val puzzleLadderView: View = main.findViewById(R.id.puzzle_ladder_view)
    val solutionLadderView: View = main.findViewById(R.id.solution_ladder_view)
    val puzzleLadderScroller: ScrollView = main.findViewById(R.id.scroll_puzzle_ladder)
    val solutionLadderScroller: ScrollView = main.findViewById(R.id.scroll_solution_ladder)
    // create puzzle controls...
    val createButton: Button = main.findViewById(R.id.button_create)
    val cancelButton: Button = main.findViewById(R.id.button_cancel)
    val wordLengthSpinner: Spinner = main.findViewById(R.id.spinner_word_length)
    val ladderLengthSpinner: Spinner = main.findViewById(R.id.spinner_ladder_length)
    val startWordEdit: EditText = main.findViewById(R.id.edit_start_word)
    val randomStartWordButton: Button = main.findViewById(R.id.button_random_start_word)
    val endWordEdit: EditText = main.findViewById(R.id.edit_end_word)
    val randomEndWordButton: Button = main.findViewById(R.id.button_random_end_word)
    // backgrounds...
    var backgroundNormal = R.drawable.word_back
    var backgroundError = R.drawable.word_back_error
    var backgroundGood = R.drawable.word_back_good
    var backgroundUnknown = R.drawable.word_back_unknown
    var backgroundWarning = R.drawable.word_back_warning

    val firstWordTextView: TextView = main.findViewById(R.id.puzzle_start_word)
    val endWordTextView: TextView = main.findViewById(R.id.puzzle_end_word)
    val puzzleRows: Array<TableRow> = arrayOf(
        main.findViewById(R.id.puzzle_row_01), main.findViewById(R.id.puzzle_row_02), main.findViewById(R.id.puzzle_row_03), main.findViewById(R.id.puzzle_row_04),
        main.findViewById(R.id.puzzle_row_05), main.findViewById(R.id.puzzle_row_06), main.findViewById(R.id.puzzle_row_07), main.findViewById(R.id.puzzle_row_08), main.findViewById(R.id.puzzle_row_09),
        main.findViewById(R.id.puzzle_row_10), main.findViewById(R.id.puzzle_row_11), main.findViewById(R.id.puzzle_row_12), main.findViewById(R.id.puzzle_row_13), main.findViewById(R.id.puzzle_row_14),
        main.findViewById(R.id.puzzle_row_15), main.findViewById(R.id.puzzle_row_16), main.findViewById(R.id.puzzle_row_17), main.findViewById(R.id.puzzle_row_18), main.findViewById(R.id.puzzle_row_19),
        main.findViewById(R.id.puzzle_row_20), main.findViewById(R.id.puzzle_row_21), main.findViewById(R.id.puzzle_row_22), main.findViewById(R.id.puzzle_row_23), main.findViewById(R.id.puzzle_row_24),
        main.findViewById(R.id.puzzle_row_25), main.findViewById(R.id.puzzle_row_26), main.findViewById(R.id.puzzle_row_27), main.findViewById(R.id.puzzle_row_28), main.findViewById(R.id.puzzle_row_29),
        main.findViewById(R.id.puzzle_row_30), main.findViewById(R.id.puzzle_row_31), main.findViewById(R.id.puzzle_row_32), main.findViewById(R.id.puzzle_row_33), main.findViewById(R.id.puzzle_row_34),
        main.findViewById(R.id.puzzle_row_35), main.findViewById(R.id.puzzle_row_36), main.findViewById(R.id.puzzle_row_37), main.findViewById(R.id.puzzle_row_38), main.findViewById(R.id.puzzle_row_39),
        main.findViewById(R.id.puzzle_row_40), main.findViewById(R.id.puzzle_row_41), main.findViewById(R.id.puzzle_row_42), main.findViewById(R.id.puzzle_row_43), main.findViewById(R.id.puzzle_row_44),
        main.findViewById(R.id.puzzle_row_45), main.findViewById(R.id.puzzle_row_46), main.findViewById(R.id.puzzle_row_47), main.findViewById(R.id.puzzle_row_48)
    )
    val puzzleEdits: Array<EditText> = arrayOf(
        main.findViewById(R.id.puzzle_word_01), main.findViewById(R.id.puzzle_word_02), main.findViewById(R.id.puzzle_word_03), main.findViewById(R.id.puzzle_word_04),
        main.findViewById(R.id.puzzle_word_05), main.findViewById(R.id.puzzle_word_06), main.findViewById(R.id.puzzle_word_07), main.findViewById(R.id.puzzle_word_08), main.findViewById(R.id.puzzle_word_09),
        main.findViewById(R.id.puzzle_word_10), main.findViewById(R.id.puzzle_word_11), main.findViewById(R.id.puzzle_word_12), main.findViewById(R.id.puzzle_word_13), main.findViewById(R.id.puzzle_word_14),
        main.findViewById(R.id.puzzle_word_15), main.findViewById(R.id.puzzle_word_16), main.findViewById(R.id.puzzle_word_17), main.findViewById(R.id.puzzle_word_18), main.findViewById(R.id.puzzle_word_19),
        main.findViewById(R.id.puzzle_word_20), main.findViewById(R.id.puzzle_word_21), main.findViewById(R.id.puzzle_word_22), main.findViewById(R.id.puzzle_word_23), main.findViewById(R.id.puzzle_word_24),
        main.findViewById(R.id.puzzle_word_25), main.findViewById(R.id.puzzle_word_26), main.findViewById(R.id.puzzle_word_27), main.findViewById(R.id.puzzle_word_28), main.findViewById(R.id.puzzle_word_29),
        main.findViewById(R.id.puzzle_word_30), main.findViewById(R.id.puzzle_word_31), main.findViewById(R.id.puzzle_word_32), main.findViewById(R.id.puzzle_word_33), main.findViewById(R.id.puzzle_word_34),
        main.findViewById(R.id.puzzle_word_35), main.findViewById(R.id.puzzle_word_36), main.findViewById(R.id.puzzle_word_37), main.findViewById(R.id.puzzle_word_38), main.findViewById(R.id.puzzle_word_39),
        main.findViewById(R.id.puzzle_word_40), main.findViewById(R.id.puzzle_word_41), main.findViewById(R.id.puzzle_word_42), main.findViewById(R.id.puzzle_word_43), main.findViewById(R.id.puzzle_word_44),
        main.findViewById(R.id.puzzle_word_45), main.findViewById(R.id.puzzle_word_46), main.findViewById(R.id.puzzle_word_47), main.findViewById(R.id.puzzle_word_48)
    )
    val solutionRows: Array<TableRow> = arrayOf(
        main.findViewById(R.id.solution_row_00), main.findViewById(R.id.solution_row_01), main.findViewById(R.id.solution_row_02), main.findViewById(R.id.solution_row_03), main.findViewById(R.id.solution_row_04),
        main.findViewById(R.id.solution_row_05), main.findViewById(R.id.solution_row_06), main.findViewById(R.id.solution_row_07), main.findViewById(R.id.solution_row_08), main.findViewById(R.id.solution_row_09),
        main.findViewById(R.id.solution_row_10), main.findViewById(R.id.solution_row_11), main.findViewById(R.id.solution_row_12), main.findViewById(R.id.solution_row_13), main.findViewById(R.id.solution_row_14),
        main.findViewById(R.id.solution_row_15), main.findViewById(R.id.solution_row_16), main.findViewById(R.id.solution_row_17), main.findViewById(R.id.solution_row_18), main.findViewById(R.id.solution_row_19),
        main.findViewById(R.id.solution_row_20), main.findViewById(R.id.solution_row_21), main.findViewById(R.id.solution_row_22), main.findViewById(R.id.solution_row_23), main.findViewById(R.id.solution_row_24),
        main.findViewById(R.id.solution_row_25), main.findViewById(R.id.solution_row_26), main.findViewById(R.id.solution_row_27), main.findViewById(R.id.solution_row_28), main.findViewById(R.id.solution_row_29),
        main.findViewById(R.id.solution_row_30), main.findViewById(R.id.solution_row_31), main.findViewById(R.id.solution_row_32), main.findViewById(R.id.solution_row_33), main.findViewById(R.id.solution_row_34),
        main.findViewById(R.id.solution_row_35), main.findViewById(R.id.solution_row_36), main.findViewById(R.id.solution_row_37), main.findViewById(R.id.solution_row_38), main.findViewById(R.id.solution_row_39),
        main.findViewById(R.id.solution_row_40), main.findViewById(R.id.solution_row_41), main.findViewById(R.id.solution_row_42), main.findViewById(R.id.solution_row_43), main.findViewById(R.id.solution_row_44),
        main.findViewById(R.id.solution_row_45), main.findViewById(R.id.solution_row_46), main.findViewById(R.id.solution_row_47), main.findViewById(R.id.solution_row_48), main.findViewById(R.id.solution_row_49)
    )
    val solutionWords: Array<TextView> = arrayOf(
        main.findViewById(R.id.solution_word_00), main.findViewById(R.id.solution_word_01), main.findViewById(R.id.solution_word_02), main.findViewById(R.id.solution_word_03), main.findViewById(R.id.solution_word_04),
        main.findViewById(R.id.solution_word_05), main.findViewById(R.id.solution_word_06), main.findViewById(R.id.solution_word_07), main.findViewById(R.id.solution_word_08), main.findViewById(R.id.solution_word_09),
        main.findViewById(R.id.solution_word_10), main.findViewById(R.id.solution_word_11), main.findViewById(R.id.solution_word_12), main.findViewById(R.id.solution_word_13), main.findViewById(R.id.solution_word_14),
        main.findViewById(R.id.solution_word_15), main.findViewById(R.id.solution_word_16), main.findViewById(R.id.solution_word_17), main.findViewById(R.id.solution_word_18), main.findViewById(R.id.solution_word_19),
        main.findViewById(R.id.solution_word_20), main.findViewById(R.id.solution_word_21), main.findViewById(R.id.solution_word_22), main.findViewById(R.id.solution_word_23), main.findViewById(R.id.solution_word_24),
        main.findViewById(R.id.solution_word_25), main.findViewById(R.id.solution_word_26), main.findViewById(R.id.solution_word_27), main.findViewById(R.id.solution_word_28), main.findViewById(R.id.solution_word_29),
        main.findViewById(R.id.solution_word_30), main.findViewById(R.id.solution_word_31), main.findViewById(R.id.solution_word_32), main.findViewById(R.id.solution_word_33), main.findViewById(R.id.solution_word_34),
        main.findViewById(R.id.solution_word_35), main.findViewById(R.id.solution_word_36), main.findViewById(R.id.solution_word_37), main.findViewById(R.id.solution_word_38), main.findViewById(R.id.solution_word_39),
        main.findViewById(R.id.solution_word_40), main.findViewById(R.id.solution_word_41), main.findViewById(R.id.solution_word_42), main.findViewById(R.id.solution_word_43), main.findViewById(R.id.solution_word_44),
        main.findViewById(R.id.solution_word_45), main.findViewById(R.id.solution_word_46), main.findViewById(R.id.solution_word_47), main.findViewById(R.id.solution_word_48), main.findViewById(R.id.solution_word_49)
    )

    var menu: Menu? = null

    var wordHintToaster: Toast? = null
    var lastFocused: EditText? = null
    var viewSwitching: Boolean = false

    init {
        val nightMode = (main.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        setNightMode(nightMode)
        if (nightMode) {
            startWordEdit.setBackgroundResource(backgroundNormal)
            endWordEdit.setBackgroundResource(backgroundNormal)
            firstWordTextView.setBackgroundResource(backgroundNormal)
            endWordTextView.setBackgroundResource(backgroundNormal)
            puzzleEdits.forEach { edit -> edit.setBackgroundResource(backgroundNormal) }
            solutionWords.forEach { text -> text.setBackgroundResource(backgroundNormal) }
        }
        customActionBar.setNavigationOnClickListener { onNavigationClick() }
    }

    private fun onNavigationClick() {
        show(DisplayView.PUZZLE)
    }

    fun setNightMode(on: Boolean) {
        if (on) {
            backgroundNormal = R.drawable.word_back_dark
            backgroundError = R.drawable.word_back_dark_error
            backgroundGood = R.drawable.word_back_dark_good
            backgroundUnknown = R.drawable.word_back_dark_unknown
            backgroundWarning = R.drawable.word_back_dark_warning
        } else {
            backgroundNormal = R.drawable.word_back
            backgroundError = R.drawable.word_back_error
            backgroundGood = R.drawable.word_back_good
            backgroundUnknown = R.drawable.word_back_unknown
            backgroundWarning = R.drawable.word_back_warning
        }
    }

    fun showMenu(show: Boolean) {
        if (menu != null) {
            for (item: MenuItem in menu!!.iterator()) {
                item.isVisible = show
            }
        }
    }

    fun setHintsOn(on: Boolean) {
        if (menu != null) {
            menu!!.findItem(R.id.hints_switch).isChecked = on
        }
    }

    fun show(view: DisplayView) {
        viewSwitching = true
        when (view) {
            DisplayView.CREATE -> {
                helpContainer.visibility = View.GONE
                puzzleContainer.visibility = View.GONE
                createContainer.visibility = View.VISIBLE
                main.setTitle(R.string.actionbar_title_create)
                if (main.supportActionBar != null) {
                    main.supportActionBar!!.hide()
                    customActionBar.visibility = View.VISIBLE
                    customActionBar.setTitle(R.string.actionbar_title_create)
                } else {
                    showMenu(false)
                }
            }
            DisplayView.HELP -> {
                puzzleContainer.visibility = View.GONE
                createContainer.visibility = View.GONE
                helpContainer.visibility = View.VISIBLE
                main.setTitle(R.string.actionbar_title_help)
                if (main.supportActionBar != null) {
                    main.supportActionBar!!.hide()
                    customActionBar.visibility = View.VISIBLE
                    customActionBar.setTitle(R.string.actionbar_title_help)
                } else {
                    showMenu(false)
                }
            }
            DisplayView.PUZZLE -> {
                helpContainer.visibility = View.GONE
                createContainer.visibility = View.GONE
                puzzleContainer.visibility = View.VISIBLE
                main.setTitle(R.string.app_name)
                if (main.supportActionBar != null) {
                    customActionBar.visibility = View.GONE
                    main.supportActionBar!!.show()
                } else {
                    showMenu(true)
                }
            }
        }
    }

    val currentView: DisplayView
        get() = if (createContainer.isVisible) {
            DisplayView.CREATE
        } else {
            DisplayView.PUZZLE
        }

    fun createWordHintToaster(): Toast {
        if (wordHintToaster == null) {
            wordHintToaster = Toast.makeText(main, "...", Toast.LENGTH_LONG)
        }
        return wordHintToaster!!
    }

    fun cancelWordHintToaster() {
        if (wordHintToaster != null) {
            wordHintToaster!!.cancel()
            wordHintToaster = null
        }
    }
}
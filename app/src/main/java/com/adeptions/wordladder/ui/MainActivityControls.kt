package com.adeptions.wordladder.ui

import android.content.res.Configuration
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
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
    val wordLokupContainer: ConstraintLayout = main.findViewById(R.id.view_word_lookup)
    val customActionBar: Toolbar = main.findViewById(R.id.custom_actionbar)
    val customKeyboardContainer: ConstraintLayout = main.findViewById(R.id.custom_keyboard_display)

    // main puzzle controls...
    val solutionsHeaderArea: View = main.findViewById(R.id.puzzle_header_area)
    val solutionsHeader: TextView = main.findViewById(R.id.puzzle_header)
    val previousSolutionButton: Button = main.findViewById(R.id.previous_solution_button)
    val nextSolutionButton: Button = main.findViewById(R.id.next_solution_button)
    val puzzleLadderView: View = main.findViewById(R.id.puzzle_ladder_view)
    val solutionLadderView: View = main.findViewById(R.id.solution_ladder_view)
    val puzzleScroller: ScrollView = main.findViewById(R.id.puzzle_scroll_area)
    // create puzzle controls...
    val createScroller: ScrollView = main.findViewById(R.id.create_scroller)
    val createButton: Button = main.findViewById(R.id.button_create)
    val cancelButton: Button = main.findViewById(R.id.button_cancel)
    val wordLengthSpinner: Spinner = main.findViewById(R.id.spinner_word_length)
    val ladderLengthSpinner: Spinner = main.findViewById(R.id.spinner_ladder_length)
    val startWordEdit: EditText = main.findViewById(R.id.edit_start_word)
    val randomStartWordButton: Button = main.findViewById(R.id.button_random_start_word)
    val endWordEdit: EditText = main.findViewById(R.id.edit_end_word)
    val randomEndWordButton: Button = main.findViewById(R.id.button_random_end_word)
    val dictionaryWordCount: TextView = main.findViewById(R.id.dictionary_word_count)
    val ladderLengthWordCount: TextView = main.findViewById(R.id.ladder_length_word_count)
    // word lookup...
    val lookupWordEdit: EditText = main.findViewById(R.id.edit_lookup_word)
    val lookupButton: Button = main.findViewById(R.id.button_lookup)
    val lookupErrorRow: TableRow = main.findViewById(R.id.lookup_error_row)
    val lookupNotFoundRow: TableRow = main.findViewById(R.id.lookup_not_found_row)
    val lookupNotFound404Row: TableRow = main.findViewById(R.id.lookup_not_found_404_row)
    val lookupOffensiveRow: TableRow = main.findViewById(R.id.lookup_offensive_row)
    val lookupLoading: ProgressBar = main.findViewById(R.id.lookup_loading)
    val lookupSeeAlsoRow: TableRow = main.findViewById(R.id.lookup_see_also_row)
    val lookupSeeAlsos: TextView = main.findViewById(R.id.lookup_see_also)
    val lookupMeaningHeaderRows: Array<TableRow> = arrayOf(
        main.findViewById(R.id.lookup_meaning_header_row_1),
        main.findViewById(R.id.lookup_meaning_header_row_2),
        main.findViewById(R.id.lookup_meaning_header_row_3),
        main.findViewById(R.id.lookup_meaning_header_row_4),
        main.findViewById(R.id.lookup_meaning_header_row_5)
    )
    val lookupMeaningTypes: Array<TextView> = arrayOf(
        main.findViewById(R.id.lookup_meaning_type_1),
        main.findViewById(R.id.lookup_meaning_type_2),
        main.findViewById(R.id.lookup_meaning_type_3),
        main.findViewById(R.id.lookup_meaning_type_4),
        main.findViewById(R.id.lookup_meaning_type_5)
    )
    val lookupMeaningDefinitionRows: Array<TableRow> = arrayOf(
        main.findViewById(R.id.lookup_meaning_definition_row_1),
        main.findViewById(R.id.lookup_meaning_definition_row_2),
        main.findViewById(R.id.lookup_meaning_definition_row_3),
        main.findViewById(R.id.lookup_meaning_definition_row_4),
        main.findViewById(R.id.lookup_meaning_definition_row_5)
    )
    val lookupMeaningDefinitions: Array<TextView> = arrayOf(
        main.findViewById(R.id.lookup_meaning_definition_1),
        main.findViewById(R.id.lookup_meaning_definition_2),
        main.findViewById(R.id.lookup_meaning_definition_3),
        main.findViewById(R.id.lookup_meaning_definition_4),
        main.findViewById(R.id.lookup_meaning_definition_5)
    )
    var wordLookupBack: DisplayView = DisplayView.PUZZLE
    // backgrounds...
    var backgroundNormal = R.drawable.word_back
    var backgroundError = R.drawable.word_back_error
    var backgroundGood = R.drawable.word_back_good
    var backgroundUnknown = R.drawable.word_back_unknown
    var backgroundWarning = R.drawable.word_back_warning

    val pointsTotal: TextView = main.findViewById(R.id.points_total)
    val pointsRemaining: TextView = main.findViewById(R.id.points_remaining)
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
        main.findViewById(R.id.puzzle_row_45), main.findViewById(R.id.puzzle_row_46), main.findViewById(R.id.puzzle_row_47), main.findViewById(R.id.puzzle_row_48), main.findViewById(R.id.puzzle_row_49),
        main.findViewById(R.id.puzzle_row_50), main.findViewById(R.id.puzzle_row_51), main.findViewById(R.id.puzzle_row_52), main.findViewById(R.id.puzzle_row_53), main.findViewById(R.id.puzzle_row_54),
        main.findViewById(R.id.puzzle_row_55), main.findViewById(R.id.puzzle_row_56), main.findViewById(R.id.puzzle_row_57), main.findViewById(R.id.puzzle_row_58), main.findViewById(R.id.puzzle_row_59),
        main.findViewById(R.id.puzzle_row_60), main.findViewById(R.id.puzzle_row_61), main.findViewById(R.id.puzzle_row_62), main.findViewById(R.id.puzzle_row_63), main.findViewById(R.id.puzzle_row_64),
        main.findViewById(R.id.puzzle_row_65), main.findViewById(R.id.puzzle_row_66), main.findViewById(R.id.puzzle_row_67), main.findViewById(R.id.puzzle_row_68), main.findViewById(R.id.puzzle_row_69),
        main.findViewById(R.id.puzzle_row_70), main.findViewById(R.id.puzzle_row_71), main.findViewById(R.id.puzzle_row_72), main.findViewById(R.id.puzzle_row_73), main.findViewById(R.id.puzzle_row_74),
        main.findViewById(R.id.puzzle_row_75), main.findViewById(R.id.puzzle_row_76), main.findViewById(R.id.puzzle_row_77), main.findViewById(R.id.puzzle_row_78)
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
        main.findViewById(R.id.puzzle_word_45), main.findViewById(R.id.puzzle_word_46), main.findViewById(R.id.puzzle_word_47), main.findViewById(R.id.puzzle_word_48), main.findViewById(R.id.puzzle_word_49),
        main.findViewById(R.id.puzzle_word_50), main.findViewById(R.id.puzzle_word_51), main.findViewById(R.id.puzzle_word_52), main.findViewById(R.id.puzzle_word_53), main.findViewById(R.id.puzzle_word_54),
        main.findViewById(R.id.puzzle_word_55), main.findViewById(R.id.puzzle_word_56), main.findViewById(R.id.puzzle_word_57), main.findViewById(R.id.puzzle_word_58), main.findViewById(R.id.puzzle_word_59),
        main.findViewById(R.id.puzzle_word_60), main.findViewById(R.id.puzzle_word_61), main.findViewById(R.id.puzzle_word_62), main.findViewById(R.id.puzzle_word_63), main.findViewById(R.id.puzzle_word_64),
        main.findViewById(R.id.puzzle_word_65), main.findViewById(R.id.puzzle_word_66), main.findViewById(R.id.puzzle_word_67), main.findViewById(R.id.puzzle_word_68), main.findViewById(R.id.puzzle_word_69),
        main.findViewById(R.id.puzzle_word_70), main.findViewById(R.id.puzzle_word_71), main.findViewById(R.id.puzzle_word_72), main.findViewById(R.id.puzzle_word_73), main.findViewById(R.id.puzzle_word_74),
        main.findViewById(R.id.puzzle_word_75), main.findViewById(R.id.puzzle_word_76), main.findViewById(R.id.puzzle_word_77), main.findViewById(R.id.puzzle_word_78)
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
        main.findViewById(R.id.solution_row_45), main.findViewById(R.id.solution_row_46), main.findViewById(R.id.solution_row_47), main.findViewById(R.id.solution_row_48), main.findViewById(R.id.solution_row_49),
        main.findViewById(R.id.solution_row_50), main.findViewById(R.id.solution_row_51), main.findViewById(R.id.solution_row_52), main.findViewById(R.id.solution_row_53), main.findViewById(R.id.solution_row_54),
        main.findViewById(R.id.solution_row_55), main.findViewById(R.id.solution_row_56), main.findViewById(R.id.solution_row_57), main.findViewById(R.id.solution_row_58), main.findViewById(R.id.solution_row_59),
        main.findViewById(R.id.solution_row_60), main.findViewById(R.id.solution_row_61), main.findViewById(R.id.solution_row_62), main.findViewById(R.id.solution_row_63), main.findViewById(R.id.solution_row_64),
        main.findViewById(R.id.solution_row_65), main.findViewById(R.id.solution_row_66), main.findViewById(R.id.solution_row_67), main.findViewById(R.id.solution_row_68), main.findViewById(R.id.solution_row_69),
        main.findViewById(R.id.solution_row_70), main.findViewById(R.id.solution_row_71), main.findViewById(R.id.solution_row_72), main.findViewById(R.id.solution_row_73), main.findViewById(R.id.solution_row_74),
        main.findViewById(R.id.solution_row_75), main.findViewById(R.id.solution_row_76), main.findViewById(R.id.solution_row_77), main.findViewById(R.id.solution_row_78), main.findViewById(R.id.solution_row_79)
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
        main.findViewById(R.id.solution_word_45), main.findViewById(R.id.solution_word_46), main.findViewById(R.id.solution_word_47), main.findViewById(R.id.solution_word_48), main.findViewById(R.id.solution_word_49),
        main.findViewById(R.id.solution_word_50), main.findViewById(R.id.solution_word_51), main.findViewById(R.id.solution_word_52), main.findViewById(R.id.solution_word_53), main.findViewById(R.id.solution_word_54),
        main.findViewById(R.id.solution_word_55), main.findViewById(R.id.solution_word_56), main.findViewById(R.id.solution_word_57), main.findViewById(R.id.solution_word_58), main.findViewById(R.id.solution_word_59),
        main.findViewById(R.id.solution_word_60), main.findViewById(R.id.solution_word_61), main.findViewById(R.id.solution_word_62), main.findViewById(R.id.solution_word_63), main.findViewById(R.id.solution_word_64),
        main.findViewById(R.id.solution_word_65), main.findViewById(R.id.solution_word_66), main.findViewById(R.id.solution_word_67), main.findViewById(R.id.solution_word_68), main.findViewById(R.id.solution_word_69),
        main.findViewById(R.id.solution_word_70), main.findViewById(R.id.solution_word_71), main.findViewById(R.id.solution_word_72), main.findViewById(R.id.solution_word_73), main.findViewById(R.id.solution_word_74),
        main.findViewById(R.id.solution_word_75), main.findViewById(R.id.solution_word_76), main.findViewById(R.id.solution_word_77), main.findViewById(R.id.solution_word_78), main.findViewById(R.id.solution_word_79)
    )

    val allEdits: Array<EditText> = arrayOf(
        startWordEdit, endWordEdit, lookupWordEdit,
        main.findViewById(R.id.puzzle_word_01), main.findViewById(R.id.puzzle_word_02), main.findViewById(R.id.puzzle_word_03), main.findViewById(R.id.puzzle_word_04),
        main.findViewById(R.id.puzzle_word_05), main.findViewById(R.id.puzzle_word_06), main.findViewById(R.id.puzzle_word_07), main.findViewById(R.id.puzzle_word_08), main.findViewById(R.id.puzzle_word_09),
        main.findViewById(R.id.puzzle_word_10), main.findViewById(R.id.puzzle_word_11), main.findViewById(R.id.puzzle_word_12), main.findViewById(R.id.puzzle_word_13), main.findViewById(R.id.puzzle_word_14),
        main.findViewById(R.id.puzzle_word_15), main.findViewById(R.id.puzzle_word_16), main.findViewById(R.id.puzzle_word_17), main.findViewById(R.id.puzzle_word_18), main.findViewById(R.id.puzzle_word_19),
        main.findViewById(R.id.puzzle_word_20), main.findViewById(R.id.puzzle_word_21), main.findViewById(R.id.puzzle_word_22), main.findViewById(R.id.puzzle_word_23), main.findViewById(R.id.puzzle_word_24),
        main.findViewById(R.id.puzzle_word_25), main.findViewById(R.id.puzzle_word_26), main.findViewById(R.id.puzzle_word_27), main.findViewById(R.id.puzzle_word_28), main.findViewById(R.id.puzzle_word_29),
        main.findViewById(R.id.puzzle_word_30), main.findViewById(R.id.puzzle_word_31), main.findViewById(R.id.puzzle_word_32), main.findViewById(R.id.puzzle_word_33), main.findViewById(R.id.puzzle_word_34),
        main.findViewById(R.id.puzzle_word_35), main.findViewById(R.id.puzzle_word_36), main.findViewById(R.id.puzzle_word_37), main.findViewById(R.id.puzzle_word_38), main.findViewById(R.id.puzzle_word_39),
        main.findViewById(R.id.puzzle_word_40), main.findViewById(R.id.puzzle_word_41), main.findViewById(R.id.puzzle_word_42), main.findViewById(R.id.puzzle_word_43), main.findViewById(R.id.puzzle_word_44),
        main.findViewById(R.id.puzzle_word_45), main.findViewById(R.id.puzzle_word_46), main.findViewById(R.id.puzzle_word_47), main.findViewById(R.id.puzzle_word_48), main.findViewById(R.id.puzzle_word_49),
        main.findViewById(R.id.puzzle_word_50), main.findViewById(R.id.puzzle_word_51), main.findViewById(R.id.puzzle_word_52), main.findViewById(R.id.puzzle_word_53), main.findViewById(R.id.puzzle_word_54),
        main.findViewById(R.id.puzzle_word_55), main.findViewById(R.id.puzzle_word_56), main.findViewById(R.id.puzzle_word_57), main.findViewById(R.id.puzzle_word_58), main.findViewById(R.id.puzzle_word_59),
        main.findViewById(R.id.puzzle_word_60), main.findViewById(R.id.puzzle_word_61), main.findViewById(R.id.puzzle_word_62), main.findViewById(R.id.puzzle_word_63), main.findViewById(R.id.puzzle_word_64),
        main.findViewById(R.id.puzzle_word_65), main.findViewById(R.id.puzzle_word_66), main.findViewById(R.id.puzzle_word_67), main.findViewById(R.id.puzzle_word_68), main.findViewById(R.id.puzzle_word_69),
        main.findViewById(R.id.puzzle_word_70), main.findViewById(R.id.puzzle_word_71), main.findViewById(R.id.puzzle_word_72), main.findViewById(R.id.puzzle_word_73), main.findViewById(R.id.puzzle_word_74),
        main.findViewById(R.id.puzzle_word_75), main.findViewById(R.id.puzzle_word_76), main.findViewById(R.id.puzzle_word_77), main.findViewById(R.id.puzzle_word_78)
    )
    var menu: Menu? = null

    var toaster: Toast? = null
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
        if (currentView == DisplayView.WORD_LOOKUP) {
            show(wordLookupBack)
        } else {
            show(DisplayView.PUZZLE)
        }
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
                wordLokupContainer.visibility = View.GONE
                helpContainer.visibility = View.GONE
                puzzleContainer.visibility = View.GONE
                createContainer.visibility = View.VISIBLE
                customKeyboardContainer.visibility = View.VISIBLE
                main.customKeyboardController.currentViewChanged(view)
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
                wordLokupContainer.visibility = View.GONE
                puzzleContainer.visibility = View.GONE
                createContainer.visibility = View.GONE
                customKeyboardContainer.visibility = View.GONE
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
            DisplayView.WORD_LOOKUP -> {
                if (currentView != DisplayView.WORD_LOOKUP) {
                    wordLookupBack = currentView
                }
                puzzleContainer.visibility = View.GONE
                createContainer.visibility = View.GONE
                helpContainer.visibility = View.GONE
                customKeyboardContainer.visibility = View.VISIBLE
                main.customKeyboardController.currentViewChanged(view)
                wordLokupContainer.visibility = View.VISIBLE
                main.setTitle(R.string.actionbar_title_word_lookup)
                if (main.supportActionBar != null) {
                    main.supportActionBar!!.hide()
                    customActionBar.visibility = View.VISIBLE
                    customActionBar.setTitle(R.string.actionbar_title_word_lookup)
                } else {
                    showMenu(false)
                }
            }
            DisplayView.PUZZLE -> {
                wordLokupContainer.visibility = View.GONE
                helpContainer.visibility = View.GONE
                createContainer.visibility = View.GONE
                customKeyboardContainer.visibility = View.VISIBLE
                main.customKeyboardController.currentViewChanged(view)
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
        } else if (helpContainer.isVisible) {
            DisplayView.HELP
        } else if (wordLokupContainer.isVisible) {
            DisplayView.WORD_LOOKUP
        } else {
            DisplayView.PUZZLE
        }

    fun createToaster(): Toast {
        if (toaster == null) {
            toaster = Toast.makeText(main, "...", Toast.LENGTH_LONG)
        }
        return toaster!!
    }

    fun createToaster(message: String): Toast {
        val result = createToaster()
        result.setText(message)
        return result
    }

    fun cancelToaster() {
        if (toaster != null) {
            toaster!!.cancel()
            toaster = null
        }
    }

    val isLandscape: Boolean get() {
        return main.getResources().getConfiguration().orientation == ORIENTATION_LANDSCAPE
    }
}
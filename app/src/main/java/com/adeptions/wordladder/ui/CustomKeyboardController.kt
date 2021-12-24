package com.adeptions.wordladder.ui

import android.text.InputType
import android.view.View
import android.widget.EditText
import com.adeptions.wordladder.MainActivity
import android.view.inputmethod.EditorInfo

import android.view.inputmethod.InputConnection
import android.widget.LinearLayout
import android.widget.Space
import android.widget.TextView
import com.adeptions.wordladder.R


class CustomKeyboardController(private val main: MainActivity): View.OnClickListener {
    private val controls: MainActivityControls = main.controls

    private val keyPrevPortrait: TextView = main.findViewById(R.id.button_key_PREV)
    private val keyPrevLandscape: TextView = main.findViewById(R.id.button_key_PREV_landscape)
    private val keyNextPortrait: TextView = main.findViewById(R.id.button_key_NEXT)
    private val keyNextLandscape: TextView = main.findViewById(R.id.button_key_NEXT_landscape)
    private val keyClearPortrait: TextView = main.findViewById(R.id.button_key_CLEAR)
    private val keyClearLandscape: TextView = main.findViewById(R.id.button_key_CLEAR_landscape)
    private val keySuggestPortrait: TextView = main.findViewById(R.id.button_key_SUGGEST)
    private val keySuggestLandscape: TextView = main.findViewById(R.id.button_key_SUGGEST_landscape)
    private val keyHintPortrait: TextView = main.findViewById(R.id.button_key_HINT)
    private val keyHintLandscape: TextView = main.findViewById(R.id.button_key_HINT_landscape)
    private val keyLookupPortrait: TextView = main.findViewById(R.id.button_key_LOOKUP)
    private val keyLookupLandscape: TextView = main.findViewById(R.id.button_key_LOOKUP_landscape)

    private val keyRowPortrait: LinearLayout = main.findViewById(R.id.key_row_portrait)
    private val rowASpacer: Space = main.findViewById(R.id.row_A_spacer)
    private val rowZSpacer: Space = main.findViewById(R.id.row_Z_spacer)

    private val keyboardButtons: Array<TextView> = arrayOf(
        main.findViewById(R.id.button_key_A),
        main.findViewById(R.id.button_key_B),
        main.findViewById(R.id.button_key_C),
        main.findViewById(R.id.button_key_D),
        main.findViewById(R.id.button_key_E),
        main.findViewById(R.id.button_key_F),
        main.findViewById(R.id.button_key_G),
        main.findViewById(R.id.button_key_H),
        main.findViewById(R.id.button_key_I),
        main.findViewById(R.id.button_key_J),
        main.findViewById(R.id.button_key_K),
        main.findViewById(R.id.button_key_L),
        main.findViewById(R.id.button_key_M),
        main.findViewById(R.id.button_key_N),
        main.findViewById(R.id.button_key_O),
        main.findViewById(R.id.button_key_P),
        main.findViewById(R.id.button_key_Q),
        main.findViewById(R.id.button_key_R),
        main.findViewById(R.id.button_key_S),
        main.findViewById(R.id.button_key_T),
        main.findViewById(R.id.button_key_U),
        main.findViewById(R.id.button_key_V),
        main.findViewById(R.id.button_key_W),
        main.findViewById(R.id.button_key_X),
        main.findViewById(R.id.button_key_Y),
        main.findViewById(R.id.button_key_Z),
        main.findViewById(R.id.button_key_BACKSPACE),
        main.findViewById(R.id.button_key_LEFT),
        main.findViewById(R.id.button_key_RIGHT),
        keyPrevPortrait, keyPrevLandscape,
        keyNextPortrait, keyNextLandscape,
        keyClearPortrait, keyClearLandscape,
        keySuggestPortrait, keySuggestLandscape,
        keyHintPortrait, keyHintLandscape,
        keyLookupPortrait, keyLookupLandscape
    )

    init {
        for (edit: EditText in controls.allEdits) {
            edit.setRawInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS)
            edit.setTextIsSelectable(true)
            edit.showSoftInputOnFocus = false
            edit.isSoundEffectsEnabled = false
            val ic: InputConnection = edit.onCreateInputConnection(EditorInfo())
            edit.tag = ic
        }
        for (button: TextView in keyboardButtons) {
            button.setOnClickListener(this)
        }
        configChanged()
    }

    override fun onClick(buttonView: View?) {
        if (buttonView != null) {
            val focused = main.currentFocus
            val button: TextView = buttonView as TextView
            if (focused != null) {
                val tag: Any? = focused.tag
                if (tag is InputConnection) {
                    val ic: InputConnection = tag
                    val edit: EditText = focused as EditText
                    when (button.id) {
                        R.id.button_key_BACKSPACE -> {
                            val selectedText: CharSequence? = ic.getSelectedText(0)
                            if (selectedText == null || selectedText.isEmpty()) {
                                // no selection, so delete previous character
                                ic.deleteSurroundingText(1, 0)
                            } else {
                                // delete the selection
                                ic.commitText("", 1)
                            }
                        }
                        R.id.button_key_LEFT -> {
                            val start = Math.min(edit.selectionStart, edit.selectionEnd)
                            val end = Math.max(edit.selectionStart, edit.selectionEnd)
                            if (start == 0 && end == 1) {
                                ic.setSelection(0, 0)
                            } else if (start < 1) {
                                ic.setSelection(0, 1)
                            } else {
                                ic.setSelection(start - 1, start)
                            }
                        }
                        R.id.button_key_RIGHT -> {
                            val start = Math.min(edit.selectionStart, edit.selectionEnd)
                            val end = Math.max(edit.selectionStart, edit.selectionEnd)
                            val text = edit.text.toString()
                            if (start == text.length - 1 && end == text.length) {
                                ic.setSelection(text.length, text.length)
                            } else if (start == -1 || start >= text.length - 1) {
                                ic.setSelection(text.length - 1, text.length)
                            } else if (start < end) {
                                ic.setSelection(start + 1, start + 2)
                            } else {
                                ic.setSelection(start, start + 1)
                            }
                        }
                        R.id.button_key_CLEAR, R.id.button_key_CLEAR_landscape -> {
                            val text = edit.text.toString()
                            ic.setSelection(0, text.length)
                            ic.commitText("", 1)
                        }
                        R.id.button_key_PREV, R.id.button_key_PREV_landscape -> {
                            val prev: View? = focused.focusSearch(View.FOCUS_UP)
                            prev?.requestFocus()
                        }
                        R.id.button_key_NEXT, R.id.button_key_NEXT_landscape -> {
                            ic.performEditorAction(EditorInfo.IME_ACTION_NEXT)
                        }
                        R.id.button_key_SUGGEST, R.id.button_key_SUGGEST_landscape -> {
                            val text = edit.text.toString()
                            ic.setSelection(0, text.length)
                            ic.commitText("?", 1)
                        }
                        R.id.button_key_HINT, R.id.button_key_HINT_landscape -> {
                            ic.commitText(".", 0)
                        }
                        R.id.button_key_LOOKUP, R.id.button_key_LOOKUP_landscape -> {
                            main.wordLookupController.lookup()
                        }
                        else -> {
                            ic.commitText(button.text.toString(), 1)
                        }
                    }
                }
            } else if (controls.currentView == DisplayView.WORD_LOOKUP
                && (button.id == R.id.button_key_LOOKUP || button.id == R.id.button_key_LOOKUP_landscape)) {
                main.wordLookupController.lookup()
            }
        }
    }

    fun currentViewChanged(view: DisplayView) {
        when (view) {
            DisplayView.PUZZLE -> {
                showLookupKey(false)
                val hintsOn = main.puzzleDisplayController.isHintsOn
                showSuggestKey(hintsOn)
                showHintKey(hintsOn)
            }
            DisplayView.WORD_LOOKUP -> {
                showLookupKey(true)
                showSuggestKey(false)
                showHintKey(false)
            }
            DisplayView.CREATE -> {
                showLookupKey(false)
                showHintKey(false)
                showSuggestKey(true)
            }
        }
    }

    private fun showHintKey(show: Boolean) {
        if (show && controls.isLandscape) {
            keyHintPortrait.visibility = View.GONE
            keyHintLandscape.visibility = View.VISIBLE
        } else if (show) {
            keyHintPortrait.visibility = View.VISIBLE
            keyHintLandscape.visibility = View.GONE
        } else {
            keyHintPortrait.visibility = View.GONE
            keyHintLandscape.visibility = View.GONE
        }
        adjustARowSpacing()
    }

    private fun adjustARowSpacing() {
        if (controls.isLandscape) {
            when (controls.currentView) {
                DisplayView.PUZZLE -> {
                    setSpacerLayoutWeight(rowASpacer,
                        if (main.puzzleDisplayController.isHintsOn) {
                            1.2f
                        } else {
                            0.95f
                        })
                }
                DisplayView.WORD_LOOKUP -> {
                    setSpacerLayoutWeight(rowASpacer, 1.8f)
                }
                DisplayView.CREATE -> {
                    setSpacerLayoutWeight(rowASpacer, 1.7f)
                }
            }
        } else {
            setSpacerLayoutWeight(rowASpacer, 1.0f)
        }
    }

    private fun setSpacerLayoutWeight(spacer: Space, weight: Float) {
        val layoutParams: LinearLayout.LayoutParams = LinearLayout.LayoutParams(spacer.layoutParams)
        layoutParams.weight = weight
        spacer.layoutParams = layoutParams
    }

    private fun showSuggestKey(show: Boolean) {
        if (show && controls.isLandscape) {
            keySuggestPortrait.visibility = View.GONE
            keySuggestLandscape.visibility = View.VISIBLE
        } else if (show) {
            keySuggestPortrait.visibility = View.VISIBLE
            keySuggestLandscape.visibility = View.GONE
        } else {
            keySuggestPortrait.visibility = View.GONE
            keySuggestLandscape.visibility = View.GONE
        }
    }

    private fun showLookupKey(show: Boolean) {
        if (show && controls.isLandscape) {
            keyLookupPortrait.visibility = View.GONE
            keyLookupLandscape.visibility = View.VISIBLE
        } else if (show) {
            keyLookupPortrait.visibility = View.VISIBLE
            keyLookupLandscape.visibility = View.GONE
        } else {
            keyLookupPortrait.visibility = View.GONE
            keyLookupLandscape.visibility = View.GONE
        }
    }

    fun hintsChanged(show: Boolean) {
        currentViewChanged(controls.currentView)
    }

    fun configChanged() {
        currentViewChanged(controls.currentView)
        if (controls.isLandscape) {
            keyRowPortrait.visibility = View.GONE
            keyPrevPortrait.visibility = View.GONE
            keyNextPortrait.visibility = View.GONE
            keyClearPortrait.visibility = View.GONE
            keyPrevLandscape.visibility = View.VISIBLE
            keyNextLandscape.visibility = View.VISIBLE
            keyClearLandscape.visibility = View.VISIBLE

            setSpacerLayoutWeight(rowZSpacer, 1.5f)

        } else {
            keyRowPortrait.visibility = View.VISIBLE
            keyPrevLandscape.visibility = View.GONE
            keyNextLandscape.visibility = View.GONE
            keyClearLandscape.visibility = View.GONE
            keyPrevPortrait.visibility = View.VISIBLE
            keyNextPortrait.visibility = View.VISIBLE
            keyClearPortrait.visibility = View.VISIBLE

            setSpacerLayoutWeight(rowZSpacer, 1.0f)
        }
    }
}
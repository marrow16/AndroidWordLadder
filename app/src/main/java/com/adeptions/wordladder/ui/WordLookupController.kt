package com.adeptions.wordladder.ui

import android.text.InputFilter
import com.adeptions.wordladder.MainActivity
import com.adeptions.wordladder.core.words.Word
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import org.xml.sax.Attributes
import org.xml.sax.InputSource
import org.xml.sax.XMLReader
import org.xml.sax.helpers.DefaultHandler
import java.io.StringReader
import javax.xml.parsers.SAXParser
import javax.xml.parsers.SAXParserFactory
import android.widget.TextView.BufferType

import android.text.method.LinkMovementMethod
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.text.SpannableString
import android.view.KeyEvent
import android.view.View
import android.widget.TableRow
import com.adeptions.wordladder.core.words.Dictionary
import java.lang.Exception
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


private const val WORD_LOOKUP_API_URL = "https://freedictionaryapi.com/api/v1/entries"
private const val WORD_LOOKUP_API_URL2 = "https://api.dictionaryapi.dev/api/v2/entries"
private const val API_PARAM = "/en/"

class WordLookupController(val main: MainActivity) {
    val controls: MainActivityControls = main.controls

    init {
        controls.lookupWordEdit.filters = arrayOf(InputFilter.LengthFilter(15), InputFilter.AllCaps())
        controls.lookupButton.setOnClickListener { onLookupClick() }

        controls.lookupWordEdit.setImeActionLabel("Lookup", KeyEvent.KEYCODE_ENTER)
        controls.lookupWordEdit.setOnEditorActionListener { v, actionId, event -> onEditAction(actionId) }
        controls.lookupWordEdit.setSelectAllOnFocus(true)
    }

    fun lookup() {
        onLookupClick()
    }

    fun lookupWord(word: Word?, enteredWord: String?) {
        controls.show(DisplayView.WORD_LOOKUP)
        if (word != null) {
            controls.lookupWordEdit.setText(word.toString())
            apiLookup(word)
        } else {
            controls.lookupWordEdit.setText(enteredWord?: "")
            startLoading()
            if (enteredWord != null && enteredWord.length in 2..15) {
                val dictionary = Dictionary.Factory.fromWord(main.resources, enteredWord)
                val actualWord = dictionary[enteredWord]
                if (actualWord != null) {
                    apiLookup(actualWord)
                } else {
                    endLoadingNotFound()
                }
            } else {
                endLoadingNotFound()
            }
        }
    }

    private fun onEditAction(actionId: Int): Boolean {
        if (actionId == KeyEvent.KEYCODE_ENTER) {
            onLookupClick()
        }
        return true
    }

    private fun onLookupClick() {
        startLoading()
        val word = controls.lookupWordEdit.text.toString()
        if (word.length in 2..15) {
            val dictionary = Dictionary.Factory.fromWord(main.resources, word)
            lookupWord(dictionary[word], word)
        } else {
            endLoadingNotFound()
        }
    }

    private fun startLoading() {
        controls.lookupLoading.visibility = View.VISIBLE
        controls.lookupErrorRow.visibility = View.GONE
        controls.lookupNotFoundRow.visibility = View.GONE
        controls.lookupNotFound404Row.visibility = View.GONE
        controls.lookupOffensiveRow.visibility = View.GONE
        controls.lookupSeeAlsoRow.visibility = View.GONE
        for (tr: TableRow in controls.lookupMeaningHeaderRows) {
            tr.visibility = View.GONE
        }
        for (tr: TableRow in controls.lookupMeaningDefinitionRows) {
            tr.visibility = View.GONE
        }
    }

    private fun endLoading() {
        controls.lookupLoading.visibility = View.GONE
    }

    private fun endLoadingError() {
        endLoading()
        controls.lookupErrorRow.visibility = View.VISIBLE
    }

    private fun endLoadingOffensive() {
        endLoading()
        controls.lookupOffensiveRow.visibility = View.VISIBLE
    }

    private fun endLoadingNotFound() {
        endLoading()
        controls.lookupNotFoundRow.visibility = View.VISIBLE
    }

    private fun endLoadingNotFound404() {
        endLoading()
        controls.lookupNotFound404Row.visibility = View.VISIBLE
    }

    private fun showMeaning(meaning: WordMeaning) {
        endLoading()
        for (i in 0 until Math.min(5, meaning.list.size)) {
            val definition = meaning.list[i]
            controls.lookupMeaningTypes[i].setText(definition.type?: "unknown")
            val textContent = buildString {
                for (j in 0 until definition.meanings.size) {
                    if (j > 0) {
                        append("\n\n")
                    }
                    append("* ")
                    append(definition.meanings[j])
                }
            }
            controls.lookupMeaningDefinitions[i].setText(textContent)
            controls.lookupMeaningHeaderRows[i].visibility = View.VISIBLE
            controls.lookupMeaningDefinitionRows[i].visibility = View.VISIBLE
        }

        if (meaning.seeAlso.isNotEmpty()) {
            controls.lookupSeeAlsoRow.visibility = View.VISIBLE
            val textContent = buildString {
                for (i in meaning.seeAlso.indices) {
                    if (i > 0) {
                        append(" ")
                    }
                    append(meaning.seeAlso[i].uppercase())
                }
            }
            val spannableContent = SpannableString(textContent)
            var start = 0
            for (also in meaning.seeAlso) {
                val span: ClickableSpan = object : ClickableSpan() {
                    override fun updateDrawState(ds: TextPaint) {
                        ds.isUnderlineText = true
                    }

                    override fun onClick(widget: View) {
                        onSeeAlsoClicked(also.uppercase())
                    }
                }
                val end = start + also.length
                spannableContent.setSpan(span, start, end, 0)
                start = end + 1
            }
            //val wrapperView = controls.lookupSeeAlsos
            controls.lookupSeeAlsos.movementMethod = LinkMovementMethod.getInstance()
            controls.lookupSeeAlsos.setText(spannableContent, BufferType.SPANNABLE)
        }
        controls.customKeyboardContainer.visibility = View.GONE
        controls.lookupWordEdit.clearFocus()
    }

    private fun onSeeAlsoClicked(see: String) {
        startLoading()
        controls.lookupWordEdit.setText(see)
        if (see.length in 2..15) {
            val dictionary = Dictionary.Factory.fromWord(main.resources, see)
            lookupWord(dictionary[see], see)
        } else {
            endLoadingNotFound()
        }
    }

    private fun apiLookup(word: Word) {
        main.requestQueue.cancelAll(null)
        startLoading()
        val stringRequest = StringRequest(
            Request.Method.GET, WORD_LOOKUP_API_URL + API_PARAM + word.toString().toLowerCase(),
            { response ->
                parseResponse(response)
            },
            {
                if (it.networkResponse.statusCode == 404) {
                    endLoadingNotFound404()
                } else {
                    endLoadingError()
                }
            })
        // Add the request to the RequestQueue.
        main.requestQueue.add(stringRequest)
    }

    fun jsonToMap(json: String): Map<String, Any?> {
        val type = object : TypeToken<Map<String, Any?>>() {}.type
        return Gson().fromJson(json, type)
    }

    fun jsonArrayToList(json: String): List<Map<String, Any?>> {
        val type = object : TypeToken<List<Map<String, Any?>>>() {}.type
        return Gson().fromJson(json, type)
    }

    // freedictionaryapi.com
    private fun parseResponse(response: String) {
        try {
            var obj: Map<String, Any?> = jsonToMap(response)
            var entries: List<Map<String, Any?>> = obj.get("entries") as List<Map<String, Any?>>
            if (entries.isEmpty()) {
                endLoadingNotFound404()
            } else {
                val meaning = WordMeaning()
                for (item: Map<String, Any?> in entries) {
                    meaning.add(item)
                }
                showMeaning(meaning)
            }
        } catch (e: Exception) {
            endLoadingError()
        }
    }

    // api.dictionaryapi.dev
    private fun parseResponseAlt(response: String) {
        try {
            val arr: List<Map<String, Any?>> = jsonArrayToList(response)
            val meaning = WordMeaning()
            for (item: Map<String, Any?> in arr) {
                var wd: Any? = item.get("word")
                if (wd != null) {
                    var ms: List<Map<String, Any?>>? = item.get("meanings") as List<Map<String, Any?>>?
                    if (ms != null) {
                        for (m: Map<String, Any?> in ms) {
                            meaning.addAlt(m)
                        }
                    }
                }
            }
            showMeaning(meaning)
        } catch (e: Exception) {
            endLoadingError()
        }
    }

    private class WordMeaning() {
        val types: MutableMap<String,WordDefinitionType> = mutableMapOf()
        val list: MutableList<WordDefinitionType> = ArrayList()
        val seeAlso: MutableList<String> = mutableListOf()

        fun add(m: Map<String, Any?>) {
            val type: String? = m.get("partOfSpeech") as String?
            val defs: List<Map<String, Any?>>? = m.get("senses") as List<Map<String, Any?>>?
            if (type != null && defs != null && defs.size > 0) {
                var wd: WordDefinitionType? = types.get(type)
                if (wd == null) {
                    wd = WordDefinitionType()
                    wd.type = type
                    types.put(type, wd)
                    list.add(wd)
                }
                wd.add(defs)
                addSeeAlsos(defs)
            }
        }

        fun addSeeAlsos(defs: List<Map<String, Any?>>) {
            for (def: Map<String, Any?> in defs) {
                val tags: List<String> = def.get("tags") as List<String>
                for (tag: String in tags) {
                    if (tag.equals("plural")) {
                        val meaning: String? = def.get("definition") as String?
                        if (meaning != null && meaning.startsWith("plural of ", true)) {
                            //                                           1234567890
                            seeAlso.add(meaning.substring(10))
                        }
                    }
                }
            }
        }

        fun addAlt(m: Map<String, Any?>) {
            val type: String? = m.get("partOfSpeech") as String?
            val defs: List<Map<String, Any?>>? = m.get("definitions") as List<Map<String, Any?>>?
            if (type != null && defs != null && defs.size > 0) {
                var wd: WordDefinitionType? = types.get(type)
                if (wd == null) {
                    wd = WordDefinitionType()
                    wd.type = type
                    types.put(type, wd)
                    list.add(wd)
                }
                wd.add(defs)
            }
        }
    }

    private class WordDefinitionType {
        var type: String? = null
        val meanings: MutableList<String> = ArrayList()

        fun add(defs: List<Map<String, Any?>>) {
            for (def: Map<String, Any?> in defs) {
                val txt: String? = def.get("definition") as String?
                if (txt != null) {
                    meanings.add(txt)
                }
            }
        }
    }
}
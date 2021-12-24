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

private const val WORD_LOOKUP_API_URL = "https://unikove.com/projects/scrabble_widget/scrabble_api.php"
private const val API_PARAM = "?word="

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

    private fun showMeaning(meaning: WordMeaning) {
        endLoading()
        for (i in 0 until Math.min(5, meaning.definitions.size)) {
            val definition = meaning.definitions[i]
            controls.lookupMeaningTypes[i].setText(definition.type?: "unknown")
            controls.lookupMeaningDefinitions[i].setText(definition.text?: "Unknown meaning")
            controls.lookupMeaningHeaderRows[i].visibility = View.VISIBLE
            controls.lookupMeaningDefinitionRows[i].visibility = View.VISIBLE
        }
        if (meaning.crossRefs.isNotEmpty()) {
            controls.lookupSeeAlsoRow.visibility = View.VISIBLE
            val textContent = buildString {
                for (i in meaning.crossRefs.indices) {
                    if (i > 0) {
                        append(" ")
                    }
                    append(meaning.crossRefs[i].see!!.uppercase())
                }
            }
            val spannableContent = SpannableString(textContent)
            var start = 0
            for (crossRef in meaning.crossRefs) {
                val text = crossRef.see?: ""
                val span: ClickableSpan = object : ClickableSpan() {
                    override fun updateDrawState(ds: TextPaint) {
                        ds.isUnderlineText = true
                    }

                    override fun onClick(widget: View) {
                        onSeeAlsoClicked(text.uppercase())
                    }
                }
                val end = start + text.length
                spannableContent.setSpan(span, start, end, 0)
                start = end + 1
            }
            //val wrapperView = controls.lookupSeeAlsos
            controls.lookupSeeAlsos.movementMethod = LinkMovementMethod.getInstance()
            controls.lookupSeeAlsos.setText(spannableContent, BufferType.SPANNABLE)
        }
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
            Request.Method.GET, WORD_LOOKUP_API_URL + API_PARAM + word.toString(),
            { response ->
                parseResponse(response)
            },
            {
                endLoadingError()
            })
        // Add the request to the RequestQueue.
        main.requestQueue.add(stringRequest)
    }

    private fun parseResponse(response: String) {
        if (response.startsWith("0")) {
            endLoadingOffensive()
        } else {
            try {
                val saxParserFactory: SAXParserFactory = SAXParserFactory.newInstance()
                val saxParser: SAXParser = saxParserFactory.newSAXParser()
                val xmlReader: XMLReader = saxParser.xmlReader
                val meaningHandler = MeaningHTMLHandler()
                xmlReader.contentHandler = meaningHandler
                val stringReader = StringReader(response)
                val inputSource = InputSource(stringReader)
                xmlReader.parse(inputSource)

                showMeaning(meaningHandler.meaning)
            } catch (e: Exception) {
                endLoadingError()
            }
        }
    }

    private class MeaningHTMLHandler(): DefaultHandler() {
        val meaning = WordMeaning()

        private var onDefinition: WordDefinition? = null
        private var onCrossRef: WordCrossRef? = null

        private var inPos: Boolean = false
        private var inDefinition: Boolean = false
        private var inCrossLink: Boolean = false

        override fun startElement(uri: String?, localName: String, qName: String?, attributes: Attributes?) {
            val classVal: String = if (attributes != null) {
                attributes.getValue("class")?: ""
            } else {
                ""
            }
            when (localName) {
                "div" -> {
                    if (classVal == "hom") {
                        onDefinition = meaning.addDefinition()
                    }
                }
                "span" -> {
                    inPos = classVal == "pos"
                    inDefinition = classVal == "def"
                }
                "a" -> {
                    inCrossLink = attributes != null && (attributes.getValue("data-resource")?: "") == "scrabble"
                    if (inCrossLink) {
                        onCrossRef = meaning.addCrossRef()
                    }
                }
            }
        }

        override fun endElement(uri: String?, localName: String?, qName: String?) {
            inPos = false;
            inDefinition = false
            inCrossLink = false
            onCrossRef = null
        }

        override fun characters(ch: CharArray?, start: Int, length: Int) {
            val str = String(ch!!, start, length)
            if (onDefinition != null && inPos) {
                onDefinition!!.addType(str)
            } else if (onDefinition != null && inDefinition) {
                onDefinition!!.addText(str)
            } else if (onCrossRef != null && inCrossLink) {
                onCrossRef!!.addText(str)
            }
        }
    }

    private class WordMeaning() {
        val definitions: MutableList<WordDefinition> = ArrayList()
        val crossRefs: MutableList<WordCrossRef> = ArrayList()

        fun addDefinition(): WordDefinition {
            val result = WordDefinition()
            definitions.add(result)
            return result
        }

        fun addCrossRef(): WordCrossRef {
            val result =  WordCrossRef()
            crossRefs.add(result)
            return result
        }
    }

    private class WordDefinition {
        var type: String? = null
        var text: String? = null

        fun addType(str: String) {
            type = type?:"" + str
        }

        fun addText(str: String) {
            text = text?:"" + str
        }
    }

    private class WordCrossRef {
        var see: String? = null

        fun addText(str: String) {
            see = see?:"" + str
        }
    }
}
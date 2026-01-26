package com.example.oneclickdictionary.fragments
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.oneclickdictionary.DictionaryDBHelper
import com.example.oneclickdictionary.R
import com.example.oneclickdictionary.SavedWordsViewModel
import com.example.oneclickdictionary.WordSavedListener
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.util.Timer
import java.util.TimerTask

class DictionaryFragment : Fragment(R.layout.dictionary_fragment), WordSavedListener {
    private lateinit var databaseHelper: DictionaryDBHelper
    private lateinit var viewModel: SavedWordsViewModel
    private lateinit var inputBox: TextInputEditText
    private lateinit var inputBoxLayout: TextInputLayout
    private lateinit var constraintLayout: ConstraintLayout
    private lateinit var saveButton: Button
    private lateinit var clearButton: Button
    private lateinit var resultListView: ListView
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var handler: Handler
    private val resultList = ArrayList<String>()

    private fun moveEditTextToTop(constraintLayout: ConstraintLayout, editText: TextInputLayout) {
        requireActivity().runOnUiThread {
            val constraintSet = ConstraintSet()
            constraintSet.clone(constraintLayout)

            constraintSet.clear(editText.id, ConstraintSet.BOTTOM)
            constraintSet.applyTo(constraintLayout)
        }
    }

    private var textWatcher: TextWatcher = object : TextWatcher {
        var delay : Long = 1000
        var timer = Timer()

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            clearButton.visibility = if (s.isEmpty()) View.GONE else View.VISIBLE
            saveButton.visibility = if (s.isEmpty()) View.GONE else View.VISIBLE
            timer.cancel()
            timer.purge()
            resultList.clear()
        }

        override fun afterTextChanged(s: Editable) {
            timer = Timer()
            timer.schedule(object : TimerTask() {
                override fun run() {
                    val word = inputBox.getText().toString().trim()
                    if(word != ""){
                        val wordDefinitions = databaseHelper.getWord(word)
                        var foundDefinition = true
                        if (wordDefinitions.isEmpty()){
                            resultList.add("There were no definitions found")
                            foundDefinition = false
                        }
                        else {
                            for (item in wordDefinitions) {
                                resultList.add(item.definition.removeSurrounding("\""))
                            }
                            moveEditTextToTop(constraintLayout, inputBoxLayout)
                        }
                        handler.postDelayed({
                            adapter.notifyDataSetChanged()
                            if (foundDefinition) {
                                saveButton.visibility = if (s.isEmpty()) View.GONE else View.VISIBLE
                            }
                        }, 0)
                    }
                }
            }, delay)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel = ViewModelProvider(requireActivity())[SavedWordsViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val root = inflater.inflate(R.layout.dictionary_fragment, null)
        val context = requireContext()
        databaseHelper = DictionaryDBHelper(context)
        databaseHelper.createDatabase()

        handler = Handler(Looper.getMainLooper())

        inputBox = root.findViewById(R.id.inputBox)
        inputBoxLayout = root.findViewById(R.id.outlined_text_input_layout)
        inputBox.addTextChangedListener(textWatcher)

        inputBox.requestFocus()
        inputBoxLayout.requestFocus()

        constraintLayout = root.findViewById(R.id.constraint_layout)

        resultListView = root.findViewById(R.id.resultListView)
        adapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, resultList)
        resultListView.adapter = adapter

        saveButton = root.findViewById(R.id.searchButton)
        saveButton.setOnClickListener {
            val word = inputBox.getText().toString()
            databaseHelper.addWord(word, resultList)
            val definitions = resultList.toMutableList()
            onWordSaved(word, definitions)

            inputBox.getText()?.clear()
            resultList.clear()
            adapter.notifyDataSetChanged()
        }

        clearButton = root.findViewById(R.id.clearButton)
        clearButton.setOnClickListener {
            inputBox.getText()?.clear()
            resultList.clear()
            adapter.notifyDataSetChanged()
        }

        return root
    }

    override fun onWordSaved(word: String, definitions: MutableList<String>) {
        viewModel.addWord(word, definitions)
        Toast.makeText(context, "Word saved", Toast.LENGTH_SHORT).show()
    }
}

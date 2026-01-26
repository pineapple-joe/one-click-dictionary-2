package com.example.oneclickdictionary.fragments
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ExpandableListView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.oneclickdictionary.DictionaryDBHelper
import com.example.oneclickdictionary.R
import com.example.oneclickdictionary.SavedWordsViewModel
import com.example.oneclickdictionary.SortOrder
import com.example.oneclickdictionary.adapters.SavedTranslationsAdapter
import com.example.oneclickdictionary.receivers.AlarmReceiver
import com.google.android.material.button.MaterialButton
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SavedDefinitionsFragment : Fragment(R.layout.saved_definitions) {
    private lateinit var databaseHelper: DictionaryDBHelper
    private lateinit var expandableListView: ExpandableListView
    private lateinit var viewModel: SavedWordsViewModel
    private lateinit var adapter: SavedTranslationsAdapter
    private lateinit var exportButton: MaterialButton
    private lateinit var importButton: MaterialButton
    private lateinit var testNotificationButton: MaterialButton  // Test button - comment out for production
    private lateinit var sortSpinner: Spinner

    private val createDocumentLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        uri?.let {
            try {
                val savedWords = viewModel.savedWords.value ?: emptyList()
                val jsonObject = JSONObject()

                for (entry in savedWords) {
                    val definitionsArray = org.json.JSONArray(entry.definitions)
                    jsonObject.put(entry.word, definitionsArray)
                }

                requireContext().contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(jsonObject.toString(2).toByteArray())
                }

                Toast.makeText(requireContext(), "Words exported successfully", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val openDocumentLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            try {
                requireContext().contentResolver.openInputStream(uri)?.use { inputStream ->
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val jsonString = reader.readText()
                    val jsonObject = JSONObject(jsonString)

                    var importedCount = 0
                    val keys = jsonObject.keys()
                    while (keys.hasNext()) {
                        val word = keys.next()
                        val definitionsArray = jsonObject.getJSONArray(word)
                        val definitions = ArrayList<String>()

                        for (i in 0 until definitionsArray.length()) {
                            definitions.add(definitionsArray.getString(i))
                        }

                        databaseHelper.addWord(word, definitions)
                        importedCount++
                    }

                    viewModel.loadSavedWords()
                    Toast.makeText(requireContext(), "Imported $importedCount words successfully", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Import failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel = ViewModelProvider(requireActivity())[SavedWordsViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.saved_definitions, container, false)
        expandableListView = view.findViewById(R.id.savedDefinitionsListView)
        exportButton = view.findViewById(R.id.exportButton)
        importButton = view.findViewById(R.id.importButton)
        testNotificationButton = view.findViewById(R.id.testNotificationButton)  // Test button - comment out for production
        sortSpinner = view.findViewById(R.id.sortSpinner)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = requireContext()
        databaseHelper = DictionaryDBHelper(context)

        adapter = SavedTranslationsAdapter(requireContext(), emptyList()) { word ->
            viewModel.removeWord(word)
        }
        expandableListView.setAdapter(adapter)

        setupSortSpinner()

        viewModel.savedWords.observe(viewLifecycleOwner) { savedWords ->
            adapter.updateData(savedWords)
        }

        exportButton.setOnClickListener {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "dictionary_export_$timestamp.json"
            createDocumentLauncher.launch(fileName)
        }

        importButton.setOnClickListener {
            openDocumentLauncher.launch(arrayOf("application/json", "application/*"))
        }

        // Test button - comment out for production
        testNotificationButton.setOnClickListener {
            AlarmReceiver().onReceive(requireContext(), android.content.Intent())
            Toast.makeText(requireContext(), "Test notification sent", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupSortSpinner() {
        val sortOptions = resources.getStringArray(R.array.sort_options)
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, sortOptions)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sortSpinner.adapter = spinnerAdapter

        sortSpinner.setSelection(viewModel.getSortOrder().ordinal)

        sortSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val sortOrder = SortOrder.entries[position]
                viewModel.setSortOrder(sortOrder)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

}

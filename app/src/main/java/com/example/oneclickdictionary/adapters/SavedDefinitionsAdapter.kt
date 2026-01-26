package com.example.oneclickdictionary.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.oneclickdictionary.R
import com.example.oneclickdictionary.SavedWordEntry

class SavedTranslationsAdapter(
    private val context: Context,
    private var savedWords: List<SavedWordEntry>,
    private val onWordRemoved: (String) -> Unit
) : BaseExpandableListAdapter() {

    override fun getChild(groupPosition: Int, childPosition: Int): String {
        return savedWords[groupPosition].definitions[childPosition]
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    fun updateData(newSavedWords: List<SavedWordEntry>) {
        savedWords = newSavedWords
        notifyDataSetChanged()
    }

    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
        val definition = getChild(groupPosition, childPosition)
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(android.R.layout.simple_list_item_1, null)
        val textView = view.findViewById<TextView>(android.R.id.text1)
        textView.text = definition
        return view
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return savedWords[groupPosition].definitions.size
    }

    override fun getGroup(groupPosition: Int): String {
        return savedWords[groupPosition].word
    }

    override fun getGroupCount(): Int {
        return savedWords.size
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
        val view = LayoutInflater.from(context).inflate(R.layout.custom_group_item, parent, false)
        val textView = view.findViewById<TextView>(R.id.groupTextView)
        val removeIcon = view.findViewById<ImageView>(R.id.removeIcon)

        val word = getGroup(groupPosition)

        removeIcon.setOnClickListener {
            onWordRemoved(word)
        }

        textView.text = word.replaceFirstChar { it.uppercase() }
        return view
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }
}
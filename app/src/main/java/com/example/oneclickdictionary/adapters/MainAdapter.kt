package com.example.oneclickdictionary.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.oneclickdictionary.fragments.DictionaryFragment
import com.example.oneclickdictionary.fragments.QuizFragment
import com.example.oneclickdictionary.fragments.SavedDefinitionsFragment


class ViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> DictionaryFragment()
            1 -> SavedDefinitionsFragment()
            2 -> QuizFragment()
            else -> throw IllegalArgumentException("Invalid position")
        }
    }
}
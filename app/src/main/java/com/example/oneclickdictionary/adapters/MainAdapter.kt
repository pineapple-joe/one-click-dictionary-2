package com.example.oneclickdictionary.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.oneclickdictionary.fragments.DictionaryFragment
import com.example.oneclickdictionary.fragments.SavedDefinitionsFragment


class ViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> DictionaryFragment()
            1 -> SavedDefinitionsFragment()
            else -> throw IllegalArgumentException("Invalid position")
        }
    }
}
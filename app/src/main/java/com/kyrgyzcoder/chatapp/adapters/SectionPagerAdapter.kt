package com.kyrgyzcoder.chatapp.adapters

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.kyrgyzcoder.chatapp.main_fragments.*

//private val TAB_TITLES = arrayOf(
//    "",
//    "",
//    "",
//    "",
//    ""
//)

class SectionPagerAdapter(private val context: Context, fm: FragmentManager) :
    FragmentStatePagerAdapter(fm) {
    override fun getItem(position: Int): Fragment {

        return when (position) {
            0 -> ChatsMainFragment()
            1 -> FriendsMainFragment()
            else -> RequestsFragment()
        }
    }

//    override fun getPageTitle(position: Int): CharSequence? {
//        return TAB_TITLES[position]
//    }

    override fun getCount(): Int {
        return 3
    }

}
package com.example.test

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator


class MyCoursesFragment : Fragment(R.layout.fragment_my_courses) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val viewPager = view.findViewById<ViewPager2>(R.id.viewPager)
        val tabLayout = view.findViewById<TabLayout>(R.id.tabLayout)

        val adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = 2
            override fun createFragment(position: Int): Fragment {
                return if (position == 0) CreatedCourseFragment() else EnrolledCourseFragment()
            }
        }

        viewPager.adapter = adapter
        TabLayoutMediator(tabLayout, viewPager) { tab, pos ->
            tab.text = if (pos == 0) "Dibuat Saya" else "Enrolled"
        }.attach()
    }
}

package com.york.exordi

import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.york.exordi.adapters.FragmentAdapter
import com.york.exordi.base.BaseActivity
import com.york.exordi.shared.Const
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {

    private var feedFragment: RootFragment? = null
    private var addPostFragment: RootFragment? = null
    private var exploreFragment: RootFragment? = null
    private var selectedFragment: RootFragment? = null

    private var prevMenuItem: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        feedFragment = RootFragment.newInstance(Const.FRAGMENT_FEED)
        addPostFragment = RootFragment.newInstance(Const.FRAGMENT_PROFILE)
        exploreFragment = RootFragment.newInstance(Const.FRAGMENT_EXPLORE)
        selectedFragment = feedFragment
        setupBottomNav()
        setupViewPager()

    }

    private fun setupBottomNav() {
        bottomNav.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_feed -> selectFragment(feedFragment, 0)
                R.id.navigation_add_post -> selectFragment(addPostFragment, 1)
                R.id.navigation_explore -> selectFragment(exploreFragment, 2)
                else -> false
            }
        }
    }

    private fun selectFragment(fragment: RootFragment?, fragmentIndex: Int): Boolean {
        nonSwipeableViewPager.setCurrentItem(fragmentIndex, false)
        if (selectedFragment == fragment) fragment?.popToRoot()
        selectedFragment = fragment
        return true
    }

    private fun setupViewPager() {
        nonSwipeableViewPager.adapter = FragmentAdapter(
            supportFragmentManager, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
        )
            .apply {
                feedFragment?.let { add(it, getString(R.string.nav_feed)) }
                addPostFragment?.let { add(it, getString(R.string.nav_add_post)) }
                exploreFragment?.let { add(it, getString(R.string.nav_explore)) }
            }

        nonSwipeableViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                if (prevMenuItem != null) {
                    prevMenuItem!!.isChecked = false
                } else {
                    bottomNav.menu.getItem(0).isChecked = false
                }
                bottomNav.menu.getItem(position).isChecked = true
                prevMenuItem = bottomNav.menu.getItem(position)
            }
        })

        nonSwipeableViewPager.offscreenPageLimit = 2
    }

}
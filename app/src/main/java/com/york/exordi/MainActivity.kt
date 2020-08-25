package com.york.exordi

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.core.view.get
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.york.exordi.adapters.FragmentAdapter
import com.york.exordi.addpost.CameraActivity
import com.york.exordi.base.BaseActivity
import com.york.exordi.events.ChangeTabEvent
import com.york.exordi.shared.Const
import com.york.exordi.shared.registerActivityForEvents
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.Subscribe

class MainActivity : BaseActivity() {

    private var feedFragment: RootFragment? = null
    private var addPostFragment: RootFragment? = null
    private var exploreFragment: RootFragment? = null
    private var selectedFragment: RootFragment? = null

    private var prevMenuItem: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        registerActivityForEvents()
        feedFragment = RootFragment.newInstance(Const.FRAGMENT_FEED)
        addPostFragment = RootFragment.newInstance(Const.FRAGMENT_ADD_POST)
        exploreFragment = RootFragment.newInstance(Const.FRAGMENT_EXPLORE)
        selectedFragment = feedFragment
        setupBottomNav()
        setupViewPager()

    }

    @Subscribe
    fun onChangeTabEvent(event: ChangeTabEvent) {
        nonSwipeableViewPager.currentItem = 0
    }

    private fun setupBottomNav() {
        bottomNav.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_feed -> selectFragment(feedFragment, 0)
//                R.id.navigation_add_post -> selectFragment(addPostFragment, 1)
                R.id.navigation_add_post -> startCameraActivity()
                R.id.navigation_explore -> selectFragment(exploreFragment, 2)
                else -> false
            }
        }
    }

    private fun startCameraActivity(): Boolean {
        startActivityForResult(Intent(this, CameraActivity::class.java), 100)
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100) {
            if (prevMenuItem == null) {
                prevMenuItem = bottomNav.menu.getItem(0)
            }
            bottomNav.menu.getItem(1).isChecked = false
            prevMenuItem?.isChecked = true
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
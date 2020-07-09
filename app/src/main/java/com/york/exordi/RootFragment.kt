package com.york.exordi

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import com.york.exordi.explore.ExploreFragment
import com.york.exordi.feed.FeedFragment
import com.york.exordi.profile.ProfileFragment
import com.york.exordi.shared.Const

/**
 * A simple [Fragment] subclass.
 */
class RootFragment : Fragment() {

    companion object {

        private const val KEY = "FragmentKey"
        fun newInstance(key: String) = RootFragment().apply {
            arguments = Bundle()
                .apply { putString(KEY, key) }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_root, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            when (it.getString(KEY)) {
                Const.FRAGMENT_FEED -> replaceContainer(FeedFragment())
                Const.FRAGMENT_PROFILE -> replaceContainer(ProfileFragment())
                Const.FRAGMENT_EXPLORE -> replaceContainer(ExploreFragment())
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Log.e("ROOT_FRAGMENT:", childFragmentManager.backStackEntryCount.toString())
                if (childFragmentManager.backStackEntryCount == 0) {
                    activity?.finish()
                }
            }

        })
    }

    private fun replaceContainer(fragment: Fragment) {
        if (!isAdded) return

        childFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment, null)
            .commit()
    }

    fun popToRoot() {
        if (!isAdded) return

        for (i in 0 until childFragmentManager.backStackEntryCount) {
            childFragmentManager.popBackStack()
        }
    }
}

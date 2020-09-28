package com.york.exordi.explore

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import com.york.exordi.R
import com.york.exordi.adapters.SearchUserAdapter
import com.york.exordi.events.ChangeTabEvent
import com.york.exordi.feed.ui.OtherUserProfileActivity
import com.york.exordi.feed.ui.ProfileActivity
import com.york.exordi.models.SearchUserResult
import com.york.exordi.shared.Const
import com.york.exordi.shared.OnItemClickListener
import com.york.exordi.shared.PrefManager
import com.york.exordi.shared.makeInternetSafeRequest
import kotlinx.android.synthetic.main.fragment_explore.*
import org.greenrobot.eventbus.EventBus

class ExploreFragment : Fragment() {

    private val viewModel by viewModels<ExploreViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_explore, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        browseArtBtn.setOnClickListener { EventBus.getDefault().post(ChangeTabEvent("art")) }
//        browseFashionBtn.setOnClickListener { EventBus.getDefault().post(ChangeTabEvent("fashion")) }
        browseMusicBtn.setOnClickListener { EventBus.getDefault().post(ChangeTabEvent("music")) }
        browseSportBtn.setOnClickListener { EventBus.getDefault().post(ChangeTabEvent("sport")) }

        exploreSearchEt.doOnTextChanged { text, start, before, count ->
            if (text.toString().isNotEmpty()) {
                requireContext().makeInternetSafeRequest {
                    searchUser(text.toString())
                }
            } else {
                exploreSearchPb.visibility = View.INVISIBLE
                exploreUsersRv.visibility = View.GONE
            }
        }
    }

    private fun startProfileActivity(username: String) {
        if (username == PrefManager.getMyPrefs(requireContext().applicationContext).getString(Const.PREF_USERNAME, "")) {
            startActivity(Intent(activity, ProfileActivity::class.java))
        } else {
            startActivity(Intent(activity, OtherUserProfileActivity::class.java).apply {
                putExtra(Const.EXTRA_USERNAME, username)
            })
        }
    }

    private fun searchUser(username: String) {
        val adapter = SearchUserAdapter(object: OnItemClickListener {
            override fun <T> onItemClick(listItem: T) {
                startProfileActivity((listItem as SearchUserResult).username)
            }

        })
        exploreSearchPb.visibility = View.VISIBLE
        exploreUsersRv.visibility = View.VISIBLE
        viewModel.isUserListEmpty.observe(viewLifecycleOwner) {
            exploreSearchPb.visibility = View.INVISIBLE
            if (it) {
                exploreEmptyView.visibility = View.VISIBLE
            } else {
                exploreEmptyView.visibility = View.GONE
            }
        }
        exploreUsersRv.layoutManager = LinearLayoutManager(requireContext())
        exploreUsersRv.adapter = adapter
        viewModel.searchUser(username)?.observe(viewLifecycleOwner) {
            exploreSearchPb.visibility = View.INVISIBLE
            adapter.submitList(it)
        }
    }
}
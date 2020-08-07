package com.york.exordi.feed

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.PopupMenu
import android.widget.PopupWindow
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.york.exordi.R
import com.york.exordi.adapters.PostAdapter
import com.york.exordi.events.EditProfileEvent
import com.york.exordi.models.Profile
import com.york.exordi.models.ProfileData
import com.york.exordi.shared.Const
import com.york.exordi.shared.OnItemClickListener
import com.york.exordi.shared.makeInternetSafeRequest
import kotlinx.android.synthetic.main.fragment_feed.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

/**
 * A simple [Fragment] subclass.
 * Use the [FeedFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FeedFragment : Fragment() {

    private val viewModel by viewModels<FeedViewModel>()

    private var layout: FrameLayout? = null
    private lateinit var categoriesPopupWindow: PopupWindow
    private var progressBar: CircularProgressDrawable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_feed, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        context?.makeInternetSafeRequest { viewModel.getNewResults() }
        layout = view.findViewById<FrameLayout>(R.id.feedDimLayout)
        layout?.foreground?.alpha = 0

        progressBar = CircularProgressDrawable(requireContext()).apply {
            strokeWidth = 5F
            centerRadius = 30F
            setColorSchemeColors(ContextCompat.getColor(requireContext(), R.color.textColorPrimary))
            start()
        }

        viewModel.profile.observe(viewLifecycleOwner) {
            if (!TextUtils.isEmpty(it.data.profilePic)) {
                Glide.with(requireContext()).load(it.data.profilePic).placeholder(progressBar).into(feedProfileButton)
            }
            feedProfileButton.setOnClickListener {v ->
                if (!EventBus.getDefault().isRegistered(this)) {
                    EventBus.getDefault().register(this)
                }
                startActivity(Intent(activity, ProfileActivity::class.java).apply { putExtra(Const.EXTRA_PROFILE, it) })
            }
        }

        val adapter = PostAdapter(object : OnItemClickListener {
            override fun <T> onItemClick(listItem: T) {

            }
        })
        viewModel.results?.observe(viewLifecycleOwner) {
            feedPb.visibility = View.GONE
            feedSwipeRefreshLayout.isRefreshing = false
            adapter.submitList(it)
            feedRv.visibility = View.VISIBLE
            if (it.isNotEmpty()) {
//                feedEmptyView.visibility = View.GONE
            } else {
//                feedEmptyView.visibility = View.VISIBLE
//                feedRv.visibility = View.INVISIBLE
            }
        }
        feedRv.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        feedRv.adapter = adapter
        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(feedRv)
        feedSwipeRefreshLayout.setOnRefreshListener {
            viewModel.dataSourceLiveData?.value?.invalidate()
        }
        setupPopupWindow()
        feedCategoryBtn.setOnClickListener { v ->
            categoriesPopupWindow.showAsDropDown(v)
            layout?.foreground?.alpha = 220
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }

    @Subscribe
    fun onEditProfile(event: EditProfileEvent) {
        val profile = Profile(ProfileData(event.email, event.username, event.birthday, event.bio, event.profilePic))
        viewModel.profile.value = profile
    }

    private fun setupPopupWindow() {
        val inflater = context?.applicationContext?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val layout = inflater.inflate(R.layout.feed_categories, null)
        // TODO set onClickListeners for popup buttons
        categoriesPopupWindow = PopupWindow(layout, ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT, true)
        categoriesPopupWindow.setOnDismissListener {
            this.layout?.foreground?.alpha = 0
        }

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FeedFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) = FeedFragment()
    }
}
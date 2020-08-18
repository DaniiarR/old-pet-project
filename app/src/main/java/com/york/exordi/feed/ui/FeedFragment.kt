package com.york.exordi.feed

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.PopupWindow
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.york.exordi.R
import com.york.exordi.adapters.CategoryAdapter
import com.york.exordi.adapters.CommentAdapter
import com.york.exordi.adapters.PostAdapter
import com.york.exordi.events.EditProfileEvent
import com.york.exordi.models.CategoryData
import com.york.exordi.models.Profile
import com.york.exordi.models.ProfileData
import com.york.exordi.models.Result
import com.york.exordi.shared.*
import kotlinx.android.synthetic.main.feed_categories.view.*
import kotlinx.android.synthetic.main.fragment_feed.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

/**
 * A simple [Fragment] subclass.
 * Use the [FeedFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FeedFragment : Fragment() {

    companion object {
        const val RC_FULLSCREEN_ACTIVITY = 10
    }

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

        layout = view.findViewById<FrameLayout>(R.id.feedDimLayout)
        layout?.foreground?.alpha = 0

        setupPopupWindow()

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

        val commentAdapter = CommentAdapter()
//        val adapter = PostAdapter(Glide.with(this), object : OnBindListener {
//            override fun onBind(postId: String, recyclerView: RecyclerView) {
//                commentAdapter.submitList(null)
//                viewModel.getNewComments(postId)
//                viewModel.comments?.observe(viewLifecycleOwner) {
//                    if (it.isNotEmpty()) {
//                        commentAdapter.submitList(it)
//                        recyclerView.layoutManager = LinearLayoutManager(requireContext())
//                        recyclerView.adapter = commentAdapter
//            }
//        }
//            }
//        })
        val adapter = PostAdapter(Glide.with(this), viewLifecycleOwner)
        adapter.clickListener = object : OnPostClickListener {
            override fun onItemClick(position: Int, post: Result, tag: String, view: View?) {
                when (tag) {
                    Const.TAG_UPVOTE -> {
                        requireContext().makeInternetSafeRequest {
                            toggleUpvoteButton(view as ImageButton, post.upvotedByUser)
                            post.upvotedByUser = !post.upvotedByUser
                            viewModel.toggleUpvote(post.id)
                            viewModel.isUpvoteSuccessful.observe(viewLifecycleOwner) {
                                it?.let {
                                    if (!it) {
                                        post.upvotedByUser = !post.upvotedByUser
                                        adapter.notifyItemChanged(position)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        viewModel.results?.observe(viewLifecycleOwner) {
            feedPb.visibility = View.GONE
            feedSwipeRefreshLayout.isRefreshing = false
            feedRv.setResultArrayList(it)
            adapter.submitList(it)
            feedRv.visibility = View.VISIBLE
        }
        feedRv.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        feedRv.setOnFullscreenButtonClickListener(object : OnFullscreenButtonClickListener {
            override fun onButtonClick(videoUrl: String, currentPosition: Long) {
                launchFullscreenVideoActivity(videoUrl, currentPosition)
            }
        })
        feedRv.adapter = adapter
        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(feedRv)
        feedSwipeRefreshLayout.setOnRefreshListener {
            viewModel.refreshResults()
            feedPb.visibility = View.VISIBLE

        }
        feedCategoryBtn.setOnClickListener { v ->
            categoriesPopupWindow.showAsDropDown(v)
            layout?.foreground?.alpha = 220
        }
    }

    private fun toggleUpvoteButton(button: ImageButton, upvotedByUser: Boolean) {
        if (!upvotedByUser) {
            button.setImageResource(R.drawable.ic_upvote_filled)
        } else {
            button.setImageResource(R.drawable.ic_upvote)
        }
    }

    private fun launchFullscreenVideoActivity(
        videoUrl: String,
        currentPosition: Long
    ) {
        startActivityForResult(Intent(activity, FullscreenVideoActivity::class.java).apply {
            putExtra(Const.EXTRA_VIDEO_URL, videoUrl)
            putExtra(Const.EXTRA_PLAYBACK_POSITION, currentPosition)
        }, RC_FULLSCREEN_ACTIVITY)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_FULLSCREEN_ACTIVITY && resultCode == Activity.RESULT_OK) {
            setPlaybackPosition(data!!.getLongExtra(Const.EXTRA_PLAYBACK_POSITION, 0))
        }
    }

    private fun setPlaybackPosition(position: Long) {
        feedRv.setPlaybackPosition(position)
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
        val adapter = CategoryAdapter()
        viewModel.categories.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                adapter.setValues(it)
                adapter.setOnItemClickListener(object : OnItemClickListener {
                    override fun <T> onItemClick(listItem: T) {
                        val category = listItem as CategoryData
                        category.isSelected = true
                        viewModel.selectedCategory.value = category.id
                        feedCategoryBtn.text = category.name
                        for (cat in adapter.getCategories()) {
                            if (cat.id != category.id) {
                                cat.isSelected = false
                            }
                        }
                        adapter.notifyDataSetChanged()
                        categoriesPopupWindow.dismiss()
                        viewModel.refreshResults()
                        feedPb.visibility = View.VISIBLE
                    }
                })
            }
        }
//        viewModel.selectedCategory.observe(viewLifecycleOwner) {
//            if (it != 0) {
//                viewModel.getNewResults()
//            }
//        }
        layout.categoriesRv.layoutManager = LinearLayoutManager(requireContext())
        layout.categoriesRv.adapter = adapter
        // TODO set onClickListeners for popup buttons
        categoriesPopupWindow = PopupWindow(layout, ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT, true)
        categoriesPopupWindow.setOnDismissListener {
            this.layout?.foreground?.alpha = 0
        }

    }
}
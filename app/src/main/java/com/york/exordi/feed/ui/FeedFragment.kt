package com.york.exordi.feed.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.york.exordi.R
import com.york.exordi.adapters.CategoryAdapter
import com.york.exordi.adapters.CommentAdapter
import com.york.exordi.adapters.PostAdapter
import com.york.exordi.events.ChangeTabEvent
import com.york.exordi.events.EditProfileEvent
import com.york.exordi.feed.viewmodel.FeedViewModel
import com.york.exordi.models.*
import com.york.exordi.shared.*
import kotlinx.android.synthetic.main.feed_categories.view.*
import kotlinx.android.synthetic.main.feed_list_item.*
import kotlinx.android.synthetic.main.feed_list_item.view.*
import kotlinx.android.synthetic.main.fragment_feed.*
import kotlinx.android.synthetic.main.fragment_feed.view.*
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

    private var adapter: PostAdapter? = null
    private lateinit var categoryAdapter: CategoryAdapter
    private val orderButtons = arrayListOf<MaterialButton>()

    private var areCommentsOpen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerFragmentForEvents()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_feed, container, false)
    }

    @Subscribe
    fun onChangeTabEvent(event: ChangeTabEvent) {
        requireContext().makeInternetSafeRequest {
            viewModel.categories.value?.forEach {
                if (event.category.toLowerCase() == it.name.toLowerCase()) {
                    it.isSelected = true
                    feedCategoryBtn.text = it.name
                    viewModel.selectedCategory.value = it.id
                }
            }
            viewModel.refreshResults()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        layout = view.findViewById<FrameLayout>(R.id.feedDimLayout)
        layout?.foreground?.alpha = 0

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }

        requireContext().makeInternetSafeRequest { setupPopupWindow() }

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
            PrefManager.getMyPrefs(requireContext().applicationContext).edit().putString(Const.PREF_USERNAME, it.data.username).apply()
        }

        adapter = PostAdapter(Glide.with(this), viewLifecycleOwner)
        adapter?.clickListener = object : OnPostClickListener {
            override fun onItemClick(position: Int, post: Result, tag: String, vararg view: View?) {
                when (tag) {
                    Const.TAG_UPVOTE -> {
                        requireContext().makeInternetSafeRequest {
                            toggleUpvoteButton(view[0] as ImageButton, post.upvotedByUser)
                            post.upvotedByUser = !post.upvotedByUser
                            viewModel.toggleUpvote(post.id)
                            viewModel.isUpvoteSuccessful.observe(viewLifecycleOwner) {
                                it?.let {
                                    if (!it) {
                                        post.upvotedByUser = !post.upvotedByUser
                                        adapter?.notifyItemChanged(position)

                                    }
                                }
                            }
                        }
                    }
                    Const.TAG_PROFILE -> {
                        launchOtherUserProfileActivity(post.author.username)
                    }
                    Const.TAG_COMMENTS -> {
                        if (!areCommentsOpen) {
                            showComments(post, view[0] as View, position)
                        } else {
                            hideComments(view[0] as View)
                        }
                    }
                    Const.TAG_DELETE_POST -> {
                        showDeletePostDialog(post.id)
                        viewModel.isDeletePostSuccessful.observe(viewLifecycleOwner) {
                            it?.let {
                                if (it) {
                                    Toast.makeText(requireContext(), "Post deleted successfully", Toast.LENGTH_SHORT).show()
                                    adapter?.notifyItemRemoved(position)
                                } else {
                                    Toast.makeText(requireContext(), "Could not delete this post", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                }
            }
        }
        adapter?.onViewDetachedListener = object : OnViewDetachedFromWindowListener {
            override fun onViewDetached() {
                feedCommentsLayout.visibility = View.INVISIBLE
                areCommentsOpen = false
            }
        }

        orderButtons.add(feedDateFilterButton)
        orderButtons.add(feedPopularFilterButton)
        orderButtons.add(feedFollowingFilterButton)

        feedDateFilterButton.setOnClickListener {
            setOrderMode(it as MaterialButton)
        }
        feedPopularFilterButton.setOnClickListener {
            setOrderMode(it as MaterialButton)
        }
        feedFollowingFilterButton.setOnClickListener {
            setOrderMode(it as MaterialButton)
        }


        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (feedCommentsLayout.isVisible) {
                    feedCommentsLayout.visibility = View.INVISIBLE
                } else {
                    activity?.finish()
                }
            }

        })
    }

    private fun showDeletePostDialog(postId: String) {
        requireContext().makeInternetSafeRequest {
            AlertDialog.Builder(requireContext())
                .setTitle("Delete post")
                .setMessage("Are you sure you want to delete this post?")
                .setPositiveButton("Delete") { _, _ ->
                    viewModel.deletePost(postId)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun setOrderMode(button: MaterialButton) {
        requireContext().makeInternetSafeRequest {
            button.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.textColorPrimary))
            button.setTextColor(ContextCompat.getColor(requireContext(), R.color.backgroundColorPrimary))
            for (btn in orderButtons) {
                if (btn != button) {
                    btn.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.buttonBackgroundColor))
                    btn.setTextColor(ContextCompat.getColor(requireContext(), R.color.textColorSecondary))
                }
            }
            viewModel.selectedOrder = button.text.toString().toLowerCase()
            viewModel.refreshResults()
        }

    }

    private fun hideComments(itemView: View) {
        itemView.feedListItemScrollView.post {
            itemView.feedListItemScrollView.smoothScrollTo(0, 0)
            areCommentsOpen = false
            viewModel.comments = null
            viewModel.isCommentListEmpty.value =  false
            feedCommentsLayout.visibility = View.INVISIBLE
            itemView.feedCommentsRv.visibility = View.GONE
            itemView.feedBlankSpace.visibility = View.GONE
            feedCommentsEmptyView.visibility = View.GONE
            commentsPb.visibility = View.INVISIBLE
        }

    }

    private fun showComments(
        post: Result,
        itemView: View,
        position: Int
    ) {
        areCommentsOpen = true
        viewModel.comments = null
        viewModel.isCommentListEmpty.value =  false
        // show the recyclerView, editText, and blank space here
        // but hide them in PostAdapter
        feedCommentsLayout.feedCommentSendBtn.setOnClickListener {
            if (feedCommentsLayout.feedCommentsEt.text.isNotEmpty()) {
                postComment(feedCommentsLayout.feedCommentsEt.text.toString().trim(), post.id)
            } else {
                Toast.makeText(requireContext(), "Cannot post an empty comment!", Toast.LENGTH_SHORT).show()
            }
        }
        feedCommentsLayout.visibility = View.VISIBLE
        itemView.feedCommentsRv.visibility = View.VISIBLE
        itemView.feedBlankSpace.visibility = View.VISIBLE
        feedCommentsEmptyView.visibility = View.GONE
        commentsPb.visibility = View.VISIBLE
        val commentAdapter = PrefManager.getMyPrefs(requireContext().applicationContext).getString(Const.PREF_USERNAME, null)
            ?.let {
                CommentAdapter(post.isCurrentUserPost, it, object : OnCommentClickListener {
                override fun onItemClick(comment: CommentResult, tag: String) {
                    when (tag) {
                        Const.TAG_PROFILE -> launchOtherUserProfileActivity(comment.author.username)
                        Const.TAG_COMMENT_DETAILS -> showDeleteCommentDialog(comment)
                    }
                }
            })
            }
        itemView.feedCommentsRv.adapter = commentAdapter
        itemView.feedCommentsRv.layoutManager = LinearLayoutManager(itemView.feedCommentsRv.context)
        viewModel.isCommentListEmpty.observe(viewLifecycleOwner) {
            if (it) {
                feedCommentsEmptyView.visibility = View.VISIBLE
                feedCommentsRv.visibility = View.INVISIBLE
            } else {
                feedCommentsEmptyView.visibility = View.GONE
                feedCommentsRv.visibility = View.VISIBLE
            }
        }
        viewModel.getNewComments(post.id)?.observe(viewLifecycleOwner) {
            commentAdapter?.submitList(it)
            viewModel.commentsAmount.value = it.size
            commentsPb.visibility = View.INVISIBLE
            scrollToComments(itemView)
        }
        viewModel.isDeleteCommentSuccessful.observe(viewLifecycleOwner) {
            it?.let {
                if (it) {
                    viewModel.getNewComments(post.id)
                }
            }
        }
//        if (viewModel.commentsAmount.value == 0) {
//            itemView.feedCommentsEmptyView.visibility = View.VISIBLE
//        }
    }

    private fun showDeleteCommentDialog(comment: CommentResult) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete comment")
            .setMessage("Are you sure you want to delete this comment?")
            .setPositiveButton("Delete") { _, _ ->
                deleteComment(comment)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteComment(comment: CommentResult) {
        requireContext().makeInternetSafeRequest {
            viewModel.deleteComment(comment)
        }
    }


    private fun scrollToComments(itemView: View) {
        itemView.feedListItemScrollView.post {
            itemView.feedListItemScrollView.smoothScrollTo(0, itemView.feedCommentsRv.bottom)
        }
    }

    private fun launchOtherUserProfileActivity(username: String) {
        if (username == viewModel.profile.value!!.data.username) {
            startActivity(Intent(activity, ProfileActivity::class.java).apply { putExtra(Const.EXTRA_PROFILE, viewModel.profile.value) })
        } else {
            startActivity(Intent(activity, OtherUserProfileActivity::class.java).apply {
                putExtra(Const.EXTRA_USERNAME, username)
            })
        }
    }


    private fun postComment(comment: String, postId: String) {
        requireContext().makeInternetSafeRequest {

            viewModel.postComment(comment, postId).observe(viewLifecycleOwner) {
                it?.let {
                    feedCommentsEmptyView.visibility = View.GONE
                    if (it) {
                        feedCommentsLayout.feedCommentsEt.text = null
                        viewModel.getNewComments(postId)
                    } else {
                        Toast.makeText(requireContext(), "Could not comment this post", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun toggleUpvoteButton(button: ImageButton, upvotedByUser: Boolean) {
        if (!upvotedByUser) {
            button.setImageResource(R.drawable.ic_upvote_filled)
        } else {
            button.setImageResource(R.drawable.ic_upvote)
        }
    }

    override fun onStop() {
        super.onStop()
        feedRv.pauseVideo()
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
        feedRv?.releasePlayer()
    }

    @Subscribe
    fun onEditProfile(event: EditProfileEvent) {
        val profile = Profile(ProfileData(event.id, event.email, event.username, event.birthday, event.bio, event.profilePic, event.rating, event.ratingChange, event.numberOfPosts, event.numberOfFollowers,event.followersChange, event.upvotesChange, event.numberOfFollowings))
        viewModel.profile.value = profile
    }

    private fun setupPopupWindow() {
        val inflater = context?.applicationContext?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val layout = inflater.inflate(R.layout.feed_categories, null)
        categoryAdapter = CategoryAdapter()
        viewModel.categories.observe(viewLifecycleOwner) {
//            if (it.isNotEmpty()) {
            categoryAdapter.setValues(it)
            it.forEach {data ->
                if (data.isSelected) {
                    feedCategoryBtn.text = data.name
                    viewModel.selectedCategory.value = data.id
                }
            }
            requireContext().makeInternetSafeRequest {
            }
            setupPosts()
            categoryAdapter.setOnItemClickListener(object : OnItemClickListener {
                    override fun <T> onItemClick(listItem: T) {
                        val category = listItem as CategoryData
                        category.isSelected = true
                        feedCategoryBtn.text = category.name
                        viewModel.selectedCategory.value = category.id
                        for (cat in categoryAdapter.getCategories()) {
                            if (cat.id != category.id) {
                                cat.isSelected = false
                            }
                        }
                        categoryAdapter.notifyDataSetChanged()
                        categoriesPopupWindow.dismiss()
                        this@FeedFragment.adapter?.submitList(null)
                        feedPb.visibility = View.VISIBLE
                        viewModel.refreshResults()
                    }
                })
//            }
        }
//        viewModel.selectedCategory.observe(viewLifecycleOwner) {
//            if (it != 0) {
//                viewModel.getNewResults()
//            }
//        }
        layout.categoriesRv.layoutManager = LinearLayoutManager(requireContext())
        layout.categoriesRv.adapter = categoryAdapter
        // TODO set onClickListeners for popup buttons
        categoriesPopupWindow = PopupWindow(layout, ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT, true)
        categoriesPopupWindow.setOnDismissListener {
            this.layout?.foreground?.alpha = 0
        }

    }

    private fun setupPosts() {
        viewModel.getNewResults()
        viewModel.results?.observe(viewLifecycleOwner) {
            feedPb.visibility = View.GONE
            feedSwipeRefreshLayout.isRefreshing = false
            feedRv.setResultArrayList(it)
            adapter?.submitList(it)
        }
        viewModel.isFollowersListEmpty.observe(viewLifecycleOwner) {empty ->
            if (empty) {
                feedEmptyView.visibility = View.VISIBLE
                feedRv.visibility = View.GONE
            } else {
                feedEmptyView.visibility = View.GONE
                feedRv.visibility = View.VISIBLE
            }
        }
//        viewModel.commentsAmount.observe(viewLifecycleOwner) {
//            adapter.setCommentsAmount()
//        }
        feedRv.setOnPlayerReadyListener(object : OnPlayerReadyListener {
            override fun onPlayerReady() {

            }
        })
        feedRv.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        feedRv.setOnFullscreenButtonClickListener(object : OnFullscreenButtonClickListener {
            override fun onButtonClick(videoUrl: String, currentPosition: Long) {
                launchFullscreenVideoActivity(videoUrl, currentPosition)
            }
        })
        feedRv.adapter = adapter
        feedSwipeRefreshLayout.setOnRefreshListener {
            requireContext().makeInternetSafeRequest {
                viewModel.refreshResults()
                feedPb.visibility = View.VISIBLE
            }
        }
        feedCategoryBtn.setOnClickListener { v ->
            categoriesPopupWindow.showAsDropDown(v)
            layout?.foreground?.alpha = 220
        }
    }
}
package com.york.exordi.feed.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.york.exordi.R
import com.york.exordi.feed.viewmodel.ProfileViewModel
import com.york.exordi.shared.Const
import kotlinx.android.synthetic.main.profile_bottom_sheet.*

class ProfileBottomSheetDialog : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.profile_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            if (it.getString(Const.EXTRA_CALLING_ACTIVITY) == Const.EXTRA_OWN_PROFILE) {
                bottomSheetRatingTv.text = it.getDouble(Const.EXTRA_RATING).toString()
                followersAmountTv.text = it.getInt(Const.EXTRA_NUMBER_OF_FOLLOWERS).toString()
                ratingIncreaseTv.text = it.getInt(Const.EXTRA_RATING_CHANGE).toString()
                followersIncreaseTv.text = it.getInt(Const.EXTRA_FOLLOWERS_CHANGE).toString()
                upvotesIncreaseTv.text = it.getInt(Const.EXTRA_UPVOTES_CHANGE).toString()

                if (it.getInt(Const.EXTRA_RATING_CHANGE) < 0) {
                    makeViewsRed(ratingIncreaseTv, ratingIncreaseIv)
                }
                if (it.getInt(Const.EXTRA_FOLLOWERS_CHANGE) < 0) {
                    makeViewsRed(followersIncreaseTv, followersIncreaseIv)
                }
                if (it.getInt(Const.EXTRA_UPVOTES_CHANGE) < 0) {
                    makeViewsRed(upvotesIncreaseTv, upvotesIncreaseIv)
                }
            } else if (it.getString(Const.EXTRA_CALLING_ACTIVITY) == Const.EXTRA_OTHER_PROFILE) {
                divider.visibility = View.GONE
                constraintLayout.visibility = View.GONE
                constraintLayout2.visibility = View.GONE
                ratingIncreaseIv.visibility = View.GONE
                ratingIncreaseTv.visibility = View.GONE

                bottomSheetRatingTv.text = it.getDouble(Const.EXTRA_RATING, 0.0).toString()
            }

        }
    }

    private fun makeViewsRed(textView: TextView, imageView: ImageView) {
        textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.textColorDecrease))
        imageView.setImageResource(R.drawable.ic_decrease)
    }

}
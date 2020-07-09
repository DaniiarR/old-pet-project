package com.york.exordi.adapters

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.york.exordi.R

class ProfilePhotosAdapter() : BaseQuickAdapter<String, BaseViewHolder>(R.layout.profile_post_list_item, arrayListOf("","","","","","","","","","")) {

    override fun convert(holder: BaseViewHolder, item: String) {
        Glide.with(context).load(R.drawable.jisoo).into(holder.getView<ImageView>(R.id.profilePostIv))
    }

}
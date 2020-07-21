package com.york.exordi.adapters

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.york.exordi.R
import java.util.ArrayList

class SettingsAdapter(val settings: ArrayList<String>) : BaseQuickAdapter<String, BaseViewHolder>(R.layout.settings_list_item, settings) {
    override fun convert(holder: BaseViewHolder, item: String) {
        holder.setText(R.id.settingNameTv, item)
    }
}

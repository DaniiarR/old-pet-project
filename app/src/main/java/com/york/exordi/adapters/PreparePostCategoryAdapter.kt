package com.york.exordi.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.york.exordi.R
import com.york.exordi.models.AddPostCategory
import com.york.exordi.models.CategoryData
import com.york.exordi.shared.OnItemClickListener
import kotlinx.android.synthetic.main.prepare_post_category_list_item.view.*

class PreparePostCategoryAdapter :
    RecyclerView.Adapter<PreparePostCategoryAdapter.PreparePostCategoryViewHolder>() {

    private var categories = arrayListOf<CategoryData>()
    var clickListener: OnItemClickListener? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PreparePostCategoryViewHolder {
        return PreparePostCategoryViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.prepare_post_category_list_item, parent, false))
    }

    override fun getItemCount(): Int {
        return categories.size
    }

    override fun onBindViewHolder(holder: PreparePostCategoryViewHolder, position: Int) {
        holder.bind(categories[position], clickListener)
    }

    fun setValues(cats: ArrayList<CategoryData>?) {
        if (!cats.isNullOrEmpty()) {
            categories.clear()
            categories.addAll(cats)
            notifyDataSetChanged()
        }
    }

    class PreparePostCategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {


        fun bind(category: CategoryData, clickListener: OnItemClickListener?) {
            itemView.categoryNameTv.text = category.name
            if (category.isSelected) {
                itemView.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.selectedTextViewColor))
                itemView.categoryTickIv.visibility = View.VISIBLE
            } else {
                itemView.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.backgroundColorPrimary))
                itemView.categoryTickIv.visibility = View.INVISIBLE
            }
            itemView.categoryNameTv.setOnClickListener {
                clickListener?.onItemClick(category)
            }
        }
    }
}
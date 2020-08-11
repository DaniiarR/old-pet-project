package com.york.exordi.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.york.exordi.R
import com.york.exordi.models.CategoryData
import com.york.exordi.shared.OnItemClickListener
import kotlinx.android.synthetic.main.category_list_item.view.*

class CategoryAdapter : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    val categoryList = arrayListOf<CategoryData>()
    lateinit var clickListener: OnItemClickListener

    fun setValues(categories: List<CategoryData>?) {
        categories?.let {
            if (it.isNotEmpty()) {
                categoryList.clear()
                categoryList.addAll(it)
                notifyDataSetChanged()
            }
        }
    }

    fun setOnItemClickListener(clickListener: OnItemClickListener) {
        this.clickListener = clickListener
    }

    fun getCategories(): List<CategoryData> {
        return categoryList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        return CategoryViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.category_list_item, parent, false))
    }

    override fun getItemCount(): Int {
        return categoryList.size
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categoryList[position], clickListener)
    }

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(categoryData: CategoryData, listener: OnItemClickListener) {
            itemView.categoryBtn.setOnClickListener { listener.onItemClick(categoryData) }

            if (categoryData.isSelected) {
                itemView.categoryBtn.setTextColor(ContextCompat.getColor(itemView.context, R.color.colorPrimary))
            } else {
                itemView.categoryBtn.setTextColor(ContextCompat.getColor(itemView.context, R.color.textColorPrimary))
            }
            itemView.categoryBtn.text = categoryData.name
        }
    }
}
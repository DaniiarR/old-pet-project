package com.york.exordi.addpost

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.york.exordi.R
import com.york.exordi.shared.Const
import kotlinx.android.synthetic.main.activity_select_category.*

class SelectCategoryActivity : AppCompatActivity() {

    private val categoryList: ArrayList<TextView> = arrayListOf()
    private val ticksHashMap = HashMap<TextView, ImageView>()

    private var selectedCategory: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_category)

        categoryList.apply {
            add(artBtn)
            add(fashionBtn)
            add(musicBtn)
            add(sportBtn)
        }
        ticksHashMap[artBtn] = artTick
        ticksHashMap[fashionBtn] = fashionTick
        ticksHashMap[musicBtn] = musicTick
        ticksHashMap[sportBtn] = sportTick

        val category = intent.getIntExtra(Const.EXTRA_CATEGORY, 0)
        if (category != 0) {
            selectedCategory = category
            selectCategoryUsingName(getCategoryNameById(category))
            doneBtn.visibility = View.VISIBLE
        }

        setSupportActionBar(selectCategoryBar)
        selectCategoryBar.setNavigationOnClickListener { finish() }

        artBtn.setOnClickListener { selectCategory(it as TextView) }
        fashionBtn.setOnClickListener { selectCategory(it as TextView) }
        musicBtn.setOnClickListener { selectCategory(it as TextView) }
        sportBtn.setOnClickListener { selectCategory(it as TextView) }
        doneBtn.setOnClickListener {
            val intent = Intent()
            intent.putExtra(Const.EXTRA_CATEGORY, selectedCategory)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

    private fun selectCategoryUsingName(category: String) {
        for (cat in categoryList) {
            if (cat.text.toString() == category) {
                cat.setBackgroundColor(ContextCompat.getColor(this, R.color.selectedTextViewColor))
                ticksHashMap[cat]?.visibility = View.VISIBLE
            } else {
                cat.setBackgroundColor(ContextCompat.getColor(this, R.color.backgroundColorPrimary))
                ticksHashMap[cat]?.visibility = View.INVISIBLE
            }
        }
    }

    private fun selectCategory(view: TextView) {
        for (cat in categoryList) {
            if (cat == view) {
                cat.setBackgroundColor(ContextCompat.getColor(this, R.color.selectedTextViewColor))
                ticksHashMap[cat]?.visibility = View.VISIBLE
            } else {
                cat.setBackgroundColor(ContextCompat.getColor(this, R.color.backgroundColorPrimary))
                ticksHashMap[cat]?.visibility = View.INVISIBLE
            }
        }
        selectedCategory = getCategoryIdByName(view.text.toString())
        doneBtn.visibility = View.VISIBLE

    }

    fun getCategoryIdByName(name: String): Int {
        when (name) {
            "Art" -> return 1
            "Fashion" -> return 2
            "Music" -> return 3
            "Sport" -> return 4
        }
        return 0
    }

    fun getCategoryNameById(id: Int): String {
        when (id) {
            1 -> return "Art"
            2 -> return "Fashion"
            3 -> return "Music"
            4 -> return "Sport"
        }
        return ""
    }
}
package com.york.exordi.addpost

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.york.exordi.R
import com.york.exordi.adapters.PreparePostCategoryAdapter
import com.york.exordi.models.AddPostCategory
import com.york.exordi.models.Category
import com.york.exordi.models.CategoryData
import com.york.exordi.network.AuthWebWebService
import com.york.exordi.network.RetrofitInstance
import com.york.exordi.network.WebService
import com.york.exordi.shared.Const
import com.york.exordi.shared.OnItemClickListener
import com.york.exordi.shared.PrefManager
import kotlinx.android.synthetic.main.activity_select_category.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SelectCategoryActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "SelectCategoryActivity"
    }

    private var categoryList: List<CategoryData>? = null

    private var selectedCategory: CategoryData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_category)
        setSupportActionBar(selectCategoryBar)
        selectCategoryBar.setNavigationOnClickListener { finish() }

        val adapter = PreparePostCategoryAdapter()
        adapter.clickListener = object : OnItemClickListener {
            override fun <T> onItemClick(listItem: T) {
                selectedCategory = listItem as CategoryData
                adapter.setValues(selectCategory(categoryList!!) as ArrayList<CategoryData>)
                doneBtn.visibility = View.VISIBLE
            }
        }

        val category = intent.getSerializableExtra(Const.EXTRA_CATEGORY) as CategoryData?
        category?.let {
            selectedCategory = it
            doneBtn.visibility = View.VISIBLE
        }
        doneBtn.setOnClickListener {
            selectedCategory?.let { cat ->
                val intent = Intent()
                intent.putExtra(Const.EXTRA_CATEGORY, cat)
                setResult(Activity.RESULT_OK, intent)
                finish()

            }
        }

        val api = RetrofitInstance.SimpleInstance.get().create(WebService::class.java)
        api.getAllCategories(PrefManager.getMyPrefs(applicationContext).getString(Const.PREF_AUTH_TOKEN, null)!!).enqueue(
            object : Callback<Category> {
                override fun onFailure(call: Call<Category>, t: Throwable) {
                    Log.e(TAG, "onFailure: " + t.message!! )
                }

                override fun onResponse(call: Call<Category>, response: Response<Category>) {
                    if (response.isSuccessful) {
                        response.body()?.let {
                            categoryList = selectCategory(it.data)
                            adapter.setValues(categoryList as ArrayList<CategoryData>)
                            preparePostCategoryRv.layoutManager = LinearLayoutManager(this@SelectCategoryActivity)
                            preparePostCategoryRv.adapter = adapter
                        }
                    }
                }

            })
    }

    private fun selectCategory(cats: List<CategoryData>): List<CategoryData> {
        selectedCategory?.let {
            cats.forEach {cat ->
                cat.isSelected = cat.id == it.id
            }
        }
        return cats
    }

}
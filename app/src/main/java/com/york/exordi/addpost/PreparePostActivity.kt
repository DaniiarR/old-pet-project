package com.york.exordi.addpost

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.devbrackets.android.exomedia.listener.OnPreparedListener
import com.york.exordi.R
import com.york.exordi.events.CreatePostEvent
import com.york.exordi.repository.AppRepository
import com.york.exordi.shared.Const
import kotlinx.android.synthetic.main.activity_prepare_post.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.greenrobot.eventbus.EventBus
import java.io.File

class PreparePostActivity : AppCompatActivity() {

    companion object {
        const val RC = 100
    }
    private var imagePath: String? = null
    private var selectedCategory: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prepare_post)

        imagePath = intent.getStringExtra(Const.EXTRA_IMAGE_PATH)
        Glide.with(this).load(imagePath).into(imagePreview)

        selectCategoryBtn.setOnClickListener {
            val intent = Intent(this, SelectCategoryActivity::class.java)
            selectedCategory?.let {
                intent.putExtra(Const.EXTRA_CATEGORY, it)
            }
            startActivityForResult(intent, RC)
        }

        publishBtn.setOnClickListener {
            publishPhoto()
        }
//        videoPreview.setVideoURI(Uri.parse())
        videoPreview.setOnPreparedListener(object : OnPreparedListener {
            override fun onPrepared() {

            }
        })
    }

    private fun publishPhoto() {
        createFileMultipartBody()?.let {
            var description: String? = null
            if (descriptionEt.text.toString().isNotEmpty()) {
                description = descriptionEt.text.toString()
            }
            val api = AppRepository.getInstance(application)
            api.createPost(selectedCategory!!, description, it) { response ->
                if (response.code == 200) {
                    Toast.makeText(this, "Successfully created new post", Toast.LENGTH_SHORT).show()
                    EventBus.getDefault().post(CreatePostEvent())
                    finish()
                }
            }
        }
    }

    private fun createFileMultipartBody(): MultipartBody.Part? {
        imagePath?.let {
            val file = File(it)
            val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
            return MultipartBody.Part.createFormData("post_file", file.name, requestBody)
        }
        return null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            RC -> {
                if (resultCode == Activity.RESULT_OK) {
                    selectedCategory = data?.getIntExtra(Const.EXTRA_CATEGORY, 0)
                    publishBtn.visibility = View.VISIBLE
                }
            }
        }
    }
}
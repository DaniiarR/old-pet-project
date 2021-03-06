package com.york.exordi.addpost

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.isseiaoki.simplecropview.callback.CropCallback
import com.isseiaoki.simplecropview.callback.LoadCallback
import com.isseiaoki.simplecropview.callback.SaveCallback
import com.york.exordi.R
import com.york.exordi.base.BaseActivity
import com.york.exordi.events.CreatePostEvent
import com.york.exordi.feed.ui.EditProfileActivity
import com.york.exordi.shared.Const
import com.york.exordi.shared.registerActivityForEvents
import com.york.exordi.shared.unregisterActivityFromEvents
import kotlinx.android.synthetic.main.activity_crop_image.*
import org.greenrobot.eventbus.Subscribe
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class CropImageActivity : BaseActivity() {

    private var imagePath: String? = null
    private var imageUri: Uri? = null
    private var isInCropMode = false
    private var croppedImagePath: String? = null

    private val compressFormat = Bitmap.CompressFormat.JPEG

    private val loadCallback: LoadCallback = object : LoadCallback {
        override fun onSuccess() {
            cropView.setCropEnabled(false)
            cropPb.visibility = View.INVISIBLE
            continueBtn.isEnabled = true
            cropSwitchBtn.background = null
            isInCropMode = false
        }

        override fun onError(e: Throwable?) {
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crop_image)

        backBtn.setOnClickListener { finish() }
        imagePath = intent.getStringExtra(Const.EXTRA_FILE_PATH)
        // this logic is for checking whether the image came from camera or gallery
//         if images is selected from gallery, we only have its URI that is passes as setData()
        if (imagePath == null) {
//            imageUri = intent.data
            imageUri = Uri.parse(intent.getStringExtra(Const.EXTRA_FILE_URI))
        } else {
            imageUri = Uri.fromFile(File(imagePath))
        }
        cropView.load(imageUri).execute(loadCallback)
//        Glide.with(this).load(imagePath).into(cropView)
        cropSwitchBtn.setOnClickListener {
            cropView.setCropEnabled(!isInCropMode)
            if (isInCropMode) {
                cropSwitchBtn.background = null
                cropBtn.visibility = View.INVISIBLE
            } else {
                cropSwitchBtn.background = ContextCompat.getDrawable(this, R.drawable.circle)
                cropBtn.visibility = View.VISIBLE
            }
            isInCropMode = !isInCropMode
        }
        cropView.setCustomRatio(4, 5)

        continueBtn.setOnClickListener {

            val requestCode = intent.getIntExtra(Const.EXTRA_REQUEST_CODE, 0)
            if (requestCode == EditProfileActivity.INTENT_CROP) {
                val intent = Intent()
                intent.putExtra(Const.EXTRA_FILE_URI, imageUri.toString())
                setResult(Activity.RESULT_OK, intent)
                finish()
            } else {
                registerActivityForEvents()

                startActivity(Intent(this, PreparePostActivity::class.java).apply {
                    putExtra(Const.EXTRA_FILE_PATH, imagePath)
                    putExtra(Const.EXTRA_FILE_TYPE, Const.EXTRA_FILE_TYPE_PHOTO)
//                setData(imageUri)
//                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//                    .addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                    putExtra(Const.EXTRA_FILE_URI, imageUri.toString())
                })
            }
        }
        cropBtn.setOnClickListener {
            continueBtn.isEnabled = false
            cropPb.visibility = View.VISIBLE
            cropBtn.visibility = View.INVISIBLE
            cropView.crop(imageUri).execute(cropCallback)
        }
    }

    @Subscribe
    fun onCreatePost(event: CreatePostEvent) {
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterActivityFromEvents()
    }

    private val cropCallback = object : CropCallback {
        override fun onSuccess(cropped: Bitmap?) {
            cropView.save(cropped).compressFormat(compressFormat).execute(createSaveUri(), saveCallback)
        }

        override fun onError(e: Throwable?) {
            TODO("Not yet implemented")
        }
    }

    // write ext storage
    fun createSaveUri(): Uri {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val currentTimeMillis = System.currentTimeMillis()
        val fileName = "EXORDI_" + timeStamp + "_" + "cropped" + "." + getMimeType()
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
//        val file = File.createTempFile(fileName, ".jpg", storageDir)
        val dirPath = getDirPath()
        val path = dirPath + "/" + fileName
        val file = File(path)
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, timeStamp)
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/" + getMimeType())
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
//            values.put(MediaStore.Images.Media.DATA, path)
//        }
        val time = currentTimeMillis / 1000
        values.put(MediaStore.MediaColumns.DATE_ADDED, time)
        values.put(MediaStore.MediaColumns.DATE_MODIFIED, time)
        if (file.exists()) {
            values.put(MediaStore.Images.Media.SIZE, file.length())
        }
        val resolver = contentResolver
        val uri: Uri? = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        return uri!!
    }
    
    private fun getMimeType(): String {
        when (compressFormat) {
            Bitmap.CompressFormat.JPEG -> return "jpeg"
            Bitmap.CompressFormat.PNG -> return "png"
        }
        return "png"
    }

    private fun getDirPath(): String {
        var dirPath = ""
        var imageDir: File? = null
        val extStorageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        if (extStorageDir!!.canWrite()) {
            imageDir = File(extStorageDir.path + "/cropped")
        }
        imageDir?.let {
            if (!imageDir.exists()) {
                imageDir.mkdirs()
            }
            if (imageDir.canWrite()) {
                dirPath = imageDir.path
            }
        }
        return dirPath
    }

    private val saveCallback = object : SaveCallback {
        override fun onSuccess(uri: Uri?) {
            imageUri = uri
//            val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)

//            val cursor = contentResolver.query(uri!!, null, null, null, null)
//            cursor?.moveToFirst()
//            imagePath = cursor?.getString(cursor.getColumnIndex(filePathColumn[0]))
//            cursor?.close()
            cropView.load(imageUri).execute(loadCallback)

        }


        override fun onError(e: Throwable?) {
            TODO("Not yet implemented")
        }
    }
}
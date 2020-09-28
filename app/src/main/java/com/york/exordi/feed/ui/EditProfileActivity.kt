package com.york.exordi.feed.ui

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.york.exordi.BuildConfig
import com.york.exordi.R
import com.york.exordi.addpost.CropImageActivity
import com.york.exordi.base.BaseActivity
import com.york.exordi.events.EditProfileEvent
import com.york.exordi.models.Profile
import com.york.exordi.models.UsernameCheck
import com.york.exordi.repository.AppRepository
import com.york.exordi.shared.Const
import com.york.exordi.shared.getFileName
import com.york.exordi.shared.makeInternetSafeRequest
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import id.zelory.compressor.constraint.resolution
import kotlinx.android.synthetic.main.activity_edit_profile.*
import kotlinx.android.synthetic.main.activity_edit_profile.usernameErrorTv
import kotlinx.android.synthetic.main.activity_edit_profile.usernameEt
import kotlinx.android.synthetic.main.activity_edit_profile.usernamePb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.apache.commons.io.IOUtils
import org.greenrobot.eventbus.EventBus
import pub.devrel.easypermissions.EasyPermissions
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class EditProfileActivity : BaseActivity(), EasyPermissions.PermissionCallbacks {

    companion object {
        const val RC_CAMERA = 100
        const val RC_GALLERY = 101

        const val CAMERA = 10
        const val GALLERY = 11

        const val INTENT_CROP = 1000
        private const val TAG = "EditProfileActivity"
    }

    private var repository: AppRepository? = null

    private var imageAbsolutePath: String? = null
    private var imageUri: Uri? = null

    private var isUsernameValid = true

    private var usernameDrawable: GradientDrawable? = null

    private var initialUsername: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        repository = AppRepository.getInstance(application)
        hideUsernameError()
        usernameDrawable = usernameEt.background as GradientDrawable
        setupInitialProfile()
        editProfileToolbar.setNavigationOnClickListener { finish() }
        editProfileSaveButton.setOnClickListener {
            makeInternetSafeRequest { compressImage() }
        }
        profileIv.setOnClickListener { showSelectActionDialog() }
        uploadPhotoTv.setOnClickListener { showSelectActionDialog() }
        usernameEt.doOnTextChanged { text, _, _, _ ->
            if (!TextUtils.isEmpty(text)) {
                when {
                    text!!.length < 4 -> {
                        isUsernameValid = false
                        showUsernameError("Username is too short")
                    }
                    text.length > 15 -> {
                        isUsernameValid = false
                        showUsernameError("Username is too long")
                    }
                    text == initialUsername -> {
                        isUsernameValid = true
                        hideUsernameError()
                    }
                    else -> {
                        if (text.toString().matches("^[a-zA-Z0-9_-]{4,15}$".toRegex())) {
                            makeInternetSafeRequest { checkUsername(text.toString()) }
                        } else {
                            isUsernameValid = false
                            showUsernameError("Username must only contain letters a-z, numbers 0-9, and underscores")
                        }
                    }
                }
            } else {
                isUsernameValid = false
            }
        }
    }

    private fun setupInitialProfile() {
        val profile = intent.getSerializableExtra(Const.EXTRA_PROFILE) as? Profile
        profile?.let {
            it.data.profilePic?.let { Glide.with(this).load(it).into(profileIv) }
            usernameEt.setText(it.data.username)
            descriptionEt.setText(if (!TextUtils.isEmpty(it.data.bio)) it.data.bio else "")
            initialUsername = it.data.username
        }
    }

    private fun checkUsername(username: String) {
        usernamePb.visibility = View.VISIBLE
        repository?.checkUsername(UsernameCheck(username)) {
            if (it == "OK") {
                isUsernameValid = true
                hideUsernameError()
            } else {
                isUsernameValid = false
                showUsernameError(it ?: "Error occurred")
            }
        }
    }

    private fun showUsernameError(error: String) {
        usernamePb.visibility = View.INVISIBLE
        usernameErrorTv.text = error
        usernameErrorTv.visibility = View.VISIBLE
        usernameDrawable?.setStroke(5, Color.RED)
    }

    private fun hideUsernameError() {
        usernamePb.visibility = View.INVISIBLE
        usernameErrorTv.visibility = View.INVISIBLE
        usernameDrawable?.setStroke(2, ContextCompat.getColor(this, R.color.buttonBackgroundColor))
    }

    private fun showSelectActionDialog() {
        AlertDialog.Builder(this).apply {
            setTitle("Select an action")
            setItems(arrayOf("Choose photo from gallery", "Take a new picture"), object: DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    when (which) {
                        0 -> selectImageFromGallery()
                        1 -> takePictureWithCamera()
                    }
                }

            })
        }.create().show()
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        onPermissionDenied()
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        when (requestCode) {
            RC_GALLERY -> selectImageFromGallery()
            RC_CAMERA -> takePictureWithCamera()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    private fun onPermissionDenied() {
        Toast.makeText(this, "App will no function properly without this permission", Toast.LENGTH_SHORT).show()
    }

    // read external storage
    fun selectImageFromGallery() {
        if (EasyPermissions.hasPermissions(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            val intent = Intent(Intent.ACTION_PICK).apply {
                setType("image/*")
                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png"))
            }
            startActivityForResult(intent,
                GALLERY
            )
        } else {
            EasyPermissions.requestPermissions(this, "The App needs this permission to select images from gallery",
                RC_GALLERY, Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    // write external storage
    fun takePictureWithCamera() {
        if (EasyPermissions.hasPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)) {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            takePictureIntent.resolveActivity(packageManager)?.let {
                var photoFile: File? = null
                try {
                    photoFile = createImageFile()
                } catch (e: IOException) {

                }
                photoFile?.let {
                    val photoUri: Uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", photoFile)
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                    startActivityForResult(takePictureIntent,
                        CAMERA
                    )
                }
            }
        } else {
            EasyPermissions.requestPermissions(this, "The App needs this permission to select take photos from camera",
                RC_CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
        }

    }

    fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        val image: File = File.createTempFile(imageFileName, ".jpg", storageDir)
//        cameraImageAbsolutePath = image.absolutePath
        imageAbsolutePath = image.absolutePath
        return image
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CAMERA -> startCropActivityFromCamera()
                GALLERY -> {
                    imageUri = data?.data
                    startCropActivityFromGallery(imageUri!!)
                }
                INTENT_CROP -> {
                    setImage(data?.getStringExtra(Const.EXTRA_FILE_URI))
                }
            }
        }
    }

    private fun setImage(fileUri: String?) {
        fileUri?.let {
            imageUri = Uri.parse(fileUri)
            Glide.with(this).load(it).into(profileIv)

        }
    }

    private fun startCropActivityFromGallery(uri: Uri) {
        imageAbsolutePath = null
        startActivityForResult(Intent(this, CropImageActivity::class.java).apply {
            putExtra(Const.EXTRA_FILE_URI, uri.toString())
            putExtra(Const.EXTRA_REQUEST_CODE, INTENT_CROP)
        }, INTENT_CROP)
    }

    private fun startCropActivityFromCamera() {
        imageUri = null
        startActivityForResult(Intent(this, CropImageActivity::class.java).apply {
            putExtra(Const.EXTRA_FILE_PATH, imageAbsolutePath)
            putExtra(Const.EXTRA_REQUEST_CODE, INTENT_CROP)
        }, INTENT_CROP)
    }

    private fun setImageFromGallery(uri: Uri?) {
        Glide.with(this).load(uri).into(profileIv)
        imageAbsolutePath = null
    }

    private fun checkUsernameValidity(photo: File?) {
        if (isUsernameValid) {
            editProfilePb.visibility = View.VISIBLE
            editProfileSaveButton.visibility = View.GONE

            val username = usernameEt.text.toString()
            val description = descriptionEt.text.toString()

            if (username != initialUsername) {
                makeInternetSafeRequest { editProfile(username, description, photo) }
            } else {
                makeInternetSafeRequest { editDescription(description, photo) }
            }
        } else {
            Toast.makeText(this, "Username is invalid", Toast.LENGTH_SHORT).show()
        }
    }

    private fun compressImage() {
        var file: File? = null
        if (imageUri != null) {
            file = copyFileToCache(imageUri!!)
        } else if (imageAbsolutePath != null) {
            file = File(imageAbsolutePath!!)
        }
        if (file != null) {
            lifecycleScope.launch {
                val compressedFile = Compressor.compress(this@EditProfileActivity, file, Dispatchers.Main) {
                    resolution(1080, 1350)
                    quality(80)
                    format(Bitmap.CompressFormat.JPEG)
                }
                checkUsernameValidity(compressedFile)
            }
        } else {
            checkUsernameValidity(file)
        }

    }

    private fun editProfile(username: String, description: String, photo: File?) {
        var multipartBody: MultipartBody.Part? = null
        if (photo != null) {
            multipartBody = MultipartBody.Part.createFormData("photo", photo.name, photo.asRequestBody("image/*".toMediaTypeOrNull()))
        }
        repository?.editProfile(username, description, multipartBody) {
            if (it != null) {    // if profile is null, the profile could not be updated
                handleCallback(it)
            } else {
                Toast.makeText(this, "Could not update profile", Toast.LENGTH_SHORT).show()
                editProfilePb.visibility = View.GONE
                editProfileSaveButton.visibility = View.VISIBLE
            }
        }
    }

    private fun editDescription(description: String, photo: File?) {
        var multipartBody: MultipartBody.Part? = null
        if (photo != null) {
            multipartBody = MultipartBody.Part.createFormData("photo", photo.name, photo.asRequestBody("image/*".toMediaTypeOrNull()))
        }
        repository?.editDescription(description, multipartBody) {
            if (it != null) {    // if profile is null, the profile could not be updated
                handleCallback(it)
            } else {
                Toast.makeText(this, "Could not update profile", Toast.LENGTH_SHORT).show()
                editProfilePb.visibility = View.GONE
                editProfileSaveButton.visibility = View.VISIBLE
            }
        }
    }

//    private fun createFileMultipartBody(): MultipartBody.Part? {
//        if (imageUri == null && imageAbsolutePath == null) {
//            return null
//        }
////        fileUri?.let {
//        var file: File? = null
//        if (imageUri != null) {
////            val utils: com.york.exordi.shared.FileUtils = com.york.exordi.shared.FileUtils(this)
////            file = File(utils.getRealPathFromUri(fileUri))
//            file = copyFileToCache(imageUri!!)
//        } else if (imageAbsolutePath != null) {
//            file = File(imageAbsolutePath!!)
//        }
//        var requestBody: RequestBody? = null
//        if (file!!.length() / 1024 > 1000) {
//            launch {
//                file = Compressor.compress(this@EditProfileActivity, file!!, Dispatchers.Main) {
//                    size(999999)
//                }
//
//            }
//            requestBody = file!!.asRequestBody("image/*".toMediaTypeOrNull())
//        } else {
//            requestBody = file!!.asRequestBody("image/*".toMediaTypeOrNull())
//        }
//            return MultipartBody.Part.createFormData("photo", file!!.name, requestBody)
////        }
//    }

    private fun copyFileToCache(fileUri: Uri): File {
        val parcelFileDescriptor = contentResolver.openFileDescriptor(fileUri, "r", null)
        val inputStream = FileInputStream(parcelFileDescriptor!!.fileDescriptor)
        val file = File(cacheDir, contentResolver.getFileName(fileUri))
        val outputStream = FileOutputStream(file)
        IOUtils.copy(inputStream, outputStream)
        return file
    }

    private fun handleCallback(profile: Profile) {
        val event = EditProfileEvent(profile.data.id, profile.data.email, profile.data.username, profile.data.birthday, profile.data.bio, profile.data.profilePic, profile.data.rating, profile.data.ratingChange, profile.data.numberOfPosts, profile.data.numberOfFollowers, profile.data.followersChange, profile.data.upvotesChange, profile.data.numberOfFollowings)
        EventBus.getDefault().post(event)
        finish()
    }
}
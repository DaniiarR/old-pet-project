package com.york.exordi.feed

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.observe
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.widget.doOnTextChanged
import com.bumptech.glide.Glide
import com.york.exordi.BuildConfig
import com.york.exordi.R
import com.york.exordi.events.EditProfileEvent
import com.york.exordi.models.EditProfile
import com.york.exordi.models.Profile
import com.york.exordi.models.ResponseMessage
import com.york.exordi.models.UsernameCheck
import com.york.exordi.network.RetrofitInstance
import com.york.exordi.network.WebService
import com.york.exordi.repository.AppRepository
import com.york.exordi.shared.Const
import com.york.exordi.shared.makeInternetSafeRequest
import kotlinx.android.synthetic.main.activity_edit_profile.*
import kotlinx.android.synthetic.main.activity_edit_profile.usernameErrorTv
import kotlinx.android.synthetic.main.activity_edit_profile.usernameEt
import kotlinx.android.synthetic.main.activity_edit_profile.usernamePb
import kotlinx.android.synthetic.main.activity_email_step_three.*
import org.greenrobot.eventbus.EventBus
import permissions.dispatcher.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

@RuntimePermissions
class EditProfileActivity : AppCompatActivity() {

    companion object {
        const val CAMERA_REQUEST_CODE = 100
        const val GALLERY_REQUEST_CODE = 101
        private const val TAG = "EditProfileActivity"
    }

    private var repository: AppRepository? = null

    private var cameraImageAbsolutePath: String? = null

    private var isUsernameValid = false

    private var usernameDrawable: GradientDrawable? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        repository = AppRepository.getInstance(application)
        usernameDrawable = usernameEt.background as GradientDrawable
        setupInitialProfile()
        editProfileToolbar.setNavigationOnClickListener { finish() }
        editProfileSaveButton.setOnClickListener { makeInternetSafeRequest { editProfile() } }
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
                    else -> {
                        if (text.toString().matches("^[a-z0-9_-]{4,15}$".toRegex())) {
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
            Glide.with(this).load(it.profilePic).into(profileIv)
            usernameEt.setText(it.username)
            descriptionEt.setText(if (!TextUtils.isEmpty(it.bio)) it.bio else "")
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
                        0 -> selectImageFromGalleryWithPermissionCheck()
                        1 -> takePictureWithCameraWithPermissionCheck()
                    }
                }

            })
        }.create().show()
    }

    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    fun selectImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            setType("image/*")
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png"))
        }
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun takePictureWithCamera() {
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
                startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE)
            }
        }
    }

    @OnPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun onCameraDenied() {
        Toast.makeText(this, "You must allow the app to use the camera to use this feature", Toast.LENGTH_SHORT).show()
    }

    @OnShowRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun onShowCameraRationale() {
        onCameraDenied()
    }

    @OnNeverAskAgain(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun onNeverAskCameraAgain() {
        Toast.makeText(this, "You cannot use camera unless the appropriate permission is given in settings", Toast.LENGTH_SHORT).show()
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        val image: File = File.createTempFile(imageFileName, ".jpg", storageDir)
//        cameraImageAbsolutePath = image.absolutePath
        cameraImageAbsolutePath = image.absolutePath
        return image
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CAMERA_REQUEST_CODE -> setImage()
                GALLERY_REQUEST_CODE -> saveGalleryImagePath(data)
            }
        }
    }

    private fun saveGalleryImagePath(data: Intent?) {
        val selectedImage: Uri = data!!.data!!
        val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver?.query(selectedImage, filePathColumn, null, null, null)
        cursor?.moveToFirst()
        cameraImageAbsolutePath = cursor?.getString(cursor.getColumnIndex(filePathColumn[0]))
        setImage()
    }

    private fun setImage() {
        Glide.with(this).load(cameraImageAbsolutePath).into(profileIv)
    }

    private fun editProfile() {
        if (isUsernameValid) {
            editProfilePb.visibility = View.VISIBLE
            editProfileSaveButton.visibility = View.GONE
//            viewModel.isProfileEditedSuccessfully.observe(this) {
//                if (it) {
//                    EventBus.getDefault().post(EditProfileEvent())
//                    finish()
//                }
//            }
            repository?.editProfile(EditProfile(usernameEt.text.toString(), descriptionEt.text.toString())) {
                if (it) {
                    EventBus.getDefault().post(EditProfileEvent())
                } else {
                    Toast.makeText(this, "Could not update profile", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "Username is invalid", Toast.LENGTH_SHORT).show()
        }
    }
}
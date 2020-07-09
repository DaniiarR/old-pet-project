package com.york.exordi.feed

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.york.exordi.BuildConfig
import com.york.exordi.R
import com.york.exordi.shared.makeInternetSafeRequest
import kotlinx.android.synthetic.main.activity_edit_profile.*
import permissions.dispatcher.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

@RuntimePermissions
class EditProfileActivity : AppCompatActivity() {

    companion object {
        const val CAMERA_REQUEST_CODE = 100
        const val GALLERY_REQUEST_CODE = 101
    }

    private var cameraImageAbsolutePath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        editProfileToolbar.setNavigationOnClickListener { finish() }
        editProfileSaveButton.setOnClickListener { makeInternetSafeRequest { editProfile() } }
        profileIv.setOnClickListener { showSelectActionDialog() }
        uploadPhotoTv.setOnClickListener { showSelectActionDialog() }
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

    }


}
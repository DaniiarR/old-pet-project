package com.york.exordi.addpost

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.ImageReader
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.Surface
import android.view.TextureView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.FileCallback
import com.otaliastudios.cameraview.PictureResult
import com.york.exordi.R
import com.york.exordi.events.CreatePostEvent
import com.york.exordi.shared.Const
import com.york.exordi.shared.registerActivityForEvents
import kotlinx.android.synthetic.main.activity_camera.*
import kotlinx.coroutines.selects.select
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class CameraActivity : AppCompatActivity() {

    companion object {
        const val RC_ALL = 100

        const val GALLERY_CODE = 1000
    }

    private val permissions = arrayOf<String>(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE)

    private var imageAbsolutePath: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        setupCamera()
        rotateCameraBtn.setOnClickListener {
            if (cameraView.isOpened) {
                cameraView.toggleFacing()
            }
        }

        galleryBtn.setOnClickListener {
            selectImageFromGallery()
        }
        captureImageBtn.setOnClickListener {
            Toast.makeText(this, "Capture button clicked", Toast.LENGTH_SHORT).show()
            captureImageBtn.setColorFilter(ContextCompat.getColor(this, R.color.textColorPrimary), PorterDuff.Mode.SRC_IN)
            if (cameraView.isOpened) {
                cameraView.takePicture()
            }
        }
    }

    @Subscribe
    fun onPostCreated(event: CreatePostEvent) {
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    @AfterPermissionGranted(RC_ALL)
    fun setupCamera() {
        if (EasyPermissions.hasPermissions(this, *permissions)) {
            cameraView.setLifecycleOwner(this)
            cameraView.addCameraListener(object : CameraListener() {
                override fun onPictureTaken(result: PictureResult) {
                    result.toFile(createImageFile()) {
                        registerActivityForEvents()
                        startActivity(
                            Intent(this@CameraActivity, CropImageActivity::class.java).apply {
                                putExtra(Const.EXTRA_IMAGE_PATH, imageAbsolutePath)
                            })
                    }
                }
            })
        } else {
            EasyPermissions.requestPermissions(this, "The app needs these permissions to function properly", RC_ALL, *permissions)
        }
    }

    fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val fileName = "EXORDI_" + timeStamp + "_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val savedPhoto = File.createTempFile(fileName, ".jpg", storageDir)
        imageAbsolutePath = savedPhoto.absolutePath
        return savedPhoto
    }

    // read ext storage
    fun selectImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            setType("image/*")
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png"))
        }
        startActivityForResult(intent, GALLERY_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                GALLERY_CODE -> saveGalleryImagePath(data)
            }
        }
    }

    private fun saveGalleryImagePath(data: Intent?) {
        val selectedImage: Uri = data!!.data!!
        val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(selectedImage, filePathColumn, null, null, null)
        cursor?.moveToFirst()
        imageAbsolutePath = cursor?.getString(cursor.getColumnIndex(filePathColumn[0]))
        cursor?.close()
        startActivity(Intent(this@CameraActivity, CropImageActivity::class.java).apply {
            putExtra(Const.EXTRA_IMAGE_PATH, imageAbsolutePath)
        })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

}
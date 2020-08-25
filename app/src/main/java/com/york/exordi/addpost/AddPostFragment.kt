package com.york.exordi.addpost

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Camera
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.york.exordi.R
import com.york.exordi.events.ChangeTabEvent
import com.york.exordi.events.CreatePostEvent
import com.york.exordi.shared.Const
import com.york.exordi.shared.registerActivityForEvents
import com.york.exordi.shared.registerFragmentForEvents
import kotlinx.android.synthetic.main.fragment_add_post.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions

class AddPostFragment : Fragment() {

    companion object {
        const val RC_ALL = 100

        const val GALLERY_CODE = 1000
    }

    private val permissions = arrayOf<String>(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_post, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cameraBtn.setOnClickListener {
            startActivity(Intent(activity, CameraActivity::class.java))
        }

        galleryBtn.setOnClickListener {
            selectMediaFromGallery()
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

    @AfterPermissionGranted(CameraActivity.RC_ALL)
    fun selectMediaFromGallery() {
        if (EasyPermissions.hasPermissions(requireContext(), *permissions)) {
            val intent = Intent(Intent.ACTION_PICK).apply {
                setType("image/* video/*")
                putExtra(
                    Intent.EXTRA_MIME_TYPES,
                    arrayOf("image/jpeg", "image/png", "video/mp4", "video/avi", "video/quicktime")
                )
                putExtra(MediaStore.EXTRA_DURATION_LIMIT, 60)
            }
            startActivityForResult(intent, CameraActivity.GALLERY_CODE)
        } else {
            EasyPermissions.requestPermissions(this, "The app needs these permissions to function properly",
                RC_ALL, *permissions)

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            val path = data?.data?.path
            val uri = data?.data
            path?.let {
                registerFragmentForEvents()
                if (it.contains(".mp4") || it.contains("video")) {
                    startPreparePostActivity(uri!!)
                } else if (it.contains(".jpg") || it.contains(".jpeg") || it.contains(".png") || it.contains("images")) {
                    startCropActivity(uri!!)
                }
            }

        }
    }

    private fun startPreparePostActivity(uri: Uri) {
        startActivity(Intent(activity, PreparePostActivity::class.java).apply {
            setData(uri)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                .addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            putExtra(Const.EXTRA_FILE_TYPE, Const.EXTRA_FILE_TYPE_VIDEO)
        })
    }

    private fun startCropActivity(uri: Uri) {
        startActivity(Intent(activity, CropImageActivity::class.java).apply {
            setData(uri)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                .addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        })
    }

    @Subscribe
    fun onPostCreated(event: CreatePostEvent) {
        EventBus.getDefault().post(ChangeTabEvent(""))
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}
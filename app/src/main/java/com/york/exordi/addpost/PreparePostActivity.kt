package com.york.exordi.addpost

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.FileUtils
import android.provider.MediaStore
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.devbrackets.android.exomedia.listener.OnPreparedListener
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelection
import com.google.android.exoplayer2.trackselection.TrackSelector
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.upstream.BandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.york.exordi.R
import com.york.exordi.events.CreatePostEvent
import com.york.exordi.feed.ui.FeedFragment
import com.york.exordi.feed.ui.FullscreenVideoActivity
import com.york.exordi.models.CategoryData
import com.york.exordi.repository.AppRepository
import com.york.exordi.shared.Const
import com.york.exordi.shared.getFileName
import com.york.exordi.shared.getPathFromUri
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.*
import kotlinx.android.synthetic.main.activity_prepare_post.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.apache.commons.io.IOUtils
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.CoroutineContext

class PreparePostActivity : AppCompatActivity() {

    companion object {
        const val RC = 100
        const val RC_FULLSCREEN_ACTIVITY = 10
    }

    private var filePath: String? = null
    private var fileUri: Uri? = null
    private var selectedCategory: CategoryData? = null
    private var fileType: String? = null

    private var videoPlayer: SimpleExoPlayer? = null




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prepare_post)

        setSupportActionBar(preparePostTb)
        preparePostTb.setNavigationOnClickListener {
            finish()
        }
        val fileCategory = intent.getStringExtra(Const.EXTRA_FILE_TYPE)
        intent.getStringExtra(Const.EXTRA_FILE_PATH)?.let {
            filePath = File(it).toString()
        }
        if (filePath == null) {
            fileUri = intent.data
        }
        fileCategory?.let {
            fileType = it
            if (it == Const.EXTRA_FILE_TYPE_PHOTO) {
                Glide.with(this).load(fileUri ?: filePath).into(imagePreview)
                videoView.visibility = View.INVISIBLE
            } else if (it == Const.EXTRA_FILE_TYPE_VIDEO) {
                setupVideoPlayer()
                imagePreview.visibility = View.INVISIBLE
            }
        }

        selectCategoryBtn.setOnClickListener {
            val intent = Intent(this, SelectCategoryActivity::class.java)
            selectedCategory?.let {
                intent.putExtra(Const.EXTRA_CATEGORY, it)
            }
            startActivityForResult(intent, RC)
        }

        publishBtn.setOnClickListener {
            publishPb.visibility = View.VISIBLE
            publishBtn.visibility = View.INVISIBLE
            if (fileType == Const.EXTRA_FILE_TYPE_PHOTO) {
                compressImage()
            } else {
                compressVideo()

            }
        }
    }

    private fun compressVideo() {
        var file: File? = null
        if (fileUri != null) {
            file = copyFileToCache(fileUri!!)
        } else if (filePath != null) {
            file = File(filePath!!)
        }
        publishPost(file!!, "video")
    }

//    private fun saveVideoFile(filePath: String?): File? {
//        filePath?.let {
//            val videoFile = File(filePath)
//            val videoFileName = "${System.currentTimeMillis()}_${videoFile.name}"
//            val folderName = Environment.DIRECTORY_MOVIES
//            if (Build.VERSION.SDK_INT >= 29) {
//
//                val values = ContentValues().apply {
//
//                    put(
//                        MediaStore.Images.Media.DISPLAY_NAME,
//                        videoFileName
//                    )
//                    put(MediaStore.Images.Media.MIME_TYPE, "video/mp4")
//                    put(MediaStore.Images.Media.RELATIVE_PATH, folderName)
//                    put(MediaStore.Images.Media.IS_PENDING, 1)
//                }
//
//                val collection =
//                    MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
//
//                val fileUri = applicationContext.contentResolver.insert(collection, values)
//
//                fileUri?.let {
//                    application.contentResolver.openFileDescriptor(fileUri, "w").use { descriptor ->
//                        descriptor?.let {
//                            FileOutputStream(descriptor.fileDescriptor).use { out ->
//                                FileInputStream(videoFile).use { inputStream ->
//                                    val buf = ByteArray(4096)
//                                    while (true) {
//                                        val sz = inputStream.read(buf)
//                                        if (sz <= 0) break
//                                        out.write(buf, 0, sz)
//                                    }
//                                }
//                            }
//                        }
//                    }
//
//                    values.clear()
//                    values.put(MediaStore.Video.Media.IS_PENDING, 0)
//                    applicationContext.contentResolver.update(fileUri, values, null, null)
//
//                    return File(com.york.exordi.shared.FileUtils(applicationContext).getRealPathFromUri(fileUri))
//                }
//            } else {
//                val downloadsPath =
//                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
//                val desFile = File(downloadsPath, videoFileName)
//
//                if (desFile.exists())
//                    desFile.delete()
//
//                try {
//                    desFile.createNewFile()
//                } catch (e: IOException) {
//                    e.printStackTrace()
//                }
//
//                return desFile
//            }
//        }
//        return null
//    }

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
    private fun setupVideoPlayer() {
        videoView.visibility = View.VISIBLE
        videoView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
        videoView.findViewById<ImageButton>(R.id.exo_fullscreen).setOnClickListener {
            videoPlayer?.playWhenReady = false
            launchFullscreenVideoActivity(filePath!!, videoPlayer!!.currentPosition)
        }
        videoView.findViewById<LinearLayout>(R.id.exoTimelineControls).visibility = View.GONE
        val bandwidthMeter: BandwidthMeter = DefaultBandwidthMeter()
        val videoTrackSelectionFactory: TrackSelection.Factory = AdaptiveTrackSelection.Factory()
        val trackSelector: TrackSelector = DefaultTrackSelector(videoTrackSelectionFactory)

        // 2. Create the player
        videoPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelector)
        // Bind the player to the view.
        // Bind the player to the view.
        videoView.useController = true
        videoView.player = videoPlayer
        videoPlayer?.addListener(object : Player.EventListener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> preparePostPb.visibility = View.VISIBLE
                    Player.STATE_ENDED -> {
                        videoPlayer?.seekTo(0)
                        videoPlayer?.playWhenReady = false
                    }
                    Player.STATE_IDLE -> {
                    }
                    Player.STATE_READY -> {
                        preparePostPb.visibility = View.GONE
//                        if (!viewModel.isVideoViewAdded) {
//                            addVideoView()
//                        }
                    }
                }
            }

        })
        val userAgent = Util.getUserAgent(this, getString(R.string.app_name))
        //2
        val mediaSource = ExtractorMediaSource
            .Factory(DefaultDataSourceFactory(this, userAgent))
            .setExtractorsFactory(DefaultExtractorsFactory())
            .createMediaSource(fileUri ?: Uri.parse(filePath))
        videoPlayer?.prepare(mediaSource)
        videoPlayer?.playWhenReady = false

    }

    private fun publishPost(file: File, fileType: String) {
        var description: String? = null
        if (descriptionEt.text.toString().isNotEmpty()) {
            description = descriptionEt.text.toString()
        }
        val multipartBody = MultipartBody.Part.createFormData("post_file", file.name, file.asRequestBody(fileType.toMediaTypeOrNull()))
        val api = AppRepository.getInstance(application)
        api.createPost(this.fileType!!, selectedCategory!!.id, description, multipartBody) { response ->
            publishPb.visibility = View.INVISIBLE
            publishBtn.visibility = View.VISIBLE
            if (response.code == 200 || response.code == 201) {
                Toast.makeText(this, "Successfully created new post", Toast.LENGTH_SHORT).show()
                EventBus.getDefault().post(CreatePostEvent())
                finish()
            }
        }
    }

    private fun launchFullscreenVideoActivity(
        videoUrl: String,
        currentPosition: Long
    ) {
        startActivityForResult(Intent(this, FullscreenVideoActivity::class.java).apply {
            putExtra(Const.EXTRA_VIDEO_URL, videoUrl)
            putExtra(Const.EXTRA_PLAYBACK_POSITION, currentPosition)
        }, FeedFragment.RC_FULLSCREEN_ACTIVITY)
    }

    fun setCompressedImage(file: File) {
        Glide.with(this).load(file).into(compressedImage)
    }

    private fun compressImage() {
        var file: File? = null
        if (fileUri != null) {
            file = copyFileToCache(fileUri!!)
        } else if (filePath != null) {
            file = File(filePath!!)
        }
        file?.let {
            lifecycleScope.launch {
                val compressedFile = Compressor.compress(this@PreparePostActivity, file, Dispatchers.Main) {
                    resolution(1080, 1350)
                    quality(80)
                    format(Bitmap.CompressFormat.JPEG)
                }
                publishPost(compressedFile, "image/*")
            }
        }

    }

    private fun createFileMultipartBody(): MultipartBody.Part? {
        var file: File? = null
        if (fileUri != null) {
            file = copyFileToCache(fileUri!!)
        } else if (filePath != null) {
            file = File(filePath!!)
        }
        var requestBody: RequestBody? = null
        if (fileType == Const.EXTRA_FILE_TYPE_PHOTO) {
            if (file!!.length() / 1024 > 1000) {
                lifecycleScope.launch {
                    val compressedFile = Compressor.compress(this@PreparePostActivity, file, Dispatchers.Main) {
                        default(1080, 1350)
                    }
                    requestBody = compressedFile.asRequestBody("image/*".toMediaTypeOrNull())
                }
            } else {
                requestBody = file!!.asRequestBody("image/*".toMediaTypeOrNull())
            }
        } else if (fileType == Const.EXTRA_FILE_TYPE_VIDEO) {
            requestBody = file!!.asRequestBody("video/*".toMediaTypeOrNull())
        }
        requestBody?.let {
            return MultipartBody.Part.createFormData("post_file", file!!.name, it)
        }
        return null
    }

    private fun copyFileToCache(fileUri: Uri): File {
        val parcelFileDescriptor = contentResolver.openFileDescriptor(fileUri, "r", null)
        val inputStream = FileInputStream(parcelFileDescriptor!!.fileDescriptor)
        val file = File(cacheDir, contentResolver.getFileName(fileUri))
        val outputStream = FileOutputStream(file)
        IOUtils.copy(inputStream, outputStream)
        return file
    }

    fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val fileName = "EXORDI_" + timeStamp + "_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val savedPhoto = File.createTempFile(fileName, ".jpg", storageDir)
        return savedPhoto
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            RC -> {
                if (resultCode == Activity.RESULT_OK) {
                    selectedCategory = data?.getSerializableExtra(Const.EXTRA_CATEGORY) as CategoryData?
                    publishBtn.visibility = View.VISIBLE
                }
            }
            RC_FULLSCREEN_ACTIVITY -> {
                setPlaybackPosition(data!!.getLongExtra(Const.EXTRA_PLAYBACK_POSITION, 0))
            }
        }
    }

    private fun setPlaybackPosition(position: Long) {
        videoPlayer?.seekTo(position)
        videoPlayer?.playWhenReady = true
    }


}
package com.york.exordi.addpost

import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.ExecuteCallback
import com.arthenica.mobileffmpeg.FFmpeg
import com.york.exordi.R
import com.york.exordi.events.CreatePostEvent
import com.york.exordi.shared.Const
import com.york.exordi.shared.getMediaPath
import com.york.exordi.shared.registerActivityForEvents
import org.greenrobot.eventbus.Subscribe
import java.io.File
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class ProgressBarActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "ProgressBarActivity"
    }

    private var fileUri: Uri? = null

    private var ffMpeg: FFmpeg? = null
//    private var ffmpeg: FFmpeg? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_progress_bar)

        fileUri = Uri.parse(intent.getStringExtra(Const.EXTRA_FILE_URI))
        fileUri?.let {
            trimVideo()
//            loadFFMpegLibrary()
        }
    }

    private fun trimVideo() {
        // 3 command rabochiy
        val originalPath = getMediaPath(this, fileUri!!)
        val destinationFile = createVideoFile()
//        val folder = File(cacheDir, "Exordi")
//        folder.mkdir()
//        val destinationFile = File(folder, "${System.currentTimeMillis()}.mp4")
//        val command = arrayOf("-ss", "0", "-y", "-i", originalPath, "-t", "" + 60*1000, "-r", "15", "-vcodec", "mpeg4", "-b:v", "2097152", "-b:a", "48000", "-ac", "2", "-ar", "22050", destinationFile.absolutePath)
//        val command = arrayOf("-ss", "00:00:00", "-y", "-i", originalPath, "-t", "" + 60*1000, "-vcodec", "mpeg4", "-b:v", "2097152", "-b:a", "48000", "-ac", "2", "-ar", "22050", destinationFile.absolutePath)
        val command = arrayOf("-ss", "00:00:00", "-i", originalPath, "-to", "00:01:00", "-c", "copy", destinationFile.absolutePath)
//        val command = arrayOf("-ss", "00:00:00", "-y", "-i", originalPath, "-t", "00:01:00", "-vcodec", "mpeg4", "-b:v", "2500000", "-b:a", "128000", "-ac", "2", "-ar", "44100", destinationFile.absolutePath)
//        val command = arrayOf("-y", "-i", originalPath, "-ss", "00:00:00", "-to", "00:01:00", "-c", "copy", destinationFile.absolutePath)
//        loadFFMpegLibrary()
        try {
            val rc = FFmpeg.executeAsync(command, object : ExecuteCallback {
                override fun apply(executionId: Long, returnCode: Int) {
                    when (returnCode) {
                        Config.RETURN_CODE_SUCCESS -> {
                            Toast.makeText(this@ProgressBarActivity, "Video trimmed successfully", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@ProgressBarActivity, PreparePostActivity::class.java).apply {
                                putExtra(Const.EXTRA_FILE_TYPE, Const.EXTRA_FILE_TYPE_VIDEO)
                                putExtra(Const.EXTRA_FILE_PATH, destinationFile.absolutePath)
                            })
                            finish()
                        }
                        Config.RETURN_CODE_CANCEL -> {
                            Toast.makeText(this@ProgressBarActivity, "Cancelled", Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            Config.printLastCommandOutput(Log.ERROR)
                        }
                    }
                }
            })
//            ffmpeg?.execute(command, object : ExecuteBinaryResponseHandler() {
//                override fun onFinish() {
//                    Log.e(TAG, "onFinish: " )
//                    Toast.makeText(this@ProgressBarActivity, "Video trimmed successfully", Toast.LENGTH_SHORT).show()
//                            startActivity(Intent(this@ProgressBarActivity, PreparePostActivity::class.java).apply {
//                                putExtra(Const.EXTRA_FILE_TYPE, Const.EXTRA_FILE_TYPE_VIDEO)
//                                putExtra(Const.EXTRA_FILE_PATH, destinationFile.absolutePath)
//                            })
//                            finish()
//                }
//
//                override fun onSuccess(message: String?) {
//                    Log.e(TAG, "onSuccess: ")
//                }
//
//                override fun onFailure(message: String?) {
//                    Log.e(TAG, "onFailure: "  + message)
//                }
//
//                override fun onProgress(message: String?) {
//                    Log.e(TAG, "onProgress: " )
//                }
//
//                override fun onStart() {
//                    Log.e(TAG, "onStart: " )
//                }
//            })
        } catch (e: Exception) {

        }
    }

//    private fun loadFFMpegLibrary() {
//        try {
//            if (ffmpeg == null) {
//                ffmpeg = FFmpeg.getInstance(this)
//            }
//            ffmpeg?.loadBinary(object : LoadBinaryResponseHandler() {
//                override fun onSuccess() {
//                    Log.e(TAG, "onSuccess: " + "FFMpeg loaded successfully" )
//                    trimVideo()
//                }
//
//                override fun onFailure() {
//                    Log.e(TAG, "onFailure: " + "Cannot load FFMpeg library" )
//                    Toast.makeText(this@ProgressBarActivity, "Cannot trim video on this device!", Toast.LENGTH_LONG).show()
//                    finish()
//                }
//            })
//        } catch (e: FFmpegNotSupportedException) {
//            Log.e(TAG, "loadFFMpegLibrary: " + e.message )
//            Toast.makeText(this@ProgressBarActivity, "Cannot trim video on this device!", Toast.LENGTH_LONG).show()
//            finish()
//        }
//    }

    private fun createVideoFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val fileName = "EXORDI_" + timeStamp + "_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_MOVIES)
        return File(storageDir, "$fileName.mp4")
    }
}
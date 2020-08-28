package com.york.exordi.shared

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.text.format.DateUtils
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.york.exordi.R
import com.york.exordi.models.CommentResult
import com.york.exordi.models.SearchUserResult
import com.york.exordi.models.Result
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject
import java.io.*
import java.text.DecimalFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.log10
import kotlin.math.pow

interface OnItemClickListener {
    fun <T> onItemClick(listItem: T)
}

interface OnItemWithTagClickListener {
    fun <T> onItemClick(listItem: T, tag: String, position: Int)
}

interface OnPostClickListener {
    fun onItemClick(position: Int, post: Result, tag: String, vararg view: View?)
}

interface OnCommentClickListener {
    fun onItemClick(comment: CommentResult, tag: String)
}

interface OnViewDetachedFromWindowListener {
    fun onViewDetached()
}

interface OnBindListener {
    fun onBind(postId: String, recyclerView: RecyclerView)
}

interface OnPlayerReadyListener {
    fun onPlayerReady()
}

interface OnFullscreenButtonClickListener {
    fun onButtonClick(videoUrl: String, currentPosition: Long)
}

inline fun FragmentManager.inTransaction(func: FragmentTransaction.() -> FragmentTransaction) {
    beginTransaction().func().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit()
}

fun Fragment.handleOnBackPressed(owner: LifecycleOwner) {
    requireActivity().onBackPressedDispatcher.addCallback(owner, object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            parentFragmentManager.popBackStack()
        }

    })
}

fun Context.isNetworkAvailable(): Boolean {
    val cm: ConnectivityManager? = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        cm?.let { it ->
            val capabilities: NetworkCapabilities? = it.getNetworkCapabilities(it.activeNetwork)
            capabilities?.let {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) or
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    return true
                }
            }
        }
    } else {
        cm?.let {
            val activeNetwork: NetworkInfo? = it.activeNetworkInfo
            activeNetwork?.let {
                if (activeNetwork.type == ConnectivityManager.TYPE_WIFI ||
                    activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                    return true
                }
            }
        }
    }
    return false
}

fun Context.makeNoConnectionToast() {
    Toast.makeText(applicationContext, "There is no internet connection", Toast.LENGTH_LONG).show()
}

fun String.toErrorString(): String {
    return JSONObject(this).getString("message")
}

fun Context.makeInternetSafeRequest(func: () -> (Unit)) {
    if (this.isNetworkAvailable()) {
        func()
    } else {
        this.makeNoConnectionToast()
    }
}

fun AppCompatActivity.registerActivityForEvents() {
    if (!EventBus.getDefault().isRegistered(this)) {
        EventBus.getDefault().register(this)
    }
}

fun AppCompatActivity.unregisterActivityFromEvents() {
    EventBus.getDefault().unregister(this)
}

fun Fragment.registerFragmentForEvents() {
    if (!EventBus.getDefault().isRegistered(this)) {
        EventBus.getDefault().register(this)
    }
}

fun Fragment.unregisterFragmentFromEvents() {
    EventBus.getDefault().unregister(this)
}

fun Context.getCircularProgressDrawable(): CircularProgressDrawable {
    val drawable = CircularProgressDrawable(this).apply {
        strokeWidth = 5F
        centerRadius = 30F
    }
    drawable.setColorSchemeColors(ContextCompat.getColor(this, R.color.textColorPrimary))
    drawable.start()
    return drawable
}

fun ContentResolver.getFileName(fileUri: Uri): String {

    var name = ""
    val returnCursor = this.query(fileUri, null, null, null, null)
    if (returnCursor != null) {
        val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        returnCursor.moveToFirst()
        name = returnCursor.getString(nameIndex)
        returnCursor.close()
    }

    return name
}

fun String.toHoursAgo(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.UK)
    sdf.timeZone = TimeZone.getTimeZone("GMT")
    try {
        val time: Long = sdf.parse(this).time
        val now = System.currentTimeMillis()
        val ago = DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS)
        return ago.toString()
    } catch (e: ParseException) {
        e.printStackTrace()
    }
    return this
}

fun getMediaPath(context: Context, uri: Uri): String {

    val resolver = context.contentResolver
    val projection = arrayOf(MediaStore.Video.Media.DATA)
    var cursor: Cursor? = null
    try {
        cursor = resolver.query(uri, projection, null, null, null)
        return if (cursor != null) {
            val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            cursor.moveToFirst()
            cursor.getString(columnIndex)

        } else ""

    } catch (e: Exception) {
        resolver.let {
            val filePath = (context.applicationInfo.dataDir + File.separator
                    + System.currentTimeMillis())
            val file = File(filePath)

            resolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    val buf = ByteArray(4096)
                    var len: Int
                    while (inputStream.read(buf).also { len = it } > 0) outputStream.write(
                        buf,
                        0,
                        len
                    )
                }
            }
            return file.absolutePath
        }
    } finally {
        cursor?.close()
    }
}

fun getFileSize(size: Long): String {
    if (size <= 0)
        return "0"

    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()

    return DecimalFormat("#,##0.#").format(
        size / 1024.0.pow(digitGroups.toDouble())
    ) + " " + units[digitGroups]
}

//The following methods can be alternative to [getMediaPath].
// todo(abed): remove [getPathFromUri], [getVideoExtension], and [copy]
fun getPathFromUri(context: Context, uri: Uri): String {
    var file: File? = null
    var inputStream: InputStream? = null
    var outputStream: OutputStream? = null
    var success = false
    try {
        val extension: String = getVideoExtension(uri)
        inputStream = context.contentResolver.openInputStream(uri)
        file = File.createTempFile("compressor", extension, context.cacheDir)
        file.deleteOnExit()
        outputStream = FileOutputStream(file)
        if (inputStream != null) {
            copy(inputStream, outputStream)
            success = true
        }
    } catch (ignored: IOException) {
    } finally {
        try {
            inputStream?.close()
        } catch (ignored: IOException) {
        }
        try {
            outputStream?.close()
        } catch (ignored: IOException) {
            // If closing the output stream fails, we cannot be sure that the
            // target file was written in full. Flushing the stream merely moves
            // the bytes into the OS, not necessarily to the file.
            success = false
        }
    }
    return if (success) file!!.path else ""
}

/** @return extension of video with dot, or default .mp4 if it none.
 */
private fun getVideoExtension(uriVideo: Uri): String {
    var extension: String? = null
    try {
        val imagePath = uriVideo.path
        if (imagePath != null && imagePath.lastIndexOf(".") != -1) {
            extension = imagePath.substring(imagePath.lastIndexOf(".") + 1)
        }
    } catch (e: Exception) {
        extension = null
    }
    if (extension == null || extension.isEmpty()) {
        //default extension for matches the previous behavior of the plugin
        extension = "mp4"
    }
    return ".$extension"
}

private fun copy(`in`: InputStream, out: OutputStream) {
    val buffer = ByteArray(4 * 1024)
    var bytesRead: Int
    while (`in`.read(buffer).also { bytesRead = it } != -1) {
        out.write(buffer, 0, bytesRead)
    }
    out.flush()
}


package com.york.exordi.shared

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.os.Build
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.LifecycleOwner
import com.york.exordi.models.Result
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject

interface OnItemClickListener {
    fun <T> onItemClick(listItem: T)
}

interface OnPostClickListener {
    fun onItemClick(position: Int, post: Result, tag: String, view: View?)
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
    val cm: ConnectivityManager? = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
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
    Toast.makeText(applicationContext, "Нет подключения к интернету!", Toast.LENGTH_SHORT).show()
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
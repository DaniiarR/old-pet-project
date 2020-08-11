package com.york.exordi.addpost

import android.content.Intent
import android.graphics.Camera
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.york.exordi.R
import kotlinx.android.synthetic.main.fragment_add_post.*

class AddPostFragment : Fragment() {

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
            Toast.makeText(requireContext(), "Feature is to be implemented", Toast.LENGTH_SHORT).show()
        }
    }

}
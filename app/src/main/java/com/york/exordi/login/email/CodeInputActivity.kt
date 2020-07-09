package com.york.exordi.login.email

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.york.exordi.R
import kotlinx.android.synthetic.main.activity_code_input.*

class CodeInputActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_code_input)

        backBtn.setOnClickListener { finish() }
    }
}
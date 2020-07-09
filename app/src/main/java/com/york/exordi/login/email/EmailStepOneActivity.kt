package com.york.exordi.login.email

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.DatePicker
import androidx.core.content.ContextCompat
import com.york.exordi.BaseActivity
import com.york.exordi.R
import com.york.exordi.shared.Const
import kotlinx.android.synthetic.main.activity_email_step_one.*
import java.text.SimpleDateFormat
import java.util.*

class EmailStepOneActivity : BaseActivity(), DatePickerDialog.OnDateSetListener {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_email_step_one)

        backBtn.setOnClickListener {
            finish()
        }

        birthdayRl.setOnClickListener {
            showSelectBithdayDialog()
        }
    }

    private fun showSelectBithdayDialog() {
        DatePickerDialog(this,
            this,
        Calendar.getInstance().get(Calendar.YEAR),
        Calendar.getInstance().get(Calendar.MONTH),
        Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    override fun onDateSet(picker: DatePicker?, p1: Int, p2: Int, p3: Int) {
        val year = picker?.year ?: 2020
        val month = picker?.month ?: 0
        val day = picker?.dayOfMonth ?: 26
        val calendar: Calendar = Calendar.getInstance()
        calendar.set(year, month, day)
//        val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy")
        val formatter = SimpleDateFormat("dd MMMM yyyy", Locale.US)
        setBirthDate(formatter.format(calendar.time))
    }

    private fun setBirthDate(date: String) {
        birthdayTv.text = date
        birthdayTv.setTextColor(ContextCompat.getColor(this, R.color.textColorPrimary))
        nextBtn.visibility = View.VISIBLE
        nextBtn.setOnClickListener {
            startActivity(Intent(this, EmailStepTwoActivity::class.java)
                .apply {
                    putExtra(Const.EXTRA_BIRTHDATE, date)
                })
        }

    }


}
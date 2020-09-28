package com.york.exordi.login.email

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.york.exordi.R
import com.york.exordi.base.BaseActivity
import com.york.exordi.shared.Const
import kotlinx.android.synthetic.main.activity_email_step_two.*
import java.util.*

class EmailStepTwoActivity : BaseActivity() {

    private lateinit var birthday: String
    private lateinit var countries: Array<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_email_step_two)

        birthday = intent.getStringExtra(Const.EXTRA_BIRTHDATE).toString()
        backBtn.setOnClickListener { finish() }
        countryTiL.setOnClickListener {
            showCountriesPopup()
        }
    }

    private fun showCountriesPopup() {
        AlertDialog.Builder(this)
            .apply {
                title = "Select your country"
                setItems(getCountryArray(), object : DialogInterface.OnClickListener {
                    override fun onClick(p0: DialogInterface?, p1: Int) {
                        p0?.dismiss()
                        countryTv.text = countries[p1]
                        nextBtn.visibility = View.VISIBLE
                        nextBtn.setOnClickListener {
                            startActivity(Intent(this@EmailStepTwoActivity, EmailStepThreeActivity::class.java)
                                .apply {
                                       putExtra(Const.EXTRA_BIRTHDATE, birthday)
                                    putExtra(Const.EXTRA_COUNTRY, countries[p1])
                                })
                        }
                    }

                })
            }.show()
    }

    private fun getCountryArray(): Array<String> {
        val countries: SortedSet<String> = TreeSet()
        for (countryCode in Locale.getISOCountries()) {
            val locale = Locale("en-us", countryCode)
            if (!TextUtils.isEmpty(locale.displayCountry)) {
                countries.add(locale.displayCountry)
            }
        }
        this.countries = countries.toTypedArray()
        return this.countries
    }

}
package com.example.kumarjit

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
class MainActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var adView: AdView
    private var adsEnabled = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = getSharedPreferences("PIN_PREFS", MODE_PRIVATE)

        // Root check first
        if (RootCheckUtil.isDeviceRooted()) {
            AlertDialog.Builder(this)
                .setTitle("Root detected")
                .setMessage("Root detected - app will close.")
                .setPositiveButton("OK") { _, _ -> finish() }
                .setCancelable(false)
                .show()
            return
        }

        // Force PIN entry if not unlocked
        val isPinUnlocked = prefs.getBoolean("PIN_UNLOCKED", false)
        if (!isPinUnlocked) {
            startActivity(Intent(this, PinActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
            return
        }

        // normal UI (one button to go to Access page)
        val btnAccess: Button = findViewById(R.id.btn_access)
        val tvStatus: TextView = findViewById(R.id.tv_status)
        tvStatus.text = if (prefs.getBoolean("isAdmin", false)) "Role: Admin" else "Role: Normal"

        btnAccess.setOnClickListener {
            startActivity(Intent(this, AccessActivity::class.java))
        }
        // Initialize AdMob
        MobileAds.initialize(this) {}

        // Setup AdView
        adView = findViewById(R.id.adView)
        loadAds()
    }
    private fun loadAds() {
        if (adsEnabled) {
            val adRequest = AdRequest.Builder().build()
            adView.loadAd(adRequest)
        }

    }

}

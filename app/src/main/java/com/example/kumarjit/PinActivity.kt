package com.example.kumarjit

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Vibrator
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import net.sqlcipher.Cursor
import net.sqlcipher.database.SQLiteDatabase
import java.util.*

class PinActivity : AppCompatActivity() {

    private lateinit var secureDB: SQLiteDatabase
    private lateinit var prefs: SharedPreferences
    private var maxTries = 3
    private var tries = 0
    private lateinit var pinInput: EditText
    private lateinit var statusTv: TextView
    private lateinit var submitBtn: Button
    private lateinit var vibrator: Vibrator
    private lateinit var adView: AdView
    private var adsEnabled = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin)

        SQLiteDatabase.loadLibs(this)
        prefs = getSharedPreferences("PIN_PREFS", Context.MODE_PRIVATE)

        pinInput = findViewById(R.id.pin_input)
        statusTv = findViewById(R.id.tv_status)
        submitBtn = findViewById(R.id.submit_button)
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        adView = findViewById(R.id.adView)

        setupDatabase()
        setupPinSubmit()

        MobileAds.initialize(this) {}
        loadAds()
    }

    private fun loadAds() {
        if (adsEnabled) {
            val adRequest = AdRequest.Builder().build()
            adView.loadAd(adRequest)
        }
    }

    private fun setupDatabase() {
        try {
            val pinDbFile = getDatabasePath("secure_pin.db")
            pinDbFile.parentFile?.mkdirs()

            val password = "123456"
            secureDB = SQLiteDatabase.openOrCreateDatabase(pinDbFile, password, null)
            secureDB.execSQL("CREATE TABLE IF NOT EXISTS pin_table(pin VARCHAR);")

            // Always delete old PIN on each app open
            secureDB.execSQL("DELETE FROM pin_table;")

            val pin = generatePin()
            secureDB.execSQL("INSERT INTO pin_table(pin) VALUES('$pin');")

            // Show new PIN every time app opens (for testing/demo)
            Toast.makeText(this, "Generated PIN: $pin", Toast.LENGTH_LONG).show()

            // Reset stored JWT and PIN_UNLOCKED when app reopens
            prefs.edit().remove("PIN_UNLOCKED").apply()
            prefs.edit().remove("jwt_token").apply()
            prefs.edit().putBoolean("isAdmin", false).apply()
        } catch (e: Exception) {
            Toast.makeText(this, "DB Error: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun setupPinSubmit() {
        submitBtn.setOnClickListener {
            val enteredPin = pinInput.text.toString()

            if (enteredPin.length != 4) {
                statusTv.text = "PIN must be 4 digits"
                return@setOnClickListener
            }

            val cursor: Cursor = secureDB.rawQuery("SELECT * FROM pin_table", null)
            cursor.moveToFirst()
            val correctPin = cursor.getString(0)
            cursor.close()

            if (enteredPin == correctPin) {
                Toast.makeText(this, "PIN correct! Welcome.", Toast.LENGTH_SHORT).show()
                prefs.edit().putBoolean("PIN_UNLOCKED", true).apply()

                val jwtManager = JwtManager(this)
                jwtManager.ensureDefaultJwtAssigned()

                startActivity(Intent(this, AccessActivity::class.java))
                finish()
            } else {
                tries++
                if (tries >= maxTries) {
                    statusTv.text = "Max attempts reached! Wait 30 seconds."
                    pinInput.isEnabled = false
                    if (vibrator.hasVibrator()) vibrator.vibrate(400)
                    Handler().postDelayed({
                        tries = 0
                        pinInput.isEnabled = true
                        statusTv.text = ""
                    }, 30_000)
                } else {
                    statusTv.text = "Incorrect PIN! ${maxTries - tries} attempts remaining."
                    pinInput.text.clear()
                    if (vibrator.hasVibrator()) vibrator.vibrate(200)
                }
            }
        }
    }

    private fun generatePin(): String {
        val rnd = Random()
        return (rnd.nextInt(9000) + 1000).toString()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::secureDB.isInitialized) secureDB.close()
    }
}

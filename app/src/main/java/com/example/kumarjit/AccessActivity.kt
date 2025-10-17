package com.example.kumarjit

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import android.util.Log

class AccessActivity : AppCompatActivity() {

    private lateinit var jwtManager: JwtManager
    private lateinit var tvTitle: TextView
    private lateinit var tvRole: TextView
    private lateinit var btnGetAccess: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_access)

        jwtManager = JwtManager(this)
        jwtManager.ensureDefaultJwtAssigned()

        tvTitle = findViewById(R.id.tv_title)
        tvRole = findViewById(R.id.tv_role)
        btnGetAccess = findViewById(R.id.btn_get_access)

        updateRoleDisplay()

        // Hidden admin activation: long-press title to assign admin token inside app
        tvTitle.setOnLongClickListener {
            jwtManager.assignAdminJwt()
            updateRoleDisplay()
            Toast.makeText(this, "Admin token assigned (internal)", Toast.LENGTH_SHORT).show()
            true
        }

        btnGetAccess.setOnClickListener {
            val stored = jwtManager.getStoredJwt()
            val payload: JSONObject? = jwtManager.verifyJwtAndReturnPayload(stored)
            if (payload != null) {
                val role = payload.optString("role", "user")
                if (role == "admin") {
                    // mark admin in prefs and proceed
                    val prefs = getSharedPreferences("PIN_PREFS", MODE_PRIVATE)
                    prefs.edit().putBoolean("isAdmin", true).apply()
                    startActivity(Intent(this, AdminActivity::class.java))
                } else {
                    Toast.makeText(this, "Access denied: not an admin ", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Invalid token", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateRoleDisplay() {
        val stored = jwtManager.getStoredJwt()
        val payload = jwtManager.verifyJwtAndReturnPayload(stored)
        val role = payload?.optString("role", "unknown") ?: "invalid"
        tvRole.text = "Current role: normal $role "
        Log.d("JwtDebug", "Stored JWT: $stored")
        Log.d("JwtDebug", "Verified payload: $payload")
    }
}

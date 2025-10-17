package com.example.kumarjit

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.BufferedReader
import java.io.InputStreamReader

class FinalScoreActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_final_score)

        val tvDate = findViewById<TextView>(R.id.tv_date)
        val tvPlayerName = findViewById<TextView>(R.id.tv_player_name)
        val tvRpsScore = findViewById<TextView>(R.id.tv_rps_score)
        val tvSnakeScore = findViewById<TextView>(R.id.tv_snake_score)
        val btnSecretAccess = findViewById<Button>(R.id.btn_secret_access)

        val playerName = intent.getStringExtra("PLAYER_NAME") ?: "Player"
        val rpsScore = intent.getIntExtra("RPS_SCORE", 0)
        val snakeScore = intent.getIntExtra("SNAKE_SCORE", 0)

        tvPlayerName.text = playerName
        tvRpsScore.text = rpsScore.toString()
        tvSnakeScore.text = snakeScore.toString()

        // Show or hide the secret access button based on snake score
        if (snakeScore > 5) {
            btnSecretAccess.visibility = View.VISIBLE
            // Set click listener for the secret access button
            btnSecretAccess.setOnClickListener {
                val intent = Intent(this, FakePaymentActivity::class.java)
                startActivity(intent)
            }
        } else {
            btnSecretAccess.visibility = View.GONE
        }

        // Run `date` command in background
        Thread {
            val dateOutput = try {
                val proc = Runtime.getRuntime().exec("date")
                val reader = BufferedReader(InputStreamReader(proc.inputStream))
                val result = StringBuilder()
                var line = reader.readLine()
                while (line != null) {
                    result.append(line).append("\n")
                    line = reader.readLine()
                }
                proc.waitFor()
                result.toString().trim()
            } catch (e: Exception) {
                "Error getting date: ${e.message}"
            }

            // Update TextView on UI thread
            runOnUiThread { tvDate.text = "Date: $dateOutput" }
        }.start()
    }
}

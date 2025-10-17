package com.example.kumarjit

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView

class SnakeActivity : AppCompatActivity() {

    private lateinit var snakeView: SnakeView
    private lateinit var btnQuit: Button
    private lateinit var tvScore: TextView
    private lateinit var adView: AdView

    private var playerName: String = ""
    private var rpsScore: Int = 0
    private var gameEnded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_snake)

        playerName = intent.getStringExtra("PLAYER_NAME") ?: "Player"
        rpsScore = intent.getIntExtra("RPS_SCORE", 0)

        snakeView = findViewById(R.id.snake_game_view)
        btnQuit = findViewById(R.id.btnQuit)
        tvScore = findViewById(R.id.tvScore)
        adView = findViewById(R.id.adViewSnake)

        // Load Ad
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        // Update score live
        snakeView.onScoreUpdate = { score -> runOnUiThread { tvScore.text = "Score: $score" } }

        // Game over callback
        snakeView.onGameOver = { score -> runOnUiThread { endGame(score) } }

        btnQuit.setOnClickListener { if (!gameEnded) snakeView.quitGame() }

        // Adjust bottom boundary after layout
        snakeView.post {
            val reservedPixels = btnQuit.height + adView.height + 32
            snakeView.setBottomLimit(snakeView.height - reservedPixels)
        }
    }

    private fun endGame(snakeScore: Int) {
        if (gameEnded) return
        gameEnded = true
        val intent = Intent(this, FinalScoreActivity::class.java)
        intent.putExtra("PLAYER_NAME", playerName)
        intent.putExtra("RPS_SCORE", rpsScore)
        intent.putExtra("SNAKE_SCORE", snakeScore)
        startActivity(intent)
        finish()
    }
}

package com.example.kumarjit

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.util.*
import android.content.Intent

class AdminActivity : AppCompatActivity() {

    private lateinit var tvTitle: TextView
    private lateinit var etName: EditText
    private lateinit var btnStart: Button
    private lateinit var tvRound: TextView
    private lateinit var tvAppMove: TextView
    private lateinit var tvResult: TextView
    private lateinit var tvScore: TextView

    private lateinit var btnRock: Button
    private lateinit var btnPaper: Button
    private lateinit var btnScissors: Button

    private var playerName: String = ""
    private var roundIndex = 0
    private var playerScore = 0
    private var appScore = 0
    private val totalRounds = 4
    private lateinit var appSequence: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        tvTitle = findViewById(R.id.tv_admin)
        etName = findViewById(R.id.et_player_name)
        btnStart = findViewById(R.id.btn_start_game)
        tvRound = findViewById(R.id.tv_round)
        tvAppMove = findViewById(R.id.tv_app_move)
        tvResult = findViewById(R.id.tv_result)
        tvScore = findViewById(R.id.tv_score)

        btnRock = findViewById(R.id.btn_rock)
        btnPaper = findViewById(R.id.btn_paper)
        btnScissors = findViewById(R.id.btn_scissors)

        setupUiInitial()

        btnStart.setOnClickListener {
            playerName = etName.text.toString().trim()
            if (playerName.isEmpty()) {
                Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            startGame()
        }

        btnRock.setOnClickListener { playRound("R") }
        btnPaper.setOnClickListener { playRound("P") }
        btnScissors.setOnClickListener { playRound("S") }

        tvTitle.setOnLongClickListener {
            AlertDialog.Builder(this)
                .setTitle("App Sequence (debug)")
                .setMessage(appSequence.joinToString(", "))
                .setPositiveButton("OK", null)
                .show()
            true
        }
    }

    private fun setupUiInitial() {
        etName.visibility = View.VISIBLE
        btnStart.visibility = View.VISIBLE

        tvRound.visibility = View.GONE
        tvAppMove.visibility = View.GONE
        tvResult.visibility = View.GONE
        tvScore.visibility = View.GONE

        btnRock.visibility = View.GONE
        btnPaper.visibility = View.GONE
        btnScissors.visibility = View.GONE
    }

    private fun startGame() {
        // Generate sequence (modifiable via smali)
        appSequence = listOf(part1(), part2(), part3(), part4())

        // Reset counters
        roundIndex = 0
        playerScore = 0
        appScore = 0

        // Show/hide UI
        etName.visibility = View.GONE
        btnStart.visibility = View.GONE

        tvRound.visibility = View.VISIBLE
        tvAppMove.visibility = View.VISIBLE
        tvResult.visibility = View.VISIBLE
        tvScore.visibility = View.VISIBLE

        btnRock.visibility = View.VISIBLE
        btnPaper.visibility = View.VISIBLE
        btnScissors.visibility = View.VISIBLE

        // Reset text and button colors
        tvAppMove.text = "App move will appear here"
        tvAppMove.setTextColor(Color.parseColor("#880E4F"))
        tvResult.text = ""
        resetButtonColors()

        // Reset UI texts
        tvRound.text = "Round: 1 / $totalRounds"
        tvScore.text = "Score — $playerName: 0 | App: 0"
    }

    private fun playRound(playerMove: String) {
        if (roundIndex >= totalRounds) return

        val appMove = appSequence[roundIndex]
        val result = decideRound(playerMove, appMove)

        when (result) {
            1 -> playerScore++
            -1 -> appScore++
        }

        resetButtonColors()
        highlightUserChoice(playerMove)

        tvAppMove.text = "App chose: ${moveToWord(appMove)}"
        tvAppMove.setTextColor(Color.RED)

        tvResult.text = when (result) {
            1 -> "✅ You Win!"
            -1 -> "❌ You Lose!"
            else -> "🤝 It's a Draw!"
        }

        roundIndex++

        if (roundIndex >= totalRounds) {
            val finalMsg = when {
                playerScore > appScore -> "🎉 You win, $playerName! ($playerScore : $appScore)"
                playerScore < appScore -> "💻 App wins ($appScore : $playerScore)"
                else -> "🤝 It's a draw ($playerScore : $appScore)"
            }
            AlertDialog.Builder(this)
                .setTitle("Game Over")
                .setMessage(finalMsg + "\n\nPlay next Snake Game?")
                .setPositiveButton("Yes") { _, _ ->
                    val intent = Intent(this, SnakeActivity::class.java)
                    intent.putExtra("PLAYER_NAME", playerName)
                    intent.putExtra("RPS_SCORE", playerScore)
                    startActivity(intent)
                    finish()
                }
                .setNegativeButton("No") { _, _ -> setupUiInitial() }
                .show()
        }

        updateUi()
    }

    private fun updateUi() {
        tvRound.text = "Round: ${minOf(roundIndex + 1, totalRounds)} / $totalRounds"
        tvScore.text = "Score — $playerName: $playerScore | App: $appScore"
    }

    private fun resetButtonColors() {
        val neutral = Color.parseColor("#B0BEC5")
        btnRock.setBackgroundColor(neutral)
        btnPaper.setBackgroundColor(neutral)
        btnScissors.setBackgroundColor(neutral)
    }

    private fun highlightUserChoice(move: String) {
        val green = Color.parseColor("#4CAF50")
        when (move) {
            "R" -> btnRock.setBackgroundColor(green)
            "P" -> btnPaper.setBackgroundColor(green)
            "S" -> btnScissors.setBackgroundColor(green)
        }
    }

    private fun moveToWord(m: String): String = when (m) {
        "R" -> "Rock"
        "P" -> "Paper"
        "S" -> "Scissors"
        else -> "Unknown"
    }

    private fun decideRound(player: String, app: String): Int {
        if (player == app) return 0
        return when {
            player == "R" && app == "S" -> 1
            player == "P" && app == "R" -> 1
            player == "S" && app == "P" -> 1
            else -> -1
        }
    }

    // Editable by smali
    fun part1(): String = randomMove()
    fun part2(): String = randomMove()
    fun part3(): String = randomMove()
    fun part4(): String = randomMove()

    private fun randomMove(): String {
        return when (Random().nextInt(3)) {
            0 -> "R"
            1 -> "P"
            else -> "S"
        }
    }
}

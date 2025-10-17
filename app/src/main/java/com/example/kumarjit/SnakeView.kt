package com.example.kumarjit

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.random.Random

class SnakeView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val snakePaint = Paint().apply { color = Color.GREEN }
    private val foodPaint = Paint().apply { color = Color.RED }
    private val boundaryPaint = Paint().apply {
        color = Color.YELLOW
        style = Paint.Style.STROKE
        strokeWidth = 8f
    }
    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 60f
        textAlign = Paint.Align.CENTER
    }

    private val handler = Handler()
    private val updateDelay = 150L
    private val blockSize = 50
    private var numBlocksX = 0
    private var numBlocksY = 0
    private var bottomLimitBlocks = 0 // for reserved area

    private var snake = mutableListOf<Point>()
    private var food = Point()
    private var score = 0
    private var isGameOver = false

    private var currentDirection = Direction.RIGHT
    private var nextDirection = Direction.RIGHT

    var onGameOver: ((Int) -> Unit)? = null
    var onScoreUpdate: ((Int) -> Unit)? = null

    private val updateRunnable = object : Runnable {
        override fun run() {
            if (!isGameOver) {
                moveSnake()
                invalidate()
                handler.postDelayed(this, updateDelay)
            }
        }
    }

    init { post { resetGame() } }

    fun setBottomLimit(pixels: Int) {
        bottomLimitBlocks = pixels / blockSize
    }

    fun resetGame() {
        post {
            numBlocksX = width / blockSize
            numBlocksY = height / blockSize
            if (bottomLimitBlocks == 0) bottomLimitBlocks = numBlocksY // default full height
            if (numBlocksX <= 0 || numBlocksY <= 0) return@post

            snake.clear()
            snake.add(Point(numBlocksX / 2, numBlocksY / 2))
            generateFood()
            score = 0
            isGameOver = false
            currentDirection = Direction.RIGHT
            nextDirection = Direction.RIGHT
            handler.removeCallbacks(updateRunnable)
            handler.post(updateRunnable)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw boundary
        canvas.drawRect(
            0f,
            0f,
            (numBlocksX * blockSize).toFloat(),
            (bottomLimitBlocks * blockSize).toFloat(),
            boundaryPaint
        )

        // Draw snake
        for (p in snake) {
            canvas.drawRect(
                (p.x * blockSize).toFloat(),
                (p.y * blockSize).toFloat(),
                ((p.x + 1) * blockSize).toFloat(),
                ((p.y + 1) * blockSize).toFloat(),
                snakePaint
            )
        }

        // Draw food
        canvas.drawRect(
            (food.x * blockSize).toFloat(),
            (food.y * blockSize).toFloat(),
            ((food.x + 1) * blockSize).toFloat(),
            ((food.y + 1) * blockSize).toFloat(),
            foodPaint
        )

        if (isGameOver) {
            canvas.drawText(
                "Game Over!",
                (width / 2).toFloat(),
                (height / 2).toFloat(),
                textPaint
            )
        }
    }

    private var touchStartX = 0f
    private var touchStartY = 0f
    private var touchThreshold = 100f // Minimum distance for swipe detection

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isGameOver) return false

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchStartX = event.x
                touchStartY = event.y
            }
            MotionEvent.ACTION_UP -> {
                val dx = event.x - touchStartX
                val dy = event.y - touchStartY

                // Only change direction if movement is significant enough
                if (kotlin.math.abs(dx) > touchThreshold || kotlin.math.abs(dy) > touchThreshold) {
                    nextDirection = if (kotlin.math.abs(dx) > kotlin.math.abs(dy)) {
                        if (dx > 0) Direction.RIGHT else Direction.LEFT
                    } else {
                        if (dy > 0) Direction.DOWN else Direction.UP
                    }
                }
            }
        }
        return true
    }


    private fun moveSnake() {
        if (snake.isEmpty()) return

        // Update direction if not reversing
        if (!isOppositeDirection(currentDirection, nextDirection)) currentDirection = nextDirection

        val head = snake.first()
        val newHead = Point(head.x, head.y)
        when (currentDirection) {
            Direction.UP -> newHead.y -= 1
            Direction.DOWN -> newHead.y += 1
            Direction.LEFT -> newHead.x -= 1
            Direction.RIGHT -> newHead.x += 1
        }

        // Check boundary collision with reserved bottom
        if (newHead.x < 0 || newHead.x >= numBlocksX || newHead.y < 0 || newHead.y >= bottomLimitBlocks) {
            gameOver()
            return
        }

        // Check self-collision
        if (snake.contains(newHead)) { gameOver(); return }

        snake.add(0, newHead)

        // Food collision
        if (newHead == food) {
            score++
            onScoreUpdate?.invoke(score)
            generateFood()
        } else {
            snake.removeLast()
        }
    }

    private fun isOppositeDirection(dir1: Direction, dir2: Direction): Boolean {
        return (dir1 == Direction.UP && dir2 == Direction.DOWN) ||
                (dir1 == Direction.DOWN && dir2 == Direction.UP) ||
                (dir1 == Direction.LEFT && dir2 == Direction.RIGHT) ||
                (dir1 == Direction.RIGHT && dir2 == Direction.LEFT)
    }

    private fun generateFood() {
        val rand = Random(System.currentTimeMillis())
        val minDistanceFromBoundary = 2
        val minDistanceFromSnake = 3 // Minimum blocks away from snake body
        var newFood: Point
        var attempts = 0
        val maxAttempts = 100 // Prevent infinite loop

        do {
            // Generate food position with 2-block buffer from boundaries
            val xRange = minDistanceFromBoundary until (numBlocksX - minDistanceFromBoundary)
            val yRange = minDistanceFromBoundary until (bottomLimitBlocks - minDistanceFromBoundary)

            // Ensure we have valid ranges
            if (xRange.isEmpty() || yRange.isEmpty()) {
                // Fallback to center if boundaries are too restrictive
                newFood = Point(numBlocksX / 2, bottomLimitBlocks / 2)
                break
            }

            newFood = Point(
                xRange.random(rand),
                yRange.random(rand)
            )

            attempts++
        } while ((snake.contains(newFood) || isTooCloseToSnake(newFood, minDistanceFromSnake)) && attempts < maxAttempts)

        food = newFood
    }

    private fun isTooCloseToSnake(foodPos: Point, minDistance: Int): Boolean {
        for (segment in snake) {
            val distance = kotlin.math.abs(foodPos.x - segment.x) + kotlin.math.abs(foodPos.y - segment.y)
            if (distance < minDistance) {
                return true
            }
        }
        return false
    }

    private fun gameOver() {
        if (!isGameOver) {
            isGameOver = true
            handler.removeCallbacks(updateRunnable)
            onGameOver?.invoke(score)
            invalidate()
        }
    }

    fun quitGame() { if (!isGameOver) gameOver() }
    fun currentScore(): Int = score

    enum class Direction { UP, DOWN, LEFT, RIGHT }
}

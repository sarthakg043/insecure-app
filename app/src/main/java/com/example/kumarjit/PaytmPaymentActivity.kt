// kotlin
package com.example.kumarjit

import android.Manifest
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.BounceInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

class PaytmPaymentActivity : AppCompatActivity() {

    private lateinit var llSuccessContainer: LinearLayout
    private lateinit var llCheckmarkContainer: LinearLayout
    private lateinit var tvCheckmark: TextView
    private lateinit var tvSuccessMessage: TextView
    private lateinit var tvSuccessSubtitle: TextView
    private lateinit var llPaymentDetails: LinearLayout
    private lateinit var tvAmount: TextView
    private lateinit var tvTransactionId: TextView
    private lateinit var tvDateTime: TextView
    private lateinit var tvReceiverName: TextView
    private lateinit var llShareReceipt: LinearLayout
    private lateinit var llDownloadReceipt: LinearLayout
    private lateinit var llDoneButton: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Hide the action bar to remove "SecureSPY" header for authentic Paytm look
        supportActionBar?.hide()

        setContentView(R.layout.paytm_payment_page)

        initializeViews()
        setupData()
        playSound()
        startPaytmAnimations()
        setupClickListeners()
    }

    private fun initializeViews() {
        llSuccessContainer = findViewById(R.id.ll_success_container)
        llCheckmarkContainer = findViewById(R.id.ll_checkmark_container)
        tvCheckmark = findViewById(R.id.tv_checkmark)
        tvSuccessMessage = findViewById(R.id.tv_success_message)
        tvSuccessSubtitle = findViewById(R.id.tv_success_subtitle)
        llPaymentDetails = findViewById(R.id.ll_payment_details)
        tvAmount = findViewById(R.id.tv_amount)
        tvTransactionId = findViewById(R.id.tv_transaction_id)
        tvDateTime = findViewById(R.id.tv_date_time)
        tvReceiverName = findViewById(R.id.tv_receiver_name)
        llShareReceipt = findViewById(R.id.ll_share_receipt)
        llDownloadReceipt = findViewById(R.id.ll_download_receipt)
        llDoneButton = findViewById(R.id.ll_done_button)
    }

    private fun setupData() {
        // Get data from intent or use defaults
        val amount = intent.getStringExtra("AMOUNT") ?: "1,250.00"
        val receiverName = intent.getStringExtra("RECEIVER_NAME") ?: "John Doe"

        // Generate random transaction ID
        val transactionId = "TXN" + (100000000..999999999).random()

        // Set current date and time
        val dateFormat = SimpleDateFormat("dd MMM yyyy, h:mm a", Locale.getDefault())
        val currentDateTime = dateFormat.format(Date())

        // Format amount properly - add commas if needed and ensure proper currency display
        val formattedAmount = if (amount.contains(",")) {
            amount // Already formatted
        } else {
            try {
                val numericAmount = amount.toDouble()
                String.format("%.2f", numericAmount)
            } catch (e: NumberFormatException) {
                amount
            }
        }

        tvAmount.text = "₹$formattedAmount"
        tvReceiverName.text = receiverName
        tvTransactionId.text = transactionId
        tvDateTime.text = currentDateTime
    }

    private fun startPaytmAnimations() {
        // Initially hide all elements
        llCheckmarkContainer.alpha = 0f
        llCheckmarkContainer.scaleX = 0f
        llCheckmarkContainer.scaleY = 0f
        tvSuccessMessage.alpha = 0f
        tvSuccessSubtitle.alpha = 0f
        llPaymentDetails.alpha = 0f
        llPaymentDetails.translationY = 100f
        llShareReceipt.alpha = 0f
        llDownloadReceipt.alpha = 0f
        llDoneButton.alpha = 0f

        // Start the animation sequence
        animateCheckmarkEntry()
    }

    private fun animateCheckmarkEntry() {
        Handler(Looper.getMainLooper()).postDelayed({
            // Checkmark circle animation - Paytm style bounce
            val scaleXAnimator = ObjectAnimator.ofFloat(llCheckmarkContainer, "scaleX", 0f, 1.2f, 1f)
            val scaleYAnimator = ObjectAnimator.ofFloat(llCheckmarkContainer, "scaleY", 0f, 1.2f, 1f)
            val alphaAnimator = ObjectAnimator.ofFloat(llCheckmarkContainer, "alpha", 0f, 1f)

            val checkmarkAnimatorSet = AnimatorSet()
            checkmarkAnimatorSet.playTogether(scaleXAnimator, scaleYAnimator, alphaAnimator)
            checkmarkAnimatorSet.duration = 600
            checkmarkAnimatorSet.interpolator = BounceInterpolator()
            checkmarkAnimatorSet.start()

            // Animate checkmark text with slight delay
            Handler(Looper.getMainLooper()).postDelayed({
                animateCheckmarkText()
            }, 300)

        }, 500) // Initial delay like Paytm
    }

    private fun animateCheckmarkText() {
        val checkmarkBounce = ObjectAnimator.ofFloat(tvCheckmark, "scaleX", 0.5f, 1.3f, 1f)
        val checkmarkBounceY = ObjectAnimator.ofFloat(tvCheckmark, "scaleY", 0.5f, 1.3f, 1f)

        val checkmarkSet = AnimatorSet()
        checkmarkSet.playTogether(checkmarkBounce, checkmarkBounceY)
        checkmarkSet.duration = 400
        checkmarkSet.interpolator = OvershootInterpolator()
        checkmarkSet.start()

        Handler(Looper.getMainLooper()).postDelayed({
            animateSuccessMessage()
        }, 200)
    }

    private fun animateSuccessMessage() {
        tvSuccessMessage.translationY = 30f
        val messageAnimator = ObjectAnimator.ofFloat(tvSuccessMessage, "alpha", 0f, 1f)
        val messageTranslationAnimator = ObjectAnimator.ofFloat(tvSuccessMessage, "translationY", 30f, 0f)

        val messageSet = AnimatorSet()
        messageSet.playTogether(messageAnimator, messageTranslationAnimator)
        messageSet.duration = 400
        messageSet.interpolator = AccelerateDecelerateInterpolator()
        messageSet.start()

        Handler(Looper.getMainLooper()).postDelayed({
            animateSuccessSubtitle()
        }, 200)
    }

    private fun animateSuccessSubtitle() {
        tvSuccessSubtitle.translationY = 20f
        val subtitleAnimator = ObjectAnimator.ofFloat(tvSuccessSubtitle, "alpha", 0f, 1f)
        val subtitleTranslationAnimator = ObjectAnimator.ofFloat(tvSuccessSubtitle, "translationY", 20f, 0f)

        val subtitleSet = AnimatorSet()
        subtitleSet.playTogether(subtitleAnimator, subtitleTranslationAnimator)
        subtitleSet.duration = 400
        subtitleSet.interpolator = AccelerateDecelerateInterpolator()
        subtitleSet.start()

        Handler(Looper.getMainLooper()).postDelayed({
            animatePaymentDetails()
        }, 300)
    }

    private fun animatePaymentDetails() {
        val detailsAlphaAnimator = ObjectAnimator.ofFloat(llPaymentDetails, "alpha", 0f, 1f)
        val detailsTranslationAnimator = ObjectAnimator.ofFloat(llPaymentDetails, "translationY", 100f, 0f)

        val detailsSet = AnimatorSet()
        detailsSet.playTogether(detailsAlphaAnimator, detailsTranslationAnimator)
        detailsSet.duration = 500
        detailsSet.interpolator = AccelerateDecelerateInterpolator()
        detailsSet.start()

        Handler(Looper.getMainLooper()).postDelayed({
            animateActionButtons()
        }, 200)
    }

    private fun animateActionButtons() {
        val shareAlphaAnimator = ObjectAnimator.ofFloat(llShareReceipt, "alpha", 0f, 1f)
        val shareScaleXAnimator = ObjectAnimator.ofFloat(llShareReceipt, "scaleX", 0.8f, 1f)
        val shareScaleYAnimator = ObjectAnimator.ofFloat(llShareReceipt, "scaleY", 0.8f, 1f)

        val shareSet = AnimatorSet()
        shareSet.playTogether(shareAlphaAnimator, shareScaleXAnimator, shareScaleYAnimator)
        shareSet.duration = 400
        shareSet.interpolator = OvershootInterpolator()
        shareSet.start()

        Handler(Looper.getMainLooper()).postDelayed({
            val downloadAlphaAnimator = ObjectAnimator.ofFloat(llDownloadReceipt, "alpha", 0f, 1f)
            val downloadScaleXAnimator = ObjectAnimator.ofFloat(llDownloadReceipt, "scaleX", 0.8f, 1f)
            val downloadScaleYAnimator = ObjectAnimator.ofFloat(llDownloadReceipt, "scaleY", 0.8f, 1f)

            val downloadSet = AnimatorSet()
            downloadSet.playTogether(downloadAlphaAnimator, downloadScaleXAnimator, downloadScaleYAnimator)
            downloadSet.duration = 400
            downloadSet.interpolator = OvershootInterpolator()
            downloadSet.start()
        }, 100)

        Handler(Looper.getMainLooper()).postDelayed({
            val doneAnimator = ObjectAnimator.ofFloat(llDoneButton, "alpha", 0f, 1f)
            doneAnimator.duration = 400
            doneAnimator.start()
        }, 300)
    }

    private fun setupClickListeners() {
        llShareReceipt.setOnClickListener {
            animateButtonClick(llShareReceipt) {
                shareReceipt()
            }
        }

        llDownloadReceipt.setOnClickListener {
            animateButtonClick(llDownloadReceipt) {
                downloadReceipt()
            }
        }

        llDoneButton.setOnClickListener {
            animateButtonClick(llDoneButton) {
                finish()
            }
        }

        findViewById<View>(R.id.iv_back_arrow).setOnClickListener {
            finish()
        }
    }

    private fun animateButtonClick(button: View, action: () -> Unit) {
        val scaleDownX = ObjectAnimator.ofFloat(button, "scaleX", 1f, 0.95f)
        val scaleDownY = ObjectAnimator.ofFloat(button, "scaleY", 1f, 0.95f)
        val scaleUpX = ObjectAnimator.ofFloat(button, "scaleX", 0.95f, 1f)
        val scaleUpY = ObjectAnimator.ofFloat(button, "scaleY", 0.95f, 1f)

        val scaleDown = AnimatorSet()
        scaleDown.playTogether(scaleDownX, scaleDownY)
        scaleDown.duration = 100

        val scaleUp = AnimatorSet()
        scaleUp.playTogether(scaleUpX, scaleUpY)
        scaleUp.duration = 100

        scaleDown.start()
        scaleDown.addListener(object : android.animation.Animator.AnimatorListener {
            override fun onAnimationStart(animation: android.animation.Animator) {}
            override fun onAnimationEnd(animation: android.animation.Animator) {
                scaleUp.start()
                action()
            }
            override fun onAnimationCancel(animation: android.animation.Animator) {}
            override fun onAnimationRepeat(animation: android.animation.Animator) {}
        })
    }

    private fun shareReceipt() {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Payment Receipt\nAmount: ${tvAmount.text}\nTransaction ID: ${tvTransactionId.text}")
        startActivity(Intent.createChooser(shareIntent, "Share Receipt"))
    }

    private fun downloadReceipt() {
        // For Android 10+ (API 29+), we don't need WRITE_EXTERNAL_STORAGE for MediaStore
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ uses scoped storage, no permission needed for MediaStore
            performReceiptDownload()
        } else {
            // Android 9 and below need WRITE_EXTERNAL_STORAGE permission
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                // Request permission for older Android versions
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    STORAGE_PERMISSION_REQUEST_CODE
                )
            } else {
                // Permission already granted, proceed with download
                performReceiptDownload()
            }
        }
    }

    private fun performReceiptDownload() {
        try {
            // Get the root view of the entire activity to capture full screen
            val rootView = window.decorView.rootView

            // Ensure the view is properly measured and laid out
            if (rootView.width == 0 || rootView.height == 0) {
                rootView.measure(
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                )
                rootView.layout(0, 0, rootView.measuredWidth, rootView.measuredHeight)
            }

            // Create bitmap from the entire screen view with proper dimensions
            val width = if (rootView.width > 0) rootView.width else rootView.measuredWidth
            val height = if (rootView.height > 0) rootView.height else rootView.measuredHeight

            if (width <= 0 || height <= 0) {
                Toast.makeText(this, "Unable to capture receipt - invalid dimensions", Toast.LENGTH_SHORT).show()
                return
            }

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            // Set white background for the bitmap
            canvas.drawColor(android.graphics.Color.WHITE)
            rootView.draw(canvas)

            // Save bitmap to gallery
            var success = false
            val timestamp = System.currentTimeMillis()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Use MediaStore API for Android 10 and above
                val contentValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, "PaymentReceipt_$timestamp.png")
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Receipts")
                }

                val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                uri?.let { imageUri ->
                    contentResolver.openOutputStream(imageUri)?.use { outputStream ->
                        success = bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    }
                }
            } else {
                // Use legacy method for Android 9 and below
                val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val receiptsDir = File(picturesDir, "Receipts")
                if (!receiptsDir.exists()) {
                    receiptsDir.mkdirs()
                }

                val imageFile = File(receiptsDir, "PaymentReceipt_$timestamp.png")
                FileOutputStream(imageFile).use { outputStream ->
                    success = bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                }

                if (success) {
                    // Notify gallery about the new file
                    @Suppress("DEPRECATION")
                    sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(imageFile)))
                }
            }

            if (success) {
                Toast.makeText(this, "Full receipt screenshot saved to Pictures/Receipts folder!", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Failed to save receipt - compression failed", Toast.LENGTH_SHORT).show()
            }

            // Clean up
            bitmap.recycle()

        } catch (e: Exception) {
            Toast.makeText(this, "Error saving receipt: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    private fun playSound() {
        // play a success sound from res/raw/success.mp3
        val mediaPlayer: MediaPlayer? = try {
            MediaPlayer.create(this, R.raw.phonepe_audio)
        } catch (e: Exception) {
            null
        }
        mediaPlayer?.start()
        mediaPlayer?.setOnCompletionListener { mp -> mp.release() }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            STORAGE_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, proceed with download
                    performReceiptDownload()
                } else {
                    // Permission denied, show message
                    Toast.makeText(this, "Storage permission is required to download receipt", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    companion object {
        private const val STORAGE_PERMISSION_REQUEST_CODE = 1001
    }
}

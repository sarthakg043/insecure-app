package com.example.kumarjit

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class FakePaymentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fake_payment_form)

        val editAccountName = findViewById<EditText>(R.id.edit_account_name2)
        val editAmount = findViewById<EditText>(R.id.editTextNumberDecimal)
        val buttonGeneratePaytm = findViewById<Button>(R.id.button_generate_paytm)

        buttonGeneratePaytm.setOnClickListener {
            val accountName = editAccountName.text.toString().trim()
            val amount = editAmount.text.toString().trim()

            if (accountName.isNotEmpty() && amount.isNotEmpty()) {
                // Navigate to Paytm payment confirmation page
                val intent = Intent(this, PaytmPaymentActivity::class.java)
                intent.putExtra("AMOUNT", amount)
                intent.putExtra("RECEIVER_NAME", accountName)
                startActivity(intent)
                finish() // Close the payment form
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

package com.example.test

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class PaymentActivity : AppCompatActivity() {

    private val db = Firebase.firestore
    private var transactionId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_payment)
        supportActionBar?.setTitle("Pembayaran")
        transactionId = intent.getStringExtra("TRANSACTION_ID")
        val amount = intent.getLongExtra("AMOUNT", 0)

        val tvAmount = findViewById<TextView>(R.id.tvPaymentAmount)
        val btnConfirm = findViewById<Button>(R.id.btnConfirmPayment)
        val progressBar = findViewById<ProgressBar>(R.id.paymentProgressBar)

        tvAmount.text = "Total Pembayaran:\nRp $amount"

        if (transactionId == null) {
            Toast.makeText(this, "ID Transaksi tidak valid", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        btnConfirm.setOnClickListener {
            btnConfirm.visibility = View.GONE
            progressBar.visibility = View.VISIBLE

            db.collection("transactions").document(transactionId!!)
                .update("status", "waiting_approval")
                .addOnSuccessListener {
                    progressBar.visibility = View.GONE
                    showSuccessDialog()
                }
                .addOnFailureListener {
                    progressBar.visibility = View.GONE
                    btnConfirm.visibility = View.VISIBLE
                    Toast.makeText(this, "Gagal konfirmasi: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun showSuccessDialog() {
        AlertDialog.Builder(this)
            .setTitle("Konfirmasi Terkirim")
            .setMessage("Pembayaran Anda sedang diverifikasi oleh Admin. Course akan muncul di 'My Courses' setelah disetujui.")
            .setCancelable(false)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .show()
    }
}
package com.example.test

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class VerificationFragment : Fragment(R.layout.fragment_verification) {

    private val db = Firebase.firestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: VerificationAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView
    private val transactionList = mutableListOf<Transaction>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.rvVerification)
        progressBar = view.findViewById(R.id.verificationProgressBar)
        tvEmpty = view.findViewById(R.id.tvEmptyVerification)

        adapter = VerificationAdapter(transactionList) { transaction ->
            approveTransaction(transaction)
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        loadPendingTransactions()
    }

    private fun loadPendingTransactions() {
        progressBar.visibility = View.VISIBLE
        tvEmpty.visibility = View.GONE
        recyclerView.visibility = View.GONE

        db.collection("transactions")
            .whereEqualTo("status", "waiting_approval")
            .get()
            .addOnSuccessListener { result ->
                progressBar.visibility = View.GONE
                transactionList.clear()
                for (doc in result) {
                    val transaction = doc.toObject(Transaction::class.java)
                    transaction.id = doc.id
                    transactionList.add(transaction)
                }

                adapter.notifyDataSetChanged()

                if (transactionList.isEmpty()) {
                    tvEmpty.visibility = View.VISIBLE
                } else {
                    recyclerView.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Gagal memuat data: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun approveTransaction(transaction: Transaction) {
        val transRef = db.collection("transactions").document(transaction.id)
        val userRef = db.collection("users").document(transaction.userId)

        db.runBatch { batch ->
            batch.update(transRef, "status", "success")

            batch.update(userRef, "enrolledCourses", FieldValue.arrayUnion(transaction.courseId))
        }
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Pembayaran ${transaction.userName} disetujui!", Toast.LENGTH_SHORT).show()

                val index = transactionList.indexOf(transaction)
                if (index != -1) {
                    transactionList.removeAt(index)
                    adapter.notifyItemRemoved(index)
                    if (transactionList.isEmpty()) {
                        tvEmpty.visibility = View.VISIBLE
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Gagal menyetujui: ${it.message}", Toast.LENGTH_SHORT).show()
                loadPendingTransactions()
            }
    }
}
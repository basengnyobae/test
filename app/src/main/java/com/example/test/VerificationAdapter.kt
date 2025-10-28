package com.example.test

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class VerificationAdapter(
    private val transactions: List<Transaction>,
    private val onApproveClicked: (Transaction) -> Unit
) : RecyclerView.Adapter<VerificationAdapter.VerificationViewHolder>() {

    class VerificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCourse: TextView = itemView.findViewById(R.id.tvVerificationCourse)
        val tvUser: TextView = itemView.findViewById(R.id.tvVerificationUser)
        val btnApprove: Button = itemView.findViewById(R.id.btnApprovePayment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_verification, parent, false)
        return VerificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: VerificationViewHolder, position: Int) {
        val transaction = transactions[position]

        holder.tvCourse.text = "Course: ${transaction.courseName}"
        holder.tvUser.text = "User: ${transaction.userName} (Rp ${transaction.amount})"

        holder.btnApprove.setOnClickListener {
            holder.btnApprove.isEnabled = false
            holder.btnApprove.text = "Memproses..."
            onApproveClicked(transaction)
        }
    }

    override fun getItemCount() = transactions.size
}
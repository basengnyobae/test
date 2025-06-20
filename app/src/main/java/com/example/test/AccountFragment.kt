package com.example.test

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth

class AccountFragment : Fragment(R.layout.fragment_account) {
    private lateinit var auth: FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        auth = FirebaseAuth.getInstance()

        val btnLogin = view.findViewById<Button>(R.id.btnLogin)
        val btnLogout = view.findViewById<Button>(R.id.btnLogout)

        if (auth.currentUser == null) {
            btnLogin.visibility = View.VISIBLE
            btnLogout.visibility = View.GONE
        } else {
            btnLogin.visibility = View.GONE
            btnLogout.visibility = View.VISIBLE
        }

        btnLogin.setOnClickListener {
            startActivity(Intent(requireContext(), LoginActivity::class.java))
        }

        btnLogout.setOnClickListener {
            auth.signOut()
            Toast.makeText(requireContext(), "Logout berhasil", Toast.LENGTH_SHORT).show()
            requireActivity().recreate()
        }
    }
}



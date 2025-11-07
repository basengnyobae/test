package com.example.test

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import de.hdodenhof.circleimageview.CircleImageView

class AccountFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore
    private val storage = Firebase.storage

    private lateinit var ivProfilePicture: CircleImageView
    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var btnSettings: Button
    private lateinit var btnLogout: Button

    private var currentUserId: String? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri: Uri? = result.data?.data
            imageUri?.let { uploadProfilePicture(it) }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_account, container, false)

        auth = FirebaseAuth.getInstance()
        currentUserId = auth.currentUser?.uid

        ivProfilePicture = view.findViewById(R.id.ivProfilePicture)
        tvUserName = view.findViewById(R.id.tvUserName)
        tvUserEmail = view.findViewById(R.id.tvUserEmail)
        btnSettings = view.findViewById(R.id.btnSettings)
        btnLogout = view.findViewById(R.id.btnLogout)

        if (currentUserId == null) {
            goToLogin()
            return null
        }

        loadUserProfile()

        ivProfilePicture.setOnClickListener {
            showImagePickerDialog()
        }

        btnSettings.setOnClickListener {
            startActivity(Intent(requireContext(), SettingsActivity::class.java))
        }

        btnLogout.setOnClickListener {
            logoutUser()
        }

        return view
    }

    private fun loadUserProfile() {
        currentUserId?.let { uid ->
            db.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val name = document.getString("name") ?: "Nama Tidak Ditemukan"
                        val email = document.getString("email") ?: "Email Tidak Ditemukan"
                        val photoUrl = document.getString("photoUrl")

                        tvUserName.text = name
                        tvUserEmail.text = email

                        if (!photoUrl.isNullOrEmpty()) {
                            Glide.with(this)
                                .load(photoUrl)
                                .placeholder(R.drawable.ic_default_profile_placeholder)
                                .error(R.drawable.ic_default_profile_placeholder)
                                .into(ivProfilePicture)
                        } else {
                            ivProfilePicture.setImageResource(R.drawable.ic_default_profile_placeholder)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Gagal memuat profil: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun showImagePickerDialog() {
        val options = arrayOf<CharSequence>("Pilih dari Galeri", "Hapus Foto", "Batalkan")
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Ubah Foto Profil")
        builder.setItems(options) { dialog, item ->
            when (options[item]) {
                "Pilih dari Galeri" -> {
                    val galleryIntent = Intent(Intent.ACTION_PICK)
                    galleryIntent.type = "image/*"
                    pickImage.launch(galleryIntent)
                }
                "Hapus Foto" -> {
                    deleteProfilePicture()
                }
                "Batalkan" -> dialog.dismiss()
            }
        }
        builder.show()
    }

    private fun uploadProfilePicture(imageUri: Uri) {
        currentUserId?.let { uid ->
            val storageRef = storage.reference.child("profile_pictures/$uid.jpg")
            storageRef.putFile(imageUri)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        updateProfilePhotoUrl(uid, uri.toString())
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Gagal upload: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun updateProfilePhotoUrl(uid: String, photoUrl: String) {
        db.collection("users").document(uid)
            .update("photoUrl", photoUrl)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Foto profil diperbarui.", Toast.LENGTH_SHORT).show()
                Glide.with(this).load(photoUrl).into(ivProfilePicture)
            }
    }

    private fun deleteProfilePicture() {
        currentUserId?.let { uid ->
            db.collection("users").document(uid)
                .update("photoUrl", "")
                .addOnSuccessListener {
                    val storageRef = storage.reference.child("profile_pictures/$uid.jpg")
                    storageRef.delete().addOnCompleteListener {
                        Toast.makeText(requireContext(), "Foto profil dihapus.", Toast.LENGTH_SHORT).show()
                        ivProfilePicture.setImageResource(R.drawable.ic_default_profile_placeholder)
                    }
                }
        }
    }

    private fun logoutUser() {
        auth.signOut()
        clearUserRoleFromPreferences()
        goToLogin()
    }

    private fun goToLogin() {
        if (activity == null) return
        val intent = Intent(activity, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        activity?.finish()
    }

    private fun clearUserRoleFromPreferences() {
        val sharedPref = activity?.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE) ?: return
        with (sharedPref.edit()) {
            remove("USER_ROLE")
            apply()
        }
    }
}
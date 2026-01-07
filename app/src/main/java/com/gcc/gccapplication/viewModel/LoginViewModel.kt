package com.gcc.gccapplication.viewModel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.gcc.gccapplication.data.local.UserPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class LoginViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun login(
        context: Context,
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        // Login with the raw password (no manual hashing)
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        // Ambil UID pengguna
                        val uid = user.uid

                        // Ambil data role dari Firestore berdasarkan UID
                        firestore.collection("users").document(uid).get()
                            .addOnSuccessListener { document ->
                                if (document.exists()) {
                                    val role = document.getString("role") ?: "user"
                                    val fullName = document.getString("name")
                                    val address = document.getString("address")
                                    val noHp = document.getString("phone_number")
                                    val photo_url = document.getString("photo_url")

                                    // Simpan token dan data lainnya ke SharedPreferences
                                    val userPreferences = UserPreferences(context)
                                    userPreferences.saveRole(role)
                                    userPreferences.saveUid(uid)
                                    userPreferences.saveEmail(email)
                                    if (fullName != null && noHp != null && photo_url != null && address != null) {
                                        userPreferences.saveFullName(fullName)
                                        userPreferences.saveAddress(address)
                                        userPreferences.saveUrlProfile(photo_url)
                                        userPreferences.saveNoHp(noHp)
                                    }
//                                    if (noHp != null) {
//
//                                    }
//
//                                    if (photo_url != null) {
//
//                                    }
//                                    if (address != null) {
//
//                                    }


                                    onSuccess()
                                } else {
                                    onFailure("Role not found for user")
                                }
                            }
                            .addOnFailureListener { e ->
                                onFailure("Failed to retrieve role: ${e.message}")
                            }
                    } else {
                        onFailure("User not found")
                    }
                } else {
                    onFailure("Login failed: ${task.exception?.message}")
                }
            }
    }

    fun saveNotificationToken(
        context: Context,
        email: String,
        tokenFcm: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val userPreferences = UserPreferences(context)
        val uid = auth.currentUser?.uid
        userPreferences.saveFCMtoken(tokenFcm)

        // Pastikan UID tidak null
        if (uid.isNullOrEmpty()) {
            onFailure("User is not authenticated.")
            return
        }

        val firestore = FirebaseFirestore.getInstance()
        val userRef = firestore.collection("tokenNotification").document(uid)

        // Cek apakah dokumen dengan UID sudah ada
        userRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    // Jika dokumen sudah ada, hanya update fcmTokens
                    val data = mapOf(
                        "tokensFcm" to FieldValue.arrayUnion(tokenFcm)
                    )

                    userRef.set(data, SetOptions.merge())
                        .addOnSuccessListener {
                            userPreferences.saveFCMtoken(tokenFcm)
                            Log.d("FCM", "Token FCM berhasil diperbarui di Firestore.")
                            onSuccess()
                        }
                        .addOnFailureListener { e ->
                            Log.w("FCM", "Gagal memperbarui token FCM di Firestore", e)
                            onFailure("Failed to save token data: ${e.message}")
                        }
                } else {
                    // Jika dokumen belum ada, buat dokumen baru dengan semua data
                    val tokenData = mapOf(
                        "uid" to uid,
                        "email" to email,
//                        "role" to userPreferences.getRole(),
                        "tokensFcm" to listOf(tokenFcm) // Inisialisasi fcmTokens sebagai list
                    )

                    userRef.set(tokenData, SetOptions.merge())
                        .addOnSuccessListener {
                            userPreferences.saveFCMtoken(tokenFcm)
                            Log.d("FCM", "Data pengguna berhasil disimpan di Firestore.")
                            onSuccess()
                        }
                        .addOnFailureListener { e ->
                            Log.w("FCM", "Gagal menyimpan data pengguna di Firestore", e)
                            onFailure("Failed to save token data: ${e.message}")
                        }
                }
            }

            .addOnFailureListener { e ->
                Log.w("FCM", "Gagal mengambil data dokumen di Firestore", e)
                onFailure("Failed to get document data: ${e.message}")
            }
    }


}

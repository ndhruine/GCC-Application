package com.gcc.gccapplication.ui.activity

import android.content.Intent
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.gcc.gccapplication.data.local.UserPreferences
import com.gcc.gccapplication.databinding.ActivityLoginBinding
import com.gcc.gccapplication.viewModel.LoginViewModel
import com.google.firebase.messaging.FirebaseMessaging
import com.gcc.gccapplication.service.MyFirebaseMessagingService

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val loginViewModel: LoginViewModel by viewModels()
    private lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPass.text.toString().trim()

            loginViewModel.login(
                context = this,
                email = email,
                password = password,
                onSuccess = {
                    userPreferences = UserPreferences(this)
                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()

                    // Tambahkan delay sebelum menjalankan intent
                    android.os.Handler(Looper.getMainLooper()).postDelayed({
                        val intent = Intent(this, PageActivity::class.java)
                        startActivity(intent)
                        handleLogin()
                        finish()
                    }, 2000) // Delay 2000 ms (2 detik)
                },
                onFailure = { errorMessage ->
                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                    Toast.makeText(this, "password salah", Toast.LENGTH_SHORT).show()
                }
            )
        }


        binding.tvAkun.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.tvPassword.setOnClickListener{
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun handleLogin() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Fetching FCM token failed", task.exception)
                return@addOnCompleteListener
            }

            // Dapatkan token FCM
            val token = task.result
            Log.d("FCM", "Token FCM didapatkan: $token")


            val email = userPreferences.getEmail()

            // Pastikan email tidak null
            if (email != null) {
                loginViewModel.saveNotificationToken(
                    this,
                    email,
                    token,
                    onSuccess = {
                        Log.d("FCM", "Token FCM berhasil dikirim ke server backend.")
                    },
                    onFailure = {
                        Log.w("FCM", "Gagal mengirim token FCM ke server backend")
                    }
                )
            } else {
                Log.w("FCM", "Email tidak ditemukan, token tidak disimpan.")
            }
        }
    }



    private fun sendTokenToServer(token: String) {
        // Implementasi untuk mengirim token baru ke server Anda
    }


}




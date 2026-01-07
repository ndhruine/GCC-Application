package com.gcc.gccapplication.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.gcc.gccapplication.BuildConfig
import androidx.core.app.NotificationCompat
import com.gcc.gccapplication.R
import com.gcc.gccapplication.data.API.ApiService
import com.gcc.gccapplication.data.local.UserPreferences
import com.gcc.gccapplication.data.model.NotificationRequest
import com.gcc.gccapplication.ui.activity.NotificationActivity
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class MyFirebaseMessagingService : FirebaseMessagingService() {
    private lateinit var userPreferences: UserPreferences
    private lateinit var apiService: ApiService
    private val sendNotifApi: Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL) // Gabisa makek http, bisa nya https akalin nya makek ngrok
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    companion object {
        const val CHANNEL_ID = "notification_channel"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        userPreferences = UserPreferences(this)
//        userPreferences.saveFCMtoken(token)
        sendTokenToServer(token)
    }

    private fun sendTokenToServer(token: String) {
        val email = userPreferences.getEmail()
        val uid = userPreferences.getUid()  // Pastikan Anda mendapatkan UID dari UserPreferences
//        val tokenFcm = userPreferences.getFCMtoken()
        val db = FirebaseFirestore.getInstance()

        // Misalnya, kita memperbarui token dalam dokumen pengguna di koleksi "tokenNotification"

        if(uid.isNullOrEmpty()){
            Log.e("FCM", "UID tidak ditemukan dalam UserPreferences.")
            return
        }

//        val data = mapOf(
//            "uid" to uid,
//            "email" to email,
//            "tokensFcm" to FieldValue.arrayUnion(token)
//        )

        val userRef = db.collection("tokenNotification").document(uid.toString())

        userRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    // Jika dokumen sudah ada, hanya update fcmTokens
                    val data = mapOf(
                        "tokensFcm" to FieldValue.arrayUnion(token)
                    )

                    userRef.set(data, SetOptions.merge())
                        .addOnSuccessListener {
                            userPreferences.saveFCMtoken(token)
                            Log.d("FCM", "Token FCM berhasil diperbarui di Firestore.")

                        }
                        .addOnFailureListener { e ->
                            Log.w("FCM", "Gagal memperbarui token FCM di Firestore", e)

                        }
                } else {
                    // Jika dokumen belum ada, buat dokumen baru dengan semua data
                    val tokenData = mapOf(
                        "uid" to uid,
                        "email" to email,
//                        "role" to userPreferences.getRole(),
                        "tokensFcm" to listOf(token) // Inisialisasi fcmTokens sebagai list
                    )

                    userRef.set(tokenData, SetOptions.merge())
                        .addOnSuccessListener {
                            userPreferences.saveFCMtoken(token)
                            Log.d("FCM", "Data pengguna berhasil disimpan di Firestore.")
                        }
                        .addOnFailureListener { e ->
                            Log.w("FCM", "Gagal menyimpan data pengguna di Firestore", e)
                        }
                }
            }

//        userRef
//            .addOnSuccessListener {
//                userPreferences.saveFCMtoken(token)
//                Log.d("FCM", "Token FCM berhasil diperbarui di Firestore.")
//            }
//            .addOnFailureListener { e ->
//                Log.w("FCM", "Gagal memperbarui token FCM di Firestore", e)
//            }

//        firestore.collection("tokenNotification").document(uid).set(tokenData, SetOptions.merge())
//            .addOnSuccessListener {
//                // Simpan token di UserPreferences
//                userPreferences.saveFCMtoken(tokenFcm)
//                onSuccess()
//            }
//            .addOnFailureListener { e ->
//                onFailure("Failed to save token data: ${e.message}")
//            }
    }


    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        userPreferences = UserPreferences(this)
        userPreferences.setHasNewNotif(true)

        remoteMessage.notification?.let {
            Log.d("FCM__Pesan", it.title.toString())
            Log.d("FCM__Pesan", it.body.toString())
            startNotification(it.title, it.body)
        }
    }



    private fun startNotification(title: String?, message: String?) {
        // Buat intent untuk membuka NotificationActivity
        val intent = Intent(this, NotificationActivity::class.java)

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Buat notifikasi
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification) // Ganti dengan icon notifikasi Anda
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // Kirim notifikasi
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0, notificationBuilder.build())
    }

    fun sendNotification(address :String,title: String, body: String) {
        apiService = sendNotifApi.create(ApiService::class.java)
        val notificationRequest = NotificationRequest(address,title, body)
        apiService.sendNotification(notificationRequest).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    // Notifikasi berhasil dikirim
                    Log.d("Notification", "Notification sent successfully")
                } else {
                    // Tangani error
                    Log.e("Notification", "Failed to send notification bla bla: ${ response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                // Tangani kegagalan
                Log.e("Notification", "Failed to send notification: ${t.message}")
            }
        })
    }
}

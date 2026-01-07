package com.gcc.gccapplication.data.local

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

class UserPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    companion object {
        private const val PREFS_NAME = "userPreferences"
        private const val TOKEN_KEY = "token"
        private const val EMAIL_KEY = "email"
        private const val NAME_KEY = "fullName"
        private const val NoHp_KEY = "nomor handphone"
        private const val ROLE_KEY = "role"  // Tambahkan konstanta untuk role
        private const val ADDRESS_KEY = "address"
        private const val UID_KEY = "uid"
        private const val FCM_TOKEN = "fcm_token"
        private const val URL_PROFILE = "url_profile"
        private const val KEY_LAST_TIMESTAMP = "last_timestamp"
        private const val NEW_NOTIF = "has_new_notif"
    }

    fun saveToken(token: String) {
        with(prefs.edit()) {
            putString(TOKEN_KEY, token)
            apply()
        }
    }

    fun saveUrlProfile(url: String) {
        with(prefs.edit()) {
            putString(URL_PROFILE, url)
            apply()
        }
    }

    fun getUrlProfile(): String? {
        return prefs.getString(URL_PROFILE, null)
    }

    fun getToken(): String? {
        return prefs.getString(TOKEN_KEY, null)
    }

//    fun saveLastTimestamp(context: Context, timestamp: Long) {
//        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
//        val editor = sharedPreferences.edit()
//        editor.putLong(KEY_LAST_TIMESTAMP, timestamp)
//        editor.apply()
//    }

    // Menyimpan timestamp terakhir
    fun saveLastTimestamp(timestamp: Long) {
        with(prefs.edit()) {
            putLong(KEY_LAST_TIMESTAMP, timestamp)
            apply()
        }
    }

    // Mengambil timestamp terakhir
    fun getLastTimestamp(): Long {
        return prefs.getLong(KEY_LAST_TIMESTAMP, 0L)
    }

    fun saveFCMtoken(token: String) {
        with(prefs.edit()) {
            putString(TOKEN_KEY, token)
            apply()
        }
    }



    fun setHasNewNotif(hasNewNotif: Boolean) {
        with(prefs.edit()) {
            putBoolean(NEW_NOTIF, hasNewNotif)
            apply()
        }
    }

    fun getHasNewNotif(): Boolean {
        return prefs.getBoolean(NEW_NOTIF, false)
    }

    fun getFCMtoken(): String? {
        return prefs.getString(FCM_TOKEN, null)
    }



    fun saveEmail(email: String) {
        with(prefs.edit()) {
            putString(EMAIL_KEY, email)
            apply()
        }
    }



    fun getEmail(): String? {
        return prefs.getString(EMAIL_KEY, null)
    }

    fun saveNoHp(nomor: String) {
        with(prefs.edit()) {
            putString(NoHp_KEY, nomor)
            apply()
        }
    }



    fun getNoHp(): String? {
        return prefs.getString(NoHp_KEY, null)
    }

    fun saveFullName(fullName: String) {
        with(prefs.edit()) {
            putString(NAME_KEY, fullName)
            apply()
        }
    }

    fun getFullName(): String? {
        return prefs.getString(NAME_KEY, "Unknown")
    }

    // Tambahkan metode untuk menyimpan dan mengambil role
    fun saveRole(role: String) {
        with(prefs.edit()) {
            putString(ROLE_KEY, role)
            apply()
        }
    }

    fun saveUid(uid: String){
        with(prefs.edit()){
            putString(UID_KEY, uid)
            apply()
        }
    }

    fun firebaseCurrrentUser(): FirebaseUser? {
        return FirebaseAuth.getInstance().currentUser
    }

    fun firebaseSignOut(){
        FirebaseAuth.getInstance().signOut()
        FirebaseMessaging.getInstance().deleteToken()
    }

    fun getRole(): String? {
        return prefs.getString(ROLE_KEY, null)
    }

    fun saveAddress(address: String) {
        with(prefs.edit()) {
            putString(ADDRESS_KEY, address)
            apply()
        }
    }

    fun getAddress(): String? {
        return prefs.getString(ADDRESS_KEY, null)
    }

    fun clear() {
        with(prefs.edit()) {
            clear()
            apply()
        }
        // Debugging line to check if preferences are actually cleared
        Log.d("Preferences", "All preferences cleared: ${prefs.all}")
    }


    fun getUid(): String? {
        return prefs.getString(UID_KEY, null)
    }

    // Fungsi untuk mendapatkan semua data user dalam satu objek User
    fun getUserData(): User? {
        val email = getEmail()
        val fullName = getFullName()
        val role = getRole()
        val address = getAddress()
        val nomor = getNoHp()
        val uid = getUid().toString()

        return if (email != null && fullName != null && role != null && address != null) {
            User(email, fullName, role, address,nomor, uid)
        } else {
            null
        }
    }

    fun clearFCMToken(
        uid: String?,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        if (uid.isNullOrEmpty()) {
            Log.d("UID", "User is not authenticated.")
            return
        }

        val db = FirebaseFirestore.getInstance()
        val tokenRef = db.collection("tokenNotification").document(uid)
        val tokenFcm = getFCMtoken()

        tokenRef.update("tokensFcm", FieldValue.arrayRemove(tokenFcm))
            .addOnSuccessListener {
                // Token FCM berhasil dihapus dari Firestore
                Log.d("FCM", "Token FCM berhasil dihapus dari Firestore.")
                onSuccess() // Panggil lambda onSuccess
            }
            .addOnFailureListener { exception ->
                // Gagal menghapus token FCM dari Firestore
                Log.w("FCM", "Gagal menghapus token FCM dari Firestore $uid", exception)
                onFailure(exception) // Panggil lambda onFailure
            }
    }


}



data class User(
    val email: String,
    val fullName: String,
    val role: String,
    val address: String,
    val nomor: String? = null,
    val Uid: String
)

package com.gcc.gccapplication.ui.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.gcc.gccapplication.R
import com.gcc.gccapplication.data.local.UserPreferences
import com.gcc.gccapplication.databinding.ActivityEditProfileBinding
import com.gcc.gccapplication.databinding.DialogAddAddressBinding
import com.gcc.gccapplication.viewModel.EditProfileVIewModel
import com.google.firebase.auth.userProfileChangeRequest
import com.yalantis.ucrop.UCrop
import java.io.File

class EditProfileActivity : AppCompatActivity() {
    //connect ke xml
    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var customTitle: TextView
    private lateinit var userPreferences: UserPreferences
    private val viewModel: EditProfileVIewModel by viewModels()
    private var currentImageUri: Uri?=null


    @SuppressLint("InflateParams", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userPreferences  = UserPreferences(this)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val URL = userPreferences.getUrlProfile()
        if (URL != null) {
            Log.d("URL", URL)
            Glide.with(this)
                .load(URL) // urlPhoto
                .placeholder(R.drawable.img_dummy_image)
                .into(binding.ivProfilePicture)
        }



        val fullName = userPreferences.getFullName() ?: "-"
        val nomor = userPreferences.getNoHp() ?: "-"
        val alamat = userPreferences.getAddress() ?: "-"
        binding.tvNama.text = fullName
        binding.tvNomor.text = nomor
        binding.tvAlamat.text = alamat

        // Inflate and set the custom title view
        val customView = layoutInflater.inflate(R.layout.actionbar_title, null)
        customTitle = customView.findViewById(R.id.custom_title)
        supportActionBar?.setDisplayShowCustomEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.customView = customView
        customTitle.text = "Ubah Profile"

        val email= userPreferences.getEmail()

        binding.ivProfilePicture.setOnClickListener{startGallery()}

        binding.btnEditNama.setOnClickListener{
            showDialogName(email.toString())
            setResult(Activity.RESULT_OK)
        }

        binding.btnEditNoHp.setOnClickListener{
            showDialogNoHp(email.toString())
        }

        binding.btnEditAlamat.setOnClickListener{
            showDialogAddress(email.toString())
        }





    }

    override fun onResume() {
        super.onResume()
        updateProfile() // Memperbarui profil setiap kali fragment kembali muncul
    }

    private fun updateProfile() {
        val url = userPreferences.getUrlProfile()
        Glide.with(this)
            .load(url)
            .placeholder(R.drawable.blank_profile)
            .into(binding.ivProfilePicture)
    }


    private fun startGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, "Choose a Picture")
        launcherIntentGallery.launch(chooser)
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedImg = result.data?.data
            selectedImg?.let { uri ->
                currentImageUri = uri
                startUCrop(uri)
            }
        }
    }

    private fun startUCrop(sourceUri: Uri) {
        val file = "cropped_image_${System.currentTimeMillis()}.jpg"
        val destinationUri = Uri.fromFile(File(cacheDir, file))
        UCrop.of(sourceUri, destinationUri)
            .withAspectRatio(1f, 1f)
            .withMaxResultSize(1000, 1000)
            .start(this)
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val email = userPreferences.getEmail().toString()
        if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK) {
            val resultUri = UCrop.getOutput(data!!)
            resultUri?.let {
                userPreferences.saveUrlProfile(it.toString())
                viewModel.saveProfilePhotoUrl(this,email,it.toString(),
                    onSuccess = {
                        updateProfile()
                        recreate()
                    }, onFailure = {
                        Toast.makeText(this, "Gagal Mengunggah Foto Profil", Toast.LENGTH_SHORT).show()
                })
                binding.ivProfilePicture.setImageURI(it)
                currentImageUri = it
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(data!!)
            Toast.makeText(this, "Crop error: ${cropError?.message}", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }


    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        setResult(Activity.RESULT_OK)
        finish()

        return super.onBackPressed()
    }

    private fun showDialogAddress(email: String) {
        val dialogBinding = DialogAddAddressBinding.inflate(LayoutInflater.from(this))
        dialogBinding.etAddress.setHint("Masukkan alamat baru anda")

        val dialog = AlertDialog.Builder(this)
            .setTitle("Ubah Alamat")
            .setView(dialogBinding.root)
            .setCancelable(false)
            .setPositiveButton("Konfirmasi") { _, _ ->
                val address = dialogBinding.etAddress.text.toString()
                if (address.isNotEmpty()) {
                    viewModel.saveAddressData(this, email, address,
                        onSuccess = {
                            // Address saved successfully
                            userPreferences.saveAddress(address)  // Save address to UserPreferences
                            Toast.makeText(this, "Alamat Berhasil Diubah", Toast.LENGTH_SHORT).show()
                            // Restart the activity
                            recreate()
                        },
                        onFailure = { exception ->
                            // Handle error
                            Toast.makeText(this, "Alamat Gagal Diubah", Toast.LENGTH_SHORT).show()
                        })
                } else {
                    Toast.makeText(this, "Masukkaan Alamat Baru Anda", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .create()

        dialog.show()
    }

    private fun showDialogName(email: String) {
        val dialogBinding = DialogAddAddressBinding.inflate(LayoutInflater.from(this))
        dialogBinding.etAddress.setHint("Masukkan Nama Pengguna Baru anda")

        val dialog = AlertDialog.Builder(this)
            .setTitle("Ubah Nama")
            .setView(dialogBinding.root)
            .setCancelable(false)
            .setPositiveButton("Konfirmasi") { _, _ ->
                val name = dialogBinding.etAddress.text.toString()
                if (name.isNotEmpty()) {
                    viewModel.saveUserNameData(this, email, name,
                        onSuccess = {
                            // Address saved successfully
                            userPreferences.saveFullName(name)  // Save userName to UserPreferences
                            Toast.makeText(this, "Username Berhasil Diubah", Toast.LENGTH_SHORT).show()
                            // Restart the activity
                            recreate()
                        },
                        onFailure = { exception ->
                            // Handle error
                            Toast.makeText(this, "Username Gagal Diubah", Toast.LENGTH_SHORT).show()
                        })
                } else {
                    Toast.makeText(this, "Masukkan Username Baru anda", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .create()

        dialog.show()
    }

    private fun showDialogNoHp(email: String) {
        val dialogBinding = DialogAddAddressBinding.inflate(LayoutInflater.from(this))
        dialogBinding.etAddress.setHint("Masukkan Handphone Baru anda")

        val dialog = AlertDialog.Builder(this)
            .setTitle("Ubah Nomor Handphone")
            .setView(dialogBinding.root)
            .setCancelable(false)
            .setPositiveButton("Konfirmasi") { _, _ ->
                val nomor = dialogBinding.etAddress.text.toString()
                if (nomor.isNotEmpty()) {
                    viewModel.savePhoneNumberData(this, email, nomor,
                        onSuccess = {
                            // Address saved successfully
                            userPreferences.saveNoHp(nomor)  // Save address to UserPreferences
                            Toast.makeText(this, "Nomor Berhasil Diubah", Toast.LENGTH_SHORT).show()
                            // Restart the activity
                            recreate()
                        },
                        onFailure = { exception ->
                            // Handle error
                            Toast.makeText(this, "Nomor Gagal Diubah", Toast.LENGTH_SHORT).show()
                        })
                } else {
                    Toast.makeText(this, "Masukkan Nomor Baru Anda", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .create()

        dialog.show()
    }

}
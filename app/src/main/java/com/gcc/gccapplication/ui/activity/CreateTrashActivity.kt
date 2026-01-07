package com.gcc.gccapplication.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.StyleSpan
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.gcc.gccapplication.R
import com.gcc.gccapplication.databinding.ActivityCreateTrashBinding
import com.gcc.gccapplication.viewModel.CreateTrashViewModel
import com.yalantis.ucrop.UCrop
import java.io.File

class CreateTrashActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateTrashBinding
    private var currentImageUri: Uri? = null
    private lateinit var customTitle: TextView
    private val viewModel: CreateTrashViewModel by viewModels()

    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateTrashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the Toolbar as the ActionBar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Inflate and set the custom title view
        val customView = layoutInflater.inflate(R.layout.actionbar_title, null)
        customTitle = customView.findViewById(R.id.custom_title)
        supportActionBar?.setDisplayShowCustomEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.customView = customView

        customTitle.text = "Tambah Data Sampah"

        binding.ivPhotoSampah.setOnClickListener { startGallery() }

       setSpinner()


        // Handle save button click
        binding.btnKonfirmasi.setOnClickListener { saveTrashData() }
    }

    private fun setSpinner() {
        // Set spinner adapter
        val spinner: Spinner = binding.spinnerTipe

        // Ambil array asli tanpa placeholder
        val tipeSampahArray = resources.getStringArray(R.array.array_tipe_sampah).toMutableList()

        // Tambahkan placeholder di awal array
        tipeSampahArray.add(0, "Pilih Tipe Sampah")

        // Buat adapter dengan array yang telah dimodifikasi
        val adapter = object : ArrayAdapter<String>(
            this,
            android.R.layout.simple_spinner_item,
            tipeSampahArray
        ) {
            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                // Jangan tampilkan placeholder di dropdown
                if (position == 0) {
                    view.visibility = View.GONE
                    view.layoutParams = ViewGroup.LayoutParams(0, 100)
                } else {
                    view.visibility = View.VISIBLE
                }
                return view
            }
        }

        // Set layout untuk dropdown
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        // Pilih default item pada spinner
        spinner.setSelection(0)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK) {
            val resultUri = UCrop.getOutput(data!!)
            resultUri?.let {
                binding.ivPhotoSampah.setImageURI(it)
                currentImageUri = it
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(data!!)
            Toast.makeText(this, "Crop error: ${cropError?.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveTrashData() {
        val trashName = binding.etTrashName.text.toString()
        val trashType = binding.spinnerTipe.selectedItem.toString()
        val trashDesc = binding.etDesc.text.toString()
        val trashAddress = binding.etAlamat.text.toString()

        val selectedTipe = binding.spinnerTipe.selectedItem.toString()

        if (trashName.isEmpty() || trashType.isEmpty() || trashDesc.isEmpty() || trashAddress.isEmpty() && selectedTipe == "Pilih Tipe Sampah") {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.saveTrashData(
            trashName,
            trashType,
            trashDesc,
            trashAddress,
            currentImageUri,
            onSuccess = {
                Toast.makeText(this, "Trash data saved successfully", Toast.LENGTH_SHORT).show()
                finish()
            },
            onFailure = { e ->
                Toast.makeText(this, "Failed to save trash data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}

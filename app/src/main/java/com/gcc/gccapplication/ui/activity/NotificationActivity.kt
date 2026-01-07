package com.gcc.gccapplication.ui.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gcc.gccapplication.R
import com.gcc.gccapplication.adapter.ItemNotifAdapter
import com.gcc.gccapplication.data.local.UserPreferences
import com.gcc.gccapplication.databinding.ActivityNotificationBinding
import com.gcc.gccapplication.viewModel.NotifikasiViewModel

class NotificationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationBinding
    private lateinit var customTitle: TextView
    private lateinit var rvNotifikasi: RecyclerView
    private lateinit var userPreferences: UserPreferences
    private val notifikasiViewModel: NotifikasiViewModel by viewModels()
    private lateinit var notifAdapter: ItemNotifAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userPreferences = UserPreferences(this)
        userPreferences.setHasNewNotif(false)
        notifikasiViewModel.setHasNewNotif(false)

        setupToolbar()
        setupRecyclerView()
        observeViewModel()
    }

    @SuppressLint("SetTextI18n", "InflateParams")
    private fun setupToolbar() {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowCustomEnabled(true)
            setDisplayShowTitleEnabled(true)
            customView = layoutInflater.inflate(R.layout.actionbar_title, null).apply {
                customTitle = findViewById(R.id.custom_title)
                customTitle.text = "Notifikasi"
            }
        }
    }

    private fun setupRecyclerView() {
        notifAdapter = ItemNotifAdapter(ArrayList(), notifikasiViewModel)
        rvNotifikasi = findViewById(R.id.rvNotifikasi)
        rvNotifikasi.apply {
            layoutManager = LinearLayoutManager(this@NotificationActivity)
            adapter = notifAdapter
        }
    }

    private var isDataEmpty = true
    @SuppressLint("NotifyDataSetChanged")
    private fun observeViewModel() {
        val role = userPreferences.getRole()
        if (role == "admin") {
            // Observe trash data from ViewModel
            notifikasiViewModel.fetchAllTrashData()
            notifikasiViewModel.notifikasiData.observe(this) { notifList ->
                isDataEmpty = notifList.isEmpty()
                if (isDataEmpty) {
                    Toast.makeText(this, "Belum ada data sampah", Toast.LENGTH_SHORT).show()
                } else {
                    notifAdapter.notifList.apply {
                        clear()
                        addAll(notifList)
                    }
                    notifAdapter.notifyDataSetChanged()
                }
            }
        }
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

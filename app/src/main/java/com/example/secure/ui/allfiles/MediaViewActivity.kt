package com.example.secure.ui.allfiles

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.secure.R
import java.io.File

class MediaViewActivity : AppCompatActivity(), MediaAdapterListener {

    private lateinit var viewPager: ViewPager2
    private lateinit var mediaAdapter: MediaAdapter
    private var currentPosition: Int = 0
    private lateinit var fileList: MutableList<Uri>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContentView(R.layout.activity_media_view)

        val fileUriString = intent.getStringExtra("file_uri")
        if (fileUriString == null) {
            finish()
            return
        }
        
        val fileUri = Uri.parse(fileUriString)
        setupMediaViewer(fileUri)
    }

    private fun setupMediaViewer(fileUri: Uri) {
        fileList = mutableListOf(fileUri) // Only display the single URI passed
        currentPosition = 0 // Always the first item since it's a single item list

        viewPager = findViewById(R.id.view_pager)
        mediaAdapter = MediaAdapter(this, fileList, this)
        viewPager.adapter = mediaAdapter
        viewPager.setCurrentItem(currentPosition, false)
    }

    override fun onDeleteFile(uri: Uri, position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Delete File")
            .setMessage("Are you sure you want to delete this file?")
            .setPositiveButton("Delete") { _, _ ->
                // Convert Uri back to File for deletion if it's a file URI
                val fileToDelete = uri.path?.let { File(it) }
                if (fileToDelete != null && fileToDelete.delete()) {
                    fileList.removeAt(position)
                    mediaAdapter.notifyItemRemoved(position)
                    if (fileList.isEmpty()) {
                        finish()
                    }
                    Toast.makeText(this, "File deleted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to delete file", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onUnhideFile(uri: Uri, position: Int) {
        // Implement unhide logic based on your app's requirements
        Toast.makeText(this, "Unhide not implemented yet", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::mediaAdapter.isInitialized) {
            // Clear any resources held by the adapter
            mediaAdapter.onDestroy()
            // Clear the adapter
            viewPager.adapter = null
        }
    }
}

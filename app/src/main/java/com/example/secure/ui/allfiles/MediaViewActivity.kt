package com.example.secure.ui.allfiles

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
    private lateinit var fileList: MutableList<File>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContentView(R.layout.activity_media_view)

        val filePath = intent.getStringExtra("file_path")
        if (filePath == null) {
            finish()
            return
        }
        
        setupMediaViewer(filePath)
    }

    private fun setupMediaViewer(filePath: String) {
        val currentFile = File(filePath)
        val parentDir = currentFile.parentFile
        
        if (parentDir != null) {
            fileList = parentDir.listFiles()
                ?.filter { 
                    it.isFile && (it.extension.lowercase() in listOf(
                        "jpg", "jpeg", "png", "gif", 
                        "mp4", "mkv", "webm", "avi", "3gp"
                    ))
                }
                ?.sorted()
                ?.toMutableList() ?: mutableListOf()
            
            currentPosition = fileList.indexOf(currentFile)
        } else {
            fileList = mutableListOf(currentFile)
            currentPosition = 0
        }

        viewPager = findViewById(R.id.view_pager)
        mediaAdapter = MediaAdapter(this, fileList, this)
        viewPager.adapter = mediaAdapter
        viewPager.setCurrentItem(currentPosition, false)
    }

    override fun onDeleteFile(file: File, position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Delete File")
            .setMessage("Are you sure you want to delete this file?")
            .setPositiveButton("Delete") { _, _ ->
                if (file.delete()) {
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

    override fun onUnhideFile(file: File, position: Int) {
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

package com.example.secure.ui.allfiles

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.secure.R
import java.io.File

class MediaViewActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var mediaAdapter: MediaAdapter
    private var currentPosition: Int = 0
    private lateinit var fileList: List<File>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media_view)

        val filePath = intent.getStringExtra("file_path")
        if (filePath == null) {
            finish()
            return
        }
        val currentFile = File(filePath)
        val parentDir = currentFile.parentFile
        if (parentDir != null) {
            fileList = parentDir.listFiles()?.filter { it.isFile && (it.extension.lowercase() in listOf("jpg", "jpeg", "png", "gif", "mp4", "mkv", "webm", "avi", "3gp")) }?.sorted() ?: emptyList()
            currentPosition = fileList.indexOf(currentFile)
        } else {
            fileList = listOf(currentFile)
            currentPosition = 0
        }

        viewPager = findViewById(R.id.view_pager)
        mediaAdapter = MediaAdapter(this, fileList)
        viewPager.adapter = mediaAdapter
        viewPager.setCurrentItem(currentPosition, false)
    }
}

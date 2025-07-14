package com.example.secure.ui.allfiles

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.MediaController
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.example.secure.R
import java.io.File

class MediaViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media_view)

        val filePath = intent.getStringExtra("file_path")
        if (filePath == null) {
            finish()
            return
        }
        val file = File(filePath)

        val imageView = findViewById<ImageView>(R.id.image_view)
        val videoView = findViewById<VideoView>(R.id.video_view)

        if (file.extension.lowercase() in listOf("jpg", "jpeg", "png", "gif")) {
            imageView.visibility = View.VISIBLE
            videoView.visibility = View.GONE
            imageView.setImageURI(Uri.fromFile(file))
        } else if (file.extension.lowercase() in listOf("mp4", "mkv", "webm", "avi", "3gp")) {
            imageView.visibility = View.GONE
            videoView.visibility = View.VISIBLE
            val mediaController = MediaController(this)
            mediaController.setAnchorView(videoView)
            videoView.setMediaController(mediaController)
            videoView.setVideoURI(Uri.fromFile(file))
            videoView.start()
        }

        // Finish activity on click
        val layout = findViewById<View>(android.R.id.content)
        layout.setOnClickListener {
            finish()
        }
    }
}

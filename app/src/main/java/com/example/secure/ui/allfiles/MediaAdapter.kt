package com.example.secure.ui.allfiles

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.RecyclerView
import com.example.secure.R
import java.io.File

class MediaAdapter(private val context: Context, private val fileList: List<File>) : RecyclerView.Adapter<MediaAdapter.MediaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_media, parent, false)
        return MediaViewHolder(view)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        val file = fileList[position]
        if (file.extension.lowercase() in listOf("jpg", "jpeg", "png", "gif")) {
            holder.imageView.visibility = View.VISIBLE
            holder.videoView.visibility = View.GONE
            holder.imageView.setImageURI(Uri.fromFile(file))
        } else {
            holder.imageView.visibility = View.GONE
            holder.videoView.visibility = View.VISIBLE
            val player = ExoPlayer.Builder(context).build()
            holder.videoView.player = player
            val mediaItem = MediaItem.fromUri(Uri.fromFile(file))
            player.setMediaItem(mediaItem)
            player.prepare()
            player.play()
        }
    }

    override fun getItemCount(): Int {
        return fileList.size
    }

    class MediaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.image_view)
        val videoView: PlayerView = itemView.findViewById(R.id.video_view)
    }
}

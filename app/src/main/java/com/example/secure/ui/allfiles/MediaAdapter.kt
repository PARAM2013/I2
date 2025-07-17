package com.example.secure.ui.allfiles

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.view.*
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.RecyclerView
import com.example.secure.R
import com.github.chrisbanes.photoview.PhotoView
import java.io.File

interface MediaAdapterListener {
    fun onDeleteFile(file: File, position: Int)
    fun onUnhideFile(file: File, position: Int)
}

class MediaAdapter(
    private val context: Context,
    private val fileList: List<File>,
    private val listener: MediaAdapterListener? = null
) : RecyclerView.Adapter<MediaAdapter.MediaViewHolder>() {

    private var isFullscreen = false
    private val players = mutableMapOf<Int, ExoPlayer>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_media, parent, false)
        return MediaViewHolder(view)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        val file = fileList[position]
        
        setupLongPressMenu(holder.rootLayout, file, position)
        setupFullscreenToggle(holder.rootLayout)

        if (file.extension.lowercase() in listOf("jpg", "jpeg", "png", "gif")) {
            setupImageView(holder, file)
        } else {
            setupVideoView(holder, file, position)
        }
    }

    private fun setupImageView(holder: MediaViewHolder, file: File) {
        holder.imageView.visibility = View.VISIBLE
        holder.videoView.visibility = View.GONE
        holder.imageView.setImageURI(Uri.fromFile(file))
    }

    private fun setupVideoView(holder: MediaViewHolder, file: File, position: Int) {
        holder.imageView.visibility = View.GONE
        holder.videoView.visibility = View.VISIBLE

        val player = players.getOrPut(position) {
            ExoPlayer.Builder(context).build().apply {
                repeatMode = Player.REPEAT_MODE_ONE
                playWhenReady = true
            }
        }

        holder.videoView.player = player
        val mediaItem = MediaItem.fromUri(Uri.fromFile(file))
        player.setMediaItem(mediaItem)
        player.prepare()
    }

    private fun setupLongPressMenu(view: View, file: File, position: Int) {
        view.setOnLongClickListener {
            showOptionsMenu(view, file, position)
            true
        }
    }

    private fun showOptionsMenu(view: View, file: File, position: Int) {
        PopupMenu(context, view).apply {
            menu.add("Delete")
            menu.add("Unhide")
            
            setOnMenuItemClickListener { item ->
                when (item.title.toString()) {
                    "Delete" -> listener?.onDeleteFile(file, position)
                    "Unhide" -> listener?.onUnhideFile(file, position)
                }
                true
            }
            show()
        }
    }

    private fun setupFullscreenToggle(view: View) {
        view.setOnClickListener {
            toggleFullscreen()
        }
    }

    private fun toggleFullscreen() {
        if (context is Activity) {
            isFullscreen = !isFullscreen
            val window = context.window
            val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
            
            if (isFullscreen) {
                windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
                windowInsetsController.systemBarsBehavior = 
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    override fun onViewRecycled(holder: MediaViewHolder) {
        super.onViewRecycled(holder)
        val position = holder.bindingAdapterPosition
        if (position != RecyclerView.NO_POSITION) {
            players[position]?.release()
            players.remove(position)
        }
    }

    override fun getItemCount(): Int = fileList.size

    class MediaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val rootLayout: View = itemView.findViewById(R.id.root_layout)
        val imageView: PhotoView = itemView.findViewById(R.id.image_view)
        val videoView: PlayerView = itemView.findViewById(R.id.video_view)
    }

    fun onDestroy() {
        // Release all players
        players.values.forEach { player ->
            player.release()
        }
        players.clear()
    }
}

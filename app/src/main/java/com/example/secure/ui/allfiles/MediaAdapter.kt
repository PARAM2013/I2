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
    fun onDeleteFile(uri: Uri, position: Int)
    fun onUnhideFile(uri: Uri, position: Int)
}

class MediaAdapter(
    private val context: Context,
    private val fileList: List<Uri>,
    private val listener: MediaAdapterListener? = null
) : RecyclerView.Adapter<MediaAdapter.MediaViewHolder>() {

    private var isFullscreen = false
    private val players = mutableMapOf<Int, ExoPlayer>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_media, parent, false)
        return MediaViewHolder(view)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        val uri = fileList[position]
        
        setupLongPressMenu(holder.rootLayout, uri, position)
        setupFullscreenToggle(holder.rootLayout)

        // Determine if it's an image or video based on URI extension or type if possible
        // For simplicity, we'll assume based on common extensions for now.
        val extension = uri.lastPathSegment?.substringAfterLast('.', "")?.lowercase()
        if (extension in listOf("jpg", "jpeg", "png", "gif")) {
            setupImageView(holder, uri)
        } else {
            setupVideoView(holder, uri, position)
        }
    }

    private fun setupImageView(holder: MediaViewHolder, uri: Uri) {
        holder.imageView.visibility = View.VISIBLE
        holder.videoView.visibility = View.GONE
        holder.imageView.setImageURI(uri)
    }

    private fun setupVideoView(holder: MediaViewHolder, uri: Uri, position: Int) {
        holder.imageView.visibility = View.GONE
        holder.videoView.visibility = View.VISIBLE

        val player = players.getOrPut(position) {
            ExoPlayer.Builder(context).build().apply {
                repeatMode = Player.REPEAT_MODE_ONE
                playWhenReady = true
            }
        }

        holder.videoView.player = player
        val mediaItem = MediaItem.fromUri(uri)
        player.setMediaItem(mediaItem)
        player.prepare()
    }

    private fun setupLongPressMenu(view: View, uri: Uri, position: Int) {
        view.setOnLongClickListener {
            showOptionsMenu(view, uri, position)
            true
        }
    }

    private fun showOptionsMenu(view: View, uri: Uri, position: Int) {
        PopupMenu(context, view).apply {
            menu.add("Delete")
            menu.add("Unhide")
            
            setOnMenuItemClickListener { item ->
                when (item.title.toString()) {
                    "Delete" -> listener?.onDeleteFile(uri, position)
                    "Unhide" -> listener?.onUnhideFile(uri, position)
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

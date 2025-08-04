package com.example.secure.util

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import java.io.File

object VideoUtils {

    fun getVideoDuration(context: Context, file: File): Long? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, Uri.fromFile(file))
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            durationStr?.toLong()
        } catch (e: Exception) {
            null
        } finally {
            retriever.release()
        }
    }
}

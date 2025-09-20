package com.example.secure.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.Log
import java.io.File

object PdfThumbnailGenerator {
    
    private const val TAG = "PdfThumbnailGenerator"
    
    /**
     * Generates a thumbnail for a PDF file
     * @param context Application context
     * @param pdfFile The PDF file to generate thumbnail for
     * @param width Desired thumbnail width (default 200)
     * @param height Desired thumbnail height (default 300)
     * @return Bitmap thumbnail or null if generation fails
     */
    fun generateThumbnail(
        @Suppress("UNUSED_PARAMETER") context: Context,
        pdfFile: File,
        width: Int = 200,
        height: Int = 300
    ): Bitmap? {
        if (!pdfFile.exists() || !pdfFile.canRead()) {
            Log.w(TAG, "PDF file does not exist or cannot be read: ${pdfFile.absolutePath}")
            return null
        }
        
        var pdfRenderer: PdfRenderer? = null
        var fileDescriptor: ParcelFileDescriptor? = null
        
        return try {
            // Open the PDF file
            fileDescriptor = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
            pdfRenderer = PdfRenderer(fileDescriptor)
            
            if (pdfRenderer.pageCount == 0) {
                Log.w(TAG, "PDF has no pages: ${pdfFile.name}")
                return null
            }
            
            // Open the first page
            val page = pdfRenderer.openPage(0)
            
            // Calculate the bitmap size maintaining aspect ratio
            val pageWidth = page.width
            val pageHeight = page.height
            val aspectRatio = pageWidth.toFloat() / pageHeight.toFloat()
            
            val bitmapWidth: Int
            val bitmapHeight: Int
            
            if (aspectRatio > 1) {
                // Landscape orientation
                bitmapWidth = width
                bitmapHeight = (width / aspectRatio).toInt()
            } else {
                // Portrait orientation
                bitmapHeight = height
                bitmapWidth = (height * aspectRatio).toInt()
            }
            
            // Create bitmap and render the page
            val bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            
            // Close the page
            page.close()
            
            Log.d(TAG, "Successfully generated thumbnail for PDF: ${pdfFile.name}")
            bitmap
            
        } catch (e: Exception) {
            Log.e(TAG, "Error generating PDF thumbnail for: ${pdfFile.name}", e)
            null
        } finally {
            try {
                pdfRenderer?.close()
                fileDescriptor?.close()
            } catch (e: Exception) {
                Log.e(TAG, "Error closing PDF resources", e)
            }
        }
    }
    
    /**
     * Checks if PDF thumbnail generation is supported on this device
     * PdfRenderer requires API level 21+
     */
    fun isSupported(): Boolean {
        return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP
    }
    
    /**
     * Generates a thumbnail asynchronously
     */
    suspend fun generateThumbnailAsync(
        context: Context,
        pdfFile: File,
        width: Int = 200,
        height: Int = 300
    ): Bitmap? {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            generateThumbnail(context, pdfFile, width, height)
        }
    }
}
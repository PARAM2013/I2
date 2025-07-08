package com.example.secure.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import com.example.secure.R
import com.example.secure.databinding.FragmentSecureDashboardBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.Locale

class SecureDashboardFragment : Fragment() {

    private var _binding: FragmentSecureDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSecureDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupFABs()
        updateCategoryPlaceholders() // TODO: Replace with actual data loading
    }

    private fun setupFABs() {
        val fabMain: FloatingActionButton = binding.fabMain
        val fabImportFile: FloatingActionButton = binding.fabImportFile
        val fabCreateFolder: FloatingActionButton = binding.fabCreateFolder
        val fabImportFileLabel: View = binding.fabImportFileLabel
        val fabCreateFolderLabel: View = binding.fabCreateFolderLabel

        val fabOpen: Animation = AnimationUtils.loadAnimation(context, R.anim.fab_open)
        val fabClose: Animation = AnimationUtils.loadAnimation(context, R.anim.fab_close)
        val rotateForward: Animation = AnimationUtils.loadAnimation(context, R.anim.rotate_forward)
        val rotateBackward: Animation = AnimationUtils.loadAnimation(context, R.anim.rotate_backward)

        var isFabMenuOpen = false

        fabMain.setOnClickListener {
            if (isFabMenuOpen) {
                fabMain.startAnimation(rotateBackward)
                fabImportFile.startAnimation(fabClose)
                fabCreateFolder.startAnimation(fabClose)
                fabImportFileLabel.startAnimation(fabClose)
                fabCreateFolderLabel.startAnimation(fabClose)
                fabImportFile.isClickable = false
                fabCreateFolder.isClickable = false
                fabImportFile.visibility = View.INVISIBLE
                fabCreateFolder.visibility = View.INVISIBLE
                fabImportFileLabel.visibility = View.INVISIBLE
                fabCreateFolderLabel.visibility = View.INVISIBLE
            } else {
                fabMain.startAnimation(rotateForward)
                fabImportFile.startAnimation(fabOpen)
                fabCreateFolder.startAnimation(fabOpen)
                fabImportFileLabel.startAnimation(fabOpen)
                fabCreateFolderLabel.startAnimation(fabOpen)
                fabImportFile.isClickable = true
                fabCreateFolder.isClickable = true
                fabImportFile.visibility = View.VISIBLE
                fabCreateFolder.visibility = View.VISIBLE
                fabImportFileLabel.visibility = View.VISIBLE
                fabCreateFolderLabel.visibility = View.VISIBLE
            }
            isFabMenuOpen = !isFabMenuOpen
        }

        fabImportFile.setOnClickListener {
            // TODO: Implement file import logic
            android.widget.Toast.makeText(context, "Import File clicked", android.widget.Toast.LENGTH_SHORT).show()
            // Close FAB menu
            if (isFabMenuOpen) fabMain.performClick()
        }

        fabCreateFolder.setOnClickListener {
            // TODO: Implement create folder logic
            android.widget.Toast.makeText(context, "Create Folder clicked", android.widget.Toast.LENGTH_SHORT).show()
            if (isFabMenuOpen) fabMain.performClick()
        }
    }

    // TODO: This method will be updated in Step 5 to use FileManager
    private fun updateCategoryPlaceholders() {
        // Placeholder data
        val allFolders = 5
        val allFiles = 20
        val allSizeMb = 225.5f

        val photoFiles = 10
        val photoSizeMb = 100.2f

        val videoFiles = 3
        val videoSizeMb = 120.1f

        val docFiles = 7
        val docSizeMb = 5.2f

        binding.textCategoryAllDetails.text = String.format(Locale.getDefault(),
            "%d Folders, %d Files, %.1f MB", allFolders, allFiles, allSizeMb)

        binding.textCategoryPhotosTitle.text = getString(R.string.category_photos, photoFiles, formatSize(photoSizeMb))
        binding.textCategoryPhotosDetails.text = String.format(Locale.getDefault(),
            "%d Files, %.1f MB", photoFiles, photoSizeMb)


        binding.textCategoryVideosTitle.text = getString(R.string.category_videos, videoFiles, formatSize(videoSizeMb))
         binding.textCategoryVideosDetails.text = String.format(Locale.getDefault(),
            "%d Files, %.1f MB", videoFiles, videoSizeMb)


        binding.textCategoryDocumentsTitle.text = getString(R.string.category_documents, docFiles, formatSize(docSizeMb))
        binding.textCategoryDocumentsDetails.text = String.format(Locale.getDefault(),
            "%d Files, %.1f MB", docFiles, docSizeMb)

        if (allFiles == 0 && allFolders == 0) {
            binding.textEmptyVault.visibility = View.VISIBLE
        } else {
            binding.textEmptyVault.visibility = View.GONE
        }
    }

    private fun formatSize(sizeMb: Float): String {
        return if (sizeMb >= 1024) {
            String.format(Locale.getDefault(), "%.1f GB", sizeMb / 1024)
        } else {
            String.format(Locale.getDefault(), "%.1f MB", sizeMb)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

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

import com.example.secure.file.FileManager // Import FileManager
import java.io.File // For potential use with File objects if needed directly

class SecureDashboardFragment : Fragment() {

    private var _binding: FragmentSecureDashboardBinding? = null
    private val binding get() = _binding!!

    // Define ActivityResultLauncher for file import and folder creation
    private val importFileLauncher = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.GetContent()) { uri: android.net.Uri? ->
        uri?.let {
            // Handle the selected file URI
            context?.let { ctx ->
                // TODO: Potentially ask user for target folder within vault, for now import to root
                val importedFile = FileManager.importFile(it, ctx, null, true)
                if (importedFile != null) {
                    android.widget.Toast.makeText(ctx, "File imported: ${importedFile.name}", android.widget.Toast.LENGTH_SHORT).show()
                    loadDashboardData() // Refresh dashboard
                } else {
                    android.widget.Toast.makeText(ctx, "Failed to import file", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    // No specific launcher for create folder as we'll use a dialog

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
        loadDashboardData() // Load actual data
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
            importFileLauncher.launch("*/*") // Launch file picker
            if (isFabMenuOpen) fabMain.performClick() // Close FAB menu
        }

        fabCreateFolder.setOnClickListener {
            showCreateFolderDialog()
            if (isFabMenuOpen) fabMain.performClick() // Close FAB menu
        }
    }

    private fun loadDashboardData() {
        // Ensure context is not null, though less likely here than in a callback
        context ?: return

        val vaultStats = FileManager.listFilesInVault()

        // Update "All Files" category
        binding.textCategoryAllDetails.text = String.format(Locale.getDefault(),
            "%d Folders, %d Files, %s",
            vaultStats.grandTotalFolders,
            vaultStats.grandTotalFiles,
            formatSize(vaultStats.grandTotalSize)
        )

        // Update "Photos" category
        binding.textCategoryPhotosTitle.text = getString(R.string.category_photos_title_dynamic, vaultStats.totalPhotoFiles)
        binding.textCategoryPhotosDetails.text = String.format(Locale.getDefault(),
            "%d Files, %s",
            vaultStats.totalPhotoFiles,
            formatSize(vaultStats.totalPhotoSize)
        )

        // Update "Videos" category
        binding.textCategoryVideosTitle.text = getString(R.string.category_videos_title_dynamic, vaultStats.totalVideoFiles)
        binding.textCategoryVideosDetails.text = String.format(Locale.getDefault(),
            "%d Files, %s",
            vaultStats.totalVideoFiles,
            formatSize(vaultStats.totalVideoSize)
        )

        // Update "Documents" category
        binding.textCategoryDocumentsTitle.text = getString(R.string.category_documents_title_dynamic, vaultStats.totalDocumentFiles)
        binding.textCategoryDocumentsDetails.text = String.format(Locale.getDefault(),
            "%d Files, %s",
            vaultStats.totalDocumentFiles,
            formatSize(vaultStats.totalDocumentSize)
        )

        // Show/hide empty vault message
        if (vaultStats.grandTotalFiles == 0 && vaultStats.grandTotalFolders == 0) {
            binding.textEmptyVault.visibility = View.VISIBLE
            binding.categoriesScrollView.visibility = View.GONE // Hide categories if empty
        } else {
            binding.textEmptyVault.visibility = View.GONE
            binding.categoriesScrollView.visibility = View.VISIBLE // Show categories
        }
    }

    private fun showCreateFolderDialog() {
        context?.let { ctx ->
            val builder = androidx.appcompat.app.AlertDialog.Builder(ctx)
            builder.setTitle("Create New Folder")

            val input = android.widget.EditText(ctx)
            input.inputType = android.text.InputType.TYPE_CLASS_TEXT
            input.hint = "Folder Name"
            val layout = android.widget.FrameLayout(ctx)
            val params = android.widget.FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            params.leftMargin = resources.getDimensionPixelSize(R.dimen.dialog_margin)
            params.rightMargin = resources.getDimensionPixelSize(R.dimen.dialog_margin)
            input.layoutParams = params
            layout.addView(input)
            builder.setView(layout)


            builder.setPositiveButton("Create") { dialog, _ ->
                val folderName = input.text.toString().trim()
                if (folderName.isNotEmpty()) {
                    // TODO: Allow selecting parent folder. For now, create in root.
                    val createdFolder = FileManager.createSubFolderInVault(folderName, null)
                    if (createdFolder != null && createdFolder.exists()) {
                        android.widget.Toast.makeText(ctx, "Folder created: $folderName", android.widget.Toast.LENGTH_SHORT).show()
                        loadDashboardData() // Refresh dashboard
                    } else {
                        android.widget.Toast.makeText(ctx, "Failed to create folder. It may already exist or the name is invalid.", android.widget.Toast.LENGTH_LONG).show()
                    }
                } else {
                    android.widget.Toast.makeText(ctx, "Folder name cannot be empty", android.widget.Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
            builder.show()
        }
    }


    private fun formatSize(sizeBytes: Long): String {
        val kb = sizeBytes / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0

        return when {
            gb >= 1 -> String.format(Locale.getDefault(), "%.1f GB", gb)
            mb >= 1 -> String.format(Locale.getDefault(), "%.1f MB", mb)
            kb >= 1 -> String.format(Locale.getDefault(), "%.1f KB", kb)
            else -> String.format(Locale.getDefault(), "%d Bytes", sizeBytes)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

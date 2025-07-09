package com.example.secure.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.secure.R
import com.example.secure.databinding.FragmentSecureDashboardBinding
import com.example.secure.file.FileManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.Locale

class SecureDashboardFragment : Fragment() {

    private var _binding: FragmentSecureDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SecureDashboardViewModel by viewModels()

    private val importFileLauncher = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.GetContent()) { uri: android.net.Uri? ->
        uri?.let {
            viewModel.importFile(it)
        }
    }

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
        observeViewModel()
        viewModel.loadDashboardData() // Initial data load
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            // Handle loading state (e.g., show/hide progress bar)
            binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE

            state.stats?.let { vaultStats ->
                updateDashboardUI(vaultStats)
            }

            state.fileOperationResult?.let { message ->
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                viewModel.clearFileOperationResult() // Clear after showing
            }

            state.error?.let { errorMessage ->
                Toast.makeText(context, "Error: $errorMessage", Toast.LENGTH_LONG).show()
                Log.e("SecureDashboardFragment", "Error from ViewModel: $errorMessage")
                viewModel.clearError() // Clear after showing
                 // Optionally, update UI to show a persistent error message
            }
        }
    }

    private fun updateDashboardUI(vaultStats: FileManager.VaultStats) {
        binding.textCategoryAllDetails.text = String.format(Locale.getDefault(),
            "%d Folders, %d Files, %s",
            vaultStats.grandTotalFolders,
            vaultStats.grandTotalFiles,
            formatSize(vaultStats.grandTotalSize)
        )
        binding.textCategoryPhotosTitle.text = getString(R.string.category_photos_title_dynamic, vaultStats.totalPhotoFiles)
        binding.textCategoryPhotosDetails.text = String.format(Locale.getDefault(),
            "%d Files, %s",
            vaultStats.totalPhotoFiles,
            formatSize(vaultStats.totalPhotoSize)
        )
        binding.textCategoryVideosTitle.text = getString(R.string.category_videos_title_dynamic, vaultStats.totalVideoFiles)
        binding.textCategoryVideosDetails.text = String.format(Locale.getDefault(),
            "%d Files, %s",
            vaultStats.totalVideoFiles,
            formatSize(vaultStats.totalVideoSize)
        )
        binding.textCategoryDocumentsTitle.text = getString(R.string.category_documents_title_dynamic, vaultStats.totalDocumentFiles)
        binding.textCategoryDocumentsDetails.text = String.format(Locale.getDefault(),
            "%d Files, %s",
            vaultStats.totalDocumentFiles,
            formatSize(vaultStats.totalDocumentSize)
        )

        binding.textEmptyVault.visibility = if (vaultStats.grandTotalFiles == 0 && vaultStats.grandTotalFolders == 0) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }


    private fun setupFABs() {
        val fabMain: FloatingActionButton = binding.fabMain
        val fabImportFile: FloatingActionButton = binding.fabImportFile
        val fabCreateFolder: FloatingActionButton = binding.fabCreateFolder
        val fabImportFileLabel: View = binding.fabImportFileLabel
        val fabCreateFolderLabel: View = binding.fabCreateFolderLabel

        // Ensure context is available for AnimationUtils
        val currentContext = context ?: return

        val fabOpen: Animation = AnimationUtils.loadAnimation(currentContext, R.anim.fab_open)
        val fabClose: Animation = AnimationUtils.loadAnimation(currentContext, R.anim.fab_close)
        val rotateForward: Animation = AnimationUtils.loadAnimation(currentContext, R.anim.rotate_forward)
        val rotateBackward: Animation = AnimationUtils.loadAnimation(currentContext, R.anim.rotate_backward)

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
            Log.d("SecureDashboardFragment", "Create Folder FAB clicked!")
            showCreateFolderDialog()
             // Closing menu here could be abrupt if dialog is interaction based.
            // Consider closing it after dialog dismisses or based on dialog interaction.
            // if (isFabMenuOpen) fabMain.performClick()
        }
    }

    private fun showCreateFolderDialog() {
        context?.let { ctx ->
            val builder = AlertDialog.Builder(ctx)
            builder.setTitle("Create New Folder")

            val input = android.widget.EditText(ctx)
            input.inputType = android.text.InputType.TYPE_CLASS_TEXT
            input.hint = "Folder Name"
            val layout = android.widget.FrameLayout(ctx)
            val params = android.widget.FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            // It's good practice to use resources for margins if available
            val margin = resources.getDimensionPixelSize(R.dimen.dialog_margin)
            params.leftMargin = margin
            params.rightMargin = margin
            input.layoutParams = params
            layout.addView(input)
            builder.setView(layout)

            builder.setPositiveButton("Create") { dialog, _ ->
                val folderName = input.text.toString().trim()
                Log.d("SecureDashboardFragment", "Attempting to create folder with name: '$folderName'")
                if (folderName.isNotEmpty()) {
                    viewModel.createFolder(folderName)
                } else {
                    Toast.makeText(ctx, "Folder name cannot be empty", Toast.LENGTH_SHORT).show()
                }
                // Consider if fab menu should be closed here
                // if (binding.fabMain.isExtended) binding.fabMain.performClick()
                dialog.dismiss()
            }
            builder.setNegativeButton("Cancel") { dialog, _ ->
                // if (binding.fabMain.isExtended) binding.fabMain.performClick() // Close if cancelling too
                dialog.cancel()
            }
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
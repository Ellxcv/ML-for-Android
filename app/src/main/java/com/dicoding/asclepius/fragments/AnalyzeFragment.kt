package com.dicoding.asclepius.fragments

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.dicoding.asclepius.R
import com.dicoding.asclepius.databinding.FragmentAnalyzeBinding
import com.dicoding.asclepius.helper.ClassifierListener
import com.dicoding.asclepius.helper.ImageClassifierHelper
import com.dicoding.asclepius.view.ResultActivity
import com.yalantis.ucrop.UCrop
import java.io.File

class AnalyzeFragment : Fragment(R.layout.fragment_analyze), ClassifierListener {

    // Binding untuk akses UI dan Image Classifier Helper untuk analisis gambar
    private var _binding: FragmentAnalyzeBinding? = null
    private val binding get() = _binding!!

    // Variabel untuk menyimpan URI gambar yang dipilih
    private var currentImageUri: Uri? = null

    // Helper untuk melakukan klasifikasi gambar
    private lateinit var classifierHelper: ImageClassifierHelper

    // ActivityResultLaunchers untuk menangani hasil dari memilih gambar dan pemotongan gambar
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private lateinit var cropLauncher: ActivityResultLauncher<Intent>

    //Test Error
    val invalidUri = Uri.parse("content://invalid/path/to/image.jpg")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalyzeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Restore URI gambar yang dipilih jika fragment di-recreate
        currentImageUri = savedInstanceState?.getParcelable("currentImageUri")
        currentImageUri?.let { showImage(it) }

        // Inisialisasi ImageClassifierHelper
        classifierHelper = ImageClassifierHelper(requireContext(), this)

        // Set listener untuk tombol gallery dan analyze
        binding.galleryButton.setOnClickListener { startGallery() }
        binding.analyzeButton.setOnClickListener { analyzeImage() }

        // ActivityResultLauncher untuk membuka galeri dan menerima hasil
        galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    currentImageUri = uri
                    showImage(uri) // Tampilkan gambar yang dipilih
                    startCrop(uri) // Mulai crop gambar
                }
            } else {
                showToast("Image selection canceled")
            }
        }

        // ActivityResultLauncher untuk menangani hasil crop gambar
        cropLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.let {
                    UCrop.getOutput(it)?.let { uri ->
                        currentImageUri = uri
                        showImage(uri) // Tampilkan gambar yang telah dipotong
                    }
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        currentImageUri?.let {
            outState.putParcelable("currentImageUri", it)
        }
    }

    // Fungsi untuk memulai proses crop gambar
    private fun startCrop(uri: Uri) {
        val uniqueFileName = "cropped_image_${System.currentTimeMillis()}.jpg" // Nama file unik untuk gambar cropped
        val destinationUri = Uri.fromFile(File(requireContext().cacheDir, uniqueFileName))
        UCrop.of(uri, destinationUri)
            .withAspectRatio(1f, 1f)
            .withOptions(UCrop.Options().apply {
                setCompressionQuality(80)
                setFreeStyleCropEnabled(true)
            })
            .let { cropLauncher.launch(it.getIntent(requireContext())) }
    }

    // Fungsi untuk memulai pemilihan gambar dari galeri
    private fun startGallery() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            openGallery()
        } else {
            requestPermissions(arrayOf(permission), PICK_IMAGE_REQUEST)
        }
    }

    // Membuka galeri untuk memilih gambar
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
        galleryLauncher.launch(intent)
    }

    // Menampilkan gambar pada ImageView
    private fun showImage(uri: Uri) {
        binding.previewImageView.setImageURI(uri)
        binding.previewImageView.invalidate() // Refresh tampilan gambar
    }

    // Menganalisis gambar yang dipilih dan menampilkan hasilnya
    private fun analyzeImage() {
        currentImageUri?.let { uri ->
            classifierHelper.classifyStaticImage(uri)
        } ?: showToast("No image selected")
    }

    override fun onClassificationSuccess(result: Pair<String, Float>) {
        moveToResult(result) // Display result in ResultActivity
    }

    override fun onClassificationError(errorMessage: String) {
        showToast(errorMessage) // Show error message as a toast
    }

    // Menampilkan hasil analisis pada Activity baru
    private fun moveToResult(result: Pair<String, Float>) {
        val intent = Intent(requireContext(), ResultActivity::class.java).apply {
            putExtra("result", result.first)
            putExtra("score", result.second)
            putExtra("imageUri", currentImageUri.toString())
        }
        startActivity(intent)
    }

    // Menampilkan pesan toast
    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    // Menangani permintaan izin untuk galeri
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PICK_IMAGE_REQUEST && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openGallery()
        } else {
            showToast("Permission denied")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        classifierHelper.closeModel() // Membersihkan model ketika fragment dihancurkan
        _binding = null // Membersihkan binding untuk menghindari memory leak
    }

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }
}

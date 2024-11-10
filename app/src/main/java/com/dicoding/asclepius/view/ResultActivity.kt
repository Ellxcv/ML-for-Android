package com.dicoding.asclepius.view

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import com.dicoding.asclepius.R
import com.dicoding.asclepius.databinding.ActivityResultBinding
import com.dicoding.asclepius.database.AnalysisResult

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding
    private val analysisResultViewModel: AnalysisResultViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // TODO: Menampilkan hasil gambar, prediksi, dan confidence score.
        // Mendapatkan hasil dan score dari intent
        val result = intent.getStringExtra("result") ?: "No result"
        val score = intent.getFloatExtra("score", 0.0f)
        val imageUri = intent.getStringExtra("imageUri")
        Log.d("ResultActivity", "Received image URI: $imageUri")

        // Menampilkan hasil klasifikasi dan confidence score
        binding.resultText.text = "Prediction: $result\nConfidence: ${"%.2f".format(score * 100)}%"

        // Menampilkan gambar yang dipilih
        imageUri?.let {
            binding.resultImage.setImageURI(Uri.parse(it))
        } ?: run {
            Log.d("ResultActivity", "No image URI found")
            binding.resultImage.setImageResource(R.drawable.ic_place_holder)
        }

        // Simpan hasil analisis ke dalam Room Database
        val analysisResult = AnalysisResult(result = result, score = score, imageUri = imageUri ?: "")

        // Cek apakah data dengan imageUri yang sama sudah ada
        analysisResultViewModel.getResultByImageUri(imageUri ?: "").observe(this) { existingResult ->
            if (existingResult == null) {
                // Jika data belum ada, masukkan ke database
                analysisResultViewModel.insertResult(analysisResult)
            } else {
                // Jika data sudah ada, log pesan atau lakukan tindakan lain
                Log.d("ResultActivity", "Result for this image URI already exists.")
            }
        }

        // Logging untuk memastikan data yang diterima benar
        Log.d("ResultActivity", "Result: $result, Confidence Score: ${score * 100}")
    }
}
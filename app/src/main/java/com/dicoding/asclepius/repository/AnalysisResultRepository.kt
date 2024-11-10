package com.dicoding.asclepius.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.dicoding.asclepius.database.AnalysisResultDao
import com.dicoding.asclepius.database.AnalysisResult

// Repository untuk berinteraksi dengan DAO (Data Access Object) terkait hasil analisis
class AnalysisResultRepository(private val dao: AnalysisResultDao) {

    // Mendapatkan semua hasil analisis dari database
    fun getAllResults(): LiveData<List<AnalysisResult>> = dao.getAllResults()

    // Memasukkan hasil analisis ke dalam database
    fun insert(result: AnalysisResult) {
        // Logging untuk debugging, bisa dihapus atau disesuaikan untuk produksi
        Log.d("AnalysisResultRepository", "Inserting result: $result")

        // Menyimpan hasil analisis ke dalam database
        dao.insert(result)
    }

    suspend fun delete(result: AnalysisResult) {
        dao.delete(result)
        Log.d("AnalysisResultRepository", "Deleted result: $result")
    }

    fun getResultByImageUri(imageUri: String): LiveData<AnalysisResult?> {
        return dao.getResultByImageUri(imageUri)
    }
}

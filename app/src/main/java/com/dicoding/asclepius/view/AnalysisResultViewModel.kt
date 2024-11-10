package com.dicoding.asclepius.view

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.dicoding.asclepius.database.AppDatabase
import com.dicoding.asclepius.database.AnalysisResult
import com.dicoding.asclepius.repository.AnalysisResultRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// ViewModel untuk menangani data dan logika bisnis terkait dengan hasil analisis
class AnalysisResultViewModel(application: Application, savedStateHandle: SavedStateHandle) : AndroidViewModel(application) {

    // Inisialisasi repository untuk berinteraksi dengan database
    private val repository: AnalysisResultRepository

    // LiveData untuk menyimpan dan mengamati hasil analisis
    val allResults: LiveData<List<AnalysisResult>>

    // LiveData untuk menampilkan status operasi (insert success atau failure)
    private val _status = MutableLiveData<String>()

    // Mendapatkan status terakhir yang disimpan dari SavedStateHandle
    val savedStatus = savedStateHandle.getLiveData<String>("savedStatus", "")

    init {
        // Menginisialisasi DAO dari database dan repository untuk mengakses data
        val dao = AppDatabase.getDatabase(application).analysisResultDao()
        repository = AnalysisResultRepository(dao)

        // Mengambil semua hasil analisis dari database
        allResults = repository.getAllResults()

        // Mengembalikan status yang tersimpan jika ada
        savedStatus.value?.let {
            _status.postValue(it)
        }
    }

    // Fungsi untuk memasukkan hasil analisis ke dalam database
    fun insertResult(result: AnalysisResult) {
        // Menjalankan operasi database di background thread
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d("AnalysisResultViewModel", "Attempting to insert result: $result")
                // Memasukkan hasil analisis ke dalam database
                repository.insert(result)

                // Setelah berhasil, kirimkan status sukses ke thread utama
                withContext(Dispatchers.Main) {
                    _status.postValue("Insert Successful")
                    savedStatus.value = "Insert Successful"
                    Log.d("AnalysisResultViewModel", "Insert Successful")
                }
            } catch (e: Exception) {
                // Jika terjadi error, kirimkan status gagal ke thread utama
                withContext(Dispatchers.Main) {
                    _status.postValue("Insert Failed: ${e.message}")
                    savedStatus.value = "Insert Failed: ${e.message}"
                    Log.e("AnalysisResultViewModel", "Insert Failed: ${e.message}")
                }
            }
        }
    }

    fun deleteResult(result: AnalysisResult) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.delete(result)
                Log.d("AnalysisResultViewModel", "Delete Successful: $result")
            } catch (e: Exception) {
                Log.e("AnalysisResultViewModel", "Delete Failed: ${e.message}")
            }
        }
    }

    fun getResultByImageUri(imageUri: String): LiveData<AnalysisResult?> {
        return repository.getResultByImageUri(imageUri)
    }
}

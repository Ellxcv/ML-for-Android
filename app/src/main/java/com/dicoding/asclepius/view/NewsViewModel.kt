package com.dicoding.asclepius.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.asclepius.models.NewsResponse
import com.dicoding.asclepius.api.RetrofitInstance
import kotlinx.coroutines.launch

// ViewModel untuk mengambil data berita terkait kanker
class NewsViewModel : ViewModel() {

    // LiveData untuk menyimpan hasil berita
    private val _news = MutableLiveData<NewsResponse>()
    val news: LiveData<NewsResponse> get() = _news

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    // Fungsi untuk mengambil berita kanker dari API
    fun fetchCancerNews(apiKey: String) {
        // Menggunakan viewModelScope untuk meluncurkan coroutine
        viewModelScope.launch {
            try {
                // Mengambil data dari API menggunakan Retrofit
                val response = RetrofitInstance.api.getCancerNews(apiKey = apiKey)

                // Menyimpan data berita yang didapatkan ke LiveData
                _news.value = response
                _error.value = null
            } catch (e: Exception) {
                // Menangani error dan mencetak stack trace untuk debugging
                e.printStackTrace()
                _error.value = "Failed to fetch news: ${e.message}"
            }
        }
    }
}

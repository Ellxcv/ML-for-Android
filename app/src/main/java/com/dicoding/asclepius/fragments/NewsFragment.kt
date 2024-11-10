package com.dicoding.asclepius.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.asclepius.R
import com.dicoding.asclepius.adapter.NewsAdapter
import com.dicoding.asclepius.databinding.FragmentNewsBinding
import com.dicoding.asclepius.view.NewsViewModel

// Fragment untuk menampilkan daftar berita terkait kanker
class NewsFragment : Fragment(R.layout.fragment_news) {

    private lateinit var binding: FragmentNewsBinding
    private val viewModel: NewsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNewsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Panggil fungsi untuk mengambil berita kanker dari API dengan API Key
        viewModel.fetchCancerNews("ffdcb4653b5740679a3a0519dc41469d")

        // Observasi LiveData 'news' untuk mendapatkan respons berita
        viewModel.news.observe(viewLifecycleOwner) { newsResponse ->
            // Menginisialisasi adapter dengan data berita yang diterima
            val adapter = NewsAdapter(newsResponse.articles) { url ->
                // Ketika item diklik, buka URL berita di browser
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
            }

            // Mengatur RecyclerView dengan LayoutManager dan adapter
            binding.recyclerView.apply {
                layoutManager = LinearLayoutManager(requireContext())
                this.adapter = adapter
            }
        }
        // Observe the error LiveData and show a Toast if there's an error
        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        }
    }
}


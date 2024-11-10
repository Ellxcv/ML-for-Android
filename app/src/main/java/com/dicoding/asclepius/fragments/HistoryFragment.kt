package com.dicoding.asclepius.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.asclepius.R
import com.dicoding.asclepius.adapter.HistoryAdapter
import com.dicoding.asclepius.database.AnalysisResult
import com.dicoding.asclepius.databinding.FragmentHistoryBinding
import com.dicoding.asclepius.view.AnalysisResultViewModel

class HistoryFragment : Fragment(R.layout.fragment_history), HistoryAdapter.OnHistoryItemClickListener {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var historyAdapter: HistoryAdapter
    private val analysisResultViewModel: AnalysisResultViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        historyAdapter = HistoryAdapter(emptyList(), this)

        binding.historyRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = historyAdapter
        }

        analysisResultViewModel.allResults.observe(viewLifecycleOwner) { results ->
            Log.d("HistoryFragment", "Received results: ${results.size}")
            if (results.isEmpty()) {
                binding.emptyHistoryText.visibility = View.VISIBLE
                binding.historyRecyclerView.visibility = View.GONE
            } else {
                binding.emptyHistoryText.visibility = View.GONE
                binding.historyRecyclerView.visibility = View.VISIBLE
                historyAdapter.updateResults(results)
                Log.d("HistoryFragment", "Adapter updated with new results")
            }
        }
    }

    override fun onShareClicked(result: AnalysisResult) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "Prediction: ${result.result}, Confidence: ${result.score * 100}%")
        }
        startActivity(Intent.createChooser(shareIntent, "Share Analysis Result"))
    }

    override fun onDeleteClicked(result: AnalysisResult) {
        analysisResultViewModel.deleteResult(result)
        Toast.makeText(requireContext(), "Deleted successfully", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

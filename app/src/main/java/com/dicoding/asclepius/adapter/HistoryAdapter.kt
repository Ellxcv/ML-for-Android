package com.dicoding.asclepius.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dicoding.asclepius.databinding.ItemHistoryBinding
import com.dicoding.asclepius.database.AnalysisResult
import android.util.Log
import androidx.core.content.ContextCompat
import com.dicoding.asclepius.R

class HistoryAdapter(
    private var results: List<AnalysisResult>,
    private val listener: OnHistoryItemClickListener
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    interface OnHistoryItemClickListener {
        fun onShareClicked(result: AnalysisResult)
        fun onDeleteClicked(result: AnalysisResult)
    }

    inner class HistoryViewHolder(private val binding: ItemHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(result: AnalysisResult) {
            val predictionText = binding.root.context.getString(R.string.history_prediction, result.result)
            val confidenceText = binding.root.context.getString(R.string.history_confidence, result.score * 100)

            binding.historyResultText.text = binding.root.context.getString(
                R.string.history_prediction_confidence,
                predictionText,
                confidenceText
            )

            Log.d("HistoryAdapter", "Binding result: ${result.result}, score: ${result.score}")

            if (result.imageUri.isNullOrEmpty()) {
                Log.e("HistoryAdapter", "Image URI is empty or null!")
            } else {
                Log.d("HistoryAdapter", "Loading image URI: ${result.imageUri}")
            }

            Glide.with(binding.historyImage.context)
                .load(Uri.parse(result.imageUri))
                .skipMemoryCache(true)
                .placeholder(R.drawable.ph)
                .error(R.drawable.ic_launcher_foreground)
                .into(binding.historyImage)

            binding.btnShare.setOnClickListener { listener.onShareClicked(result) }
            binding.btnDelete.setOnClickListener { listener.onDeleteClicked(result) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(results[position])
    }

    override fun getItemCount(): Int = results.size

    fun updateResults(newResults: List<AnalysisResult>) {
        Log.d("HistoryAdapter", "Updating results with ${newResults.size} items")
        results = newResults
        notifyDataSetChanged()
    }
}

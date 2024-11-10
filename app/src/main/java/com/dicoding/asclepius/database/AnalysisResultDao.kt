package com.dicoding.asclepius.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface AnalysisResultDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(result: AnalysisResult)

    @Query("SELECT * FROM analysis_results")
    fun getAllResults(): LiveData<List<AnalysisResult>>

    @Query("SELECT * FROM analysis_results WHERE imageUri = :imageUri LIMIT 1")
    fun getResultByImageUri(imageUri: String): LiveData<AnalysisResult?>

    // Fungsi untuk menghapus hasil analisis tertentu dari database
    @Delete
    fun delete(result: AnalysisResult)
}




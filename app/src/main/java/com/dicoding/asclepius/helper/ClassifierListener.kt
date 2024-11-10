package com.dicoding.asclepius.helper

interface ClassifierListener {
    fun onClassificationSuccess(result: Pair<String, Float>)
    fun onClassificationError(errorMessage: String)
}

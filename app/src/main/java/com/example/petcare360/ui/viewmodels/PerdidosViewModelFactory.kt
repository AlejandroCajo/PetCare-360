package com.example.petcare360.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.petcare360.data.remote.SupabaseClient

class PerdidosViewModelFactory(
    private val supabaseClient: SupabaseClient?
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PerdidosViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PerdidosViewModel(supabaseClient) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

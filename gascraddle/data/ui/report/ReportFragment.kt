package com.yourname.gascraddle.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.yourname.gascraddle.data.local.entity.Transaction
import com.yourname.gascraddle.data.repository.SQLServerRepository
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SQLServerRepository()

    private val _transactions = MutableLiveData<List<Transaction>>()
    val transactions: LiveData<List<Transaction>> = _transactions

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    init {
        loadTransactions()
    }

    fun loadTransactions() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val data = repository.getAllTransactions()
                _transactions.value = data
            } catch (e: Exception) {
                _error.value = "Error loading data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun insertTransaction(transaction: Transaction) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = repository.insertTransaction(transaction)
                if (success) {
                    loadTransactions() // Reload data
                } else {
                    _error.value = "Failed to save transaction"
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = repository.updateTransaction(transaction)
                if (success) {
                    loadTransactions() // Reload data
                } else {
                    _error.value = "Failed to update transaction"
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteTransaction(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = repository.deleteTransaction(id)
                if (success) {
                    loadTransactions() // Reload data
                } else {
                    _error.value = "Failed to delete transaction"
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}

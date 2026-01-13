package com.yourname.gascraddle.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.yourname.gascraddle.data.local.AppDatabase
import com.yourname.gascraddle.data.local.entity.Customer
import com.yourname.gascraddle.data.local.entity.Trailer
import com.yourname.gascraddle.data.local.entity.Transaction
import com.yourname.gascraddle.data.repository.TransactionRepository
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TransactionRepository

    val allTransactions: LiveData<List<Transaction>>
    val allCustomers: LiveData<List<Customer>>
    val allTrailers: LiveData<List<Trailer>>

    init {
        val database = AppDatabase.getDatabase(application)
        repository = TransactionRepository(
            database.transactionDao(),
            database.customerDao(),
            database.trailerDao()
        )
        allTransactions = repository.allTransactions
        allCustomers = repository.allCustomers
        allTrailers = repository.allTrailers
    }

    fun insertTransaction(transaction: Transaction) = viewModelScope.launch {
        repository.insertTransaction(transaction)
    }

    fun deleteTransaction(transaction: Transaction) = viewModelScope.launch {
        repository.deleteTransaction(transaction)
    }

    suspend fun getCustomerByName(nama: String) = repository.getCustomerByName(nama)
    suspend fun getTrailerByName(nama: String) = repository.getTrailerByName(nama)
}

package com.yourname.gascraddle.ui.input

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.yourname.gascraddle.data.local.AppDatabase
import com.yourname.gascraddle.data.local.entity.Transaction
import com.yourname.gascraddle.data.repository.TransactionRepository
import com.yourname.gascraddle.utils.AGA8Calculator
import kotlinx.coroutines.launch
import java.util.Calendar

class InputViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TransactionRepository

    private val _saveResult = MutableLiveData<SaveResult>()
    val saveResult: LiveData<SaveResult> = _saveResult

    init {
        val database = AppDatabase.getDatabase(application)
        repository = TransactionRepository(
            database.transactionDao(),
            database.customerDao(),
            database.trailerDao()
        )
    }

    fun saveTransaction(
        customer: String,
        craddle: String,
        tglKirim: Calendar,
        tglAmbil: Calendar,
        pAwal: Double,
        pAkhir: Double,
        tAwal: Double,
        tAkhir: Double,
        co2: Double,
        n2: Double,
        sg: Double,
        zKirim: Double,
        zAmbil: Double,
        lwc: Double,
        hargaPerSm3: Double
    ) {
        viewModelScope.launch {
            try {
                // Calculate using AGA8
                val result = AGA8Calculator.calculateTransaction(
                    pAwal, pAkhir, tAwal, tAkhir, lwc, zKirim, zAmbil
                )

                // Create transaction
                val transaction = Transaction(
                    customer = customer,
                    craddle = craddle,
                    tglKirim = tglKirim.timeInMillis,
                    tglAmbil = tglAmbil.timeInMillis,
                    pAwal = pAwal,
                    pAkhir = pAkhir,
                    tAwal = tAwal,
                    tAkhir = tAkhir,
                    co2 = co2,
                    n2 = n2,
                    sg = sg,
                    zKirim = zKirim,
                    zAmbil = zAmbil,
                    lwc = lwc,
                    fpvKirim = result.fpvKirim,
                    fpvAmbil = result.fpvAmbil,
                    sm3Kirim = result.sm3Kirim,
                    sm3Ambil = result.sm3Ambil,
                    sm3Total = result.sm3Total,
                    hargaPerSm3 = hargaPerSm3,
                    totalRevenue = result.sm3Total * hargaPerSm3
                )

                repository.insertTransaction(transaction)
                _saveResult.value = SaveResult.Success
            } catch (e: Exception) {
                _saveResult.value = SaveResult.Error(e.message ?: "Unknown error")
            }
        }
    }

    suspend fun getCustomerPrice(customerName: String): Double? {
        return repository.getCustomerByName(customerName)?.rupiahPjbg
    }

    suspend fun getTrailerLWC(trailerName: String): Double? {
        return repository.getTrailerByName(trailerName)?.lwc
    }

    sealed class SaveResult {
        object Success : SaveResult()
        data class Error(val message: String) : SaveResult()
    }
}

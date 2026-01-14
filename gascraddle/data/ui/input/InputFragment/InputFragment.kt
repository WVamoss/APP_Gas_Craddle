package com.yourname.gascraddle.ui.input

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.yourname.gascraddle.databinding.FragmentInputBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class InputFragment : Fragment() {
    private var _binding: FragmentInputBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: InputViewModel

    private val tglKirim = Calendar.getInstance()
    private val tglAmbil = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInputBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[InputViewModel::class.java]

        setupSpinners()
        setupDatePickers()
        setupDefaultValues()
        setupListeners()
        setupObservers()
    }

    private fun setupSpinners() {
        // Customer spinner
        val customers = arrayOf("PT A", "PT B", "PT C", "PT D")
        binding.spinnerCustomer.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            customers
        )

        // Craddle spinner
        val craddles = arrayOf("RC - 11", "RC - 21", "RC - 34", "RC - 46")
        binding.spinnerCraddle.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            craddles
        )
    }

    private fun setupDatePickers() {
        binding.etTglKirim.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    tglKirim.set(year, month, day)
                    binding.etTglKirim.setText(dateFormat.format(tglKirim.time))
                },
                tglKirim.get(Calendar.YEAR),
                tglKirim.get(Calendar.MONTH),
                tglKirim.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding.etTglAmbil.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    tglAmbil.set(year, month, day)
                    binding.etTglAmbil.setText(dateFormat.format(tglAmbil.time))
                },
                tglAmbil.get(Calendar.YEAR),
                tglAmbil.get(Calendar.MONTH),
                tglAmbil.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // Set initial dates
        binding.etTglKirim.setText(dateFormat.format(tglKirim.time))
        binding.etTglAmbil.setText(dateFormat.format(tglAmbil.time))
    }

    private fun setupDefaultValues() {
        binding.etCo2.setText("0.8477")
        binding.etN2.setText("0.7632")
        binding.etSg.setText("0.5841")
        binding.etZKirim.setText("0.83")
        binding.etZAmbil.setText("0.97")
        binding.etTAwal.setText("27")
        binding.etTAkhir.setText("32")
    }

    private fun setupListeners() {
        binding.btnSave.setOnClickListener {
            saveTransaction()
        }

        binding.btnClear.setOnClickListener {
            clearForm()
        }
    }

    private fun setupObservers() {
        viewModel.saveResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is InputViewModel.SaveResult.Success -> {
                    Toast.makeText(requireContext(), "Data berhasil disimpan!", Toast.LENGTH_SHORT).show()
                    clearForm()
                }
                is InputViewModel.SaveResult.Error -> {
                    Toast.makeText(requireContext(), "Error: ${result.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun saveTransaction() {
        val customer = binding.spinnerCustomer.selectedItem.toString()
        val craddle = binding.spinnerCraddle.selectedItem.toString()
        val pAwal = binding.etPAwal.text.toString().toDoubleOrNull() ?: 0.0
        val pAkhir = binding.etPAkhir.text.toString().toDoubleOrNull() ?: 0.0
        val tAwal = binding.etTAwal.text.toString().toDoubleOrNull() ?: 27.0
        val tAkhir = binding.etTAkhir.text.toString().toDoubleOrNull() ?: 32.0
        val co2 = binding.etCo2.text.toString().toDoubleOrNull() ?: 0.8477
        val n2 = binding.etN2.text.toString().toDoubleOrNull() ?: 0.7632
        val sg = binding.etSg.text.toString().toDoubleOrNull() ?: 0.5841
        val zKirim = binding.etZKirim.text.toString().toDoubleOrNull() ?: 0.83
        val zAmbil = binding.etZAmbil.text.toString().toDoubleOrNull() ?: 0.97

        // Validate
        if (pAwal == 0.0 || pAkhir == 0.0) {
            Toast.makeText(requireContext(), "Pressure tidak boleh kosong!", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val hargaPerSm3 = viewModel.getCustomerPrice(customer) ?: 9000.0
            val lwc = viewModel.getTrailerLWC(craddle) ?: 10.0

            viewModel.saveTransaction(
                customer, craddle, tglKirim, tglAmbil,
                pAwal, pAkhir, tAwal, tAkhir,
                co2, n2, sg, zKirim, zAmbil, lwc, hargaPerSm3
            )
        }
    }

    private fun clearForm() {
        binding.etPAwal.text?.clear()
        binding.etPAkhir.text?.clear()
        setupDefaultValues()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

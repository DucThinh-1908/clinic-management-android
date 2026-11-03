package com.example.management.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.management.api.RetrofitClient
import com.example.management.databinding.FragmentRecordsBinding
import com.example.management.ui.adapter.MedicalRecordAdapter
import kotlinx.coroutines.launch

class MedicalRecordsFragment : Fragment() {

    private var _binding: FragmentRecordsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: MedicalRecordAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecordsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = MedicalRecordAdapter { item ->
            Toast.makeText(
                requireContext(),
                "Hồ sơ: ${item.recordCode}\nChẩn đoán: ${item.finalDiagnosis ?: item.initialDiagnosis}",
                Toast.LENGTH_LONG
            ).show()
        }

        binding.rvRecords.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRecords.adapter = adapter

        binding.swipeRefreshRecords.setOnRefreshListener {
            loadMedicalRecords()
        }

        loadMedicalRecords()
    }

    private fun loadMedicalRecords() {
        val session = RetrofitClient.getSessionManager(requireContext())
        val user = session.getUser() ?: return
        val api = RetrofitClient.getApiService(requireContext())

        binding.progressBarRecords.visibility = View.VISIBLE
        binding.tvEmptyRecords.visibility = View.GONE

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val isStaff = user.roles?.any { it.contains("DOCTOR", true) || it.contains("ADMIN", true) || it.contains("MANAGER", true) } == true
                val response = if (isStaff) {
                    api.getAllMedicalRecords()
                } else {
                    api.getMedicalRecordsByPatient(user.id)
                }

                binding.progressBarRecords.visibility = View.GONE
                binding.swipeRefreshRecords.isRefreshing = false

                if (response.isSuccessful && response.body() != null) {
                    val list = response.body()!!
                    adapter.submitList(list)
                    binding.tvEmptyRecords.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                } else {
                    binding.tvEmptyRecords.visibility = View.VISIBLE
                    Toast.makeText(requireContext(), "Không thể tải hồ sơ bệnh án", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                binding.progressBarRecords.visibility = View.GONE
                binding.swipeRefreshRecords.isRefreshing = false
                binding.tvEmptyRecords.visibility = View.VISIBLE
                Toast.makeText(requireContext(), "Lỗi mạng: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

package com.example.management.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.management.api.RetrofitClient
import com.example.management.data.model.QueueTicketResponse
import com.example.management.data.model.RoomQueueOverviewDto
import com.example.management.data.model.SkipTicketRequest
import com.example.management.data.model.TransferRoomRequest
import com.example.management.databinding.FragmentReceptionBinding
import com.example.management.ui.adapter.QueueTicketAdapter
import com.example.management.ui.adapter.RoomQueueAdapter
import kotlinx.coroutines.launch

class ReceptionFragment : Fragment() {

    private var _binding: FragmentReceptionBinding? = null
    private val binding get() = _binding!!
    private lateinit var roomAdapter: RoomQueueAdapter
    private var allRooms: List<RoomQueueOverviewDto> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReceptionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        roomAdapter = RoomQueueAdapter(
            onCallNext = { room ->
                callNextPatient(room)
            },
            onViewQueue = { room ->
                viewRoomWaitingQueue(room)
            }
        )

        binding.rvRoomQueues.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRoomQueues.adapter = roomAdapter

        binding.btnWalkIn.setOnClickListener {
            startActivity(Intent(requireContext(), WalkInReceptionActivity::class.java))
        }

        binding.btnPatientLookup.setOnClickListener {
            startActivity(Intent(requireContext(), PatientLookupActivity::class.java))
        }

        binding.swipeRefreshReception.setOnRefreshListener {
            loadReceptionData()
        }

        loadReceptionData()
    }

    override fun onResume() {
        super.onResume()
        loadReceptionData()
    }

    private fun loadReceptionData() {
        val api = RetrofitClient.getApiService(requireContext())
        binding.progressBarReception.visibility = View.VISIBLE

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // 1. Thống kê chung hàng chờ
                val statsRes = api.getQueueStats()
                if (statsRes.isSuccessful && statsRes.body() != null) {
                    val stats = statsRes.body()!!
                    binding.tvStatWaiting.text = stats.waiting.toString()
                    binding.tvStatServing.text = stats.serving.toString()
                    binding.tvStatTotal.text = stats.totalTickets.toString()
                }

                // 2. Danh sách phòng khám và hàng chờ
                val roomRes = api.getRoomQueueOverview()
                binding.progressBarReception.visibility = View.GONE
                binding.swipeRefreshReception.isRefreshing = false

                if (roomRes.isSuccessful && roomRes.body() != null) {
                    allRooms = roomRes.body()!!
                    roomAdapter.submitList(allRooms)
                    binding.tvRoomCount.text = "${allRooms.size} phòng hoạt động"
                    binding.tvEmptyReception.visibility = if (allRooms.isEmpty()) View.VISIBLE else View.GONE
                } else {
                    binding.tvEmptyReception.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                binding.progressBarReception.visibility = View.GONE
                binding.swipeRefreshReception.isRefreshing = false
                binding.tvEmptyReception.visibility = View.VISIBLE
                Toast.makeText(requireContext(), "Lỗi tải dữ liệu tiếp đón: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun callNextPatient(room: RoomQueueOverviewDto) {
        val session = RetrofitClient.getSessionManager(requireContext())
        val user = session.getUser()
        val api = RetrofitClient.getApiService(requireContext())

        val roomName = room.roomName ?: room.roomNumber ?: "Phòng ${room.roomId}"

        AlertDialog.Builder(requireContext())
            .setTitle("Gọi số kế tiếp")
            .setMessage("Gọi bệnh nhân tiếp theo vào khám tại $roomName?")
            .setPositiveButton("Gọi ngay") { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        val res = api.callNextPatientInRoom(room.roomId, user?.id)
                        if (res.isSuccessful && res.body() != null) {
                            val ticket = res.body()!!
                            val stt = String.format("%02d", ticket.ticketNumber)
                            Toast.makeText(requireContext(), "Đã gọi STT #$stt: ${ticket.patientName ?: "Bệnh nhân"}", Toast.LENGTH_LONG).show()
                            loadReceptionData()
                        } else {
                            Toast.makeText(requireContext(), "Phòng hiện không có bệnh nhân nào trong hàng chờ", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun viewRoomWaitingQueue(room: RoomQueueOverviewDto) {
        val api = RetrofitClient.getApiService(requireContext())
        val roomName = room.roomName ?: room.roomNumber ?: "Phòng ${room.roomId}"

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val res = api.getQueueByRoom(room.roomId, "WAITING")
                if (res.isSuccessful && res.body() != null) {
                    val tickets = res.body()!!
                    if (tickets.isEmpty()) {
                        Toast.makeText(requireContext(), "$roomName hiện không có bệnh nhân nào đang chờ", Toast.LENGTH_SHORT).show()
                        return@launch
                    }
                    showWaitingTicketsDialog(room, tickets)
                } else {
                    Toast.makeText(requireContext(), "Không thể tải danh sách chờ", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Lỗi kết nối: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showWaitingTicketsDialog(room: RoomQueueOverviewDto, tickets: List<QueueTicketResponse>) {
        val rv = RecyclerView(requireContext()).apply {
            layoutManager = LinearLayoutManager(requireContext())
            setPadding(20, 20, 20, 20)
        }

        var dialog: AlertDialog? = null

        val ticketAdapter = QueueTicketAdapter(
            tickets = tickets,
            onTransfer = { ticket ->
                dialog?.dismiss()
                showTransferDialog(ticket)
            },
            onSkip = { ticket ->
                dialog?.dismiss()
                showSkipDialog(ticket)
            },
            onRecordVitals = { ticket ->
                dialog?.dismiss()
                val intent = Intent(requireContext(), RecordVitalSignsActivity::class.java).apply {
                    putExtra("EXTRA_PATIENT_ID", ticket.patientId ?: -1L)
                    putExtra("EXTRA_PATIENT_NAME", ticket.patientName)
                    putExtra("EXTRA_PATIENT_CODE", ticket.patientCode)
                    putExtra("EXTRA_PATIENT_AGE", ticket.patientAge ?: 0)
                    putExtra("EXTRA_PATIENT_GENDER", ticket.patientGender)
                    putExtra("EXTRA_TICKET_ID", ticket.id)
                }
                startActivity(intent)
            }
        )
        rv.adapter = ticketAdapter

        dialog = AlertDialog.Builder(requireContext())
            .setTitle("Hàng chờ: ${room.roomName ?: room.roomNumber} (${tickets.size} người)")
            .setView(rv)
            .setPositiveButton("Đóng", null)
            .create()

        dialog.show()
    }

    private fun showTransferDialog(ticket: QueueTicketResponse) {
        val otherRooms = allRooms.filter { it.roomId != ticket.roomId }
        if (otherRooms.isEmpty()) {
            Toast.makeText(requireContext(), "Không có phòng khám nào khác để chuyển", Toast.LENGTH_SHORT).show()
            return
        }

        val roomNames = otherRooms.map { it.roomName ?: it.roomNumber ?: "Phòng ${it.roomId}" }
        val spinner = Spinner(requireContext()).apply {
            adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, roomNames)
            setPadding(40, 20, 40, 20)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Chuyển phòng cho STT #${ticket.ticketNumber}")
            .setMessage("Chọn phòng khám đích để chuyển bệnh nhân ${ticket.patientName ?: ""}:")
            .setView(spinner)
            .setPositiveButton("Chuyển ngay") { _, _ ->
                val target = otherRooms[spinner.selectedItemPosition]
                val session = RetrofitClient.getSessionManager(requireContext())
                val user = session.getUser()
                val api = RetrofitClient.getApiService(requireContext())

                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        val req = TransferRoomRequest(
                            targetRoomId = target.roomId,
                            targetDoctorId = target.doctorId,
                            reason = "Lễ tân điều phối lại hàng chờ"
                        )
                        val res = api.transferPatient(ticket.id, req, user?.id)
                        if (res.isSuccessful) {
                            Toast.makeText(requireContext(), "Đã chuyển bệnh nhân sang ${target.roomName ?: target.roomNumber}", Toast.LENGTH_SHORT).show()
                            loadReceptionData()
                        } else {
                            Toast.makeText(requireContext(), "Chuyển phòng thất bại", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun showSkipDialog(ticket: QueueTicketResponse) {
        val session = RetrofitClient.getSessionManager(requireContext())
        val user = session.getUser()
        val api = RetrofitClient.getApiService(requireContext())

        AlertDialog.Builder(requireContext())
            .setTitle("Bỏ qua lượt khám")
            .setMessage("Bỏ qua lượt của STT #${ticket.ticketNumber} (${ticket.patientName ?: "Bệnh nhân"}) do vắng mặt khi gọi?")
            .setPositiveButton("Bỏ qua") { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        val res = api.skipPatient(ticket.id, SkipTicketRequest(), user?.id)
                        if (res.isSuccessful) {
                            Toast.makeText(requireContext(), "Đã bỏ qua lượt khám", Toast.LENGTH_SHORT).show()
                            loadReceptionData()
                        } else {
                            Toast.makeText(requireContext(), "Thao tác thất bại", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

package com.example.capstonehabitapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.capstonehabitapp.databinding.FragmentTaskListBinding

class TaskListFragment : Fragment() {

    private var _binding: FragmentTaskListBinding? = null
    private val binding get() = _binding!!

    private lateinit var taskList: MutableList<Task>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Set toolbar title
        activity?.title = getString(R.string.task_list)

        // Populate task list with dummy data
        taskList = mutableListOf(
            Task(
                "Mandi Pagi",
                "Kamar Mandi",
                "06:00 - 07:00",
                "Selesai Dikerjakan"),
            Task(
                "Menyapu Kamar",
                "Kamar Tidur",
                "08:00 - 10:00",
                "Belum Dikerjakan"),
            Task(
                "Sikat Gigi",
                "Kamar Mandi",
                "20:00 - 21:00",
                "Belum Dikerjakan"),
            Task(
                "Jogging",
                "Sekitar Rumah",
                "05:30 - 06:30",
                "Selesai Dikerjakan")
        )

        _binding = FragmentTaskListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set the adapter and layoutManager for task list RecyclerView
        binding.recycleView.apply {
            adapter = TaskAdapter(taskList)
            layoutManager = LinearLayoutManager(context)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
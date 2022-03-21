package com.example.capstonehabitapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.capstonehabitapp.databinding.ItemTaskBinding

class TaskAdapter(private var tasks: List<Task>)
    : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(val itemBinding: ItemTaskBinding): RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val from = LayoutInflater.from((parent.context))
        val binding = ItemTaskBinding.inflate(from, parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.itemBinding.apply {
            titleText.text = tasks[position].title
            areaText.text = tasks[position].area
            val timeLimit = "${tasks[position].startTimeLimit} - ${tasks[position].finishTimeLimit}"
            timeLimitText.text = timeLimit
            statusText.text = tasks[position].status.toString()
        }
        holder.itemView.setOnClickListener { view ->
            val action = TaskListFragmentDirections.actionTaskListFragmentToTaskDetailFragment(tasks[position].id)
            view.findNavController().navigate(action)
        }
    }

    override fun getItemCount(): Int {
        return tasks.size
    }
}
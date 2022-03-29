package com.example.capstonehabitapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.capstonehabitapp.R
import com.example.capstonehabitapp.model.Task
import com.example.capstonehabitapp.databinding.ItemTaskBinding
import com.example.capstonehabitapp.ui.TaskListFragmentDirections

class TaskAdapter(private var tasks: List<Task>)
    : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(val itemBinding: ItemTaskBinding): RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val from = LayoutInflater.from(parent.context)
        val binding = ItemTaskBinding.inflate(from, parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        // Get ViewHolder's ItemView context
        val context = holder.itemView.context

        // Bind the data to RecyclerView item's TextViews
        holder.itemBinding.apply {
            titleText.text = tasks[position].title
            areaText.text = tasks[position].area
            timeLimitText.text = context.getString(
                R.string.task_time_limit_placeholder,
                tasks[position].startTimeLimit,
                tasks[position].finishTimeLimit
            )
            statusText.text = tasks[position].status.toString()
        }

        // Set RecyclerView item OnClickListener to navigate to Task Detail screen
        holder.itemView.setOnClickListener { view ->
            val action = TaskListFragmentDirections.actionTaskListFragmentToTaskDetailFragment(tasks[position].id)
            view.findNavController().navigate(action)
        }
    }

    override fun getItemCount(): Int {
        return tasks.size
    }
}
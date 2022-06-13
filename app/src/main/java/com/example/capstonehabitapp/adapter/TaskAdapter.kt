package com.example.capstonehabitapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.capstonehabitapp.R
import com.example.capstonehabitapp.model.Task
import com.example.capstonehabitapp.databinding.ItemTaskBinding
import com.example.capstonehabitapp.ui.TaskListFragmentDirections
import com.example.capstonehabitapp.util.getTaskDifficultyString

class TaskAdapter(private val tasks: MutableList<Task>, private val isForParent: Boolean)
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

        // Bind the data to RecyclerView item's Views
        holder.itemBinding.apply {
            titleText.text = tasks[position].title
            areaText.text = tasks[position].area
            timeLimitText.text = context.getString(
                R.string.task_time_limit_placeholder,
                tasks[position].startTimeLimit,
                tasks[position].finishTimeLimit
            )

            // Display status text
            when (tasks[position].status.toInt()) {
                0 -> {
                    statusText.text = context.getString(R.string.task_status_0)
                    statusText.setTextColor(ContextCompat.getColor(context, R.color.state_error))
                }
                1 -> {
                    statusText.text = context.getString(R.string.task_status_1_with_child_name, tasks[position].childName)
                    statusText.setTextColor(ContextCompat.getColor(context, R.color.state_warning_dark))
                }
                2 -> {
                    statusText.text = context.getString(R.string.task_status_2_with_child_name, tasks[position].childName)
                    statusText.setTextColor(ContextCompat.getColor(context, R.color.state_success))
                }
                3 -> {
                    if (isForParent) {
                        statusText.text = context.getString(R.string.task_status_3_for_parent_with_child_name, tasks[position].childName)
                        statusText.setTextColor(ContextCompat.getColor(context, R.color.state_error))
                    }
                    else {
                        statusText.text = context.getString(R.string.task_status_3_for_child_with_child_name, tasks[position].childName)
                        statusText.setTextColor(ContextCompat.getColor(context, R.color.state_info))
                    }
                }
                4 -> {
                    statusText.text = context.getString(R.string.task_status_4)
                    statusText.setTextColor(ContextCompat.getColor(context, R.color.state_success))
                }
                5 -> {
                    statusText.text = context.getString(R.string.task_status_5)
                    statusText.setTextColor(ContextCompat.getColor(context, R.color.state_error))
                }
            }

            // Display difficulty text
            val difficulty = tasks[position].difficulty.toInt()
            difficultyText.text = getTaskDifficultyString(context, difficulty)
            when (difficulty) {
                0 -> difficultyText.setTextColor(ContextCompat.getColor(context, R.color.state_success))
                1 -> difficultyText.setTextColor(ContextCompat.getColor(context, R.color.state_info))
                2 -> difficultyText.setTextColor(ContextCompat.getColor(context, R.color.state_error_dark))
            }
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

    fun updateList(newList: List<Task>) {
        tasks.clear()
        tasks.addAll(newList)
        notifyDataSetChanged()
    }
}
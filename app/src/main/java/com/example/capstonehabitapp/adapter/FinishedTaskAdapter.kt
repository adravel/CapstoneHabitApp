package com.example.capstonehabitapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.capstonehabitapp.model.Task
import com.example.capstonehabitapp.databinding.ItemEssentialTaskBinding
import com.example.capstonehabitapp.ui.HistoryFragmentDirections
import com.example.capstonehabitapp.util.convertTimestampToString
import com.example.capstonehabitapp.util.getTaskDifficultyImageResId

class FinishedTaskAdapter(private val tasks: MutableList<Task>)
    : RecyclerView.Adapter<FinishedTaskAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(val itemBinding: ItemEssentialTaskBinding): RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val from = LayoutInflater.from(parent.context)
        val binding = ItemEssentialTaskBinding.inflate(from, parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        // Bind the data to RecyclerView item's Views
        holder.itemBinding.apply {
            titleText.text = tasks[position].title

            // Set the task info text
            val timestamp = tasks[position].timeFinishWorking
            val date = convertTimestampToString(timestamp!!, "dd MMM yyyy")
            infoText.text = "$date - ${tasks[position].childName}"

            // Display the difficulty image
            difficultyImage.setImageResource(getTaskDifficultyImageResId(tasks[position].difficulty.toInt()))
        }

        // Set RecyclerView item OnClickListener to navigate to Task Detail screen
        holder.itemView.setOnClickListener { view ->
            val action = HistoryFragmentDirections.actionHistoryFragmentToTaskDetailFragment(tasks[position].id)
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
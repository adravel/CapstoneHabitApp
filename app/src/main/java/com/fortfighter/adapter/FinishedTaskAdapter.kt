package com.fortfighter.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.fortfighter.databinding.ItemEssentialTaskBinding
import com.fortfighter.model.Task
import com.fortfighter.ui.fragment.FinishedTaskListFragmentDirections
import com.fortfighter.ui.fragment.HistoryFragmentDirections
import com.fortfighter.util.convertTimestampToString
import com.fortfighter.util.getTaskDifficultyImageResId

class FinishedTaskAdapter(private val tasks: MutableList<Task>, private val showAllItems: Boolean)
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
            val action = if (showAllItems) {
                // Navigate from finished task list page to task detail page
                FinishedTaskListFragmentDirections.actionFinishedTaskListFragmentToTaskDetailFragment(tasks[position].id)
            } else {
                // Navigate from history page to task detail page
                HistoryFragmentDirections.actionHistoryFragmentToTaskDetailFragment(tasks[position].id)
            }
            view.findNavController().navigate(action)
        }
    }

    override fun getItemCount(): Int {
        // Set RecyclerView to show only the first 3 task items if showAllItems is set to false
        val itemCount = 3
        return if (!showAllItems && tasks.size > itemCount) itemCount else tasks.size
    }

    fun updateList(newList: List<Task>) {
        tasks.clear()
        tasks.addAll(newList)
        notifyDataSetChanged()
    }
}
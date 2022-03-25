package com.example.capstonehabitapp

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.capstonehabitapp.databinding.ItemEssentialTaskBinding

class EssentialTaskAdapter(private var tasks: List<Task>, private var isForParent: Boolean)
    : RecyclerView.Adapter<EssentialTaskAdapter.TaskViewHolder>() {

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

            val info = if (isForParent)
                "${tasks[position].timeAskForGrading} - ${tasks[position].childName}"
            else
                "${tasks[position].timeFinishWorking} - ${tasks[position].area}"
            infoText.text = info
        }

        // Set RecyclerView item OnClickListener to navigate to Task Detail screen
        holder.itemView.setOnClickListener { view ->
            val action = if (isForParent)
                ParentHomeFragmentDirections.actionParentHomeFragmentToTaskDetailFragment(tasks[position].id)
            else
                ChildHomeFragmentDirections.actionChildHomeFragmentToTaskDetailFragment(tasks[position].id)
            view.findNavController().navigate(action)
        }
    }

    override fun getItemCount(): Int {
        return tasks.size
    }
}
package com.example.capstonehabitapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.capstonehabitapp.databinding.ItemChildAccountBinding

class ChildAccountAdapter(private var children: List<Child>)
    : RecyclerView.Adapter<ChildAccountAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(val itemBinding: ItemChildAccountBinding): RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val from = LayoutInflater.from(parent.context)
        val binding = ItemChildAccountBinding.inflate(from, parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        // Bind the data to RecyclerView item's text
        holder.itemBinding.childAccountButton.text = children[position].name
    }

    override fun getItemCount(): Int {
        return children.size
    }
}
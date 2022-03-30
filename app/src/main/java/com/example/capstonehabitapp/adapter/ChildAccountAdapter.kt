package com.example.capstonehabitapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.capstonehabitapp.model.Child
import com.example.capstonehabitapp.R
import com.example.capstonehabitapp.databinding.ItemChildAccountBinding

class ChildAccountAdapter(private var children: List<Child>)
    : RecyclerView.Adapter<ChildAccountAdapter.ChildAccountViewHolder>() {

    inner class ChildAccountViewHolder(val itemBinding: ItemChildAccountBinding): RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChildAccountViewHolder {
        val from = LayoutInflater.from(parent.context)
        val binding = ItemChildAccountBinding.inflate(from, parent, false)
        return ChildAccountViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChildAccountViewHolder, position: Int) {
        // Bind the data to RecyclerView item's text
        holder.itemBinding.childAccountButton.text = children[position].name

        // Get ViewHolder's ItemView context
        val context = holder.itemView.context

        // Define shared preference for storing child ID
        val sharedPref = context.getSharedPreferences(context.getString(R.string.role_preference_key), Context.MODE_PRIVATE)
        val editor = sharedPref.edit()

        // Set RecyclerView item OnClickListener to store childId in a shared preference
        holder.itemView.setOnClickListener {
            val childId = children[position].id
            editor.putString("selectedChildId", childId)?.apply()

            it.findNavController().navigate(R.id.childHomeFragment)
        }
    }

    override fun getItemCount(): Int {
        return children.size
    }
}
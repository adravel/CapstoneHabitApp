package com.example.capstonehabitapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.capstonehabitapp.model.Child
import com.example.capstonehabitapp.R
import com.example.capstonehabitapp.databinding.ItemChildAccountBinding
import com.example.capstonehabitapp.ui.fragment.ChildAccountSelectionFragmentDirections

class ChildAccountAdapter(private val children: MutableList<Child>, private val isForParent: Boolean)
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
        val sharedPref = context.getSharedPreferences(context.getString(R.string.role_pref_key), Context.MODE_PRIVATE)
        val editor = sharedPref.edit()

        // Set RecyclerView item OnClickListener
        holder.itemView.setOnClickListener { view ->
            val childId = children[position].id
            val childName = children[position].name

            if (isForParent) {
                // Navigate to store page
                val action = ChildAccountSelectionFragmentDirections.actionChildAccountSelectionFragmentToStoreFragment(
                    childId,
                    childName
                )
                view.findNavController().navigate(action)
            } else {
                // Set Child as role preference
                // and store childId and childName in the preference
                editor.putBoolean(context.getString(R.string.role_pref_is_parent_key), false).apply()
                editor.putString(context.getString(R.string.role_pref_child_id_key), childId).apply()
                editor.putString(context.getString(R.string.role_pref_child_name_key), childName).apply()

                // Navigate to child home page
                view.findNavController().navigate(R.id.childHomeFragment)
            }
        }
    }

    override fun getItemCount(): Int {
        return children.size
    }

    fun updateList(newList: List<Child>) {
        children.clear()
        children.addAll(newList)
        notifyDataSetChanged()
    }
}
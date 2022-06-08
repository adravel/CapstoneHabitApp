package com.example.capstonehabitapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.capstonehabitapp.R
import com.example.capstonehabitapp.model.Child
import com.example.capstonehabitapp.databinding.ItemChildRankBinding
import com.example.capstonehabitapp.util.getLevelNameString

class ChildRankAdapter(private val children: MutableList<Child>)
    : RecyclerView.Adapter<ChildRankAdapter.ChildRankViewHolder>() {

    inner class ChildRankViewHolder(val itemBinding: ItemChildRankBinding): RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChildRankViewHolder {
        val from = LayoutInflater.from(parent.context)
        val binding = ItemChildRankBinding.inflate(from, parent, false)
        return ChildRankViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChildRankViewHolder, position: Int) {
        // Get ViewHolder's ItemView context
        val context = holder.itemView.context

        // Bind the data to RecyclerView item's Views
        holder.itemBinding.apply {
            val rankNumber = position + 1
            val level = children[position].level.toInt()

            rankNumberText.text = rankNumber.toString()
            nameText.text = children[position].name
            levelNameText.text = getLevelNameString(context, level)
            totalPointsText.text = context.getString(
                R.string.child_total_points_placeholder,
                children[position].totalPoints.toInt()
            )
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
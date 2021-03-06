package com.fortfighter.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.fortfighter.R
import com.fortfighter.model.Child
import com.fortfighter.databinding.ItemChildRankBinding
import com.fortfighter.util.getLevelNameString

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

            // Display medal image according to ranking
            val medalImageResId = when (rankNumber) {
                1 -> R.drawable.img_medal_gold
                2 -> R.drawable.img_medal_silver
                3 -> R.drawable.img_medal_bronze
                else -> null
            }
            if (medalImageResId != null) {
                Glide.with(context)
                    .load(medalImageResId)
                    .into(medalImage)
            }

            // Display avatar image according to gender
            Glide.with(context)
                .load(if (children[position].isMale) {
                    R.drawable.img_soldier_male
                } else {
                    R.drawable.img_soldier_female
                })
                .into(avatarImage)
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
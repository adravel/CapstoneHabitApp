package com.example.capstonehabitapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.capstonehabitapp.R
import com.example.capstonehabitapp.databinding.ItemHouseBinding
import com.example.capstonehabitapp.model.House
import com.example.capstonehabitapp.ui.HouseListFragmentDirections

class HouseAdapter(private val houses: MutableList<House>): RecyclerView.Adapter<HouseAdapter.HouseViewHolder>() {

    inner class HouseViewHolder(val itemBinding: ItemHouseBinding): RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HouseViewHolder {
        val from = LayoutInflater.from(parent.context)
        val binding = ItemHouseBinding.inflate(from, parent, false)
        return HouseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HouseViewHolder, position: Int) {
        // Get ViewHolder's ItemView context
        val context = holder.itemView.context

        // Bind the data to RecyclerView item's Views
        holder.itemBinding.apply {
            nameText.text = houses[position].name
            hpText.text = houses[position].hp.toString()
            islandText.text = houses[position].island

            if (houses[position].status.toInt() == 0) {
                // Disable button if house has not been unlocked
                rescueButton.isEnabled = false
                rescueButton.text = context.getString(R.string.button_label_locked)

                // Overlay image with icon
                assetImage.setImageResource(R.drawable.ic_placeholder)
            } else {
                rescueButton.isEnabled = true
                rescueButton.text = context.getString(R.string.button_label_rescue)
            }

            rescueButton.setOnClickListener { view ->
                // Display house rescue confirmation dialog
                val action = HouseListFragmentDirections.actionHouseListFragmentToHouseRescueConfirmationDialogFragment(
                    houses[position].id,
                    houses[position].name
                )
                view.findNavController().navigate(action)
            }
        }
    }

    override fun getItemCount(): Int {
        return houses.size
    }

    fun updateList(newList: List<House>) {
        houses.clear()
        houses.addAll(newList)
        notifyDataSetChanged()
    }
}
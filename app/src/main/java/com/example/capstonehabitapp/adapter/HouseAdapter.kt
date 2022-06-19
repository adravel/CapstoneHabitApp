package com.example.capstonehabitapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.capstonehabitapp.R
import com.example.capstonehabitapp.databinding.ItemHouseBinding
import com.example.capstonehabitapp.model.House
import com.example.capstonehabitapp.ui.fragment.HouseListFragmentDirections

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
            val house = houses[position]
            val houseStaticData = house.getHouseStaticData()!!

            nameText.text = houseStaticData.name
            islandText.text = houseStaticData.island
            Glide.with(context)
                .load(houseStaticData.houseIntactImageResId)
                .into(houseImage)

            // Check if house is still locked (House status is 0)
            if (house.status.toInt() == 0) {
                // Disable button if house has not been unlocked
                rescueButton.isEnabled = false
                rescueButton.text = context.getString(R.string.button_label_locked)

                // Overlay house image with icon
                lockIconImage.setImageResource(R.drawable.img_lock)
                lockBackgroundImage.visibility = View.VISIBLE
            } else {
                // Enable button if house is unlocked
                rescueButton.isEnabled = true
                rescueButton.text = context.getString(R.string.button_label_rescue)

                // Hide lock images
                lockIconImage.visibility = View.GONE
                lockBackgroundImage.visibility = View.GONE
            }

            rescueButton.setOnClickListener { view ->
                // Display house rescue confirmation dialog
                val action = HouseListFragmentDirections.actionHouseListFragmentToHouseRescueConfirmationDialogFragment(
                    house.id,
                    houseStaticData.name
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
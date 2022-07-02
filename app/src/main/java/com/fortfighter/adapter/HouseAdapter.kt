package com.fortfighter.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.fortfighter.R
import com.fortfighter.databinding.ItemHouseBinding
import com.fortfighter.model.House
import com.fortfighter.ui.fragment.HouseListFragmentDirections

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

            // Load house image
            Glide.with(context)
                .load(houseStaticData.houseIntactImageResId)
                .into(houseImage)

            // Check if house is still locked (House status is 0)
            val houseStatus = house.status.toInt()
            if (houseStatus == 0) {
                // House has not been unlocked
                // Disable button
                button.isEnabled = false
                button.text = context.getString(R.string.button_label_locked)

                // Overlay house image with icon
                lockIconImage.setImageResource(R.drawable.img_lock)
                lockBackgroundImage.visibility = View.VISIBLE
            } else {
                // House is unlocked
                // Enable button if house
                button.isEnabled = true

                // Set button text depending on House status
                button.text = context.getString(
                    when (houseStatus) {
                        1 -> R.string.button_label_rescue
                        2 -> R.string.button_label_take_care
                        else -> R.string.button_label_see
                })

                // Hide lock images
                lockIconImage.visibility = View.GONE
                lockBackgroundImage.visibility = View.GONE
            }

            button.setOnClickListener { view ->
                // Display house rescue confirmation dialog
                val action = HouseListFragmentDirections
                    .actionHouseListFragmentToHouseSelectionConfirmationDialogFragment(
                        house.id,
                        houseStaticData.name,
                        houseStatus
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
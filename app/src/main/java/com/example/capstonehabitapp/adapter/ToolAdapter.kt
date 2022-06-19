package com.example.capstonehabitapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.capstonehabitapp.R
import com.example.capstonehabitapp.databinding.ItemToolBinding
import com.example.capstonehabitapp.model.Tool
import com.example.capstonehabitapp.ui.fragment.HouseDetailFragmentDirections
import com.example.capstonehabitapp.ui.fragment.StoreFragmentDirections

class ToolAdapter(
    private val tools: MutableList<Tool>,
    private val isForParent: Boolean,
    private val childId: String,
    private val childName: String = "")
    : RecyclerView.Adapter<ToolAdapter.ToolViewHolder>() {

    inner class ToolViewHolder(val itemBinding: ItemToolBinding): RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToolViewHolder {
        val from = LayoutInflater.from(parent.context)
        val binding = ItemToolBinding.inflate(from, parent, false)
        return ToolViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ToolViewHolder, position: Int) {
        // Get ViewHolder's ItemView context
        val context = holder.itemView.context

        // Bind the data to RecyclerView item's Views
        holder.itemBinding.apply {
            val tool = tools[position]
            val toolStaticData = tool.getToolStaticData()!!

            nameText.text = toolStaticData.name
            powerText.text = toolStaticData.power.toString()
            priceText.text = context.getString(R.string.tool_price_placeholder, toolStaticData.price)
            Glide.with(context)
                .load(toolStaticData.imageResId)
                .into(toolImage)

            if (isForParent && childName != "") {
                // Set button text and function for Parent in shop page
                if (tool.isForSale) {
                    itemButton.isEnabled = false
                    itemButton.text = context.getString(R.string.button_label_sent)
                } else {
                    itemButton.isEnabled = true
                    itemButton.text = context.getString(R.string.button_label_send)
                }

                itemButton.setOnClickListener { view ->
                    // Display tool shipment confirmation dialog
                    val action = StoreFragmentDirections.actionStoreFragmentToToolShipmentConfirmationDialogFragment(
                        tool.id,
                        toolStaticData.name,
                        childId,
                        childName
                    )
                    view.findNavController().navigate(action)
                }
            } else {
                // Set button text and function for Child in house detail page
                itemButton.text = context.getString(R.string.button_label_buy)

                itemButton.setOnClickListener { view ->
                    // Display tool purchase confirmation dialog
                    val action = HouseDetailFragmentDirections
                        .actionHouseDetailFragmentToToolPurchaseConfirmationDialogFragment(
                            tool.id,
                            toolStaticData.name
                        )
                    view.findNavController().navigate(action)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return tools.size
    }

    fun updateList(newList: List<Tool>) {
        tools.clear()
        tools.addAll(newList)
        notifyDataSetChanged()
    }
}